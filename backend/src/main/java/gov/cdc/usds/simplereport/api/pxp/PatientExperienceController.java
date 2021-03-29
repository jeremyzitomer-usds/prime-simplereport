package gov.cdc.usds.simplereport.api.pxp;

import static gov.cdc.usds.simplereport.api.Translators.parseEmail;
import static gov.cdc.usds.simplereport.api.Translators.parseEthnicity;
import static gov.cdc.usds.simplereport.api.Translators.parseGender;
import static gov.cdc.usds.simplereport.api.Translators.parseGenderAssignedAtBirth;
import static gov.cdc.usds.simplereport.api.Translators.parsePhoneNumber;
import static gov.cdc.usds.simplereport.api.Translators.parseRace;
import static gov.cdc.usds.simplereport.api.Translators.parseSexualOrientation;
import static gov.cdc.usds.simplereport.api.Translators.parseString;
import static gov.cdc.usds.simplereport.api.Translators.parseSymptoms;

import gov.cdc.usds.simplereport.api.model.AoEQuestions;
import gov.cdc.usds.simplereport.api.model.pxp.PxpRequestWrapper;
import gov.cdc.usds.simplereport.api.model.pxp.PxpVerifyResponse;
import gov.cdc.usds.simplereport.db.model.PatientLink;
import gov.cdc.usds.simplereport.db.model.Person;
import gov.cdc.usds.simplereport.db.model.TestEvent;
import gov.cdc.usds.simplereport.db.model.auxiliary.OrderStatus;
import gov.cdc.usds.simplereport.db.model.auxiliary.StreetAddress;
import gov.cdc.usds.simplereport.db.model.auxiliary.TestResult;
import gov.cdc.usds.simplereport.service.PatientLinkService;
import gov.cdc.usds.simplereport.service.PersonService;
import gov.cdc.usds.simplereport.service.TestEventService;
import gov.cdc.usds.simplereport.service.TestOrderService;
import gov.cdc.usds.simplereport.service.TimeOfConsentService;
import java.util.Map;
import java.util.UUID;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Note that this controller self-authorizes by means of this {@link PreAuthorize} annotation and
 * its routes are set to permitAll() in SecurityConfiguration. If this changes, please update the
 * documentation on both sides.
 *
 * <p>Similarly, this controller sends information to the audit log via a {@link PostAuthorize}.
 * Because of this, every handler method of this controller is required to have an {@link
 * HttpServletRequest} argument named {@code request}.
 */
@PreAuthorize(
    "@patientLinkService.verifyPatientLink(#body.getPatientLinkId(), #body.getDateOfBirth())")
@PostAuthorize("@restAuditLogManager.logRestSuccess(#request, returnObject)")
@RestController
@RequestMapping("/pxp")
@Validated
public class PatientExperienceController {
  private static final Logger LOG = LoggerFactory.getLogger(PatientExperienceController.class);

  @Autowired private PersonService ps;

  @Autowired private PatientLinkService pls;

  @Autowired private TestOrderService tos;

  @Autowired private TestEventService tes;

  @Autowired private TimeOfConsentService tocs;

  @PostConstruct
  private void init() {
    LOG.info("Patient Experience REST endpoints enabled");
  }

  /**
   * Verify that the patient-provided DOB matches the patient on file for the patient link id. It
   * returns the full patient object if so, otherwise it throws an exception
   */
  @PutMapping("/link/verify")
  public PxpVerifyResponse getPatientLinkVerify(
      @RequestBody PxpRequestWrapper<Void> body, HttpServletRequest request) {
    UUID plid = UUID.fromString(body.getPatientLinkId());
    PatientLink pl = pls.getPatientLink(plid);
    OrderStatus os = pl.getTestOrder().getOrderStatus();
    Person p = pls.getPatientFromLink(plid);
    TestEvent te = tes.getLastTestResultsForPatient(p);
    tocs.storeTimeOfConsent(pl);

    return new PxpVerifyResponse(p, os, te);
  }

  @PutMapping("/patient")
  public Person updatePatient(
      @RequestBody PxpRequestWrapper<Person> body, HttpServletRequest request) {
    Person person = body.getData();
    return ps.updateMe(
        parseString(person.getFirstName()),
        parseString(person.getMiddleName()),
        parseString(person.getLastName()),
        parseString(person.getSuffix()),
        person.getBirthDate(),
        StreetAddress.deAndReSerializeForSafety(person.getAddress()),
        parsePhoneNumber(person.getTelephone()),
        person.getRole(),
        parseEmail(person.getEmail()),
        parseRace(person.getRace()),
        parseEthnicity(person.getEthnicity()),
        parseGender(person.getGender()),
        parseGenderAssignedAtBirth(person.getGenderAssignedAtBirth()),
        parseSexualOrientation(person.getSexualOrientation()),
        person.getResidentCongregateSetting(),
        person.getEmployedInHealthcare());
  }

  @PutMapping("/questions")
  public void patientLinkSubmit(
      @RequestBody PxpRequestWrapper<AoEQuestions> body, HttpServletRequest request) {
    AoEQuestions data = body.getData();
    Map<String, Boolean> symptomsMap = parseSymptoms(data.getSymptoms());

    tos.updateMyTimeOfTestQuestions(
        data.getPregnancy(),
        symptomsMap,
        data.isFirstTest(),
        data.getPriorTestDate(),
        data.getPriorTestType(),
        data.getPriorTestResult() == null ? null : TestResult.valueOf(data.getPriorTestResult()),
        data.getSymptomOnset(),
        data.getNoSymptoms());

    ps.updateMyTestResultDeliveryPreference(data.getTestResultDelivery());
    pls.expireMyPatientLink();
  }
}
