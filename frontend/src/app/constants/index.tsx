import { TestResult } from "../testQueue/QueueItem";

export const COVID_RESULTS: { [key: string]: TestResult } = {
  POSITIVE: "POSITIVE",
  NEGATIVE: "NEGATIVE",
  INCONCLUSIVE: "UNDETERMINED",
};

export const TEST_RESULT_VALUES = {
  0: COVID_RESULTS.NEGATIVE,
  1: COVID_RESULTS.POSITIVE,
  2: COVID_RESULTS.INCONCLUSIVE,
};

export const TEST_RESULT_DESCRIPTIONS = {
  NEGATIVE: "Negative",
  POSITIVE: "Positive",
  UNDETERMINED: "Inconclusive",
};

export const RACE_VALUES: { value: Race; label: string }[] = [
  {
    value: "native",
    label: "American Indian or Alaskan Native",
  },
  {
    value: "asian",
    label: "Asian",
  },
  {
    value: "black",
    label: "Black or African American",
  },
  {
    value: "pacific",
    label: "Native Hawaiian or other Pacific Islander",
  },
  {
    value: "white",
    label: "White",
  },
  {
    value: "unknown",
    label: "Unknown",
  },
  {
    value: "refused",
    label: "Refused to answer",
  },
];

export const ROLE_VALUES: { value: Role; label: string }[] = [
  { label: "Staff", value: "STAFF" },
  { label: "Resident", value: "RESIDENT" },
  { label: "Student", value: "STUDENT" },
  { label: "Visitor", value: "VISITOR" },
];

export const ETHNICITY_VALUES: { value: Ethnicity; label: string }[] = [
  { label: "Hispanic or Latino", value: "hispanic" },
  { label: "Not Hispanic", value: "not_hispanic" },
];

export const GENDER: { value: string; label: string; checked?: boolean }[] = [
  { label: "Female, Woman, or Girl", value: "woman" },
  { label: "Male, Man, or Boy", value: "man" },
  { label: "Nonbinary", value: "nonbinary" },
  { label: "Questioning", value: "questioning" },
  { label: "Prefer not to disclose", value: "not_disclosed" },
  { label: "A gender identity not listed (please specify):", value: "notlisted" },
]

export const GENDER_ASSIGNED_AT_BIRTH: { value: GenderAssignedAtBirth; label: string }[] = [
  { label: "Female ('F')", value: "female" },
  { label: "Male ('M')", value: "male" },
  { label: "X ('X')", value: "x" },
  { label: "Unsure", value: "unsure" },
  { label: "I was not assigned a gender at birth", value: "none" },
  { label: "Prefer not to disclose", value: "not_disclosed" },
  { label: "A gender assigned at birth not listed (please specify):", value: "notlisted" },
];

export const SEXUAL_ORIENTATION: { value: string; label: string; checked?: boolean }[] = [
  { label: "Heterosexual (Straight, Not Lesbian or Gay)", value: "heterosexual" },
  { label: "Homosexual (Gay or Lesbian)", value: "homosexual" },
  { label: "Bisexual or Pansexual", value: "bisexual_or_pansexual" },
  { label: "Asexual or Asexual Spectrum", value: "asexual" },
  { label: "Questioning", value: "questioning" },
  { label: "Prefer not to disclose", value: "not_disclosed" },
  { label: "A sexual orientation not listed (please specify):", value: "notlisted" },
]

export const YES_NO_VALUES: { value: YesNo; label: string }[] = [
  { label: "Yes", value: "YES" },
  { label: "No", value: "NO" },
];
