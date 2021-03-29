package gov.cdc.usds.simplereport.api;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import gov.cdc.usds.simplereport.api.model.errors.IllegalGraphqlArgumentException;
import gov.cdc.usds.simplereport.db.model.auxiliary.PersonRole;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

/**
 * Static package for utilities to translate things to or from wireline format in non copy-paste
 * ways.
 */
public class Translators {
  private static final DateTimeFormatter US_SLASHDATE_SHORT_FORMATTER =
      DateTimeFormatter.ofPattern("M/d/yyyy");
  private static final int MAX_STRING_LENGTH = 500;

  public static final LocalDate parseUserShortDate(String d) {
    String date = parseString(d);
    if (date == null) {
      return null;
    }
    try {
      return LocalDate.parse(date, US_SLASHDATE_SHORT_FORMATTER);
    } catch (DateTimeParseException e) {
      throw IllegalGraphqlArgumentException.invalidInput(d, "date");
    }
  }

  public static String parsePhoneNumber(String userSuppliedPhoneNumber) {
    if (userSuppliedPhoneNumber == null) {
      return null;
    }

    try {
      var phoneUtil = PhoneNumberUtil.getInstance();
      return phoneUtil.format(
          phoneUtil.parse(userSuppliedPhoneNumber, "US"),
          PhoneNumberUtil.PhoneNumberFormat.NATIONAL);
    } catch (NumberParseException parseException) {
      throw IllegalGraphqlArgumentException.invalidInput(userSuppliedPhoneNumber, "phone number");
    }
  }

  public static String parseString(String value) {
    if (value == null || "".equals(value)) {
      return null;
    }
    if (value.length() >= MAX_STRING_LENGTH) {
      throw new IllegalGraphqlArgumentException(
          "Value received exceeds field length limit of " + MAX_STRING_LENGTH + " characters");
    }
    return value.trim();
  }

  public static UUID parseUUID(String uuid) {
    if (uuid == null || "".equals(uuid)) {
      return null;
    }
    try {
      return UUID.fromString(uuid);
    } catch (IllegalArgumentException e) {
      throw IllegalGraphqlArgumentException.invalidInput(uuid, "UUID");
    }
  }

