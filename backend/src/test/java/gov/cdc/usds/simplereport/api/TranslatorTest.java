package gov.cdc.usds.simplereport.api;

import static gov.cdc.usds.simplereport.api.Translators.parseEmail;
import static gov.cdc.usds.simplereport.api.Translators.parseEthnicity;
import static gov.cdc.usds.simplereport.api.Translators.parseGender;
import static gov.cdc.usds.simplereport.api.Translators.parseGenderAssignedAtBirth;
import static gov.cdc.usds.simplereport.api.Translators.parseSexualOrientation;
import static gov.cdc.usds.simplereport.api.Translators.parseRace;
import static gov.cdc.usds.simplereport.api.Translators.parseRaceDisplayValue;
import static gov.cdc.usds.simplereport.api.Translators.parseState;
import static gov.cdc.usds.simplereport.api.Translators.parseString;
import static gov.cdc.usds.simplereport.api.Translators.parseUUID;
import static gov.cdc.usds.simplereport.api.Translators.parseUserShortDate;
import static gov.cdc.usds.simplereport.api.Translators.parseYesNo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import gov.cdc.usds.simplereport.api.model.errors.IllegalGraphqlArgumentException;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

class TranslatorTest {
  @Test
  void testEmptyShortDate() {
    assertNull(parseUserShortDate(""));
  }

  @Test
  void testNullShortDate() {
    assertNull(parseUserShortDate(null));
  }

  @Test
  void testValidShortDate() {
    LocalDate result = parseUserShortDate("2/1/2021");
    assertEquals(2, result.getMonthValue());
    assertEquals(1, result.getDayOfMonth());
    assertEquals(2021, result.getYear());
  }

  @Test
  void testValidDateWithLeadingZeros() {
    LocalDate result = parseUserShortDate("02/01/2021");
    assertEquals(2, result.getMonthValue());
    assertEquals(1, result.getDayOfMonth());
    assertEquals(2021, result.getYear());
  }

  @Test
  void testInvalidShortDate() {
    IllegalGraphqlArgumentException caught =
        assertThrows(
            IllegalGraphqlArgumentException.class,
            () -> {
              parseUserShortDate("fooexample.com");
            });
    assertEquals("[fooexample.com] is not a valid date", caught.getMessage());
  }

  @Test
  void testEmptyParseString() {
    assertNull(parseString(""));
  }

  @Test
  void testNullParseString() {
    assertNull(parseString(null));
  }

  @Test
  void testValidParseString() {
    assertEquals("abc 123", parseString("abc 123"));
  }

  @Test
  void testValidParseStringWithSurroundingSpaces() {
    assertEquals("abc 123", parseString("   abc 123   "));
  }

  @Test
  void testParseStringWithLongString() {
    IllegalGraphqlArgumentException caught =
        assertThrows(
            IllegalGraphqlArgumentException.class,
            () -> {
              parseString(
                  "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Proin vestibulum lacus vitae condimentum ultricies. Phasellus sed velit a urna aliquam tempus. Nulla nunc ex, porta eget tristique vel, cursus eu enim. Sed malesuada turpis at rhoncus aliquam. Nullam blandit turpis ac pharetra lobortis. Ut bibendum ligula ex. Curabitur fermentum condimentum erat, in tristique justo maximus eu. Fusce posuere cursus enim, a ullamcorper augue bibendum eget. In eu nunc vitae est molestie mollis. Sed mollis fermentum ante vel bibendum. Fusce vel elit risus.");
            });
    assertEquals(
        "Value received exceeds field length limit of 500 characters", caught.getMessage());
  }

  @Test
  void testEmptyUUID() {
    assertNull(parseUUID(""));
  }

  @Test
  void testNullParseUUID() {
    assertNull(parseUUID(null));
  }

  @Test
  void testValidParseUUID() {
    assertEquals(
        "8ae1a210-fe20-44ab-80c6-214289acead7",
        parseUUID("8ae1a210-fe20-44ab-80c6-214289acead7").toString());
  }

  @Test
  void testInvalidParseUUID() {
    assertThrows(
        IllegalGraphqlArgumentException.class,
        () -> {
          parseUUID("abc 123");
        });
  }

  @Test
  void testEmptyParseRace() {
    assertNull(parseRace(""));
  }

  @Test
  void testNullParseRace() {
    assertNull(parseRace(null));
  }

  @Test
  void testValidParseRace() {
    assertEquals("native", parseRace("native"));
  }

