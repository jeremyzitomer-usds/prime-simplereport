import { gql, useQuery } from "@apollo/client";
import React, { useState } from "react";
import { useSelector } from "react-redux";
import moment from "moment";
import classnames from "classnames";

import { PATIENT_TERM_CAP } from "../../config/constants";
import { displayFullName, displayFullNameInOrder } from "../utils";
import {
  InjectedQueryWrapperProps,
  QueryWrapper,
} from "../commonComponents/QueryWrapper";
import { ActionsMenu } from "../commonComponents/ActionsMenu";
import { getUrl } from "../utils/url";
import Pagination from "../commonComponents/Pagination";

import TestResultPrintModal from "./TestResultPrintModal";
import TestResultCorrectionModal from "./TestResultCorrectionModal";
import {HorizontalBarSeries, HorizontalGridLines, VerticalGridLines, XAxis, XYPlot, YAxis} from "react-vis";

import "./TestResultsList.scss";

export const testResultQuery = gql`
  query GetFacilityResults($facilityId: ID!, $pageNumber: Int, $pageSize: Int) {
    testResults(
      facilityId: $facilityId
      pageNumber: $pageNumber
      pageSize: $pageSize
    ) {
      internalId
      dateTested
      result
      correctionStatus
      deviceType {
        internalId
        name
      }
      patient {
        internalId
        firstName
        middleName
        lastName
        birthDate
        gender
        genderAssignedAtBirth
        sexualOrientation
        lookupId
      }
      createdBy {
        nameInfo {
          firstName
          middleName
          lastName
        }
      }
      patientLink {
        internalId
      }
    }
  }
`;

export const testResultsSummaryQuery = gql`
  query GetDemographicResults($facilityId: ID!) {
    testResultsSummary(
      facilityId: $facilityId
      demographics: [
        {
        sexualOrientation: ["heterosexual"]
        },
        {
        sexualOrientation: ["homosexual"]
        },
        {
        sexualOrientation: ["asexual"]
        },
        {
        sexualOrientation: ["questioning"]
        },
        {
        race: "black"
        sexualOrientation: ["homosexual"]
        },
        {
        race: "native"
        sexualOrientation: ["homosexual"]
        },
        {
        race: "white"
        sexualOrientation: ["homosexual"]
        },
        {
        race: "asian"
        sexualOrientation: ["homosexual"]
        },
        {
        gender: "woman"
        },
        {
        gender: "man"
        },
        {
        gender: "nonbinary"
        },
        {
        gender: ["nonbinary", "woman"]
        },
        {
        gender: "woman",
        genderAssignedAtBirth: "male"
        },      
        {
        gender: "woman",
        genderAssignedAtBirth: "female"
        },
        {
        gender: "woman",
        genderAssignedAtBirth: "x"
        },
        {
        race: "white"
        gender: "woman",
        genderAssignedAtBirth: "male"
        },      
        {
        race: "black"
        gender: "woman",
        genderAssignedAtBirth: "male"
        },      
        {
        bornOnOrBefore: "1990-01-01"
        sexualOrientation: ["homosexual"]
        },
        {
        bornOnOrAfter: "1990-01-01"
        sexualOrientation: ["homosexual"]
        },
        {
        residentCongregateSetting: true
        },
        {
        residentCongregateSetting: false
        },
      ]
      since: "2021-03-26"
    ) {
      demographic {
        description
      }
      totalTests
      percentPositive
      since
    }
  }
`;

interface Props {
  data: any;
  trackAction: () => void;
  refetch: () => void;
  page: number;
  entriesPerPage: number;
  totalEntries: number;
  barSeries: any;
  testResultsSummary: any;
}