  public static PersonRole parsePersonRole(String r) {
    String role = parseString(r);
    if (role == null) {
      return PersonRole.UNKNOWN;
    }
    try {
      return PersonRole.valueOf(role.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw IllegalGraphqlArgumentException.invalidInput(r, "role");
    }
  }

  public static String parseEmail(String e) {
    String email = parseString(e);
    if (email == null) {
      return null;
    }
    if (email.contains("@")) {
      return email;
    }
    throw IllegalGraphqlArgumentException.invalidInput(e, "email");
  }

  private static final Map<String, String> RACES =
      Map.of(
          "american indian or alaskan native", "native",
          "asian", "asian",
          "black or african american", "black",
          "native hawaiian or other pacific islander", "pacific",
          "white", "white",
          "unknown", "unknown",
          "refused to answer", "refused");

  private static final Set<String> RACE_VALUES =
      RACES.values().stream().collect(Collectors.toSet());
  private static final Set<String> RACE_KEYS = RACES.keySet();

  public static String parseRace(String r) {
    String race = parseString(r);
    if (race == null) {
      return null;
    }
    race = race.toLowerCase();
    if (RACE_VALUES.contains(race)) {
      return race;
    }
    throw IllegalGraphqlArgumentException.mustBeEnumerated(r, RACE_VALUES);
  }

  public static String parseRaceDisplayValue(String r) {
    String race = parseString(r);
    if (race == null) {
      return null;
    }
    race = race.toLowerCase();
    if (RACES.containsKey(race)) {
      return RACES.get(race);
    }
    if (RACES.containsValue(race)) {
      return race; // passed in the correct value
    }
    // not found
    throw IllegalGraphqlArgumentException.mustBeEnumerated(r, RACE_KEYS);
  }

  private static final Set<String> ETHNICITIES = Set.of("hispanic", "not_hispanic");

  public static String parseEthnicity(String e) {
    String ethnicity = parseString(e);
    if (ethnicity == null) {
      return null;
    }
    ethnicity = ethnicity.toLowerCase();
    if (ETHNICITIES.contains(ethnicity)) {
      return ethnicity;
    }
    throw IllegalGraphqlArgumentException.mustBeEnumerated(e, ETHNICITIES);
  }

  private static final Set<String> GENDERS =
      Set.of("woman", "man", "nonbinary", "questioning", "not_disclosed");

  public static Set<String> parseGender(Collection<String> gs) {
    if (gs == null) {
      return null;
    }
    Set<String> genders = new HashSet<>();
    boolean containsCustom = false;
    for (String g : gs) {
      String gender = parseString(g);
      if (gender == null) {
        continue;
      }
      gender = gender.toLowerCase();
      if (!GENDERS.contains(gender)) {
        if (containsCustom) {
          throw new IllegalGraphqlArgumentException(
              "\""
                  + gs.toString()
                  + "\" must contain at most one item not listed in "
                  + "["
                  + String.join(", ", GENDERS)
                  + "].");
        }
        containsCustom = true;
      }
      genders.add(gender);
    }
    return genders;
  }

  private static final Set<String> GENDERS_ASSIGNED_AT_BIRTH =
      Set.of("female", "male", "x", "unsure", "not_assigned", "not_disclosed");

  public static String parseGenderAssignedAtBirth(String g) {
    String gender = parseString(g);
    if (gender == null) {
      return null;
    }
    gender = gender.toLowerCase();
    // Currently same logic is executed regardless of whether the string is in
    // the preset value set or not, but keeping the preset value set around
    // because it may be valuable in the future.
    if (GENDERS_ASSIGNED_AT_BIRTH.contains(gender)) {
      return gender;
    }
    return gender;
  }

  private static final Set<String> SEXUAL_ORIENTATIONS =
      Set.of(
          "asexual",
          "bisexual_or_pansexual",
          "heterosexual",
          "homosexual",
          "questioning",
          "not_disclosed");

  public static Set<String> parseSexualOrientation(Collection<String> os) {
    if (os == null) {
      return null;
    }
    Set<String> sexualOrientations = new HashSet<>();
    boolean containsCustom = false;
    for (String o : os) {
      String sexualOrientation = parseString(o);
      if (sexualOrientation == null) {
        continue;
      }
      sexualOrientation = sexualOrientation.toLowerCase();
      if (!SEXUAL_ORIENTATIONS.contains(sexualOrientation)) {
        if (containsCustom) {
          throw new IllegalGraphqlArgumentException(
              "\""
                  + os.toString()
                  + "\" must contain at most one item not listed in "
                  + "["
                  + String.join(", ", SEXUAL_ORIENTATIONS)
                  + "].");
        }
        containsCustom = true;
      }
      sexualOrientations.add(sexualOrientation);
    }
    return sexualOrientations;
  }

  private static final Map<String, Boolean> YES_NO =
      Map.of("y", true, "yes", true, "n", false, "no", false, "true", true, "false", false);

  public static Boolean parseYesNo(String v) {
    String stringValue = parseString(v);
    if (stringValue == null) {
      return null;
    }
    Boolean boolValue = YES_NO.get(stringValue.toLowerCase());
    if (boolValue == null) {
      throw IllegalGraphqlArgumentException.invalidInput(v, "value");
    }
    return boolValue;
  }

  private static final Set<String> STATE_CODES =
      Set.of(
          "AK", "AL", "AR", "AS", "AZ", "CA", "CO", "CT", "DC", "DE", "FL", "FM", "GA", "GU", "HI",
          "IA", "ID", "IL", "IN", "KS", "KY", "LA", "MA", "MD", "ME", "MH", "MI", "MN", "MO", "MP",
          "MS", "MT", "NC", "ND", "NE", "NH", "NJ", "NM", "NV", "NY", "OH", "OK", "OR", "PA", "PR",
          "PW", "RI", "SC", "SD", "TN", "TX", "UT", "VA", "VI", "VT", "WA", "WI", "WV", "WY");

  public static String parseState(String s) {
    String state = parseString(s);
    if (state == null) {
      return null;
    }
    state = state.toUpperCase();
    if (STATE_CODES.contains(state)) {
      return state;
    }
    throw IllegalGraphqlArgumentException.invalidInput(s, "state");
  }

  public static Map<String, Boolean> parseSymptoms(String symptoms) {
    Map<String, Boolean> symptomsMap = new HashMap<String, Boolean>();
    JSONObject symptomsJSONObject = new JSONObject(symptoms);
    Iterator<?> keys = symptomsJSONObject.keys();
    while (keys.hasNext()) {
      String key = (String) keys.next();
      Boolean value = symptomsJSONObject.getBoolean(key);
      symptomsMap.put(key, value);
    }
    return symptomsMap;
  }

  public static String sanitize(String input) {
    return input == null ? "" : Jsoup.clean(input, Whitelist.basic());
  }
}
