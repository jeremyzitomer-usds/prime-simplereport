package gov.cdc.usds.simplereport.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import gov.cdc.usds.simplereport.db.model.Facility;
import gov.cdc.usds.simplereport.db.model.Organization;
import gov.cdc.usds.simplereport.db.model.Person;
import gov.cdc.usds.simplereport.db.model.auxiliary.PersonName;
import gov.cdc.usds.simplereport.db.model.auxiliary.PersonRole;
import gov.cdc.usds.simplereport.test_util.SliceTestConfiguration.WithSimpleReportEntryOnlyAllFacilitiesUser;
import gov.cdc.usds.simplereport.test_util.SliceTestConfiguration.WithSimpleReportEntryOnlyUser;
import gov.cdc.usds.simplereport.test_util.SliceTestConfiguration.WithSimpleReportOrgAdminUser;
import gov.cdc.usds.simplereport.test_util.SliceTestConfiguration.WithSimpleReportStandardAllFacilitiesUser;
import gov.cdc.usds.simplereport.test_util.SliceTestConfiguration.WithSimpleReportStandardUser;
import gov.cdc.usds.simplereport.test_util.TestDataFactory;
import gov.cdc.usds.simplereport.test_util.TestUserIdentities;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;

@SuppressWarnings("checkstyle:MagicNumber")
class PersonServiceTest extends BaseServiceTest<PersonService> {

  public static final int PATIENT_PAGEOFFSET = 0;
  public static final int PATIENT_PAGESIZE = 1000;

  // I'll have you know that I didn't actually mean to do this...
  private static final PersonName AMOS = new PersonName("Amos", null, "Quint", null);
  private static final PersonName BRAD = new PersonName("Bradley", "Z.", "Jones", "Jr.");
  private static final PersonName CHARLES = new PersonName("Charles", "Mathew", "Albemarle", "Sr.");
  private static final PersonName DEXTER = new PersonName("Dexter", null, "Jones", null);
  private static final PersonName ELIZABETH =
      new PersonName("Elizabeth", "Martha", "Merriwether", null);
  private static final PersonName FRANK = new PersonName("Frank", "Mathew", "Bones", "3");

  // used for pagination and searching
  private static final PersonName GALE = new PersonName("Gale", "Mary", "Vittorio", "PhD");
  private static final PersonName HEINRICK = new PersonName("Heinrick", "Mark", "Silver", "III");
  private static final PersonName IAN = new PersonName("Ian", "Brou", "Rutter", null);
  private static final PersonName JANNELLE = new PersonName("Jannelle", "Martha", "Cromack", null);
  private static final PersonName KACEY = new PersonName("Kacey", "L", "Mathie", null);
  private static final PersonName LEELOO = new PersonName("Leeloo", "Dallas", "Multipass", null);

  @Autowired private OrganizationService _orgService;
  @Autowired private TestDataFactory _dataFactory;

  private Organization _org;
  private Facility _site1;
  private Facility _site2;

  @BeforeEach
  void setupData() {
    initSampleData();
  }

  @Test
  @WithSimpleReportOrgAdminUser
  void roundTrip() {
    makeFacilities();
    _service.addPatient(
        null,
        "FOO",
        "Fred",
        null,
        "Fosbury",
        "Sr.",
        LocalDate.of(1865, 12, 25),
        _dataFactory.getAddress(),
        "5555555555",
        PersonRole.STAFF,
        null,
        null,
        null,
        null,
        false,
        false);
    _service.addPatient(
        _site1.getInternalId(),
        "BAR",
        "Basil",
        null,
        "Barnacle",
        "4th",
        LocalDate.of(1865, 12, 25),
        _dataFactory.getAddress(),
        "5555555555",
        PersonRole.STAFF,
        null,
        null,
        null,
        null,
        false,
        false);
    _service.addPatient(
        _site2.getInternalId(),
        "BAZ",
        "Chaz",
        null,
        "Pizzazz",
        null,
        LocalDate.of(1865, 12, 25),
        _dataFactory.getAddress(),
        "5555555555",
        PersonRole.STAFF,
        null,
        null,
        null,
        null,
        false,
        false);
    List<Person> all =
        _service.getPatients(null, PATIENT_PAGEOFFSET, PATIENT_PAGESIZE, false, null);
    assertEquals(3, all.size());
    // includes patients whose facility is null
    List<Person> site1Patients =
        _service.getPatients(
            _site1.getInternalId(), PATIENT_PAGEOFFSET, PATIENT_PAGESIZE, false, null);
    assertEquals(2, site1Patients.size());
  }

