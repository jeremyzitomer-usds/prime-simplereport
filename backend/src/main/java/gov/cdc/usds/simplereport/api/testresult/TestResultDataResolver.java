package gov.cdc.usds.simplereport.api.testresult;

import gov.cdc.usds.simplereport.api.model.ApiFacility;
import gov.cdc.usds.simplereport.api.model.TestDescription;
import gov.cdc.usds.simplereport.db.model.PatientLink;
import gov.cdc.usds.simplereport.db.model.Person;
import gov.cdc.usds.simplereport.db.model.TestEvent;
import gov.cdc.usds.simplereport.db.model.auxiliary.AskOnEntrySurvey;
import gov.cdc.usds.simplereport.db.model.auxiliary.TestResult;
import graphql.kickstart.tools.GraphQLResolver;
import java.time.LocalDate;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

@Component
public class TestResultDataResolver implements GraphQLResolver<TestEvent> {

  private AskOnEntrySurvey getSurvey(TestEvent testEvent) {
    return testEvent.getSurveyData();
  }

  public UUID getId(TestEvent testEvent) {
    return testEvent.getInternalId();
  }

  public Person getPatient(TestEvent testEvent) {
    return testEvent.getPatientData();
  }

  public Date getDateAdded(TestEvent testEvent) {
    return testEvent.getTestOrder().getCreatedAt();
  }

  public String getPregnancy(TestEvent testEvent) {
    return getSurvey(testEvent).getPregnancy();
  }

  public Boolean getNoSymptoms(TestEvent testEvent) {
    return getSurvey(testEvent).getNoSymptoms();
  }

  public String getSymptoms(TestEvent testEvent) {
    Map<String, Boolean> s = getSurvey(testEvent).getSymptoms();
    JSONObject obj = new JSONObject();
    for (Map.Entry<String, Boolean> entry : s.entrySet()) {
      obj.put(entry.getKey(), entry.getValue().toString());
    }
    return obj.toString();
  }

  public LocalDate getSymptomOnset(TestEvent testEvent) {
    return getSurvey(testEvent).getSymptomOnsetDate();
  }

  public Boolean getFirstTest(TestEvent testEvent) {
    return getSurvey(testEvent).getFirstTest();
  }

  public LocalDate getPriorTestDate(TestEvent testEvent) {
    return getSurvey(testEvent).getPriorTestDate();
  }

  public String getPriorTestType(TestEvent testEvent) {
    return getSurvey(testEvent).getPriorTestType();
  }

  public TestResult getPriorTestResult(TestEvent testEvent) {
    return getSurvey(testEvent).getPriorTestResult();
  }

  public Date getDateTested(TestEvent testEvent) {
    return testEvent.getDateTested();
  }

  public TestDescription getTestPerformed(TestEvent event) {
    return TestDescription.findTestDescription(event.getDeviceType().getLoincCode());
  }

  public String getCorrectionStatus(TestEvent testEvent) {
    return testEvent.getCorrectionStatus().toString();
  }

  public String getReasonForCorrection(TestEvent testEvent) {
    return testEvent.getReasonForCorrection();
  }

  public ApiFacility getFacility(TestEvent testEvent) {
    return new ApiFacility(testEvent.getFacility());
  }

  public PatientLink getPatientLink(TestEvent testEvent) {
    return testEvent.getPatientLink();
  }
}
