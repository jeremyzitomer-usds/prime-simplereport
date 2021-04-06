package gov.cdc.usds.simplereport.db.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import gov.cdc.usds.simplereport.db.model.auxiliary.PersonName;
import gov.cdc.usds.simplereport.db.model.auxiliary.PersonRole;
import gov.cdc.usds.simplereport.db.model.auxiliary.RaceArrayConverter;
import gov.cdc.usds.simplereport.db.model.auxiliary.StreetAddress;
import gov.cdc.usds.simplereport.db.model.auxiliary.TestResultDeliveryPreference;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import org.hibernate.annotations.Type;

/**
 * The person record (generally, a patient getting a test).
 *
 * <p>ATTENTION PLEASE: if you ever add a field to this object, you must decide if it should be
 * saved forever as part of a {@link TestEvent}, and annotate it with {@link JsonIgnore} if not.
 *
 * <p>EVEN MORE ATTENTION PLEASE: if you ever change the <b>type</b> of any non-ignored field of
 * this object, you will likely break many things, so do not do that.
 */
@Entity
public class Person extends OrganizationScopedEternalEntity {

  // NOTE: facility==NULL means this person appears in ALL facilities for a given Organization.
  // this is common for imported patients.
  @ManyToOne(optional = true)
  @JoinColumn(name = "facility_id")
  @JsonIgnore // do not serialize to TestEvents
  private Facility facility;

  @Column private String lookupId;

  @Column(nullable = false)
  @Embedded
  @JsonUnwrapped
  private PersonName nameInfo;

  @Column private LocalDate birthDate;
  @Embedded private StreetAddress address;

  @Column 
  @Type(type = "list-array")
  private List<String> gender;
  @Column 
  private String genderAssignedAtBirth;
  @Column 
  @Type(type = "list-array")
  private List<String> sexualOrientation;

  @Column
  @JsonDeserialize(converter = RaceArrayConverter.class)
  private String race;

  @Column private String ethnicity;
  @Column private String telephone;
  @Column private String email;

  @Column(nullable = false)
  private boolean employedInHealthcare;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private PersonRole role;

  @Column(nullable = false)
  private boolean residentCongregateSetting;

  @Column
  @Type(type = "pg_enum")
  @Enumerated(EnumType.STRING)
  private TestResultDeliveryPreference testResultDelivery;

  protected Person() {
    /* for hibernate */
  }

  public Person(
      String firstName, String middleName, String lastName, String suffix, Organization org) {
    super(org);
    this.nameInfo = new PersonName(firstName, middleName, lastName, suffix);
    this.role = PersonRole.STAFF;
  }

  public Person(
      Organization organization,
      String lookupId,
      String firstName,
      String middleName,
      String lastName,
      String suffix,
      LocalDate birthDate,
      StreetAddress address,
      String telephone,
      PersonRole role,
      String email,
      String race,
      String ethnicity,
      Collection<String> gender,
      String genderAssignedAtBirth,
      Collection<String> sexualOrientation,
      Boolean residentCongregateSetting,
      Boolean employedInHealthcare) {
    super(organization);
    this.lookupId = lookupId;
    this.nameInfo = new PersonName(firstName, middleName, lastName, suffix);
    this.birthDate = birthDate;
    this.telephone = telephone;
    this.address = address;
    this.role = role;
    this.email = email;
    this.race = race;
    this.ethnicity = ethnicity;
    this.gender = gender == null ? null : new ArrayList<>(gender);
    this.genderAssignedAtBirth = genderAssignedAtBirth;
    this.sexualOrientation = sexualOrientation == null ? null : new ArrayList<>(sexualOrientation);
    this.residentCongregateSetting = residentCongregateSetting;
    this.employedInHealthcare = employedInHealthcare;
  }

  public Person(PersonName names, Organization org, Facility fac) {
    super(org);
    this.facility = fac;
    this.nameInfo = names;
    this.role = PersonRole.STAFF;
  }

