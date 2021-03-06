package gov.cdc.usds.simplereport.service;

import gov.cdc.usds.simplereport.api.model.errors.IllegalGraphqlArgumentException;
import gov.cdc.usds.simplereport.api.model.errors.MisconfiguredUserException;
import gov.cdc.usds.simplereport.config.AuthorizationConfiguration;
import gov.cdc.usds.simplereport.config.authorization.OrganizationRoleClaims;
import gov.cdc.usds.simplereport.db.model.DeviceSpecimenType;
import gov.cdc.usds.simplereport.db.model.Facility;
import gov.cdc.usds.simplereport.db.model.Organization;
import gov.cdc.usds.simplereport.db.model.Provider;
import gov.cdc.usds.simplereport.db.model.auxiliary.PersonName;
import gov.cdc.usds.simplereport.db.model.auxiliary.StreetAddress;
import gov.cdc.usds.simplereport.db.repository.FacilityRepository;
import gov.cdc.usds.simplereport.db.repository.OrganizationRepository;
import gov.cdc.usds.simplereport.db.repository.ProviderRepository;
import gov.cdc.usds.simplereport.idp.repository.OktaRepository;
import gov.cdc.usds.simplereport.service.model.DeviceSpecimenTypeHolder;
import gov.cdc.usds.simplereport.service.model.OrganizationRoles;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class OrganizationService {

  private static final Logger LOG = LoggerFactory.getLogger(OrganizationService.class);

  private OrganizationRepository _repo;
  private FacilityRepository _facilityRepo;
  private ProviderRepository _providerRepo;
  private AuthorizationService _authService;
  private OktaRepository _oktaRepo;

  public OrganizationService(
      OrganizationRepository repo,
      FacilityRepository facilityRepo,
      AuthorizationService authService,
      ProviderRepository providerRepo,
      OktaRepository oktaRepo) {
    _repo = repo;
    _facilityRepo = facilityRepo;
    _authService = authService;
    _providerRepo = providerRepo;
    _oktaRepo = oktaRepo;
  }

  public void migrateOktaGroups() {
    // migrate existing orgs/facilities to Okta groups on startup
    for (Organization org : _repo.findAll()) {
      _oktaRepo.createOrganization(org, getFacilities(org), true);
    }
  }

  public Optional<OrganizationRoles> getCurrentOrganizationRoles() {
    List<OrganizationRoleClaims> orgRoles = _authService.findAllOrganizationRoles();
    List<String> candidateExternalIds =
        orgRoles.stream()
            .map(OrganizationRoleClaims::getOrganizationExternalId)
            .collect(Collectors.toList());
    List<Organization> validOrgs = _repo.findAllByExternalId(candidateExternalIds);
    if (validOrgs == null || validOrgs.size() != 1) {
      int numOrgs = (validOrgs == null) ? 0 : validOrgs.size();
      LOG.warn("Found {} organizations for user", numOrgs);
      return Optional.empty();
    }
    Organization foundOrg = validOrgs.get(0);
    Optional<OrganizationRoleClaims> foundRoles =
        orgRoles.stream()
            .filter(r -> r.getOrganizationExternalId().equals(foundOrg.getExternalId()))
            .findFirst();
    return foundRoles.map(r -> getOrganizationRoles(foundOrg, r));
  }

  public Set<Facility> getAccessibleFacilities() {
    Optional<OrganizationRoles> roles = getCurrentOrganizationRoles();
    return roles.isPresent() ? roles.get().getFacilities() : Set.of();
  }

  public Organization getCurrentOrganization() {
    OrganizationRoles orgRole =
        getCurrentOrganizationRoles().orElseThrow(MisconfiguredUserException::new);
    return orgRole.getOrganization();
  }

  public OrganizationRoles getOrganizationRoles(OrganizationRoleClaims roleClaims) {
    Organization org = getOrganization(roleClaims.getOrganizationExternalId());
    return getOrganizationRoles(org, roleClaims);
  }

  public OrganizationRoles getOrganizationRoles(
      Organization org, OrganizationRoleClaims roleClaims) {
    return new OrganizationRoles(
        org, getAccessibleFacilities(org, roleClaims), roleClaims.getGrantedRoles());
  }

  public Organization getOrganization(String externalId) {
    Optional<Organization> found = _repo.findByExternalId(externalId);
    return found.orElseThrow(
        () ->
            new IllegalGraphqlArgumentException(
                "An organization with external_id=" + externalId + " does not exist"));
  }

  @AuthorizationConfiguration.RequireGlobalAdminUser
  public List<Organization> getOrganizations() {
    return _repo.findAll();
  }

  public Set<Facility> getAccessibleFacilities(
      Organization org, OrganizationRoleClaims roleClaims) {
    // If there are no facility restrictions, get all facilities in org; otherwise, get specified
    // list.
    return roleClaims.grantsAllFacilityAccess()
        ? _facilityRepo.findAllByOrganization(org)
        : _facilityRepo.findAllByOrganizationAndInternalId(org, roleClaims.getFacilities());
  }

  public List<Facility> getFacilities(Organization org) {
    return _facilityRepo.findByOrganizationOrderByFacilityName(org);
  }

  public Set<Facility> getFacilities(Organization org, Collection<UUID> facilityIds) {
    return _facilityRepo.findAllByOrganizationAndInternalId(org, facilityIds);
  }

  public Facility getFacilityInCurrentOrg(UUID facilityId) {
    Organization org = getCurrentOrganization();
    return _facilityRepo
        .findByOrganizationAndInternalId(org, facilityId)
        .orElseThrow(() -> new IllegalGraphqlArgumentException("facility could not be found"));
  }

  public void assertFacilityNameAvailable(String testingFacilityName) {
    Organization org = getCurrentOrganization();
    _facilityRepo
        .findByOrganizationAndFacilityName(org, testingFacilityName)
        .ifPresent(
            f -> {
              throw new IllegalGraphqlArgumentException("A facility with that name already exists");
            });
  }

  @Transactional(readOnly = false)
  @AuthorizationConfiguration.RequirePermissionEditFacility
  public Facility updateFacility(
      UUID facilityId,
      String testingFacilityName,
      String cliaNumber,
      StreetAddress facilityAddress,
      String phone,
      String email,
      String orderingProviderFirstName,
      String orderingProviderMiddleName,
      String orderingProviderLastName,
      String orderingProviderSuffix,
      String orderingProviderNPI,
      StreetAddress orderingProviderAddress,
      String orderingProviderTelephone,
      DeviceSpecimenTypeHolder deviceSpecimenTypes) {
    Facility facility = this.getFacilityInCurrentOrg(facilityId);
    facility.setFacilityName(testingFacilityName);
    facility.setCliaNumber(cliaNumber);
    facility.setTelephone(phone);
    facility.setEmail(email);
    facility.setAddress(facilityAddress);

    Provider p = facility.getOrderingProvider();
    p.getNameInfo().setFirstName(orderingProviderFirstName);
    p.getNameInfo().setMiddleName(orderingProviderMiddleName);
    p.getNameInfo().setLastName(orderingProviderLastName);
    p.getNameInfo().setSuffix(orderingProviderSuffix);
    p.setProviderId(orderingProviderNPI);
    p.setTelephone(orderingProviderTelephone);
    p.setAddress(orderingProviderAddress);

    for (DeviceSpecimenType ds : deviceSpecimenTypes.getFullList()) {
      facility.addDeviceSpecimenType(ds);
    }
    // remove all existing devices
    for (DeviceSpecimenType ds : facility.getDeviceSpecimenTypes()) {
      if (!deviceSpecimenTypes.getFullList().contains(ds)) {
        facility.removeDeviceSpecimenType(ds);
      }
    }
    facility.addDefaultDeviceSpecimen(deviceSpecimenTypes.getDefault());
    return _facilityRepo.save(facility);
  }

  @Transactional(readOnly = false)
  @AuthorizationConfiguration.RequireGlobalAdminUser
  public Organization createOrganization(
      String name,
      String externalId,
      String testingFacilityName,
      String cliaNumber,
      StreetAddress facilityAddress,
      String phone,
      String email,
      DeviceSpecimenTypeHolder deviceSpecimenTypes,
      PersonName providerName,
      StreetAddress providerAddress,
      String providerTelephone,
      String providerNPI) {
    Organization org = _repo.save(new Organization(name, externalId));
    Provider orderingProvider =
        _providerRepo.save(
            new Provider(providerName, providerNPI, providerAddress, providerTelephone));
    Facility facility =
        new Facility(
            org,
            testingFacilityName,
            cliaNumber,
            facilityAddress,
            phone,
            email,
            orderingProvider,
            deviceSpecimenTypes.getDefault(),
            deviceSpecimenTypes.getFullList());
    _facilityRepo.save(facility);
    _oktaRepo.createOrganization(org);
    _oktaRepo.createFacility(facility);
    return org;
  }

  @Transactional(readOnly = false)
  @AuthorizationConfiguration.RequirePermissionEditOrganization
  public Organization updateOrganization(String name) {
    Organization org = getCurrentOrganization();
    org.setOrganizationName(name);
    return _repo.save(org);
  }

  @Transactional(readOnly = false)
  @AuthorizationConfiguration.RequirePermissionEditFacility
  public Facility createFacility(
      String testingFacilityName,
      String cliaNumber,
      StreetAddress facilityAddress,
      String phone,
      String email,
      DeviceSpecimenTypeHolder deviceSpecimenTypes,
      PersonName providerName,
      StreetAddress providerAddress,
      String providerTelephone,
      String providerNPI) {
    Provider orderingProvider =
        _providerRepo.save(
            new Provider(providerName, providerNPI, providerAddress, providerTelephone));
    Organization org = getCurrentOrganization();
    Facility facility =
        new Facility(
            org,
            testingFacilityName,
            cliaNumber,
            facilityAddress,
            phone,
            email,
            orderingProvider,
            deviceSpecimenTypes.getDefault(),
            deviceSpecimenTypes.getFullList());
    facility = _facilityRepo.save(facility);
    _oktaRepo.createFacility(facility);
    return facility;
  }
}
