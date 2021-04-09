package gov.cdc.usds.simplereport.api.model;

import gov.cdc.usds.simplereport.api.Translators;
import gov.cdc.usds.simplereport.db.model.auxiliary.PersonRole;
import gov.cdc.usds.simplereport.service.model.Demographic;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

public class DemographicInput {
  private Optional<LocalDate> bornOnOrAfter;
  private Optional<LocalDate> bornOnOrBefore;
  private Optional<PersonRole> role;
  private Optional<String> race;
  private Optional<String> ethnicity;
  private Optional<Collection<String>> gender;
  private Optional<String> genderAssignedAtBirth;
  private Optional<Collection<String>> sexualOrientation;
  private Optional<Boolean> residentCongregateSetting;
  private Optional<Boolean> employedInHealthcare;

  public DemographicInput(
      LocalDate bornOnOrAfter,
      LocalDate bornOnOrBefore,
      String role,
      String race,
      String ethnicity,
      Collection<String> gender,
      String genderAssignedAtBirth,
      Collection<String> sexualOrientation,
      Boolean residentCongregateSetting,
      Boolean employedInHealthcare
  ) {
    this.bornOnOrAfter = Optional.ofNullable(bornOnOrAfter);
    this.bornOnOrBefore = Optional.ofNullable(bornOnOrBefore);
    this.role = Optional.ofNullable(role).map(Translators::parsePersonRole);
    this.race = Optional.ofNullable(race).map(Translators::parseRace);
    this.ethnicity = Optional.ofNullable(ethnicity).map(Translators::parseEthnicity);
    this.gender = Optional.ofNullable(gender);
    this.genderAssignedAtBirth = Optional.ofNullable(genderAssignedAtBirth).map(Translators::parseGenderAssignedAtBirth);
    this.sexualOrientation = Optional.ofNullable(sexualOrientation);
    this.residentCongregateSetting = Optional.ofNullable(residentCongregateSetting);
    this.employedInHealthcare = Optional.ofNullable(employedInHealthcare);
  }

  public Optional<LocalDate> getBornOnOrAfter() {
    return bornOnOrAfter;
  }

  public Optional<LocalDate> getBornOnOrBefore() {
    return bornOnOrBefore;
  }

  public Optional<PersonRole> getRole() {
    return role;
  }

  public Optional<String> getRace() {
    return race;
  }

  public Optional<String> getEthnicity() {
    return ethnicity;
  }

  public Optional<Collection<String>> getGender() {
    return gender;
  }

  public Optional<String> getGenderAssignedAtBirth() {
    return genderAssignedAtBirth;
  }

  public Optional<Collection<String>> getSexualOrientation() {
    return sexualOrientation;
  }

  public Optional<Boolean> getResidentCongregateSetting() {
    return residentCongregateSetting;
  }

  public Optional<Boolean> getEmployedInHealthcare() {
    return employedInHealthcare;
  }

  public Demographic getDemographic() {
    return new Demographic(
      bornOnOrAfter,
      bornOnOrBefore,
      role,
      race,
      ethnicity,
      gender,
      genderAssignedAtBirth,
      sexualOrientation,
      residentCongregateSetting,
      employedInHealthcare
    );
  }
}
