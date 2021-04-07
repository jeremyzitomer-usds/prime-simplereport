type Race =
  | "native"
  | "asian"
  | "black"
  | "pacific"
  | "white"
  | "unknown"
  | "refused";
type Ethnicity = "hispanic" | "not_hispanic";
type Gender = string[]; //["female" | "male" | "nonbinary" | "questioning" | "notlisted" | "notdisclosed"]
type GenderAssignedAtBirth = "female" | "male" | "x" | "unsure" | "none" | "notlisted" | "notdisclosed";
type SexualOrientation = string[]; //"heterosexual" | "homosexual" | "bipan" | "asexual" | "questioning" | "notlisted" |"notdisclosed";
type YesNo = "YES" | "NO";
type Role = "STAFF" | "RESIDENT" | "STUDENT" | "VISITOR" | "";

interface Person extends Address {
  firstName: string;
  middleName: string;
  lastName: string;
  lookupId: string;
  role: Role;
  race: Race;
  ethnicity: Ethnicity;
  gender: Gender;
  genderAssignedAtBirth: GenderAssignedAtBirth;
  sexualOrientation: SexualOrientation;
  residentCongregateSetting: boolean;
  employedInHealthcare: boolean;
  birthDate: string;
  telephone: string;
  county: string;
  email: string;
}

interface PersonFormData extends Person {
  facilityId: string | null;
}