export const DetachedTestResultsList: any = ({
  data,
  refetch,
  page,
  entriesPerPage,
  totalEntries,
  barSeries,
  testResultsSummary
}: Props) => {
  const [printModalId, setPrintModalId] = useState(undefined);
  const [markErrorId, setMarkErrorId] = useState(undefined);

  if (printModalId) {
    return (
      <TestResultPrintModal
        testResultId={printModalId}
        closeModal={() => setPrintModalId(undefined)}
      />
    );
  }
  if (markErrorId) {
    return (
      <TestResultCorrectionModal
        testResultId={markErrorId}
        closeModal={() => {
          setMarkErrorId(undefined);
          refetch();
        }}
      />
    );
  }

  const testResultsSummaryData = 
    testResultsSummary.testResultsSummary.map((t: any) => ({y: (t.demographic.description), x: t.percentPositive, color: t.percentPositive/50}));
  console.log(testResultsSummaryData);

  const testResults = data?.testResults || [];

  const testResultRows = () => {
    const byDateTested = (a: any, b: any) => {
      // ISO string dates sort nicely
      if (a.dateTested === b.dateTested) return 0;
      if (a.dateTested < b.dateTested) return 1;
      return -1;
    };

    if (testResults.length === 0) {
      return (
        <tr>
          <td>No results</td>
        </tr>
      );
    }

    // `sort` mutates the array, so make a copy
    return [...testResults].sort(byDateTested).map((r) => {
      const removed = r.correctionStatus === "REMOVED";
      const actionItems = [
        { name: "Print result", action: () => setPrintModalId(r.internalId) },
      ];
      if (!removed) {
        actionItems.push({
          name: "Mark as error",
          action: () => setMarkErrorId(r.internalId),
        });
      }
      return (
        <tr
          key={r.internalId}
          title={removed ? "Marked as error" : ""}
          className={classnames(
            "sr-test-result-row",
            removed && "sr-test-result-row--removed"
          )}
          data-patient-link={
            r.patientLink
              ? `${getUrl()}pxp?plid=${r.patientLink.internalId}`
              : null
          }
        >
          <th scope="row">
            {displayFullName(
              r.patient.firstName,
              r.patient.middleName,
              r.patient.lastName
            )}
          </th>
          <td>{moment(r.dateTested).format("lll")}</td>
          <td>{r.result}</td>
          <td>{r.deviceType.name}</td>
          <td>
            {displayFullNameInOrder(
              r.createdBy.nameInfo.firstName,
              r.createdBy.nameInfo.middleName,
              r.createdBy.nameInfo.lastName
            )}
          </td>
          <td>
            <ActionsMenu items={actionItems} />
          </td>
        </tr>
      );
    });
  };

  return (
    <main className="prime-home">
      <div className="grid-container">
        <div className="grid-row">
          <div className="prime-container usa-card__container sr-test-results-list">
            <div className="usa-card__header">
              <h2>
                Test Results
                <span className="sr-showing-results-on-page">
                  Showing {Math.min(entriesPerPage, totalEntries)} of{" "}
                  {totalEntries}
                </span>
              </h2>
            </div>
            <div className="usa-card__body">
              <div className="center-text"></div>
              <div className="responsive-bar-chart">
              <h3>Positivity Rates by Demographic, Last Two Weeks</h3>
                <XYPlot margin={{left: 250, top: 0}} 
                        yType="ordinal" 
                        width={900} 
                        height={400} 
                        xDomain={[-1, 100]} 
                        colorDomain={[0, 1, 2]}
                        colorRange={["cyan", "indigo", "red"]}>
                  <VerticalGridLines />
                  <XAxis  />
                  <YAxis />
                  <HorizontalBarSeries
                    data={testResultsSummaryData.reverse()} barWidth={0.15}
                  />
                </XYPlot>
              </div>
            </div>
            <div className="usa-card__body">
              <table className="usa-table usa-table--borderless width-full">
                <thead>
                  <tr>
                    <th scope="col">{PATIENT_TERM_CAP} Name</th>
                    <th scope="col">Date of Test</th>
                    <th scope="col">Result</th>
                    <th scope="col">Device</th>
                    <th scope="col">Submitter</th>
                    <th scope="col">Actions</th>
                  </tr>
                </thead>
                <tbody>{testResultRows()}</tbody>
              </table>
            </div>
            <div className="usa-card__footer">
              <Pagination
                baseRoute="/results"
                currentPage={page}
                entriesPerPage={entriesPerPage}
                totalEntries={totalEntries}
              />
            </div>
          </div>
        </div>
      </div>
    </main>
  );
};

export const resultsCountQuery = gql`
  query GetResultsCountByFacility($facilityId: ID!) {
    testResultsCount(facilityId: $facilityId)
  }
`;

const TestResultsList = (
  props: Omit<
    Props,
    InjectedQueryWrapperProps | "pageCount" | "entriesPerPage" | "totalEntries" | "barSeries" | "testResultsSummary"
  >
) => {
  const activeFacilityId = useSelector(
    (state) => (state as any).facility.id as string
  );

  const {
    data: totalResults,
    loading: l1,
    error: e1,
    refetch: refetchCount,
  } = useQuery(resultsCountQuery, {
    variables: { facilityId: activeFacilityId },
    fetchPolicy: "no-cache",
  });

  const {
    data: testResultsSummary,
    loading: l2,
    error: e2,
  } = useQuery(testResultsSummaryQuery, {
    variables: { facilityId: activeFacilityId },
    fetchPolicy: "no-cache",
  });

  console.log(testResultsSummary);
  if (activeFacilityId.length < 1) {
    return <div>"No facility selected"</div>;
  }

  if (l1) {
    return <p>Loading</p>;
  }
  if (e1) {
    throw e1;
  }

  if (l2) {
    return <p>Loading</p>;
  }
  if (e2) {
    throw e2;
  }

  const totalEntries = totalResults.testResultsCount;
  const entriesPerPage = 20;
  const pageNumber = props.page || 1;
  
  const barSeries = [{ y: "what", x: 59 }, { y: "how", x: 74 }];

  return (
    <div>
    {/* <QueryWrapper<Props>
      query={testResultQuery}
      queryOptions={{
        variables: {
          facilityId: activeFacilityId,
          pageNumber: pageNumber - 1,
          pageSize: entriesPerPage,
        },
      }}
      onRefetch={refetchCount}
      Component={DetachedTestResultsList}
      componentProps={{
        ...props,
        page: pageNumber,
        totalEntries,
        entriesPerPage,
        barSeries
      }}
    /> */}
    <QueryWrapper<Props>
      query={testResultQuery}
      queryOptions={{
        variables: {
          facilityId: activeFacilityId,
          pageNumber: pageNumber - 1,
          pageSize: entriesPerPage,
        },
      }}
      onRefetch={refetchCount}
      Component={DetachedTestResultsList}
      componentProps={{
        ...props,
        page: pageNumber,
        totalEntries,
        entriesPerPage,
        barSeries,
        testResultsSummary
      }}
    />
    </div>
  );
};

export default TestResultsList;
