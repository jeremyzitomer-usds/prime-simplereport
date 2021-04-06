package gov.cdc.usds.simplereport.api.model;

import gov.cdc.usds.simplereport.db.model.Facility;
import gov.cdc.usds.simplereport.service.model.Demographic;
import gov.cdc.usds.simplereport.service.model.TestResultsSummary;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ApiTestResultsSummary {

  private TestResultsSummary summary;

  public ApiTestResultsSummary(TestResultsSummary wrapped) {
    super();
    this.summary = wrapped;
  }

  public ApiFacility getFacility() {
    return new ApiFacility(summary.getFacility());
  }

  public Demographic getDemographic() {
    return summary.getDemographic();
  }

  public int getTotalTests() {
    return summary.getTotalTests();
  }

  public float getPercentPositive() {
    return summary.getPercentPositive();
  }

  public Optional<LocalDate> getSince() {
    return summary.getSince();
  }
}
