package gov.cdc.usds.simplereport.service.model;

import gov.cdc.usds.simplereport.db.model.auxiliary.PersonRole;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * contains all freeform or enumerated values for patient demographic fields 
 * that cannot be inferred by the front-end from the GraphQL schema alone;
 * contains only values that are actually used by at least one patient;
 * each list of values should be sorted from most to least common
 */
public class DemographicValues {
  private List<PersonRole> roles;
  private List<String> races;
  private List<String> ethnicities;
  private List<String> genders;
  private List<String> gendersAssignedAtBirth;
  private List<String> sexualOrientations;

  public DemographicValues(
      List<PersonRole> roles,
      List<String> races,
      List<String> ethnicities,
      List<String> genders,
      List<String> gendersAssignedAtBirth,
      List<String> sexualOrientations
  ) {
    this.roles = roles;
    this.races = races;
    this.ethnicities = ethnicities;
    this.genders = genders;
    this.gendersAssignedAtBirth = gendersAssignedAtBirth;
    this.sexualOrientations = sexualOrientations;
  }

  public List<PersonRole> getRoles() {
    return roles;
  }

  public List<String> getRaces() {
    return races;
  }

  public List<String> getEthnicities() {
    return ethnicities;
  }

  public List<String> getGenders() {
    return genders;
  }

  public List<String> getGendersAssignedAtBirth() {
    return gendersAssignedAtBirth;
  }

  public List<String> getSexualOrientations() {
    return sexualOrientations;
  }
}
