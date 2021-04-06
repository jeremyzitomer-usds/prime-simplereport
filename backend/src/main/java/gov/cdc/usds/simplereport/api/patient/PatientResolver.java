package gov.cdc.usds.simplereport.api.patient;

import gov.cdc.usds.simplereport.config.AuthorizationConfiguration;
import gov.cdc.usds.simplereport.db.model.Person;
import gov.cdc.usds.simplereport.service.PersonService;
import gov.cdc.usds.simplereport.service.model.DemographicValues;
import graphql.kickstart.tools.GraphQLQueryResolver;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Created by nickrobison on 11/17/20 */
@Component
public class PatientResolver implements GraphQLQueryResolver {

  @Autowired private PersonService ps;

  // authorization happens in calls to PersonService
  public List<Person> getPatients(
      UUID facilityId, int pageNumber, int pageSize, boolean showDeleted, String namePrefixMatch) {
    return ps.getPatients(facilityId, pageNumber, pageSize, showDeleted, namePrefixMatch, null);
  }

  // authorization happens in calls to PersonService
  public long patientsCount(UUID facilityId, boolean showDeleted, String namePrefixMatch) {
    return ps.getPatientsCount(facilityId, showDeleted, namePrefixMatch);
  }

  @AuthorizationConfiguration.RequirePermissionSearchTargetPatient
  public Person getPatient(UUID patientId) {
    return ps.getPatientNoPermissionsCheck(patientId);
  }

  public DemographicValues getDemographicValues(UUID facilityId) {
    return ps.getDemographicValues(facilityId);
  }
}