  @Test
  @WithSimpleReportStandardUser
  void addPatient_standardUser_successDependsOnFacilityAccess() {
    Organization org = _orgService.getCurrentOrganization();
    Facility fac = _dataFactory.createValidFacility(org);
    UUID facilityId = fac.getInternalId();

    _service.addPatient(
        null,
        null,
        "Pebbles",
        null,
        "Flintstone",
        "Sr.",
        LocalDate.of(1990, 1, 1),
        _dataFactory.getAddress(),
        null,
        PersonRole.RESIDENT,
        null,
        null,
        null,
        null,
        false,
        false);

    assertThrows(
        AccessDeniedException.class,
        () ->
            _service.addPatient(
                facilityId,
                null,
                "Fred",
                null,
                "Flintstone",
                "Jr.",
                LocalDate.of(1950, 1, 1),
                _dataFactory.getAddress(),
                null,
                PersonRole.RESIDENT,
                null,
                null,
                null,
                null,
                false,
                false));

    TestUserIdentities.setFacilityAuthorities(fac);
    _service.addPatient(
        facilityId,
        null,
        "Fred",
        null,
        "Flintstone",
        "Jr.",
        LocalDate.of(1950, 1, 1),
        _dataFactory.getAddress(),
        null,
        PersonRole.RESIDENT,
        null,
        null,
        null,
        null,
        false,
        false);
  }

  @Test
  @WithSimpleReportEntryOnlyUser
  void addPatient_entryOnlyUser_error() {
    assertSecurityError(
        () ->
            _service.addPatient(
                null,
                null,
                "Fred",
                null,
                "Flintstone",
                "Jr.",
                LocalDate.of(1950, 1, 1),
                _dataFactory.getAddress(),
                null,
                PersonRole.RESIDENT,
                null,
                null,
                null,
                null,
                false,
                false));
  }

  @Test
  @WithSimpleReportStandardUser
  void deletePatient_standardUser_successDependsOnFacilityAccess() {
    Facility fac = _dataFactory.createValidFacility(_orgService.getCurrentOrganization());
    UUID facilityId = fac.getInternalId();

    TestUserIdentities.setFacilityAuthorities(fac);
    Person p =
        _service.addPatient(
            facilityId,
            "FOO",
            "Fred",
            null,
            "Fosbury",
            "Sr.",
            LocalDate.of(1865, 12, 25),
            _dataFactory.getAddress(),
            "5555555555",
            PersonRole.STAFF,
            null,
            null,
            null,
            null,
            false,
            false);
    TestUserIdentities.setFacilityAuthorities();

    assertThrows(AccessDeniedException.class, () -> _service.setIsDeleted(p.getInternalId(), true));

    TestUserIdentities.setFacilityAuthorities(fac);
    _service.setIsDeleted(p.getInternalId(), true);
    assertEquals(
        0, _service.getPatients(null, PATIENT_PAGEOFFSET, PATIENT_PAGESIZE, false, null).size());
  }

  @Test
  @WithSimpleReportStandardAllFacilitiesUser
  void deletePatient_standardAllFacilitiesUser_success() {
    Facility fac = _dataFactory.createValidFacility(_orgService.getCurrentOrganization());
    UUID facilityId = fac.getInternalId();

    Person p =
        _service.addPatient(
            facilityId,
            "FOO",
            "Fred",
            null,
            "Fosbury",
            "Sr.",
            LocalDate.of(1865, 12, 25),
            _dataFactory.getAddress(),
            "5555555555",
            PersonRole.STAFF,
            null,
            null,
            null,
            null,
            false,
            false);

    _service.setIsDeleted(p.getInternalId(), true);
    assertEquals(
        0, _service.getPatients(null, PATIENT_PAGEOFFSET, PATIENT_PAGESIZE, false, null).size());
  }

  @Test
  @WithSimpleReportStandardUser
  void accessArchivedPatient_standardUser_error() {
    Facility fac = _dataFactory.createValidFacility(_orgService.getCurrentOrganization());
    UUID facilityId = fac.getInternalId();
    TestUserIdentities.setFacilityAuthorities(fac);

    assertSecurityError(
        () -> _service.getPatients(null, PATIENT_PAGEOFFSET, PATIENT_PAGESIZE, true, null));
    assertSecurityError(
        () -> _service.getPatients(facilityId, PATIENT_PAGEOFFSET, PATIENT_PAGESIZE, true, null));
  }