  public void updatePatient(
      String lookupId,
      String firstName,
      String middleName,
      String lastName,
      String suffix,
      LocalDate birthDate,
      StreetAddress address,
      String telephone,
      PersonRole role,
      String email,
      String race,
      String ethnicity,
      Collection<String> gender,
      String genderAssignedAtBirth,
      Collection<String> sexualOrientation,
      Boolean residentCongregateSetting,
      Boolean employedInHealthcare) {
    this.lookupId = lookupId;
    this.nameInfo.setFirstName(firstName);
    this.nameInfo.setMiddleName(middleName);
    this.nameInfo.setLastName(lastName);
    this.nameInfo.setSuffix(suffix);
    this.birthDate = birthDate;
    this.telephone = telephone;
    this.address = address;
    this.role = role;
    this.email = email;
    this.race = race;
    this.ethnicity = ethnicity;
    this.gender = gender == null ? null : new ArrayList<>(gender);
    this.genderAssignedAtBirth = genderAssignedAtBirth;
    this.sexualOrientation = sexualOrientation == null ? null : new ArrayList<>(sexualOrientation);
    this.residentCongregateSetting = residentCongregateSetting;
    this.employedInHealthcare = employedInHealthcare;
  }

  public Facility getFacility() {
    return facility;
  }

  public void setFacility(Facility f) {
    facility = f;
  }

  public String getLookupId() {
    return lookupId;
  }

  public PersonName getNameInfo() {
    return nameInfo;
  }

  public String getFirstName() {
    return nameInfo.getFirstName();
  }

  public String getMiddleName() {
    return nameInfo.getMiddleName();
  }

  public String getLastName() {
    return nameInfo.getLastName();
  }

  public String getSuffix() {
    return nameInfo.getSuffix();
  }

  public LocalDate getBirthDate() {
    return birthDate;
  }

  public StreetAddress getAddress() {
    return address;
  }

  public String getTelephone() {
    return telephone;
  }

  public String getEmail() {
    return email;
  }

  public String getRace() {
    return race;
  }

  public String getEthnicity() {
    return ethnicity;
  }

  public List<String> getGender() {
    return gender;
  }

  public String getGenderAssignedAtBirth() {
    return genderAssignedAtBirth;
  }

  public List<String> getSexualOrientation() {
    return sexualOrientation;
  }

  public Boolean getResidentCongregateSetting() {
    return residentCongregateSetting;
  }

  public Boolean getEmployedInHealthcare() {
    return employedInHealthcare;
  }

  @JsonIgnore
  public String getStreet() {
    return address == null ? "" : address.getStreetOne();
  }

  @JsonIgnore
  public String getStreetTwo() {
    return address == null ? "" : address.getStreetTwo();
  }

  @JsonIgnore
  public String getCity() {
    if (address == null) {
      return "";
    }
    return address.getCity();
  }

  @JsonIgnore
  public String getState() {
    if (address == null) {
      return "";
    }
    return address.getState();
  }

  @JsonIgnore
  public String getZipCode() {
    if (address == null) {
      return "";
    }
    return address.getPostalCode();
  }

  @JsonIgnore
  public String getCounty() {
    if (address == null) {
      return "";
    }
    return address.getCounty();
  }

  public PersonRole getRole() {
    return role;
  }

  // these field names strings are used by Specification builders
  public static final class SpecField {
    public static final String PERSON_NAME = "nameInfo";
    public static final String IS_DELETED = "isDeleted";
    public static final String FACILITY = "facility";
    public static final String ORGANIZATION = "organization";
    public static final String INTERNAL_ID = "internalId";
    public static final String FIRST_NAME = "firstName";
    public static final String MIDDLE_NAME = "middleName";
    public static final String LAST_NAME = "lastName";
    public static final String BIRTH_DATE = "birthDate";
    public static final String ROLE = "role";
    public static final String RACE = "race";
    public static final String ETHNICITY = "ethnicity";
    public static final String GENDER = "gender";
    public static final String GENDER_ASSIGNED_AT_BIRTH = "genderAssignedAtBirth";
    public static final String SEXUAL_ORIENTATION = "sexualOrientation";
    public static final String RESIDENT_CONGREGATE_SETTING = "residentCongregateSetting";
    public static final String EMPLOYED_IN_HEALTHCARE = "employedInHealthcare";

    private SpecField() {} // sonarcloud codesmell
  }

  public TestResultDeliveryPreference getTestResultDelivery() {
    return testResultDelivery;
  }

  public void setTestResultDelivery(TestResultDeliveryPreference testResultDelivery) {
    this.testResultDelivery = testResultDelivery;
  }
}
