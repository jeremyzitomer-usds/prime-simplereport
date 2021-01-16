package gov.cdc.usds.simplereport.service;

import org.springframework.stereotype.Service;

import java.util.Map; 
import java.util.List;
import java.util.ArrayList;

import com.okta.spring.boot.sdk.config.OktaClientProperties;
import com.okta.sdk.client.Client;
import com.okta.sdk.client.Clients;
import com.okta.sdk.resource.user.User;
import com.okta.sdk.resource.user.UserBuilder;
import com.okta.sdk.resource.group.Group;
import com.okta.sdk.resource.group.GroupList;
import com.okta.sdk.resource.group.GroupType;
import com.okta.sdk.resource.group.GroupBuilder;
import com.okta.sdk.authc.credentials.TokenClientCredentials;

import gov.cdc.usds.simplereport.api.model.errors.IllegalGraphqlArgumentException;

import gov.cdc.usds.simplereport.service.model.IdentityAttributes;
import gov.cdc.usds.simplereport.config.AuthorizationProperties;
import gov.cdc.usds.simplereport.config.authorization.OrganizationRole;

/**
 * Handles all user/organization management in Okta
 */
@Service
public class OktaService {

    private String _rolePrefix;
    private Client _client;

    public OktaService(AuthorizationProperties authorizationProperties,
                       OktaClientProperties oktaClientProperties) {
        // TODO: remove these print lines
        System.out.print("ORG IS ");
        System.out.print(oktaClientProperties.getOrgUrl());
        System.out.print("API TOKEN IS ");
        System.out.print(oktaClientProperties.getToken());

        _rolePrefix = authorizationProperties.getRolePrefix();
        System.out.print("ROLE PREFIX IS ");
        System.out.print(_rolePrefix);
        _client = Clients.builder()
                .setOrgUrl(oktaClientProperties.getOrgUrl())
                .setClientCredentials(new TokenClientCredentials(oktaClientProperties.getToken()))
                .build();
    }

    public void createUser(IdentityAttributes userIdentity, String organizationExternalId) {
        Map<String,Object> userProfileMap = Map.of(
            "firstName", userIdentity.getFirstName(),
            "middleName", userIdentity.getMiddleName(),
            "lastName", userIdentity.getLastName(),
            // This is cheating. Suffix and honorific suffix aren't the same thing. Shhh.
            "honorificSuffix", userIdentity.getSuffix(),
            // We assume login == email
            "email", userIdentity.getUsername(),
            "login", userIdentity.getUsername()
        );

        // Okta SDK's way of getting a group by group name
        String groupName = generateGroupName(organizationExternalId, OrganizationRole.USER);
        Group group = _client.listGroups(groupName, null, null).single();
        UserBuilder.instance()
                .setProfileProperties(userProfileMap)
                .setGroups(group.getId())
                .buildAndCreate(_client);
    }

    public void updateUser(String oldUsername, IdentityAttributes userIdentity) {
        User user = _client.listUsers(oldUsername, null, null, null, null).single();
        user.getProfile().setFirstName(userIdentity.getFirstName());
        user.getProfile().setMiddleName(userIdentity.getMiddleName());
        user.getProfile().setLastName(userIdentity.getLastName());
        // Is it our fault we don't accommodate honorific suffix? Or Okta's fault they 
        // don't have regular suffix? You decide.
        user.getProfile().setHonorificSuffix(userIdentity.getSuffix());
        // We assume login == email
        user.getProfile().setEmail(userIdentity.getUsername());
        user.getProfile().setLogin(userIdentity.getUsername());
        user.update();
    }

    public void createOrganization(String name, String externalId) {
        for (OrganizationRole role : OrganizationRole.values()) {
            GroupBuilder.instance()
            .setName(generateGroupName(externalId, role))
            .setDescription(generateGroupDescription(name, role))
            .buildAndCreate(_client);
        }
    }

    // returns the external ID of the organization the specified user belongs to
    public String getOrganizationExternalIdForUser(String username) {
        User user = _client.listUsers(username, null, null, null, null).single();
        GroupList oldGroups = user.listGroups();
        List<String> orgNames = new ArrayList<String>();
        oldGroups.forEach(g->{
            String groupName = g.getProfile().getName();
            if (g.getType() == GroupType.OKTA_GROUP &&
                    groupName.startsWith(_rolePrefix) &&
                    groupName.endsWith(generateRoleSuffix(OrganizationRole.USER))) {
                orgNames.add(groupName);
            }
        });

        // We assume that a user can only be a member of one user group
        String externalId = getOrganizationExternalIdFromGroupName(orgNames.get(0), 
                                                                   OrganizationRole.USER);
        return externalId;
    }

    private String generateGroupName(String externalId, OrganizationRole role) {
        return String.format("%s%s%s", _rolePrefix, externalId, generateRoleSuffix(role));
    }

    private String getOrganizationExternalIdFromGroupName(String groupName, OrganizationRole role) {
        int roleSuffixOffset = groupName.lastIndexOf(generateRoleSuffix(role));
        String externalId = groupName.substring(_rolePrefix.length(), roleSuffixOffset);
        return externalId;
    }

    private String generateGroupDescription(String orgName, OrganizationRole role) {
        return String.format("%s - %s", orgName, role.getDescription());
    }

    private String generateRoleSuffix(OrganizationRole role) {
        return ":" + role.name();
    }

}