  @Test
  @WithSimpleReportOrgAdminUser
  void deletePatient_adminUser_success() {
    Facility fac = _dataFactory.createValidFacility(_orgService.getCurrentOrganization());
    UUID facilityId = fac.getInternalId();

    Person p =
        _service.addPatient(
            facilityId,
            "FOO",
            "Fred",
            null,
            "Fosbury",
            "Sr.",
            LocalDate.of(1865, 12, 25),
            _dataFactory.getAddress(),
            "5555555555",
            PersonRole.STAFF,
            null,
            null,
            null,
            null,
            false,
            false);

    assertEquals(
        1, _service.getPatients(null, PATIENT_PAGEOFFSET, PATIENT_PAGESIZE, false, null).size());
    Person deletedPerson = _service.setIsDeleted(p.getInternalId(), true);

    assertTrue(deletedPerson.isDeleted());
    assertEquals(
        0, _service.getPatients(null, PATIENT_PAGEOFFSET, PATIENT_PAGESIZE, false, null).size());
    assertEquals(
        0,
        _service.getPatients(facilityId, PATIENT_PAGEOFFSET, PATIENT_PAGESIZE, false, null).size());

    List<Person> result =
        _service.getPatients(null, PATIENT_PAGEOFFSET, PATIENT_PAGESIZE, true, null);
    assertEquals(1, result.size());
    assertTrue(result.get(0).isDeleted());
    assertEquals(
        1,
        _service.getPatients(facilityId, PATIENT_PAGEOFFSET, PATIENT_PAGESIZE, true, null).size());
  }

  @Test
  @WithSimpleReportOrgAdminUser
  void getPatients_noFacility_allFetchedAndSorted() {
    makedata(false);
    // gets all patients across the org
    List<Person> patients =
        _service.getPatients(null, PATIENT_PAGEOFFSET, PATIENT_PAGESIZE, false, null);
    assertPatientList(patients, CHARLES, FRANK, BRAD, DEXTER, ELIZABETH, AMOS);
  }

  @Test
  @WithSimpleReportOrgAdminUser
  void getPatients_facilitySpecific_nullsAndSpecifiedFetchedAndSorted() {
    makedata(false);
    List<Person> patients =
        _service.getPatients(
            _site1.getInternalId(), PATIENT_PAGEOFFSET, PATIENT_PAGESIZE, false, null);
    assertPatientList(patients, CHARLES, BRAD, ELIZABETH, AMOS);
    patients =
        _service.getPatients(
            _site2.getInternalId(), PATIENT_PAGEOFFSET, PATIENT_PAGESIZE, false, null);
    assertPatientList(patients, FRANK, BRAD, DEXTER, AMOS);
  }

  @Test
  @WithSimpleReportOrgAdminUser
  void getPatients_pagination() {
    makedata(true);
    List<Person> patients_org_page0 = _service.getPatients(null, 0, 5, false, null);
    List<Person> patients_org_page1 = _service.getPatients(null, 1, 5, false, null);
    List<Person> patients_org_page2 = _service.getPatients(null, 2, 5, false, null);
    List<Person> patients_org_page3 = _service.getPatients(null, 3, 5, false, null);

    assertPatientList(patients_org_page0, CHARLES, FRANK, JANNELLE, BRAD, DEXTER);
    assertPatientList(patients_org_page1, KACEY, ELIZABETH, LEELOO, AMOS, IAN);
    assertPatientList(patients_org_page2, HEINRICK, GALE);
    assertEquals(0, patients_org_page3.size());

    List<Person> patients_site2_page0 =
        _service.getPatients(_site2.getInternalId(), 0, 4, false, null);
    List<Person> patients_site2_page1 =
        _service.getPatients(_site2.getInternalId(), 1, 4, false, null);
    List<Person> patients_site2_page2 =
        _service.getPatients(_site2.getInternalId(), 2, 4, false, null);

    assertPatientList(patients_site2_page0, FRANK, JANNELLE, BRAD, DEXTER);
    assertPatientList(patients_site2_page1, KACEY, LEELOO, AMOS);
    assertEquals(0, patients_site2_page2.size());
  }

