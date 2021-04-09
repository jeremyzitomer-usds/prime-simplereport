import * as yup from "yup";
import { PhoneNumberUtil } from "google-libphonenumber";

import {
  RACE_VALUES,
  ROLE_VALUES,
  ETHNICITY_VALUES,
  GENDER,
  GENDER_ASSIGNED_AT_BIRTH,
  SEXUAL_ORIENTATION,
} from "../constants";
import { Option } from "../commonComponents/Dropdown";

const phoneUtil = PhoneNumberUtil.getInstance();

type PartialBy<T, K extends keyof T> = Omit<T, K> & Partial<Pick<T, K>>;

type OptionalFields =
  | "middleName"
  | "lookupId"
  | "role"
  | "email"
  | "streetTwo"
  | "city"
  | "county"
  | "race"
  | "ethnicity"
  | "gender"
  | "genderAssignedAtBirth"
  | "sexualOrientation"
  | "residentCongregateSetting"
  | "employedInHealthcare";

export type RequiredPersonFields = PartialBy<
  Nullable<PersonFormData>,
  OptionalFields
>;

const getValues = (options: Option[]) => options.map(({ value }) => value);

export const personSchema: yup.SchemaOf<RequiredPersonFields> = yup.object({
  firstName: yup.string().required(),
  middleName: yup.string().nullable(),
  lastName: yup.string().required(),
  lookupId: yup.string().nullable(),
  role: yup.mixed().oneOf([...getValues(ROLE_VALUES), "UNKNOWN", "", null]),
  birthDate: yup.string().required(),
  facilityId: yup.string().nullable().min(1) as any,
  telephone: yup
    .string()
    .test(function (input) {
      if (!input) {
        return false;
      }
      const number = phoneUtil.parseAndKeepRawInput(input, "US");
      return phoneUtil.isValidNumber(number);
    })
    .required(),
  email: yup.string().email().nullable(),
  street: yup.string().required(),
  streetTwo: yup.string().nullable(),
  city: yup.string().nullable(),
  county: yup.string().nullable(),
  state: yup.string().required(),
  zipCode: yup.string().required(),
  race: yup.mixed().oneOf([...getValues(RACE_VALUES), "", null]),
  ethnicity: yup.mixed().oneOf([...getValues(ETHNICITY_VALUES), "", null]),
  gender: yup.array(),
  genderAssignedAtBirth: yup.mixed().oneOf([...getValues(GENDER_ASSIGNED_AT_BIRTH), "", null]),
  sexualOrientation: yup.array(),
  residentCongregateSetting: yup.bool().required(),
  employedInHealthcare: yup.bool().required(),
});

export type PersonErrors = Partial<Record<keyof PersonFormData, string>>;

export const allPersonErrors: Required<PersonErrors> = {
  firstName: "First name is required",
  middleName: "Middle name is incorrectly formatted",
  lastName: "Last name is required",
  lookupId: "Lookup ID is incorrectly formatted",
  role: "Role is incorrectly formatted",
  facilityId: "Facility is required",
  birthDate: "Date of birth is missing or incorrectly formatted",
  telephone: "Phone number is missing or invalid",
  email: "Email is missing or incorrectly formatted",
  street: "Street is missing",
  streetTwo: "Street Two is incorrectly formatted",
  zipCode: "Zip code is missing or incorrectly formatted",
  state: "State is missing or incorrectly formatted",
  city: "City is incorrectly formatted",
  county: "County is incorrectly formatted",
  race: "Race is incorrectly formatted",
  ethnicity: "Ethnicity is incorrectly formatted",
  gender: "Gender Identity correctly formatted",
  genderAssignedAtBirth: "Gender Assigned at Birth is incorrectly formatted",
  sexualOrientation: "Sexual Orientation is incorrectly formatted",
  residentCongregateSetting:
    "Resident in congregate care/living setting? is required",
  employedInHealthcare: "Work in Healthcare? is required",
};