  @Test
  void testInvalidParseRace() {
    assertThrows(
        IllegalGraphqlArgumentException.class,
        () -> {
          parseRace("xyz");
        });
  }

  @Test
  void testEmptyParseRaceDisplayValue() {
    assertNull(parseRaceDisplayValue(""));
  }

  @Test
  void testNullParseRaceDisplayValue() {
    assertNull(parseRaceDisplayValue(null));
  }

  @Test
  void testValidParseRaceDisplayValue() {
    assertEquals("black", parseRaceDisplayValue("Black or African American"));
  }

  @Test
  void testInvalidParseRaceDisplayValue() {
    assertThrows(
        IllegalGraphqlArgumentException.class,
        () -> {
          parseRaceDisplayValue("456");
        });
  }

  @Test
  void testEmptyParseEthnicity() {
    assertNull(parseEthnicity(""));
  }

  @Test
  void testNullParseEthnicity() {
    assertNull(parseEthnicity(null));
  }

  @Test
  void testValidParseEthnicity() {
    assertEquals("hispanic", parseEthnicity("hispanic"));
  }

  @Test
  void testInvalidParseEthnicity() {
    assertThrows(
        IllegalGraphqlArgumentException.class,
        () -> {
          parseEthnicity("xyz");
        });
  }

  @Test
  void testEmptyParseGender() {
    assertEquals(parseGender(List.of("")), Set.of());
  }

  @Test
  void testNullParseGender() {
    assertNull(parseGender(null));
  }

  @Test
  void testValidParseGender() {
    assertEquals(Set.of("nonbinary"), parseGender(List.of("nonbinary")));
    assertEquals(
        Set.of("nonbinary", "genderqueer"), 
        parseGender(List.of("nonbinary", "genderqueer")));
    assertEquals(
        Set.of("nonbinary", "woman"), 
        parseGender(List.of("nonbinary", "woman")));
  }

  @Test
  void testInvalidParseGender() {
    assertThrows(
        IllegalGraphqlArgumentException.class,
        () -> {
          parseGender(List.of("value1", "value2"));
        });
  }

  @Test
  void testEmptyParseSexualOrientation() {
    assertEquals(parseSexualOrientation(List.of("")), Set.of());
  }

  @Test
  void testNullParseSexualOrientation() {
    assertNull(parseSexualOrientation(null));
  }

  @Test
  void testValidParseSexualOrientation() {
    assertEquals(Set.of("asexual"), parseSexualOrientation(List.of("asexual")));
    assertEquals(
        Set.of("asexual", "aromantic"), 
        parseSexualOrientation(List.of("asexual", "aromantic")));
    assertEquals(
        Set.of("asexual", "homosexual"), 
        parseSexualOrientation(List.of("asexual", "homosexual")));
  }

  @Test
  void testInvalidParseSexualOrientation() {
    assertThrows(
        IllegalGraphqlArgumentException.class,
        () -> {
          parseSexualOrientation(List.of("value1", "value2"));
        });
  }

  @Test
  void testEmptyParseYesNo() {
    assertNull(parseYesNo(""));
  }

  @Test
  void testNullParseYesNo() {
    assertNull(parseYesNo(null));
  }

  @Test
  void testValidParseYesNo() {
    assertEquals(true, parseYesNo("y"));
    assertEquals(true, parseYesNo("yEs"));
    assertEquals(false, parseYesNo("n"));
    assertEquals(false, parseYesNo("nO"));
  }

  @Test
  void testInvalidParseYesNo() {
    assertThrows(
        IllegalGraphqlArgumentException.class,
        () -> {
          parseYesNo("positive");
        });
  }

  @Test
  void testEmptyState() {
    assertNull(parseState(""));
  }

  @Test
  void testNullState() {
    assertNull(parseState(null));
  }

  @Test
  void testValidState() {
    assertEquals("NY", parseState("ny"));
  }

  @Test
  void testInvalidState() {
    assertThrows(
        IllegalGraphqlArgumentException.class,
        () -> {
          parseState("New York");
        });
  }

  @Test
  void testEmptyEmail() {
    assertNull(parseEmail(""));
  }

  @Test
  void testNullEmail() {
    assertNull(parseEmail(null));
  }

  @Test
  void testValidEmail() {
    assertEquals("foo@example.com", parseEmail("foo@example.com"));
  }

  @Test
  void testInvalidEmail() {
    assertThrows(
        IllegalGraphqlArgumentException.class,
        () -> {
          parseEmail("fooexample.com");
        });
  }
}
