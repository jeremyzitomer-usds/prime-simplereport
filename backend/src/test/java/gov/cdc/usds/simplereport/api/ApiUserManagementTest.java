package gov.cdc.usds.simplereport.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import gov.cdc.usds.simplereport.api.model.Role;
import gov.cdc.usds.simplereport.config.authorization.UserPermission;
import gov.cdc.usds.simplereport.test_util.TestUserIdentities;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class ApiUserManagementTest extends BaseApiTest {

  private static final String NO_USER_ERROR = "Cannot find user.";

  private static final List<String> USERNAMES =
      List.of("rjj@gmail.com", "rjjones@gmail.com", "jaredholler@msn.com", "janicek90@yahoo.com");

  @Test
  void whoami_standardUser_okResponses() {
    ObjectNode who = (ObjectNode) runQuery("current-user-query").get("whoami");
    assertEquals("Bobbity", who.get("firstName").asText());
    assertEquals("Standard user", who.get("roleDescription").asText());
    assertFalse(who.get("isAdmin").asBoolean());
    assertEquals(who.get("role").asText(), Role.USER.name());
    assertEquals(Set.of(Role.USER), extractRolesFromUser(who));
    assertEquals(
        EnumSet.of(
            UserPermission.READ_PATIENT_LIST,
            UserPermission.SEARCH_PATIENTS,
            UserPermission.READ_RESULT_LIST,
            UserPermission.EDIT_PATIENT,
            UserPermission.ARCHIVE_PATIENT,
            UserPermission.START_TEST,
            UserPermission.UPDATE_TEST,
            UserPermission.SUBMIT_TEST),
        extractPermissionsFromUser(who));
    assertUserCanAccessExactFacilities(who, Set.of(TestUserIdentities.TEST_FACILITY_2));
    assertLastAuditEntry(
        TestUserIdentities.STANDARD_USER,
        "whoDat",
        EnumSet.of(
            UserPermission.READ_PATIENT_LIST,
            UserPermission.SEARCH_PATIENTS,
            UserPermission.READ_RESULT_LIST,
            UserPermission.EDIT_PATIENT,
            UserPermission.ARCHIVE_PATIENT,
            UserPermission.START_TEST,
            UserPermission.UPDATE_TEST,
            UserPermission.SUBMIT_TEST),
        null);
  }

  @Test
  void whoami_entryOnlyUser_okPermissionsRolesFacilities() {
    useOrgEntryOnly();
    ObjectNode who = (ObjectNode) runQuery("current-user-query").get("whoami");
    assertEquals("Test-entry user", who.get("roleDescription").asText());
    assertFalse(who.get("isAdmin").asBoolean());
    assertEquals(who.get("role").asText(), Role.ENTRY_ONLY.name());
    assertEquals(Set.of(Role.ENTRY_ONLY), extractRolesFromUser(who));
    assertEquals(
        EnumSet.of(
            UserPermission.START_TEST,
            UserPermission.SUBMIT_TEST,
            UserPermission.UPDATE_TEST,
            UserPermission.SEARCH_PATIENTS),
        extractPermissionsFromUser(who));
    assertUserCanAccessExactFacilities(who, Set.of(TestUserIdentities.TEST_FACILITY_1));
  }

  @Test
  void whoami_orgAdminUser_okPermissionsRolesFacilities() {
    useOrgAdmin();
    ObjectNode who = (ObjectNode) runQuery("current-user-query").get("whoami");
    assertEquals("Admin user", who.get("roleDescription").asText());
    assertFalse(who.get("isAdmin").asBoolean());
    assertEquals(who.get("role").asText(), Role.ADMIN.name());
    assertEquals(Set.of(Role.ADMIN), extractRolesFromUser(who));
    assertEquals(EnumSet.allOf(UserPermission.class), extractPermissionsFromUser(who));
    assertUserCanAccessAllFacilities(who);
  }

  @Test
  void whoami_allFacilityAccessUser_okPermissionsRolesFacilities() {
    useOrgUserAllFacilityAccess();
    ObjectNode who = (ObjectNode) runQuery("current-user-query").get("whoami");
    assertEquals("Standard user", who.get("roleDescription").asText());
    assertFalse(who.get("isAdmin").asBoolean());
    assertEquals(who.get("role").asText(), Role.USER.name());
    assertEquals(Set.of(Role.USER), extractRolesFromUser(who));
    assertEquals(
        EnumSet.of(
            UserPermission.READ_PATIENT_LIST,
            UserPermission.SEARCH_PATIENTS,
            UserPermission.READ_RESULT_LIST,
            UserPermission.EDIT_PATIENT,
            UserPermission.ARCHIVE_PATIENT,
            UserPermission.START_TEST,
            UserPermission.UPDATE_TEST,
            UserPermission.SUBMIT_TEST,
            UserPermission.ACCESS_ALL_FACILITIES),
        extractPermissionsFromUser(who));
    assertUserCanAccessAllFacilities(who);
  }

  @Test
  void whoami_superuser_okResponses() {
    useSuperUser();
    ObjectNode who = (ObjectNode) runQuery("current-user-query").get("whoami");
    assertEquals("Super Admin", who.get("roleDescription").asText());
    assertTrue(who.get("isAdmin").asBoolean());
    assertEquals(Set.of(), extractRolesFromUser(who));
    assertEquals(Collections.emptySet(), extractPermissionsFromUser(who));
    assertUserHasNoOrg(who);
  }

  @Test
  void whoami_nobody_okResponses() {
    useBrokenUser();
    ObjectNode who = (ObjectNode) runQuery("current-user-query").get("whoami");
    assertEquals("Misconfigured user", who.get("roleDescription").asText());
    assertFalse(who.get("isAdmin").asBoolean());
    assertEquals(Set.of(), extractRolesFromUser(who));
    assertEquals(Collections.emptySet(), extractPermissionsFromUser(who));
    assertUserHasNoOrg(who);
  }

  @Test
  void addUser_superUser_success() {
    useSuperUser();
    ObjectNode variables =
        getAddUserVariables(
            "Rhonda",
            "Janet",
            "Jones",
            "III",
            USERNAMES.get(0),
            TestUserIdentities.DEFAULT_ORGANIZATION,
            Role.ADMIN.name());
    ObjectNode resp = runQuery("add-user", variables);
    ObjectNode user = (ObjectNode) resp.get("addUser");
    assertEquals("Rhonda", user.get("firstName").asText());
    assertEquals(USERNAMES.get(0), user.get("email").asText());
    assertEquals(
        TestUserIdentities.DEFAULT_ORGANIZATION,
        user.get("organization").get("externalId").asText());
    assertEquals(user.get("role").asText(), Role.ADMIN.name());
    assertEquals(Set.of(Role.ADMIN), extractRolesFromUser(user));
    assertEquals(EnumSet.allOf(UserPermission.class), extractPermissionsFromUser(user));

    useOrgAdmin();
    assertUserCanAccessAllFacilities(user);
  }

  @Test
  void addUser_adminUser_failure() {
    useOrgAdmin();
    ObjectNode variables =
        getAddUserVariables(
            "Rhonda",
            "Janet",
            "Jones",
            "III",
            USERNAMES.get(0),
            TestUserIdentities.DEFAULT_ORGANIZATION,
            Role.USER.name());
    runQuery("add-user", variables, ACCESS_ERROR);
  }

  @Test
  void addUser_orgUser_failure() {
    ObjectNode variables =
        getAddUserVariables(
            "Rhonda",
            "Janet",
            "Jones",
            "III",
            USERNAMES.get(0),
            TestUserIdentities.DEFAULT_ORGANIZATION,
            Role.USER.name());
    runQuery("add-user", variables, "Current user does not have permission to request [/addUser]");
    assertLastAuditEntry(
        TestUserIdentities.STANDARD_USER,
        "addUserOp",
        Set.of(
            UserPermission.READ_PATIENT_LIST,
            UserPermission.SEARCH_PATIENTS,
            UserPermission.READ_RESULT_LIST,
            UserPermission.EDIT_PATIENT,
            UserPermission.ARCHIVE_PATIENT,
            UserPermission.START_TEST,
            UserPermission.UPDATE_TEST,
            UserPermission.SUBMIT_TEST),
        List.of("addUser"));
  }

  @Test
  void addUserToCurrentOrg_adminUser_success() {
    useOrgAdmin();
    ObjectNode variables =
        getAddUserVariables(
            "Rhonda",
            "Janet",
            "Jones",
            "III",
            USERNAMES.get(0),
            TestUserIdentities.DEFAULT_ORGANIZATION,
            Role.ENTRY_ONLY.name());
    ObjectNode resp = runQuery("add-user-to-current-org", variables);
    ObjectNode user = (ObjectNode) resp.get("addUserToCurrentOrg");
    assertEquals("Rhonda", user.get("firstName").asText());
    assertEquals(USERNAMES.get(0), user.get("email").asText());
    assertEquals(
        TestUserIdentities.DEFAULT_ORGANIZATION,
        user.get("organization").get("externalId").asText());
    assertEquals(user.get("role").asText(), Role.ENTRY_ONLY.name());
    assertEquals(Set.of(Role.ENTRY_ONLY), extractRolesFromUser(user));
    assertEquals(
        EnumSet.of(
            UserPermission.START_TEST,
            UserPermission.SUBMIT_TEST,
            UserPermission.UPDATE_TEST,
            UserPermission.SEARCH_PATIENTS),
        extractPermissionsFromUser(user));
    assertLastAuditEntry(
        TestUserIdentities.ORG_ADMIN_USER,
        "addUserToCurrentOrgOp",
        EnumSet.allOf(UserPermission.class),
        List.of());
    assertUserCanAccessExactFacilities(user, Set.of());
  }

  @Test
  void addUserToCurrentOrg_superUser_failure() {
    useSuperUser();
    ObjectNode variables =
        getAddUserVariables(
            "Rhonda",
            "Janet",
            "Jones",
            "III",
            USERNAMES.get(0),
            TestUserIdentities.DEFAULT_ORGANIZATION,
            Role.USER.name());
    runQuery("add-user-to-current-org", variables, ACCESS_ERROR);
    assertLastAuditEntry(
        TestUserIdentities.SITE_ADMIN_USER,
        "addUserToCurrentOrgOp",
        Set.of(),
        List.of("addUserToCurrentOrg"));
  }

  @Test
  void addUserToCurrentOrg_orgUser_failure() {
    ObjectNode variables =
        getAddUserVariables(
            "Rhonda",
            "Janet",
            "Jones",
            "III",
            USERNAMES.get(0),
            TestUserIdentities.DEFAULT_ORGANIZATION,
            Role.USER.name());
    runQuery(
        "add-user-to-current-org",
        variables,
        "Current user does not have permission to request [/addUserToCurrentOrg]");
  }

  @Test
  void updateUser_adminUser_success() {
    useOrgAdmin();

    ObjectNode addVariables =
        getAddUserVariables(
            "Rhonda",
            "Janet",
            "Jones",
            "III",
            USERNAMES.get(0),
            TestUserIdentities.DEFAULT_ORGANIZATION,
            Role.USER.name());
    ObjectNode addResp = runQuery("add-user-to-current-org", addVariables);
    ObjectNode addUser = (ObjectNode) addResp.get("addUserToCurrentOrg");
    String id = addUser.get("id").asText();

    ObjectNode updateVariables = getUpdateUserVariables(id, "Ronda", "J", "Jones", "III");
    ObjectNode updateResp = runQuery("update-user", updateVariables);
    ObjectNode updateUser = (ObjectNode) updateResp.get("updateUser");
    assertEquals("Ronda", updateUser.get("firstName").asText());
    assertEquals(USERNAMES.get(0), updateUser.get("email").asText());
    assertEquals(updateUser.get("role").asText(), Role.USER.name());
    assertEquals(Set.of(Role.USER), extractRolesFromUser(updateUser));
    assertEquals(
        EnumSet.of(
            UserPermission.READ_PATIENT_LIST,
            UserPermission.SEARCH_PATIENTS,
            UserPermission.READ_RESULT_LIST,
            UserPermission.EDIT_PATIENT,
            UserPermission.ARCHIVE_PATIENT,
            UserPermission.START_TEST,
            UserPermission.UPDATE_TEST,
            UserPermission.SUBMIT_TEST),
        extractPermissionsFromUser(updateUser));
    assertUserCanAccessExactFacilities(updateUser, Set.of());
  }

  @Test
  void updateUser_superUser_success() {
    useSuperUser();

    ObjectNode addVariables =
        getAddUserVariables(
            "Rhonda",
            "Janet",
            "Jones",
            "III",
            USERNAMES.get(0),
            TestUserIdentities.DEFAULT_ORGANIZATION,
            Role.ADMIN.name());
    ObjectNode addResp = runQuery("add-user", addVariables);
    ObjectNode addUser = (ObjectNode) addResp.get("addUser");
    String id = addUser.get("id").asText();

    ObjectNode updateVariables = getUpdateUserVariables(id, "Ronda", "J", "Jones", "III");
    ObjectNode resp = runQuery("update-user", updateVariables);
    ObjectNode updateUser = (ObjectNode) resp.get("updateUser");
    assertEquals("Ronda", updateUser.get("firstName").asText());
    assertEquals(USERNAMES.get(0), updateUser.get("email").asText());
    assertEquals(updateUser.get("role").asText(), Role.ADMIN.name());
    assertEquals(Set.of(Role.ADMIN), extractRolesFromUser(updateUser));
    assertEquals(EnumSet.allOf(UserPermission.class), extractPermissionsFromUser(updateUser));

    useOrgAdmin();
    assertUserCanAccessAllFacilities(updateUser);
  }

  @Test
  void updateUser_orgUser_failure() {
    useSuperUser();

    ObjectNode addVariables =
        getAddUserVariables(
            "Rhonda",
            "Janet",
            "Jones",
            "III",
            USERNAMES.get(0),
            TestUserIdentities.DEFAULT_ORGANIZATION,
            Role.ADMIN.name());
    ObjectNode addResp = runQuery("add-user", addVariables);
    ObjectNode addUser = (ObjectNode) addResp.get("addUser");
    String id = addUser.get("id").asText();

    useOrgUser();

    ObjectNode updateVariables = getUpdateUserVariables(id, "Ronda", "J", "Jones", "III");
    runQuery(
        "update-user",
        updateVariables,
        "Current user does not have permission to request [/updateUser]");
  }

  @Test
  void updateUser_nonexistentUser_failure() {
    useSuperUser();
    ObjectNode updateVariables =
        getUpdateUserVariables(
            "fa2efa2e-fa2e-fa2e-fa2e-fa2efa2efa2e", "Ronda", "J", "Jones", "III");
    runQuery("update-user", updateVariables, NO_USER_ERROR);
  }

  @Test
  void updateUser_self_success() {
    useOrgAdmin();
    ObjectNode who = (ObjectNode) runQuery("current-user-query").get("whoami");
    String id = who.get("id").asText();

    ObjectNode updateVariables = getUpdateUserVariables(id, "Ronda", "J", "Jones", "III");
    ObjectNode resp = runQuery("update-user", updateVariables);
    ObjectNode updateUser = (ObjectNode) resp.get("updateUser");
    assertEquals("Ronda", updateUser.get("firstName").asText());
    assertEquals(TestUserIdentities.ORG_ADMIN_USER, updateUser.get("email").asText());
    assertEquals(Set.of(Role.ADMIN), extractRolesFromUser(updateUser));
    assertEquals(EnumSet.allOf(UserPermission.class), extractPermissionsFromUser(updateUser));
    assertUserCanAccessAllFacilities(updateUser);
  }

  @Test
  void updateUserRole_adminUser_success() {
    useOrgAdmin();

    ObjectNode addVariables =
        getAddUserVariables(
            "Rhonda",
            "Janet",
            "Jones",
            "III",
            USERNAMES.get(0),
            TestUserIdentities.DEFAULT_ORGANIZATION,
            Role.USER.name());
    ObjectNode addResp = runQuery("add-user-to-current-org", addVariables);
    ObjectNode addUser = (ObjectNode) addResp.get("addUserToCurrentOrg");
    String id = addUser.get("id").asText();

    ObjectNode updateRoleVariables =
        JsonNodeFactory.instance.objectNode().put("id", id).put("role", Role.ADMIN.name());

    ObjectNode resp = runQuery("update-user-role", updateRoleVariables);
    assertEquals(Role.ADMIN.name(), resp.get("updateUserRole").asText());
  }

  @Test
  void updateUserRole_superUser_success() {
    useSuperUser();

    ObjectNode addVariables =
        getAddUserVariables(
            "Rhonda",
            "Janet",
            "Jones",
            "III",
            USERNAMES.get(0),
            TestUserIdentities.DEFAULT_ORGANIZATION,
            Role.ENTRY_ONLY.name());
    ObjectNode addResp = runQuery("add-user", addVariables);
    ObjectNode addUser = (ObjectNode) addResp.get("addUser");
    String id = addUser.get("id").asText();

    ObjectNode updateRoleVariables =
        JsonNodeFactory.instance.objectNode().put("id", id).put("role", Role.ADMIN.name());

    ObjectNode resp = runQuery("update-user-role", updateRoleVariables);
    assertEquals(Role.ADMIN.name(), resp.get("updateUserRole").asText());
  }

  @Test
  void updateUserRole_outsideOrgAdmin_failure() {
    useSuperUser();

    // Adding a user to get a handle on the internal ID
    ObjectNode addVariables =
        getAddUserVariables(
            "Rhonda",
            "Janet",
            "Jones",
            "III",
            USERNAMES.get(0),
            TestUserIdentities.DEFAULT_ORGANIZATION,
            Role.ENTRY_ONLY.name());
    ObjectNode addResp = runQuery("add-user", addVariables);
    ObjectNode addUser = (ObjectNode) addResp.get("addUser");
    String id = addUser.get("id").asText();

    useOutsideOrgAdmin();

    ObjectNode updateRoleVariables =
        JsonNodeFactory.instance.objectNode().put("id", id).put("role", Role.ADMIN.name());

    runQuery("update-user-role", updateRoleVariables, ACCESS_ERROR);
  }

  @Test
  void updateUserRole_outsideOrgUser_failure() {
    useSuperUser();

    // Adding a user to get a handle on the internal ID
    ObjectNode addVariables =
        getAddUserVariables(
            "Rhonda",
            "Janet",
            "Jones",
            "III",
            USERNAMES.get(0),
            TestUserIdentities.DEFAULT_ORGANIZATION,
            Role.USER.name());
    ObjectNode addResp = runQuery("add-user", addVariables);
    ObjectNode addUser = (ObjectNode) addResp.get("addUser");
    String id = addUser.get("id").asText();

    useOutsideOrgUser();

    ObjectNode updateRoleVariables =
        JsonNodeFactory.instance.objectNode().put("id", id).put("role", Role.ADMIN.name());

    runQuery(
        "update-user-role",
        updateRoleVariables,
        "Current user does not have permission to request [/updateUserRole]");
  }

  @Test
  void updateUserRole_orgUser_failure() {
    useSuperUser();

    // Adding a user to get a handle on the internal ID
    ObjectNode addVariables =
        getAddUserVariables(
            "Rhonda",
            "Janet",
            "Jones",
            "III",
            USERNAMES.get(0),
            TestUserIdentities.DEFAULT_ORGANIZATION,
            Role.USER.name());
    ObjectNode addResp = runQuery("add-user", addVariables);
    ObjectNode addUser = (ObjectNode) addResp.get("addUser");
    String id = addUser.get("id").asText();

    useOrgUser();

    ObjectNode updateRoleVariables =
        JsonNodeFactory.instance.objectNode().put("id", id).put("role", Role.ADMIN.name());

    runQuery(
        "update-user-role",
        updateRoleVariables,
        "Current user does not have permission to request [/updateUserRole]");
  }

  @Test
  void updateUserRole_orgAdmin_roleUpdated() {
    useOrgEntryOnly();
    ObjectNode who = (ObjectNode) runQuery("current-user-query").get("whoami");
    assertLastAuditEntry(
        TestUserIdentities.ENTRY_ONLY_USER,
        "whoDat",
        Set.of(
            UserPermission.START_TEST,
            UserPermission.SEARCH_PATIENTS,
            UserPermission.SUBMIT_TEST,
            UserPermission.UPDATE_TEST),
        null);
    assertEquals(who.get("role").asText(), Role.ENTRY_ONLY.name());
    assertEquals(Set.of(Role.ENTRY_ONLY), extractRolesFromUser(who));
    String id = who.get("id").asText();
    assertUserCanAccessExactFacilities(who, Set.of(TestUserIdentities.TEST_FACILITY_1));

    useOrgAdmin();
    ObjectNode updateRoleVariables =
        JsonNodeFactory.instance.objectNode().put("id", id).put("role", Role.ADMIN.name());
    runQuery("update-user-role", updateRoleVariables);

    useOrgEntryOnly();
    who = (ObjectNode) runQuery("current-user-query").get("whoami");
    assertLastAuditEntry(
        TestUserIdentities.ENTRY_ONLY_USER, "whoDat", EnumSet.allOf(UserPermission.class), null);
    assertEquals(who.get("role").asText(), Role.ADMIN.name());
    assertEquals(Set.of(Role.ADMIN), extractRolesFromUser(who));
    assertUserCanAccessAllFacilities(who);
  }

  @Test
  void updateUserRole_self_failure() {
    useOrgAdmin();
    ObjectNode who = (ObjectNode) runQuery("current-user-query").get("whoami");
    String id = who.get("id").asText();

    ObjectNode updateRoleVariables =
        JsonNodeFactory.instance.objectNode().put("id", id).put("role", Role.ENTRY_ONLY.name());
    runQuery("update-user-role", updateRoleVariables, ACCESS_ERROR);
  }

  @Test
  void updateUserPrivileges_orgAdmin_success() {
    useOrgAdmin();

    ObjectNode addVariables =
        getAddUserVariables(
            "Rhonda",
            "Janet",
            "Jones",
            "III",
            USERNAMES.get(0),
            TestUserIdentities.DEFAULT_ORGANIZATION,
            Role.USER.name());
    ObjectNode addResp = runQuery("add-user-to-current-org", addVariables);
    ObjectNode addUser = (ObjectNode) addResp.get("addUserToCurrentOrg");
    String id = addUser.get("id").asText();

    // Update 1: ENTRY_ONLY, All facilities
    ObjectNode updatePrivilegesVariables =
        getUpdateUserPrivilegesVariables(id, Role.ENTRY_ONLY, true, Set.of());

    ObjectNode updateResp = runQuery("update-user-privileges", updatePrivilegesVariables);
    ObjectNode updateUser = (ObjectNode) updateResp.get("updateUserPrivileges");

    assertEquals("Test-entry user", updateUser.get("roleDescription").asText());
    assertEquals(updateUser.get("role").asText(), Role.ENTRY_ONLY.name());
    assertEquals(Set.of(Role.ENTRY_ONLY), extractRolesFromUser(updateUser));
    assertEquals(
        EnumSet.of(
            UserPermission.START_TEST,
            UserPermission.SUBMIT_TEST,
            UserPermission.UPDATE_TEST,
            UserPermission.SEARCH_PATIENTS,
            UserPermission.ACCESS_ALL_FACILITIES),
        extractPermissionsFromUser(updateUser));
    assertUserCanAccessAllFacilities(updateUser);

    // Update 2: ADMIN, All facilities
    updatePrivilegesVariables = getUpdateUserPrivilegesVariables(id, Role.ADMIN, true, Set.of());

    updateResp = runQuery("update-user-privileges", updatePrivilegesVariables);
    updateUser = (ObjectNode) updateResp.get("updateUserPrivileges");

    assertEquals("Admin user", updateUser.get("roleDescription").asText());
    assertEquals(updateUser.get("role").asText(), Role.ADMIN.name());
    assertEquals(Set.of(Role.ADMIN), extractRolesFromUser(updateUser));
    assertEquals(EnumSet.allOf(UserPermission.class), extractPermissionsFromUser(updateUser));
    assertUserCanAccessAllFacilities(updateUser);

    // Update 3: USER, Access to 2 facilities
    updatePrivilegesVariables =
        getUpdateUserPrivilegesVariables(
            id,
            Role.USER,
            false,
            extractFacilityUuidsFromUser(
                updateUser,
                Set.of(TestUserIdentities.TEST_FACILITY_1, TestUserIdentities.TEST_FACILITY_2)));

    updateResp = runQuery("update-user-privileges", updatePrivilegesVariables);
    updateUser = (ObjectNode) updateResp.get("updateUserPrivileges");

    assertEquals("Standard user", updateUser.get("roleDescription").asText());
    assertEquals(updateUser.get("role").asText(), Role.USER.name());
    assertEquals(Set.of(Role.USER), extractRolesFromUser(updateUser));
    assertEquals(
        EnumSet.of(
            UserPermission.READ_PATIENT_LIST,
            UserPermission.SEARCH_PATIENTS,
            UserPermission.READ_RESULT_LIST,
            UserPermission.EDIT_PATIENT,
            UserPermission.ARCHIVE_PATIENT,
            UserPermission.START_TEST,
            UserPermission.UPDATE_TEST,
            UserPermission.SUBMIT_TEST),
        extractPermissionsFromUser(updateUser));
    assertUserCanAccessExactFacilities(
        updateUser, Set.of(TestUserIdentities.TEST_FACILITY_1, TestUserIdentities.TEST_FACILITY_2));
    assertUserCanAccessAllFacilities(updateUser);

    // Update 4: USER, access to no facilities
    updatePrivilegesVariables =
        getUpdateUserPrivilegesVariables(
            id, Role.USER, false, extractFacilityUuidsFromUser(updateUser, Set.of()));

    updateResp = runQuery("update-user-privileges", updatePrivilegesVariables);
    updateUser = (ObjectNode) updateResp.get("updateUserPrivileges");

    assertEquals("Standard user", updateUser.get("roleDescription").asText());
    assertEquals(updateUser.get("role").asText(), Role.USER.name());
    assertEquals(Set.of(Role.USER), extractRolesFromUser(updateUser));
    assertEquals(
        EnumSet.of(
            UserPermission.READ_PATIENT_LIST,
            UserPermission.SEARCH_PATIENTS,
            UserPermission.READ_RESULT_LIST,
            UserPermission.EDIT_PATIENT,
            UserPermission.ARCHIVE_PATIENT,
            UserPermission.START_TEST,
            UserPermission.UPDATE_TEST,
            UserPermission.SUBMIT_TEST),
        extractPermissionsFromUser(updateUser));
    assertUserCanAccessExactFacilities(updateUser, Set.of());
  }

  @Test
  void updateUserPrivileges_superUser_success() {
    useSuperUser();

    ObjectNode addVariables =
        getAddUserVariables(
            "Rhonda",
            "Janet",
            "Jones",
            "III",
            USERNAMES.get(0),
            TestUserIdentities.DEFAULT_ORGANIZATION,
            Role.ADMIN.name());
    ObjectNode addResp = runQuery("add-user", addVariables);
    ObjectNode addUser = (ObjectNode) addResp.get("addUser");
    String id = addUser.get("id").asText();

    ObjectNode updatePrivilegesVariables =
        getUpdateUserPrivilegesVariables(id, Role.ENTRY_ONLY, true, Set.of());

    ObjectNode updateResp = runQuery("update-user-privileges", updatePrivilegesVariables);
    ObjectNode updateUser = (ObjectNode) updateResp.get("updateUserPrivileges");

    assertEquals("Test-entry user", updateUser.get("roleDescription").asText());
    assertEquals(updateUser.get("role").asText(), Role.ENTRY_ONLY.name());
    assertEquals(Set.of(Role.ENTRY_ONLY), extractRolesFromUser(updateUser));
    assertEquals(
        EnumSet.of(
            UserPermission.START_TEST,
            UserPermission.SUBMIT_TEST,
            UserPermission.UPDATE_TEST,
            UserPermission.SEARCH_PATIENTS,
            UserPermission.ACCESS_ALL_FACILITIES),
        extractPermissionsFromUser(updateUser));

    useOrgAdmin();
    assertUserCanAccessAllFacilities(updateUser);
  }

  @Test
  void updateUserPrivileges_outsideOrgAdmin_failure() {
    useSuperUser();

    ObjectNode addVariables =
        getAddUserVariables(
            "Rhonda",
            "Janet",
            "Jones",
            "III",
            USERNAMES.get(0),
            TestUserIdentities.DEFAULT_ORGANIZATION,
            Role.ENTRY_ONLY.name());
    ObjectNode addResp = runQuery("add-user", addVariables);
    ObjectNode addUser = (ObjectNode) addResp.get("addUser");
    String id = addUser.get("id").asText();

    useOutsideOrgAdmin();

    ObjectNode updatePrivilegesVariables =
        getUpdateUserPrivilegesVariables(id, Role.ENTRY_ONLY, false, Set.of());

    runQuery("update-user-privileges", updatePrivilegesVariables, ACCESS_ERROR);
  }

  @Test
  void updateUserPrivileges_orgUser_failure() {
    useSuperUser();

    ObjectNode addVariables =
        getAddUserVariables(
            "Rhonda",
            "Janet",
            "Jones",
            "III",
            USERNAMES.get(0),
            TestUserIdentities.DEFAULT_ORGANIZATION,
            Role.ENTRY_ONLY.name());
    ObjectNode addResp = runQuery("add-user", addVariables);
    ObjectNode addUser = (ObjectNode) addResp.get("addUser");
    String id = addUser.get("id").asText();

    useOrgUser();

    ObjectNode updatePrivilegesVariables =
        getUpdateUserPrivilegesVariables(id, Role.ENTRY_ONLY, false, Set.of());

    runQuery(
        "update-user-privileges",
        updatePrivilegesVariables,
        "Current user does not have permission to request [/updateUserPrivileges]");
  }

  @Test
  void updateUserPrivileges_nonexistentUser_failure() {
    useSuperUser();
    ObjectNode updatePrivilegesVariables =
        getUpdateUserPrivilegesVariables(
            "fa2efa2e-fa2e-fa2e-fa2e-fa2efa2efa2e", Role.ENTRY_ONLY, true, Set.of());
    runQuery("update-user-privileges", updatePrivilegesVariables, NO_USER_ERROR);
  }

  @Test
  void updateUserPrivileges_self_failure() {
    useOrgAdmin();
    ObjectNode who = (ObjectNode) runQuery("current-user-query").get("whoami");
    String id = who.get("id").asText();

    ObjectNode updatePrivilegesVariables =
        getUpdateUserPrivilegesVariables(id, Role.ENTRY_ONLY, true, Set.of());
    runQuery("update-user-privileges", updatePrivilegesVariables, ACCESS_ERROR);
  }

  @Test
  void setUserIsDeleted_adminUser_success() {
    useOrgAdmin();

    ObjectNode addVariables =
        getAddUserVariables(
            "Rhonda",
            "Janet",
            "Jones",
            "III",
            USERNAMES.get(0),
            TestUserIdentities.DEFAULT_ORGANIZATION,
            Role.ENTRY_ONLY.name());
    ObjectNode addResp = runQuery("add-user-to-current-org", addVariables);
    ObjectNode addUser = (ObjectNode) addResp.get("addUserToCurrentOrg");
    String id = addUser.get("id").asText();

    ObjectNode deleteVariables =
        JsonNodeFactory.instance.objectNode().put("id", id).put("deleted", true);

    ObjectNode resp = runQuery("set-user-is-deleted", deleteVariables);
    assertEquals(USERNAMES.get(0), resp.get("setUserIsDeleted").get("email").asText());

    ObjectNode updateRoleVariables =
        JsonNodeFactory.instance.objectNode().put("id", id).put("role", Role.ADMIN.name());
    runQuery("update-user-role", updateRoleVariables, NO_USER_ERROR);
  }

  @Test
  void setUserIsDeleted_superUser_success() {
    useSuperUser();

    ObjectNode addVariables =
        getAddUserVariables(
            "Rhonda",
            "Janet",
            "Jones",
            "III",
            USERNAMES.get(0),
            TestUserIdentities.DEFAULT_ORGANIZATION,
            Role.USER.name());
    ObjectNode addResp = runQuery("add-user", addVariables);
    ObjectNode addUser = (ObjectNode) addResp.get("addUser");
    String id = addUser.get("id").asText();

    ObjectNode deleteVariables =
        JsonNodeFactory.instance.objectNode().put("id", id).put("deleted", true);

    ObjectNode resp = runQuery("set-user-is-deleted", deleteVariables);
    assertEquals(USERNAMES.get(0), resp.get("setUserIsDeleted").get("email").asText());

    ObjectNode updateRoleVariables =
        JsonNodeFactory.instance.objectNode().put("id", id).put("role", Role.ADMIN.name());
    runQuery("update-user-role", updateRoleVariables, NO_USER_ERROR);
  }

  @Test
  void setUserIsDeleted_outsideOrgAdmin_failure() {
    useSuperUser();

    // Adding a user to get a handle on the internal ID
    ObjectNode addVariables =
        getAddUserVariables(
            "Rhonda",
            "Janet",
            "Jones",
            "III",
            USERNAMES.get(0),
            TestUserIdentities.DEFAULT_ORGANIZATION,
            Role.ADMIN.name());
    ObjectNode addResp = runQuery("add-user", addVariables);
    ObjectNode addUser = (ObjectNode) addResp.get("addUser");
    String id = addUser.get("id").asText();

    useOutsideOrgAdmin();

    ObjectNode deleteVariables =
        JsonNodeFactory.instance.objectNode().put("id", id).put("deleted", true);
    runQuery("set-user-is-deleted", deleteVariables, ACCESS_ERROR);
  }

  @Test
  void setUserIsDeleted_outsideOrgUser_failure() {
    useSuperUser();

    // Adding a user to get a handle on the internal ID
    ObjectNode addVariables =
        getAddUserVariables(
            "Rhonda",
            "Janet",
            "Jones",
            "III",
            USERNAMES.get(0),
            TestUserIdentities.DEFAULT_ORGANIZATION,
            Role.USER.name());
    ObjectNode addResp = runQuery("add-user", addVariables);
    ObjectNode addUser = (ObjectNode) addResp.get("addUser");
    String id = addUser.get("id").asText();

    useOutsideOrgUser();

    ObjectNode deleteVariables =
        JsonNodeFactory.instance.objectNode().put("id", id).put("deleted", true);
    runQuery(
        "set-user-is-deleted",
        deleteVariables,
        "Current user does not have permission to request [/setUserIsDeleted]");
  }

  @Test
  void setUserIsDeleted_orgUser_failure() {
    useSuperUser();

    // Adding a user to get a handle on the internal ID
    ObjectNode addVariables =
        getAddUserVariables(
            "Rhonda",
            "Janet",
            "Jones",
            "III",
            USERNAMES.get(0),
            TestUserIdentities.DEFAULT_ORGANIZATION,
            Role.ADMIN.name());
    ObjectNode addResp = runQuery("add-user", addVariables);
    ObjectNode addUser = (ObjectNode) addResp.get("addUser");
    String id = addUser.get("id").asText();

    useOrgUser();

    ObjectNode deleteVariables =
        JsonNodeFactory.instance.objectNode().put("id", id).put("deleted", true);
    runQuery(
        "set-user-is-deleted",
        deleteVariables,
        "Current user does not have permission to request [/setUserIsDeleted]");
  }

  @Test
  void setUserIsDeleted_self_failure() {
    useOrgAdmin();
    ObjectNode who = (ObjectNode) runQuery("current-user-query").get("whoami");
    String id = who.get("id").asText();

    ObjectNode deleteVariables =
        JsonNodeFactory.instance.objectNode().put("id", id).put("deleted", true);
    runQuery("set-user-is-deleted", deleteVariables, ACCESS_ERROR);
  }

  // The next retrieval test also expects demo users as defined in the no-okta-mgmt profile
  @Test
  void getUsers_adminUser_success() {
    useOrgAdmin();

    List<ObjectNode> usersAdded =
        Arrays.asList(
            getAddUserVariables(
                "Rhonda",
                "Janet",
                "Jones",
                "III",
                USERNAMES.get(0),
                TestUserIdentities.DEFAULT_ORGANIZATION,
                Role.ADMIN.name()),
            getAddUserVariables(
                "Jared",
                "K",
                "Holler",
                null,
                USERNAMES.get(2),
                TestUserIdentities.DEFAULT_ORGANIZATION,
                Role.ADMIN.name()),
            getAddUserVariables(
                "Janice",
                null,
                "Katz",
                "Jr",
                USERNAMES.get(3),
                TestUserIdentities.DEFAULT_ORGANIZATION,
                Role.ADMIN.name()));
    for (ObjectNode userVariables : usersAdded) {
      runQuery("add-user-to-current-org", userVariables);
    }

    ObjectNode resp = runQuery("users-query");
    List<ObjectNode> usersRetrieved = toList((ArrayNode) resp.get("users"));

    assertTrue(usersRetrieved.size() > usersAdded.size());

    for (int i = 0; i < usersAdded.size(); i++) {
      ObjectNode userAdded = usersAdded.get(i);
      Optional<ObjectNode> found =
          usersRetrieved.stream()
              .filter(u -> u.get("email").asText().equals(userAdded.get("email").asText()))
              .findFirst();
      assertTrue(found.isPresent());
      ObjectNode userRetrieved = found.get();

      assertEquals(userRetrieved.get("firstName").asText(), userAdded.get("firstName").asText());
      assertEquals(userRetrieved.get("email").asText(), userAdded.get("email").asText());
      assertEquals(
          userRetrieved.get("organization").get("externalId").asText(),
          userAdded.get("organizationExternalId").asText());
      assertEquals(EnumSet.allOf(UserPermission.class), extractPermissionsFromUser(userRetrieved));
    }
  }

  @Test
  void getUsers_orgUser_failure() {
    runQuery("users-query", "Current user does not have permission to request [/users]");
  }

  private List<ObjectNode> toList(ArrayNode arr) {
    List<ObjectNode> list = new ArrayList<>();
    for (int i = 0; i < arr.size(); i++) {
      list.add((ObjectNode) arr.get(i));
    }
    return list;
  }

  private ObjectNode getAddUserVariables(
      String firstName,
      String middleName,
      String lastName,
      String suffix,
      String email,
      String orgExternalId,
      String role) {
    ObjectNode variables =
        JsonNodeFactory.instance
            .objectNode()
            .put("firstName", firstName)
            .put("middleName", middleName)
            .put("lastName", lastName)
            .put("suffix", suffix)
            .put("email", email)
            .put("organizationExternalId", orgExternalId)
            .put("role", role);
    return variables;
  }

  private ObjectNode getUpdateUserVariables(
      String id, String firstName, String middleName, String lastName, String suffix) {
    ObjectNode variables =
        JsonNodeFactory.instance
            .objectNode()
            .put("id", id)
            .put("firstName", firstName)
            .put("middleName", middleName)
            .put("lastName", lastName)
            .put("suffix", suffix);
    return variables;
  }

  private Set<Role> extractRolesFromUser(ObjectNode user) {
    Iterator<JsonNode> rolesIter = user.get("roles").elements();
    Set<Role> roles = new HashSet<>();
    while (rolesIter.hasNext()) {
      roles.add(Role.valueOf(rolesIter.next().asText()));
    }
    return roles;
  }

  private Set<UserPermission> extractPermissionsFromUser(ObjectNode user) {
    Iterator<JsonNode> permissionsIter = user.get("permissions").elements();
    Set<UserPermission> permissions = new HashSet<>();
    while (permissionsIter.hasNext()) {
      permissions.add(UserPermission.valueOf(permissionsIter.next().asText()));
    }
    return permissions;
  }

  // map from each facility's name to its UUID; includes all facilities user can access
  private Map<String, UUID> extractFacilitiesFromUser(ObjectNode user) {
    Iterator<JsonNode> facilitiesIter = user.get("organization").get("testingFacility").elements();
    Map<String, UUID> facilities = new HashMap<>();
    while (facilitiesIter.hasNext()) {
      JsonNode facility = facilitiesIter.next();
      facilities.put(facility.get("name").asText(), UUID.fromString(facility.get("id").asText()));
    }
    return facilities;
  }

  private Set<UUID> extractFacilityUuidsFromUser(ObjectNode user, Set<String> facilityNames) {
    Map<String, UUID> facilities = extractFacilitiesFromUser(user);
    return facilityNames.stream().map(facilities::get).collect(Collectors.toSet());
  }

  private void assertUserCanAccessAllFacilities(ObjectNode user) {
    assertEquals(extractAllFacilitiesInOrg(), extractFacilitiesFromUser(user));
  }

  private void assertUserCanAccessExactFacilities(ObjectNode user, Set<String> facilityNames) {
    assertEquals(extractFacilitiesFromUser(user).keySet(), facilityNames);
  }

  private void assertUserHasNoOrg(ObjectNode user) {
    assertTrue(user.get("organization").isNull());
  }
}
