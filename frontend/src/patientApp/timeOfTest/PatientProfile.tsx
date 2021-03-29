import moment from "moment";
import { Redirect } from "react-router-dom";

import { formatFullName } from "../../app/utils/user";
import { formatAddress } from "../../app/utils/address";
import {
  RACE_VALUES,
  ETHNICITY_VALUES,
  GENDER,
  GENDER_ASSIGNED_AT_BIRTH,
  SEXUAL_ORIENTATION,
} from "../../app/constants";
import { getPatientLinkIdFromUrl } from "../../app/utils/url";

interface Props {
  patient: any;
}

const PatientProfile = ({ patient }: Props) => {
  if (!patient) {
    return <Redirect to={`/?plid=${getPatientLinkIdFromUrl()}`} />;
  }
  const fullName = formatFullName(patient);
  const race = RACE_VALUES.find((val) => val.value === patient.race)?.label;
  const ethnicity = ETHNICITY_VALUES.find(
    (val) => val.value === patient.ethnicity
  )?.label;
  const gender = GENDER.find((val) => val.value === patient.gender)
    ?.label;
  const genderAssignedAtBirth = GENDER_ASSIGNED_AT_BIRTH.find((val) => val.value === patient.genderAssignedAtBirth)
    ?.label;
  const sexualOrientation = SEXUAL_ORIENTATION.find((val) => val.value === patient.sexualOrientation)
    ?.label;

  const newLineSpan = ({ text = "" }) => {
    let key = 1;
    return text.split("\n").map((str) => (
      <span className="display-block" key={`newLineSpan${++key}`}>
        {str}
      </span>
    ));
  };

  const address = formatAddress({
    street: patient.street,
    streetTwo: patient.streetTwo,
    city: patient.city,
    state: patient.state,
    zipCode: patient.zipCode,
  });
  const notProvided = "Not provided";

  return (
    <div className="prime-formgroup usa-prose">
      <h2 className="prime-formgroup-heading font-heading-lg">
        General information
      </h2>
      <h3 className="font-heading-sm">Name</h3>
      <p>{fullName}</p>
      <h3 className="font-heading-sm">Date of birth</h3>
      <p>
        {patient.birthDate
          ? moment(patient.birthDate).format("MM/DD/yyyy")
          : notProvided}
      </p>
      <h3 className="font-heading-sm">Phone number</h3>
      <p>{patient.telephone || notProvided}</p>
      {/* <h3 className="font-heading-sm">Phone type</h3>
      <p></p> */}
      <h3 className="font-heading-sm">Address</h3>
      <p>{address ? newLineSpan({ text: address }) : notProvided}</p>
      <h3 className="font-heading-sm">Email address</h3>
      <p id="patient-email">{patient.email || notProvided}</p>
      <h2 className="prime-formgroup-heading font-heading-lg">Demographics</h2>
      <h3 className="font-heading-sm">Race</h3>
      <p>{race || notProvided}</p>
      {/* <h3 className="font-heading-sm">Tribal affiliation</h3>
      <p></p> */}
      <h3 className="font-heading-sm">Ethnicity</h3>
      <p>{ethnicity || notProvided}</p>
      <h3 className="font-heading-sm">Gender identity</h3>
      <p>{gender || notProvided}</p>
      <h3 className="font-heading-sm">Biological sex</h3>
      <p>{genderAssignedAtBirth || notProvided}</p>
      <h3 className="font-heading-sm">Sexual orientation</h3>
      <p>{sexualOrientation || notProvided}</p>
      <h2 className="prime-formgroup-heading font-heading-lg">Other</h2>
      <h3 className="font-heading-sm">
        Resident in congregate care/living setting
      </h3>
      <p>
        {patient.residentCongregateSetting === true
          ? "Yes"
          : patient.residentCongregateSetting === false
          ? "No"
          : notProvided}
      </p>
      <h3 className="font-heading-sm">Employed in healthcare</h3>
      <p>
        {patient.employedInHealthcare === true
          ? "Yes"
          : patient.employedInHealthcare === false
          ? "No"
          : notProvided}
      </p>
    </div>
  );
};

export default PatientProfile;
