package gov.cdc.usds.simplereport.db.model;

import gov.cdc.usds.simplereport.db.model.auxiliary.AskOnEntrySurvey;
import gov.cdc.usds.simplereport.db.model.auxiliary.TestCorrectionStatus;
import gov.cdc.usds.simplereport.db.model.auxiliary.TestResult;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

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

  private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");
  private static final SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat("yyyyMMddhhmmZ");
  private static final String HL7_SECTION_SEPARATOR = "\n";
  private static final Map<String, String> RACE_CODE_MAP =
      Map.of(
          "native", "1002-5",
          "asian", "2028-9",
          "black", "2054-5",
          "pacific", "2076-8",
          "white", "2106-3",
          "other", "2131-1", // not currently in our app
          "unknown", "UNK",
          "refused", "ASKU" // Asked, but unknown
          );
  private static final Map<String, String> RACE_DESCRIPTION_MAP =
      Map.of(
          "native", "American Indian or Alaska Native",
          "asian", "Asian", 
          "black", "Black or African American",
          "pacific", "Native Hawaiian or Other Pacific Islander",
          "white", "White",
          "unknown", "Unknown",
          "refused", "Asked, but unknown");
  private static final Map<String, String> ETHNICITY_CODE_MAP =
      Map.of(
          "hispanic", "H",
          "not_hispanic", "N",
          "unknown", "U");
  private static final Map<String, String> ETHNICITY_DESCRIPTION_MAP =
      Map.of(
          "hispanic", "Hispanic or Latino",
          "not_hispanic", "Not Hispanic or Latino",
          "unknown", "Unknown");

  private static final Map<String, String> GAAB_OLD_MAP =
      Map.of(
        "female", "F",
        "male", "M",
        "x", "O",
        "unsure", "U",
        "not_assigned", "A",
        "not_disclosed", "U");

  private static final Map<String, String> GENDER_NEW_SNOMED_CODE_MAP =
      Map.of(
        "man", "446151000124109",
        "woman", "446141000124107",
        "nonbinary", "446131000124102",
        "questioning", "<NEW-SNOMED-CODE-FOR-QUESTIONING:2342339292>",
        "not_disclosed", "ASKU"
      );

  private static final Map<String, String> GENDER_NEW_SNOMED_EXP_MAP =
      Map.of(
        "man", "Identifies as male gender",
        "woman", "Identifies as female gender",
        "nonbinary", "Identifies as non-conforming gender",
        "questioning", "Identifies as questioning gender",
        "not_disclosed", "ASKU"
      );

  private static final Map<String, String> SO_NEW_SNOMED_CODE_MAP =
      Map.of(
        "bisexual_or_pansexual", "42035005",
        "asexual", "<NEW-SNOMED-CODE-FOR-ASEXUAL:23498239842>",
        "heterosexual", "20430005",
        "homosexual", "38628009",
        "questioning", "<NEW-SNOMED-CODE-FOR-QUESTIONING>",
        "not_disclosed", "ASKU"
      );

  private static final Map<String, String> SO_NEW_SNOMED_EXP_MAP =
      Map.of(
        "bisexual_or_pansexual", "Bisexual",
        "asexual", "Asexual",
        "heterosexual", "Straight or Heterosexual",
        "homosexual", "Lesbian, Gay, or Homosexual",
        "questioning", "Questioning",
        "not_disclosed", "ASKU"
      );

  private static final Map<String, String> SEX_NEW_SNOMED_CODE_MAP =
      Map.of(
        "female", "<NEW-SNOMED-CODE-FOR-FEMALE-SFCU:2393598363>",
        "male", "<NEW-SNOMED-CODE-FOR-MALE-SFCU:66894645454>",
        "x", "<NEW-SNOMED-CODE-FOR-X-SFCU:404342522222>",
        "unsure", "<NEW-SNOMED-CODE-FOR-UNSURE-SFCU:506950965555>",
        "not_assigned", "<NEW-SNOMED-CODE-FOR-NOT-ASSIGNED-SFCU:112192090283>",
        "not_disclosed", "<NEW-SNOMED-CODE-FOR-NOT-DISCLOSED-SFCU:23325235235>");
  
  private static final Map<String, String> SEX_NEW_SNOMED_EXP_MAP =
      Map.of(
        "female", "Female",
        "male", "Male",
        "x", "Complex",
        "unsure", "Unsure",
        "not_assigned", "Not assigned",
        "not_disclosed", "Not disclosed");

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

  private String val(String s) {

    return s == null ? "" : s.replace("notlisted","");
  }

  private String val(String s, String d) {
    return s == null 
        ? (d == null ? "" : d)
        : s.replace("notlisted","");
  }

  public String getHl7v2Old() {
    Random rand = new Random();
    String randInt = String.valueOf(rand.nextInt(1000000));
    String abridgedTestId = getInternalId().toString().substring(0, 15);
    String fhs = 
        "FHS|^~\\&|CDC PRIME - Atlanta, Georgia (Dekalb)^2.16.840.1.114222.4.1.237821^ISO|"
        +"CDC PRIME - Atlanta, Georgia (Dekalb)^2.16.840.1.114222.4.1.237821^ISO|||"
        +DATETIME_FORMAT.format(getDateTested());
    String mhs = 
        "MSH|^~\\&|CDC PRIME - Atlanta, Georgia (Dekalb)^2.16.840.1.114222.4.1.237821^ISO|"
        +"CDC PRIME - Atlanta, Georgia (Dekalb)^2.16.840.1.114222.4.1.237821^ISO|||"
        +DATETIME_FORMAT.format(getDateTested())
        +"||ORU^R01^ORU_R01|"+randInt
        +"|T|2.5.1|||NE|NE|USA||||PHLabReport-NoAck^ELR_Receiver^2.16.840.1.113883.9.11^ISO";
    String sft =
        "SFT|Centers for Disease Control and Prevention|0.1-SOGI_EQUITY_DEMO|PRIME SimpleReport|"
        +"0.1-SOGI_EQUITY_DEMO||20210406";
    String pid =
        "PID|1||"+abridgedTestId+"||"
        +val(patientData.getLastName())+"^"
        +val(patientData.getFirstName())+"^"
        +val(patientData.getMiddleName())+"^"
        +val(patientData.getSuffix())+"^^^L||"
        +(patientData.getBirthDate()==null
            ?""
            :DATE_FORMAT.format(Date.from(patientData.getBirthDate().atStartOfDay(ZoneId.systemDefault()).toInstant())))
        +"|*****"+GAAB_OLD_MAP.get(val(patientData.getGenderAssignedAtBirth(),"unknown"))+"*****||"
        +RACE_CODE_MAP.get(val(patientData.getRace(),"unknown"))+"^"
        +RACE_DESCRIPTION_MAP.get(val(patientData.getRace(), "unknown"))+"^"
        +"HL70005|"+val(patientData.getStreet())+"^"+val(patientData.getStreetTwo())+"^"
        +val(patientData.getCity())+"^"
        +val(patientData.getState())+"^"
        +val(patientData.getZipCode())+"||^NET^Internet^"+val(patientData.getEmail())+"~^1^"
        +(patientData.getTelephone()==null?"":patientData.getTelephone().substring(1,4))+"^"
        +(patientData.getTelephone()==null?"":patientData.getTelephone().substring(6))
        +"|||||||||"
        +ETHNICITY_CODE_MAP.get(val(patientData.getEthnicity(),"unknown"))+"^"
        +ETHNICITY_DESCRIPTION_MAP.get(val(patientData.getEthnicity(),"unknown"))+"^"
        +"HL70189||||||||N";
    String orc =
        "ORC|RE|"+randInt+"|"+randInt+"^^"+abridgedTestId+"^"+"UUID|||||||||"
        +providerData.getInternalId().toString().substring(0, 15)
        +"^"+val(providerData.getNameInfo().getLastName())+"^"+val(providerData.getNameInfo().getFirstName())
        +"^^"+val(providerData.getNameInfo().getSuffix())+"^^^^CMS&2.16.840.1.113883.3.249&ISO^^^^NPI||"
        +"^WPN^PH^^1^"
        +(providerData.getTelephone()==null?"":providerData.getTelephone().substring(1,4))
        +"^"+(providerData.getTelephone()==null?"":providerData.getTelephone().substring(6))
        +"|"+DATETIME_FORMAT.format(getDateTested())+"||||||"
        +val(getFacility().getFacilityName())+"|"+val(getFacility().getAddress().getStreetOne())
        +"^"+val(getFacility().getAddress().getStreetTwo())+"^"
        +val(getFacility().getAddress().getCity())+"^"
        +val(getFacility().getAddress().getState())+"^"
        +val(getFacility().getAddress().getPostalCode())
        +"|^WPN^PH^^1^"
        +(getFacility().getTelephone()==null?"":getFacility().getTelephone().substring(1,4))
        +"^"+(getFacility().getTelephone()==null?"":getFacility().getTelephone().substring(6))
        +"|"+val(providerData.getAddress().getStreetOne())
        +"^"+val(providerData.getAddress().getStreetTwo())+"^"
        +val(providerData.getAddress().getCity())+"^"
        +val(providerData.getAddress().getState())+"^"
        +val(providerData.getAddress().getPostalCode());
    String obr =
        "OBR|1|"+randInt+"|"+randInt+"|"+val(getDeviceType().getLoincCode())
        +"^SARS-CoV-2 (COVID-19) Ag [Presence] in Respiratory specimen by Rapid immunoassay"
        +"^LN|||"+DATETIME_FORMAT.format(getDateTested())+"|"+DATETIME_FORMAT.format(getDateTested())
        +"||||||||"+providerData.getInternalId().toString().substring(0, 15)
        +"^"+val(providerData.getNameInfo().getLastName())+"^"+val(providerData.getNameInfo().getFirstName())
        +"^^"+val(providerData.getNameInfo().getSuffix())+"^^^^CMS&2.16.840.1.113883.3.249&ISO^^^^NPI"
        +"|^WPN^PH^^1^"
        +(getFacility().getTelephone()==null?"":getFacility().getTelephone().substring(1,4))
        +"^"+(getFacility().getTelephone()==null?"":getFacility().getTelephone().substring(6))
        +"|||||"+DATETIME_FORMAT.format(getDateTested())+"|||F";
    String obx =
        "OBX|1|CWE|94558-4^SARS-CoV-2 (COVID-19) Ag [Presence] in Respiratory specimen by Rapid immunoassay^LN^^^^2.68||10828004^Positive^SCT||||||F|||202103290106-0400|78D2734280^CLIA||10811877011290_DIT^^99ELR^^^^2.68||202103240000-0500||||Any lab USA^^^^^CLIA&2.16.840.1.113883.19.4.6&ISO^XX^^^78D2734280^CLIA|2004 Ronald Parks^^Upper black eddy^PA^18972^^^^42017^MOBX|2|CWE|95418-0^Whether patient is employed in a healthcare setting^LN^^^^2.69||"+(patientData.getEmployedInHealthcare()?"Y":"N")+"^"+(patientData.getEmployedInHealthcare()?"Yes":"No")+"^HL70136||||||F|||202103290106-0400|78D2734280||||202103240000-0500||||Any lab USA^^^^^CLIA&2.16.840.1.113883.19.4.6&ISO^XX^^^78D2734280|2004 Ronald Parks^^Upper black eddy^PA^18972^^^^42017|||||QST^MOBX|3|CWE|95417-2^First test for condition of interest^LN^^^^2.69||UNK^Unknown^HL70136||||||F|||202103290106-0400|78D2734280||||202103240000-0500||||Any lab USA^^^^^CLIA&2.16.840.1.113883.19.4.6&ISO^XX^^^78D2734280|2004 Ronald Parks^^Upper black eddy^PA^18972^^^^42017|||||QST^MOBX|4|CWE|65222-2^Date and time of symptom onset^LN^^^^2.68||20210325||||||F|||202103290106-0400|78D2734280||||202103240000-0500||||Any lab USA^^^^^CLIA&2.16.840.1.113883.19.4.6&ISO^XX^^^78D2734280|2004 Ronald Parks^^Upper black eddy^PA^18972^^^^42017|||||QST^MOBX|5|CWE|95421-4^Resides in a congregate care setting^LN^^^^2.69||"+(patientData.getResidentCongregateSetting()?"Y":"N")+"^"+(patientData.getResidentCongregateSetting()?"Yes":"No")+"^HL70136||||||F|||202103290106-0400|78D2734280||||202103240000-0500||||Any lab USA^^^^^CLIA&2.16.840.1.113883.19.4.6&ISO^XX^^^78D2734280|2004 Ronald Parks^^Upper black eddy^PA^18972^^^^42017|||||QST^MOBX|6|CWE|95419-8^Has symptoms related to condition of interest^LN^^^^2.69||Y^Yes^HL70136||||||F|||202103290106-0400|78D2734280||||202103240000-0500||||Any lab USA^^^^^CLIA&2.16.840.1.113883.19.4.6&ISO^XX^^^78D2734280|2004 Ronald Parks^^Upper black eddy^PA^18972^^^^42017|||||QST^MSPM|1|574184&&78D2734280&CLIA^574184&&78D2734280&CLIA||119334006^Sputum specimen^SCT^^^^2.67||||71836000^Nasopharyngeal structure (body structure)^SCT^^^^2020-09-01|||||||||202103290106-0400|20210329010606.0000-0400";
    
    return String.join(HL7_SECTION_SEPARATOR, List.of(fhs, mhs, sft, pid, orc, obr, obx));
  }

  public String getHl7v2New() {
    Random rand = new Random();
    String randInt = String.valueOf(rand.nextInt(1000000));
    String abridgedTestId = getInternalId().toString().substring(0, 15);
    String fhs = 
        "FHS|^~\\&|CDC PRIME - Atlanta, Georgia (Dekalb)^2.16.840.1.114222.4.1.237821^ISO|"
        +"CDC PRIME - Atlanta, Georgia (Dekalb)^2.16.840.1.114222.4.1.237821^ISO|||"
        +DATETIME_FORMAT.format(getDateTested());
    String mhs = 
        "MSH|^~\\&|CDC PRIME - Atlanta, Georgia (Dekalb)^2.16.840.1.114222.4.1.237821^ISO|"
        +"CDC PRIME - Atlanta, Georgia (Dekalb)^2.16.840.1.114222.4.1.237821^ISO|||"
        +DATETIME_FORMAT.format(getDateTested())
        +"||ORU^R01^ORU_R01|"+randInt
        +"|T|2.5.1|||NE|NE|USA||||PHLabReport-NoAck^ELR_Receiver^2.16.840.1.113883.9.11^ISO";
    String sft =
        "SFT|Centers for Disease Control and Prevention|0.1-SOGI_EQUITY_DEMO|PRIME SimpleReport|"
        +"0.1-SOGI_EQUITY_DEMO||20210406";
    String pid =
        "PID|1||"+abridgedTestId+"||"
        +val(patientData.getLastName())+"^"
        +val(patientData.getFirstName())+"^"
        +val(patientData.getMiddleName())+"^"
        +val(patientData.getSuffix())+"^^^L||"
        +(patientData.getBirthDate()==null
            ?""
            :DATE_FORMAT.format(Date.from(patientData.getBirthDate().atStartOfDay(ZoneId.systemDefault()).toInstant())))
        +"|*****N*****||"
        +RACE_CODE_MAP.get(val(patientData.getRace(),"unknown"))+"^"
        +RACE_DESCRIPTION_MAP.get(val(patientData.getRace(), "unknown"))+"^"
        +"HL70005|"+val(patientData.getStreet())+"^"+val(patientData.getStreetTwo())+"^"
        +val(patientData.getCity())+"^"
        +val(patientData.getState())+"^"
        +val(patientData.getZipCode())+"||^NET^Internet^"+val(patientData.getEmail())+"~^1^"
        +(patientData.getTelephone()==null?"":patientData.getTelephone().substring(1,4))+"^"
        +(patientData.getTelephone()==null?"":patientData.getTelephone().substring(6))
        +"|||||||||"
        +ETHNICITY_CODE_MAP.get(val(patientData.getEthnicity(),"unknown"))+"^"
        +ETHNICITY_DESCRIPTION_MAP.get(val(patientData.getEthnicity(),"unknown"))+"^"
        +"HL70189||||||||N";
    String orc =
        "ORC|RE|"+randInt+"|"+randInt+"^^"+abridgedTestId+"^"+"UUID|||||||||"
        +providerData.getInternalId().toString().substring(0, 15)
        +"^"+val(providerData.getNameInfo().getLastName())+"^"+val(providerData.getNameInfo().getFirstName())
        +"^^"+val(providerData.getNameInfo().getSuffix())+"^^^^CMS&2.16.840.1.113883.3.249&ISO^^^^NPI||"
        +"^WPN^PH^^1^"
        +(providerData.getTelephone()==null?"":providerData.getTelephone().substring(1,4))
        +"^"+(providerData.getTelephone()==null?"":providerData.getTelephone().substring(6))
        +"|"+DATETIME_FORMAT.format(getDateTested())+"||||||"
        +val(getFacility().getFacilityName())+"|"+val(getFacility().getAddress().getStreetOne())
        +"^"+val(getFacility().getAddress().getStreetTwo())+"^"
        +val(getFacility().getAddress().getCity())+"^"
        +val(getFacility().getAddress().getState())+"^"
        +val(getFacility().getAddress().getPostalCode())
        +"|^WPN^PH^^1^"
        +(getFacility().getTelephone()==null?"":getFacility().getTelephone().substring(1,4))
        +"^"+(getFacility().getTelephone()==null?"":getFacility().getTelephone().substring(6))
        +"|"+val(providerData.getAddress().getStreetOne())
        +"^"+val(providerData.getAddress().getStreetTwo())+"^"
        +val(providerData.getAddress().getCity())+"^"
        +val(providerData.getAddress().getState())+"^"
        +val(providerData.getAddress().getPostalCode());
    String obr =
        "OBR|1|"+randInt+"|"+randInt+"|"+val(getDeviceType().getLoincCode())
        +"^SARS-CoV-2 (COVID-19) Ag [Presence] in Respiratory specimen by Rapid immunoassay"
        +"^LN|||"+DATETIME_FORMAT.format(getDateTested())+"|"+DATETIME_FORMAT.format(getDateTested())
        +"||||||||"+providerData.getInternalId().toString().substring(0, 15)
        +"^"+val(providerData.getNameInfo().getLastName())+"^"+val(providerData.getNameInfo().getFirstName())
        +"^^"+val(providerData.getNameInfo().getSuffix())+"^^^^CMS&2.16.840.1.113883.3.249&ISO^^^^NPI"
        +"|^WPN^PH^^1^"
        +(getFacility().getTelephone()==null?"":getFacility().getTelephone().substring(1,4))
        +"^"+(getFacility().getTelephone()==null?"":getFacility().getTelephone().substring(6))
        +"|||||"+DATETIME_FORMAT.format(getDateTested())+"|||F";
    String obx =
        "OBX|1|CWE|94558-4^SARS-CoV-2 (COVID-19) Ag [Presence] in Respiratory specimen by Rapid immunoassay^LN^^^^2.68||10828004^Positive^SCT||||||F|||202103290106-0400|78D2734280^CLIA||10811877011290_DIT^^99ELR^^^^2.68||202103240000-0500||||Any lab USA^^^^^CLIA&2.16.840.1.113883.19.4.6&ISO^XX^^^78D2734280^CLIA|2004 Ronald Parks^^Upper black eddy^PA^18972^^^^42017^MOBX|2|CWE|95418-0^Whether patient is employed in a healthcare setting^LN^^^^2.69||"+(patientData.getEmployedInHealthcare()?"Y":"N")+"^"+(patientData.getEmployedInHealthcare()?"Yes":"No")+"^HL70136||||||F|||202103290106-0400|78D2734280||||202103240000-0500||||Any lab USA^^^^^CLIA&2.16.840.1.113883.19.4.6&ISO^XX^^^78D2734280|2004 Ronald Parks^^Upper black eddy^PA^18972^^^^42017|||||QST^MOBX|3|CWE|95417-2^First test for condition of interest^LN^^^^2.69||UNK^Unknown^HL70136||||||F|||202103290106-0400|78D2734280||||202103240000-0500||||Any lab USA^^^^^CLIA&2.16.840.1.113883.19.4.6&ISO^XX^^^78D2734280|2004 Ronald Parks^^Upper black eddy^PA^18972^^^^42017|||||QST^MOBX|4|CWE|65222-2^Date and time of symptom onset^LN^^^^2.68||20210325||||||F|||202103290106-0400|78D2734280||||202103240000-0500||||Any lab USA^^^^^CLIA&2.16.840.1.113883.19.4.6&ISO^XX^^^78D2734280|2004 Ronald Parks^^Upper black eddy^PA^18972^^^^42017|||||QST^MOBX|5|CWE|95421-4^Resides in a congregate care setting^LN^^^^2.69||"+(patientData.getResidentCongregateSetting()?"Y":"N")+"^"+(patientData.getResidentCongregateSetting()?"Yes":"No")+"^HL70136||||||F|||202103290106-0400|78D2734280||||202103240000-0500||||Any lab USA^^^^^CLIA&2.16.840.1.113883.19.4.6&ISO^XX^^^78D2734280|2004 Ronald Parks^^Upper black eddy^PA^18972^^^^42017|||||QST^MOBX|6|CWE|95419-8^Has symptoms related to condition of interest^LN^^^^2.69||Y^Yes^HL70136||||||F|||202103290106-0400|78D2734280||||202103240000-0500||||Any lab USA^^^^^CLIA&2.16.840.1.113883.19.4.6&ISO^XX^^^78D2734280|2004 Ronald Parks^^Upper black eddy^PA^18972^^^^42017|||||QST^MSPM|1|574184&&78D2734280&CLIA^574184&&78D2734280&CLIA||119334006^Sputum specimen^SCT^^^^2.67||||71836000^Nasopharyngeal structure (body structure)^SCT^^^^2020-09-01|||||||||202103290106-0400|20210329010606.0000-0400";
    String gender_obx =
        String.join(HL7_SECTION_SEPARATOR,patientData.getGender().stream().map(g->"OBX|1|CWE|<NEW-LOINC-CODE-FOR-GENDER-IDENTITY:193723-5>^Gender Identity Revised^LN^^^^2.68||"+GENDER_NEW_SNOMED_CODE_MAP.getOrDefault(val(g, "unknown"), g)+"^"+GENDER_NEW_SNOMED_EXP_MAP.getOrDefault(val(g, "unknown"), g)+"^SCT||||||F|||202103290106-0400|78D2734280^CLIA||10811877011290_DIT^^99ELR^^^^2.68||202103240000-0500||||Any lab USA^^^^^CLIA&2.16.840.1.113883.19.4.6&ISO^XX^^^78D2734280^CLIA|2004 Ronald Parks^^Upper black eddy^PA^18972^^^^42017").collect(Collectors.toList()));
    String so_obx =
        String.join(HL7_SECTION_SEPARATOR,patientData.getSexualOrientation().stream().map(g->"OBX|1|CWE|76690-7^Sexual Orientation^LN^^^^2.68||"+SO_NEW_SNOMED_CODE_MAP.getOrDefault(val(g, "unknown"), g)+"^"+SO_NEW_SNOMED_EXP_MAP.getOrDefault(val(g, "unknown"), g)+"^SCT||||||F|||202103290106-0400|78D2734280^CLIA||10811877011290_DIT^^99ELR^^^^2.68||202103240000-0500||||Any lab USA^^^^^CLIA&2.16.840.1.113883.19.4.6&ISO^XX^^^78D2734280^CLIA|2004 Ronald Parks^^Upper black eddy^PA^18972^^^^42017").collect(Collectors.toList()));
    String sex_obx =
        "OBX|1|CWE|<NEW-LOINC-CODE-FOR-SEX-FOR-CLINICAL-USE:203231-9>^Sex For Clinical Use^LN^^^^2.68||"+SEX_NEW_SNOMED_CODE_MAP.getOrDefault(val(patientData.getGenderAssignedAtBirth(), "unknown"), patientData.getGenderAssignedAtBirth())+"^"+SEX_NEW_SNOMED_EXP_MAP.getOrDefault(val(patientData.getGenderAssignedAtBirth(), "unknown"), patientData.getGenderAssignedAtBirth())+"^SCT||||||F|||202103290106-0400|78D2734280^CLIA||10811877011290_DIT^^99ELR^^^^2.68||202103240000-0500||||Any lab USA^^^^^CLIA&2.16.840.1.113883.19.4.6&ISO^XX^^^78D2734280^CLIA|2004 Ronald Parks^^Upper black eddy^PA^18972^^^^42017";
    
    //^MOBX|2|CWE|95418-0^Whether patient is employed in a healthcare setting^LN^^^^2.69||N^No^HL70136||||||F|||202103290106-0400|78D2734280||||202103240000-0500||||Any lab USA^^^^^CLIA&2.16.840.1.113883.19.4.6&ISO^XX^^^78D2734280|2004 Ronald Parks^^Upper black eddy^PA^18972^^^^42017|||||QST^MOBX|3|CWE|95417-2^First test for condition of interest^LN^^^^2.69||UNK^Unknown^HL70136||||||F|||202103290106-0400|78D2734280||||202103240000-0500||||Any lab USA^^^^^CLIA&2.16.840.1.113883.19.4.6&ISO^XX^^^78D2734280|2004 Ronald Parks^^Upper black eddy^PA^18972^^^^42017|||||QST^MOBX|4|CWE|65222-2^Date and time of symptom onset^LN^^^^2.68||20210325||||||F|||202103290106-0400|78D2734280||||202103240000-0500||||Any lab USA^^^^^CLIA&2.16.840.1.113883.19.4.6&ISO^XX^^^78D2734280|2004 Ronald Parks^^Upper black eddy^PA^18972^^^^42017|||||QST^MOBX|5|CWE|95421-4^Resides in a congregate care setting^LN^^^^2.69||UNK^Unknown^HL70136||||||F|||202103290106-0400|78D2734280||||202103240000-0500||||Any lab USA^^^^^CLIA&2.16.840.1.113883.19.4.6&ISO^XX^^^78D2734280|2004 Ronald Parks^^Upper black eddy^PA^18972^^^^42017|||||QST^MOBX|6|CWE|95419-8^Has symptoms related to condition of interest^LN^^^^2.69||Y^Yes^HL70136||||||F|||202103290106-0400|78D2734280||||202103240000-0500||||Any lab USA^^^^^CLIA&2.16.840.1.113883.19.4.6&ISO^XX^^^78D2734280|2004 Ronald Parks^^Upper black eddy^PA^18972^^^^42017|||||QST^MSPM|1|574184&&78D2734280&CLIA^574184&&78D2734280&CLIA||119334006^Sputum specimen^SCT^^^^2.67||||71836000^Nasopharyngeal structure (body structure)^SCT^^^^2020-09-01|||||||||202103290106-0400|20210329010606.0000-0400
    return String.join(HL7_SECTION_SEPARATOR, List.of(fhs, mhs, sft, pid, orc, obr, obx, gender_obx, so_obx, sex_obx));
  }

}
