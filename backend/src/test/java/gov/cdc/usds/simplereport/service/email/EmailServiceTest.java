package gov.cdc.usds.simplereport.service.email;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.sendgrid.helpers.mail.Mail;
import gov.cdc.usds.simplereport.service.BaseServiceTest;
import gov.cdc.usds.simplereport.test_util.SliceTestConfiguration.WithSimpleReportSiteAdminUser;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.mock.mockito.MockBean;

class EmailServiceTest extends BaseServiceTest<EmailService> {
  @MockBean EmailProvider mockSendGrid;

  @Captor ArgumentCaptor<Mail> mail;

  @Test
  @WithSimpleReportSiteAdminUser
  void sendEmail() throws IOException {
    // GIVEN
    String toEmail = "test@foo.com";
    String subject = "Testing the email service";
    String message = "Here's a message for ya";

    // WHEN
    _service.send(toEmail, subject, message);

    // THEN
    verify(mockSendGrid, times(1)).send(mail.capture());
    assertEquals(mail.getValue().getPersonalization().get(0).getTos().get(0).getEmail(), toEmail);
    assertEquals(mail.getValue().getSubject(), subject);
    assertEquals(mail.getValue().getContent().get(0).getValue(), message);
  }

  @Test
  @WithSimpleReportSiteAdminUser
  void sendMultiRecipientEmail() throws IOException {
    // GIVEN
    List<String> tos =
        List.of(
            "test@foo.com",
            "another@foo.com",
            "apple@foo.com",
            "banana@foo.com",
            "onemore@foo.com");
    String subject = "Testing the email service";
    String message = "Here's a message for ya";

    // WHEN
    _service.send(tos, subject, message);

    // THEN
    verify(mockSendGrid, times(1)).send(mail.capture());
    for (int i = 0; i < tos.size(); i++) {
      assertEquals(
          mail.getValue().getPersonalization().get(0).getTos().get(i).getEmail(), tos.get(i));
    }
    assertEquals(mail.getValue().getSubject(), subject);
    assertEquals(mail.getValue().getContent().get(0).getValue(), message);
  }
}
