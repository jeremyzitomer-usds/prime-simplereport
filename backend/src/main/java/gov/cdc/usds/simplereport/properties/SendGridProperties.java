package gov.cdc.usds.simplereport.properties;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConfigurationProperties(prefix = "simple-report.sendgrid")
public final class SendGridProperties {

  private final boolean enabled;
  private final String apiKey;
  private final String fromEmail;
  private final List<String> accountRequestRecipient;
  private final List<String> waitlistRecipient;

  @ConstructorBinding
  public SendGridProperties(
      boolean enabled,
      String apiKey,
      String fromEmail,
      List<String> accountRequestRecipient,
      List<String> waitlistRecipient) {
    this.enabled = enabled;
    this.apiKey = apiKey;
    this.fromEmail = fromEmail;
    this.accountRequestRecipient = accountRequestRecipient;
    this.waitlistRecipient = waitlistRecipient;
  }

  public boolean getEnabled() {
    return enabled;
  }

  public String getApiKey() {
    return apiKey;
  }

  public String getFromEmail() {
    return fromEmail;
  }

  public List<String> getAccountRequestRecipient() {
    return accountRequestRecipient;
  }

  public List<String> getWaitlistRecipient() {
    return waitlistRecipient;
  }
}