  @Test
  @WithSimpleReportOrgAdminUser
  void getPatients_search_OrgAdminUser() {
    makedata(true);

    UUID site1Id = _site1.getInternalId();
    UUID site2Id = _site2.getInternalId();

    // delete some data to verify achived works as expepected
    // delete Charles (_site1)
    Person charles = _service.getPatients(null, 0, 5, false, null).get(0);
    _service.setIsDeleted(charles.getInternalId(), true);
    // Delete Frank (_site2)
    Person frank = _service.getPatients(site2Id, 0, 5, false, null).get(0);
    _service.setIsDeleted(frank.getInternalId(), true);

    // all facilities, not deleted, "ma"
    List<Person> patients = _service.getPatients(null, 0, 100, false, "ma");
    assertPatientList(patients, JANNELLE, KACEY, ELIZABETH, HEINRICK, GALE);

    // site2, not deleted, "ma"
    patients = _service.getPatients(site2Id, 0, 100, false, "ma");
    assertPatientList(patients, JANNELLE, KACEY);

    // site1, IS deleted, "ma"
    patients = _service.getPatients(site1Id, 0, 100, true, "ma");
    assertPatientList(patients, CHARLES);

    // all facilities, not deleted, "mar"
    patients = _service.getPatients(null, 0, 100, false, "mar");
    assertPatientList(patients, JANNELLE, ELIZABETH, HEINRICK, GALE);

    // all facilities, not deleted, "MARTHA"
    patients = _service.getPatients(null, 0, 100, false, "MARTHA");
    assertPatientList(patients, JANNELLE, ELIZABETH);

    assertEquals(0, _service.getPatientsCount(null, false, "M"));
    assertEquals(0, _service.getPatientsCount(null, false, ""));
  }

  @Test
  @WithSimpleReportOrgAdminUser
  void getPatients_counts() {
    makedata(true);

    UUID site1Id = _site1.getInternalId();
    UUID site2Id = _site2.getInternalId();

    List<Person> patients_org_page0 = _service.getPatients(null, 0, 100, false, null);
    assertEquals(patients_org_page0.size(), _service.getPatientsCount(null, false, null));
    assertEquals(12, _service.getPatientsCount(null, false, null));
    // count includes patients for site2 AND facility=null
    assertEquals(7, _service.getPatientsCount(site2Id, false, null));

    // delete a couple, verify counts
    List<Person> patients_site2 = _service.getPatients(site2Id, 0, 100, false, null);

    // delete Charles (_site1)
    _service.setIsDeleted(patients_org_page0.get(0).getInternalId(), true);
    // Delete Frank (_site2)
    _service.setIsDeleted(patients_site2.get(0).getInternalId(), true);

    assertEquals(10, _service.getPatientsCount(null, false, null));
    assertEquals(6, _service.getPatientsCount(site2Id, false, null));
    assertEquals(2, _service.getPatientsCount(null, true, null));
    assertEquals(1, _service.getPatientsCount(site2Id, true, null));

    // counts for name filtering
    assertEquals(5, _service.getPatientsCount(null, false, "ma"));
    assertEquals(2, _service.getPatientsCount(site2Id, false, "ma"));
    assertEquals(1, _service.getPatientsCount(site1Id, true, "ma"));
    assertEquals(4, _service.getPatientsCount(null, false, "mar"));
    assertEquals(2, _service.getPatientsCount(null, false, "MARTHA"));

    assertEquals(0, _service.getPatientsCount(null, false, "M"));
    assertEquals(0, _service.getPatientsCount(null, false, ""));
  }

  @Test
  @WithSimpleReportEntryOnlyUser
  void getPatients_counts_entryonlyuser_successDependsOnFacilityAccess() {
    makedata(true);

    UUID site1Id = _site1.getInternalId();
    UUID site2Id = _site2.getInternalId();

    assertThrows(AccessDeniedException.class, () -> _service.getPatientsCount(null, false, null));
    assertThrows(
        AccessDeniedException.class, () -> _service.getPatientsCount(site2Id, false, null));
    assertThrows(AccessDeniedException.class, () -> _service.getPatientsCount(null, true, null));
    assertThrows(AccessDeniedException.class, () -> _service.getPatientsCount(site1Id, true, null));

    // this fails because the caller does not have authority to access site2
    assertThrows(
        AccessDeniedException.class, () -> _service.getPatientsCount(site2Id, false, "ma"));

    // this will only return the number of corresponding patients with facility==null,
    // since the caller isn't yet authorized to access site1 or site2
    assertEquals(0, _service.getPatientsCount(null, false, "ma"));

    TestUserIdentities.setFacilityAuthorities(_site2);

    // counts for name filtering
    assertEquals(3, _service.getPatientsCount(site2Id, false, "ma"));

    // this fails because of the isArchive is true
    assertThrows(AccessDeniedException.class, () -> _service.getPatientsCount(site2Id, true, "ma"));

    // this will only return the number of corresponding patients with facility==site2 or
    // facility==null, since the caller isn't yet authorized to access site1
    assertEquals(3, _service.getPatientsCount(null, false, "ma"));

    TestUserIdentities.setFacilityAuthorities(_site1, _site2);

    assertEquals(7, _service.getPatientsCount(null, false, "ma"));

    // what to do when search term is too short? Return all?
    assertEquals(0, _service.getPatientsCount(site1Id, false, "M"));
    assertEquals(0, _service.getPatientsCount(site1Id, false, ""));
  }

