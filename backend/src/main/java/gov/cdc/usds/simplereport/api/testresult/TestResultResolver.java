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
import java.util.stream.Collectors;

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

  public List<ApiTestResultsSummary> getTestResultsSummary(
      UUID facilityId,
      List<Demographic> demographics,
      LocalDate since) {
    return demographics.stream()
        .map(d -> 
            new ApiTestResultsSummary(tos.getTestResultsSummary(facilityId, d, Optional.ofNullable(since))))
        .collect(Collectors.toList());
  }
}
