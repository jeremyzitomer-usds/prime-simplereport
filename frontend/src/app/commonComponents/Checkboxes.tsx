import React from "react";
import classnames from "classnames";
import { UIDConsumer } from "react-uid";

import Required from "../commonComponents/Required";
import Optional from "../commonComponents/Optional";

import TextInput from "./TextInput";

// Checkbox objects need a value and label but also can have intrinsic `input`
// DOM properties such as `disabled`, `readonly`, `aria-xxx` etc.
export type CheckboxProps = {
  value: string;
  label: string;
};
type InputProps = JSX.IntrinsicElements["input"];
type Checkbox = CheckboxProps & InputProps;
interface Props {
  boxes: Checkbox[];
  checkedValues?: { [key: string]: boolean | undefined };
  legend: React.ReactNode;
  legendSrOnly?: boolean;
  name: string;
  disabled?: boolean;
  className?: string;
  errorMessage?: string;
  validationStatus?: "error" | "success";
  onChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
  required?: boolean;
  inputRef?: React.RefObject<HTMLInputElement>;
}

const Checkboxes = (props: Props) => {
  const {
    boxes,
    name,
    legend,
    checkedValues = {},
    onChange,
    legendSrOnly,
    validationStatus,
    errorMessage,
    required,
    inputRef,
  } = props;

  return (
    <fieldset
      className={classnames(
        "usa-fieldset prime-checkboxes",
        validationStatus === "error" && "usa-form-group--error",
        props.className
      )}
    >
      <legend
        className={classnames(
          "usa-legend",
          validationStatus === "error" && "usa-label--error",
          legendSrOnly && "usa-sr-only"
        )}
      >
        {required ? <Required label={legend} /> : <Optional label={legend} />}
      </legend>
      {validationStatus === "error" && (
        <div className="usa-error-message" role="alert">
          <span className="usa-sr-only">Error: </span>
          {errorMessage}
        </div>
      )}
      <UIDConsumer>
        {(_, uid) => (
          <div
            className={classnames(
              "usa-form-group",
              validationStatus === "error" && "usa-form-group--error"
            )}
          >
            {boxes.map(
              
              function({ value, label, disabled, checked, ...inputProps }, i) {
                // DISPLAY INPUT AREA FOR FREFORM TEXT
                if (value === 'notlisted') {
                  return (
                    <div>
                      <input
                        className="usa-checkbox__input"
                        checked={checked || checkedValues?.[value] || false}
                        id={uid(i)}
                        onChange={onChange}
                        type="checkbox"
                        value={value}
                        name={name}
                        ref={inputRef}
                        disabled={disabled || props.disabled}
                        {...inputProps}
                      />
                      <label className="usa-checkbox__label" htmlFor={uid(i)}>
                        {label}
                      </label>
                      <TextInput
                        // label={label}
                        name={name+`-freeresponse`}
                        // value={value}
                        onChange={onChange}
                        // validationStatus={getValidationStatus(field)}
                        // errorMessage={errors[field]}
                        type="text"
                        required={required}
                        disabled = { !checked }
                      />
                    </div>
                  )
                } else {
                  return (
                    <div className="usa-checkbox" key={uid(i)}>
                      <input
                        className="usa-checkbox__input"
                        checked={checked || checkedValues?.[value] || false}
                        id={uid(i)}
                        onChange={onChange}
                        type="checkbox"
                        value={value}
                        name={name}
                        ref={inputRef}
                        disabled={disabled || props.disabled}
                        {...inputProps}
                      />
                      <label className="usa-checkbox__label" htmlFor={uid(i)}>
                        {label}
                      </label>
                    </div>
                  )
                }
              } 


              
            )}
          </div>
        )}
      </UIDConsumer>
    </fieldset>
  );
};

export default Checkboxes;