  @Test
  @WithSimpleReportEntryOnlyAllFacilitiesUser
  void getPatients_entryOnly_failure() {
    makedata(true);

    UUID site1Id = _site1.getInternalId();

    assertThrows(
        AccessDeniedException.class,
        () -> _service.getPatients(null, PATIENT_PAGEOFFSET, PATIENT_PAGESIZE, false, null));
    assertThrows(
        AccessDeniedException.class,
        () -> _service.getPatients(site1Id, PATIENT_PAGEOFFSET, PATIENT_PAGESIZE, false, null));
    assertThrows(
        AccessDeniedException.class,
        () -> _service.getPatients(null, PATIENT_PAGEOFFSET, PATIENT_PAGESIZE, true, null));
    _service.getPatients(null, PATIENT_PAGEOFFSET, PATIENT_PAGESIZE, false, "ma");
    _service.getPatients(site1Id, PATIENT_PAGEOFFSET, PATIENT_PAGESIZE, false, "ma");
    assertThrows(
        AccessDeniedException.class,
        () -> _service.getPatients(site1Id, PATIENT_PAGEOFFSET, PATIENT_PAGESIZE, true, "ma"));
  }

  @Test
  @WithSimpleReportStandardUser
  void getPatients_standardUser_successDependsOnFacilityAccess() {
    makedata(true);

    UUID site1Id = _site1.getInternalId();

    _service.getPatients(null, PATIENT_PAGEOFFSET, PATIENT_PAGESIZE, false, null);
    assertThrows(
        AccessDeniedException.class,
        () -> _service.getPatients(site1Id, PATIENT_PAGEOFFSET, PATIENT_PAGESIZE, false, null));
    assertThrows(
        AccessDeniedException.class,
        () -> _service.getPatients(null, PATIENT_PAGEOFFSET, PATIENT_PAGESIZE, true, null));

    TestUserIdentities.setFacilityAuthorities(_site1);
    _service.getPatients(site1Id, PATIENT_PAGEOFFSET, PATIENT_PAGESIZE, false, null);
    // standard users still can't access archived patients
    assertThrows(
        AccessDeniedException.class,
        () -> _service.getPatients(site1Id, PATIENT_PAGEOFFSET, PATIENT_PAGESIZE, true, null));
  }

  private void makedata(boolean extraPatients) {
    makeFacilities();
    _org = _orgService.getCurrentOrganization();

    // patients without a facility appear in ALL of the Org's facilities
    _dataFactory.createMinimalPerson(_org, null, AMOS);
    _dataFactory.createMinimalPerson(_org, null, BRAD);

    _dataFactory.createMinimalPerson(_org, _site1, ELIZABETH);
    _dataFactory.createMinimalPerson(_org, _site1, CHARLES);
    _dataFactory.createMinimalPerson(_org, _site2, DEXTER);
    _dataFactory.createMinimalPerson(_org, _site2, FRANK);
    if (extraPatients) {
      _dataFactory.createMinimalPerson(_org, _site1, GALE);
      _dataFactory.createMinimalPerson(_org, _site1, HEINRICK);
      _dataFactory.createMinimalPerson(_org, _site1, IAN);
      _dataFactory.createMinimalPerson(_org, _site2, JANNELLE);
      _dataFactory.createMinimalPerson(_org, _site2, KACEY);
      _dataFactory.createMinimalPerson(_org, _site2, LEELOO);
    }
  }

  private void makeFacilities() {
    _org = _orgService.getCurrentOrganization();
    _site1 = _dataFactory.createValidFacility(_org, "First One");
    _site2 = _dataFactory.createValidFacility(_org, "Second One");
  }

  private static void assertPatientList(List<Person> found, PersonName... expected) {
    // check common elements first
    for (int i = 0; i < expected.length && i < found.size(); i++) {
      assertEquals(expected[i], found.get(i).getNameInfo());
    }
    // *then* check if there are extras
    if (expected.length != found.size()) {
      fail("Expected" + expected.length + " items but found " + found.size());
    }
  }
}
