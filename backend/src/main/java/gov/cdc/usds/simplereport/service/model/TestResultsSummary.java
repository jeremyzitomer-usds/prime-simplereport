package gov.cdc.usds.simplereport.service.model;

import java.time.LocalDate;
import java.util.Optional;

import gov.cdc.usds.simplereport.db.model.Facility;

public class TestResultsSummary {
  private Facility facility;
  private Demographic demographic;
  private int totalTests;
  private float percentPositive;
  private Optional<LocalDate> since;

  public TestResultsSummary(
      Facility facility,
      Demographic demographic,
      int totalTests,
      float percentPositive,
      Optional<LocalDate> since
  ) {
    this.facility = facility;
    this.demographic = demographic;
    this.totalTests = totalTests;
    this.percentPositive = percentPositive;
    this.since = since;
  }

  public Facility getFacility() {
    return facility;
  }

  public Demographic getDemographic() {
    return demographic;
  }

  public int getTotalTests() {
    return totalTests;
  }

  public float getPercentPositive() {
    return percentPositive;
  }

  public Optional<LocalDate> getSince() {
    return since;
  }
}
