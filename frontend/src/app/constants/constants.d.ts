type Race =
  | "native"
  | "asian"
  | "black"
  | "pacific"
  | "white"
  | "unknown"
  | "refused";
type Ethnicity = "hispanic" | "not_hispanic";
type Gender = string[]; //["female" | "male" | "nonbinary" | "questioning" | "notlisted" | "not_disclosed"]
type GenderAssignedAtBirth = "female" | "male" | "x" | "unsure" | "none" | "notlisted" | "not_disclosed";
type SexualOrientation = string[]; //"heterosexual" | "homosexual" | "bisexual_or_pansexual" | "asexual" | "questioning" | "notlisted" |"not_disclosed";
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
