package gov.cdc.usds.simplereport.service.model;

import gov.cdc.usds.simplereport.db.model.auxiliary.PersonRole;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

public class Demographic {
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

  public Demographic(
      Optional<LocalDate> bornOnOrAfter,
      Optional<LocalDate> bornOnOrBefore,
      Optional<PersonRole> role,
      Optional<String> race,
      Optional<String> ethnicity,
      Optional<Collection<String>> gender,
      Optional<String> genderAssignedAtBirth,
      Optional<Collection<String>> sexualOrientation,
      Optional<Boolean> residentCongregateSetting,
      Optional<Boolean> employedInHealthcare
  ) {
    this.bornOnOrAfter = bornOnOrAfter;
    this.bornOnOrBefore = bornOnOrBefore;
    this.role = role;
    this.race = race;
    this.ethnicity = ethnicity;
    this.gender = gender;
    this.genderAssignedAtBirth = genderAssignedAtBirth;
    this.sexualOrientation = sexualOrientation;
    this.residentCongregateSetting = residentCongregateSetting;
    this.employedInHealthcare = employedInHealthcare;
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

  public String getDescription() {
    return ((race.isPresent() ? race.get() + " " : "") +
        (ethnicity.isPresent() ? ethnicity.get() + " " : "") +
        (sexualOrientation.isPresent() && !sexualOrientation.get().isEmpty() 
            ? String.join(" ", sexualOrientation.get()) + " " 
            : "") +
        (gender.isPresent() && !gender.get().isEmpty() 
            ? String.join(" ", gender.get()) + " " 
            : "") +
        (genderAssignedAtBirth.isPresent() ? "assigned " + genderAssignedAtBirth.get() + " at birth " : "") +
        (role.isPresent() ? "in " + role.get().toString().toLowerCase() + "role " : "") +
        (bornOnOrAfter.isPresent() && bornOnOrBefore.isPresent()
            ? "born between " + bornOnOrAfter.get().toString() + 
              " and " + bornOnOrBefore.get().toString() + " "
            : bornOnOrAfter.isPresent() ? "born after " + bornOnOrAfter.get().toString() + " "
            : bornOnOrBefore.isPresent() ? "born before " + bornOnOrBefore.get().toString() + " "
            : "") +
        (residentCongregateSetting.isPresent() && employedInHealthcare.isPresent()
            ? (residentCongregateSetting.get() ? "" : "not ") + "in congregate living and " +
              (employedInHealthcare.get() ? "" : "not ") + "employed in healthcare " 
            : residentCongregateSetting.isPresent() 
                ? (residentCongregateSetting.get() ? "" : "not ") + "in congregate living "
            : employedInHealthcare.isPresent() 
                ? (employedInHealthcare.get() ? "" : "not ") + "employed in healthcare "
            : "")).trim();
  }
}
