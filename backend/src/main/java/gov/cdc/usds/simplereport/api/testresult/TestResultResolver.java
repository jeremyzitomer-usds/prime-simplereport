package gov.cdc.usds.simplereport.api.testresult;

import gov.cdc.usds.simplereport.api.Translators;
import gov.cdc.usds.simplereport.api.model.ApiTestResultsSummary;
import gov.cdc.usds.simplereport.db.model.auxiliary.PersonRole;
import gov.cdc.usds.simplereport.db.model.TestEvent;
import gov.cdc.usds.simplereport.service.model.Demographic;
import gov.cdc.usds.simplereport.service.TestOrderService;
import graphql.kickstart.tools.GraphQLMutationResolver;
import graphql.kickstart.tools.GraphQLQueryResolver;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TestResultResolver implements GraphQLQueryResolver, GraphQLMutationResolver {

  @Autowired private TestOrderService tos;

  public List<TestEvent> getTestResults(UUID facilityId, int pageNumber, int pageSize) {
    if (pageNumber < 0) {
      pageNumber = TestOrderService.DEFAULT_PAGINATION_PAGEOFFSET;
    }
    if (pageSize < 1) {
      pageSize = TestOrderService.DEFAULT_PAGINATION_PAGESIZE;
    }

    return tos.getTestEventsResults(facilityId, pageNumber, pageSize);
  }

  public int testResultsCount(UUID facilityId) {
    return tos.getTestResultsCount(facilityId);
  }

  public TestEvent correctTestMarkAsError(UUID id, String reasonForCorrection) {
    return tos.correctTestMarkAsError(id, reasonForCorrection);
  }

  public TestEvent getTestResult(UUID id) {
    return tos.getTestResult(id);
  }

  public ApiTestResultsSummary getTestResultsSummary(
      UUID facilityId,
      LocalDate bornOnOrAfter,
      LocalDate bornOnOrBefore,
      String role,
      String race,
      String ethnicity,
      List<String> gender,
      String genderAssignedAtBirth,
      List<String> sexualOrientation,
      Boolean residentCongregateSetting,
      Boolean employedInHealthcare,
      LocalDate since) {
    Demographic demographic = new Demographic(
        Optional.ofNullable(bornOnOrAfter),
        Optional.ofNullable(bornOnOrBefore),
        Optional.ofNullable(role).map(Translators::parsePersonRole),
        Optional.ofNullable(race).map(Translators::parseRace),
        Optional.ofNullable(ethnicity).map(Translators::parseEthnicity),
        Optional.ofNullable(gender),
        Optional.ofNullable(genderAssignedAtBirth).map(Translators::parseGenderAssignedAtBirth),
        Optional.ofNullable(sexualOrientation),
        Optional.ofNullable(residentCongregateSetting),
        Optional.ofNullable(employedInHealthcare));
    return new ApiTestResultsSummary(tos.getTestResultsSummary(facilityId, demographic, Optional.ofNullable(since)));
  }
}
