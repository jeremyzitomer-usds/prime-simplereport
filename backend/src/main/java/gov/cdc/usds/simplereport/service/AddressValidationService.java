package gov.cdc.usds.simplereport.service;

import static gov.cdc.usds.simplereport.api.Translators.parseState;
import static gov.cdc.usds.simplereport.api.Translators.parseString;

import com.smartystreets.api.ClientBuilder;
import com.smartystreets.api.exceptions.SmartyException;
import com.smartystreets.api.us_street.Candidate;
import com.smartystreets.api.us_street.Client;
import com.smartystreets.api.us_street.Lookup;
import com.smartystreets.api.us_street.MatchType;
import gov.cdc.usds.simplereport.api.model.errors.IllegalGraphqlArgumentException;
import gov.cdc.usds.simplereport.db.model.auxiliary.StreetAddress;
import gov.cdc.usds.simplereport.properties.SmartyStreetsProperties;
import java.io.IOException;
import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AddressValidationService {
  public final String FACILITY_DISPLAY_NAME = "facility";
  public final String PROVIDER_DISPLAY_NAME = "Ordering Provider";
  private Client _client;

  public AddressValidationService(Client client) {
    _client = client;
  }

  @Autowired
  public AddressValidationService(SmartyStreetsProperties config) {
    _client = new ClientBuilder(config.getId(), config.getToken()).buildUsStreetApiClient();
  }

  private Lookup getStrictLookup(
      String street1, String street2, String city, String state, String postalCode) {
    Lookup lookup = new Lookup();
    lookup.setStreet(parseString(street1));
    // Smartystreets defines Street2 as "Any extra address information (e.g., Leave it on the front
    // porch.)"
    // and secondary as "Apartment, suite, or office number (e.g., "Apt 52" or simply "52"; not
    // "Apt52".)""
    lookup.setSecondary(parseString(street2));
    lookup.setCity(parseString(city));
    lookup.setState(parseState(state));
    lookup.setZipCode(parseString(postalCode));
    lookup.setMatch(MatchType.STRICT);
    return lookup;
  }

  public StreetAddress getValidatedAddress(Lookup lookup, String fieldName) {
    try {
      _client.send(lookup);
    } catch (SmartyException | IOException ex) {
      throw new IllegalGraphqlArgumentException(
          "The server is unable to verify the address you entered. Please try again later");
    }

    ArrayList<Candidate> results = lookup.getResult();

    if (results.isEmpty()) {
      String errorMessage =
          fieldName != null
              ? "The " + fieldName + " address could not be verified"
              : "The address you entered could not be verified";
      throw new IllegalGraphqlArgumentException(errorMessage);
    }

    Candidate addressMatch = results.get(0);
    return new StreetAddress(
        lookup.getStreet(),
        lookup.getSecondary(),
        lookup.getCity(),
        lookup.getState(),
        lookup.getZipCode(),
        addressMatch.getMetadata().getCountyName());
  }

  /** Returns a StreetAddress if the address is valid and throws an exception if it is not */
  public StreetAddress getValidatedAddress(
      String street1,
      String street2,
      String city,
      String state,
      String postalCode,
      String fieldName) {
    Lookup lookup = getStrictLookup(street1, street2, city, state, postalCode);
    return getValidatedAddress(lookup, fieldName);
  }
}
