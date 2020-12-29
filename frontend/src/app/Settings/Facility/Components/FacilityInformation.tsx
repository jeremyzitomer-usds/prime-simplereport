import React from "react";
import { stateCodes } from "../../../../config/constants";
import Dropdown from "../../../commonComponents/Dropdown";
import TextInput from "../../../commonComponents/TextInput";

interface Props {
  facility: Facility;
  updateFacility: (facility: Facility) => void;
}

const FacilityInformation: React.FC<Props> = ({ facility, updateFacility }) => {
  const onChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>
  ) => {
    updateFacility({ ...facility, [e.target.name]: e.target.value });
  };

  return (
    <div>
      <h2 style={{ margin: 0 }}>Facility Information</h2>
      <div className="grid-row grid-gap">
        <div className="tablet:grid-col">
          <TextInput
            label="Testing Facility Name"
            name="name"
            value={facility.name}
            onChange={onChange}
            required
          />
        </div>
        <div className="tablet:grid-col">
          <TextInput
            label="CLIA Number"
            name="cliaNumber"
            value={facility.cliaNumber}
            onChange={onChange}
          />
        </div>
        <div className="tablet:grid-col">
          <TextInput
            label="Phone Number"
            name="phone"
            value={facility.phone}
            onChange={onChange}
          />
        </div>
      </div>
      <div className="grid-row grid-gap">
        <div className="tablet:grid-col">
          <TextInput
            label="Street 1"
            name="street"
            value={facility.street}
            onChange={onChange}
          />
        </div>
      </div>
      <div className="grid-row grid-gap">
        <div className="tablet:grid-col">
          <TextInput
            label="Street 2"
            name="streetTwo"
            value={facility.streetTwo}
            onChange={onChange}
          />
        </div>
      </div>
      <div className="grid-row grid-gap">
        <div className="tablet:grid-col">
          <TextInput
            label="City"
            name="city"
            value={facility.city}
            onChange={onChange}
          />
        </div>
        <div className="tablet:grid-col">
          <TextInput
            label="County"
            name="county"
            value={facility.county}
            onChange={onChange}
          />
        </div>
        <div className="tablet:grid-col">
          <TextInput
            label="Zip Code"
            name="zipCode"
            value={facility.zipCode}
            onChange={onChange}
            required
          />
        </div>
        <div className="tablet:grid-col">
          <Dropdown
            label="State"
            name="state"
            selectedValue={facility.state}
            options={stateCodes.map((c) => ({ label: c, value: c }))}
            defaultSelect
            className="sr-width-sm"
            onChange={onChange}
          />
        </div>
      </div>
    </div>
  );
};

export default FacilityInformation;
