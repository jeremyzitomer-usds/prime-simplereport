import React, { useState } from "react";
import { Redirect, RouteComponentProps, withRouter } from "react-router";

import Button from "../../app/commonComponents/Button";

import ToS from "./ToS";

const TermsOfService: React.FunctionComponent<RouteComponentProps> = (
  props
) => {
  const [nextPage, setNextPage] = useState(false);

  if (nextPage) {
    console.info(props.location);
    return (
      <Redirect push to={`/birth-date-confirmation${props.location.search}`} />
    );
  }

  return (
    <main className="patient-app padding-bottom-4 bg-base-lightest">
      <form className="grid-container maxw-tablet">
        <h1 className="font-heading-lg margin-top-3 margin-bottom-2">
          Terms of Service
        </h1>
        <div className="tos-content prime-formgroup usa-prose height-card-lg overflow-x-hidden font-body-3xs">
          <ToS />
        </div>
        <Button
          id="tos-consent-button"
          label="I consent to the Terms of Service"
          onClick={() => setNextPage(true)}
          className="margin-top-3"
        />
      </form>
    </main>
  );
};

export default withRouter(TermsOfService);
