package gov.cdc.usds.simplereport.db.model;

import gov.cdc.usds.simplereport.db.model.auxiliary.AskOnEntrySurvey;
import gov.cdc.usds.simplereport.db.model.auxiliary.TestCorrectionStatus;
import gov.cdc.usds.simplereport.db.model.auxiliary.TestResult;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Random;
import java.util.UUID;
import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
@Immutable
@AttributeOverride(name = "result", column = @Column(nullable = false))
public class TestEvent extends BaseTestInfo {
  private static final Logger LOG = LoggerFactory.getLogger(TestEvent.class);

  private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMddhhmmZ");

  @Column
  @Type(type = "jsonb")
  private Person patientData;

  @Column
  @Type(type = "jsonb")
  private Provider providerData;

  @Column
  @Type(type = "jsonb")
  private AskOnEntrySurvey surveyData;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "test_order_id")
  private TestOrder order;

  @Column(columnDefinition = "uuid")
  private UUID priorCorrectedTestEventId; // used to chain events

  public TestEvent() {}

  public TestEvent(
      TestResult result,
      DeviceSpecimenType deviceType,
      Person patient,
      Facility facility,
      TestOrder order) {
    super(patient, facility, deviceType, result);
    // store a link, and *also* store the object as JSON
    this.patientData = getPatient();
    this.providerData = getFacility().getOrderingProvider();
    this.order = order;
    setDateTestedBackdate(order.getDateTestedBackdate());
    PatientAnswers answers = order.getAskOnEntrySurvey();
    if (answers != null) {
      this.surveyData = order.getAskOnEntrySurvey().getSurvey();
    } else {
      // this can happen during unit tests, but never in prod.
      LOG.error("Order {} missing PatientAnswers", order.getInternalId());
    }
  }

  public TestEvent(TestOrder order) {
    this(
        order.getResult(),
        order.getDeviceSpecimen(),
        order.getPatient(),
        order.getFacility(),
        order);
  }

  // Constructor for creating corrections. Copy the original event
  public TestEvent(
      TestEvent event, TestCorrectionStatus correctionStatus, String reasonForCorrection) {
    super(event, correctionStatus, reasonForCorrection);

    this.patientData = event.getPatientData();
    this.providerData = event.getProviderData();
    this.order = event.getTestOrder();
    this.surveyData = event.getSurveyData();
    setDateTestedBackdate(order.getDateTestedBackdate());
    this.priorCorrectedTestEventId = event.getInternalId();
  }

  public Person getPatientData() {
    return patientData;
  }

  public AskOnEntrySurvey getSurveyData() {
    return surveyData;
  }

  public Date getDateTested() {
    if (getDateTestedBackdate() != null) {
      return getDateTestedBackdate();
    } else {
      return getCreatedAt();
    }
  }

  public Provider getProviderData() {
    return providerData;
  }

  public TestOrder getTestOrder() {
    return order;
  }

  public UUID getTestOrderId() {
    return order.getInternalId();
  }

  public UUID getPriorCorrectedTestEventId() {
    return priorCorrectedTestEventId;
  }

  public PatientLink getPatientLink() {
    return order.getPatientLink();
  }

  public String getHl7v2Old() {
    Random rand = new Random();
    String fhs = 
        "FHS|^~\\&|CDC PRIME - Atlanta, Georgia (Dekalb)^2.16.840.1.114222.4.1.237821^ISO|"
        +"CDC PRIME - Atlanta, Georgia (Dekalb)^2.16.840.1.114222.4.1.237821^ISO|||"
        +DATE_FORMAT.format(getDateTested());
    String mhs = 
        "MSH|^~\\&|CDC PRIME - Atlanta, Georgia (Dekalb)^2.16.840.1.114222.4.1.237821^ISO|"
        +"CDC PRIME - Atlanta, Georgia (Dekalb)^2.16.840.1.114222.4.1.237821^ISO|||"
        +DATE_FORMAT.format(getDateTested())
        +"||ORU^R01^ORU_R01|"+String.valueOf(rand.nextInt(1000000))
        +"|T|2.5.1|||NE|NE|USA||||PHLabReport-NoAck^ELR_Receiver^2.16.840.1.113883.9.11^ISO";
    String sft =
        "SFT|Centers for Disease Control and Prevention|0.1-SOGI_EQUITY_DEMO|PRIME SimpleReport|"
        +"0.1-SOGI_EQUITY_DEMO||20210406";
    return fhs + "\r" + mhs + "\r" + sft;
  }
}
