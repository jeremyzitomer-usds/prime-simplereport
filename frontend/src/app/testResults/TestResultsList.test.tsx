import { MockedProvider } from "@apollo/client/testing";
import { render, screen } from "@testing-library/react";
import { Provider } from "react-redux";
import { MemoryRouter } from "react-router";
import configureStore from "redux-mock-store";

import TestResultsList, {
  DetachedTestResultsList,
  resultsCountQuery,
  testResultQuery,
} from "./TestResultsList";

const mockStore = configureStore([]);
const store = mockStore({
  organization: {
    name: "Organization Name",
  },
  user: {
    firstName: "Kim",
    lastName: "Mendoza",
  },
  facilities: [
    { id: "1", name: "Facility 1" },
    { id: "2", name: "Facility 2" },
  ],
  facility: { id: "1", name: "Facility 1" },
});

jest.mock("@microsoft/applicationinsights-react-js", () => ({
  useAppInsightsContext: () => {},
  useTrackEvent: jest.fn(),
}));

// Data copied from Chrome network window
const testResults = [
  {
    internalId: "0969da96-b211-41cd-ba61-002181f0918d",
    dateTested: "2021-03-17T19:27:23.806Z",
    result: "NEGATIVE",
    correctionStatus: "ORIGINAL",
    deviceType: {
      internalId: "8c1a8efe-8951-4f84-a4c9-dcea561d7fbb",
      name: "Abbott IDNow",
      __typename: "DeviceType",
    },
    patient: {
      internalId: "48c523e8-7c65-4047-955c-e3f65bb8b58a",
      firstName: "Barb",
      middleName: "Whitaker",
      lastName: "Cragell",
      birthDate: "1960-11-07",
      gender: "male",
      genderAssignedAtBirth: "male",
      sexualOrientation: "male",
      lookupId: null,
      __typename: "Patient",
    },
    createdBy: {
      nameInfo: {
        firstName: "Arthur",
        middleName: "A",
        lastName: "Admin",
      },
    },
    patientLink: {
      internalId: "68c543e8-7c65-4047-955c-e3f65bb8b58a",
    },
    __typename: "TestResult",
  },
  {
    internalId: "7c768a5d-ef90-44cd-8050-b96dd77f51d5",
    dateTested: "2021-03-17T19:27:21.052Z",
    result: "NEGATIVE",
    correctionStatus: "ORIGINAL",
    deviceType: {
      internalId: "8c1a8efe-8951-4f84-a4c9-dcea561d7fbb",
      name: "Abbott IDNow",
      __typename: "DeviceType",
    },
    patient: {
      internalId: "f74ad245-3a69-44b5-bb6d-efe06308bb85",
      firstName: "Barde",
      middleName: "X",
      lastName: "Colleer",
      birthDate: "1960-11-07",
      gender: "female",
      genderAssignedAtBirth: "female",
      sexualOrientation: "heterosexual",
      lookupId: null,
      __typename: "Patient",
    },
    createdBy: {
      nameInfo: {
        firstName: "Ursula",
        middleName: "",
        lastName: "User",
      },
    },
    patientLink: {
      internalId: "68c543e8-7c65-4047-955c-e3f65bb8b58a",
    },
    __typename: "TestResult",
  },
];

const mocks = [
  {
    request: {
      query: resultsCountQuery,
      variables: {
        facilityId: "1",
      },
    },
    result: {
      data: {
        testResultsCount: testResults.length,
      },
    },
  },
  {
    request: {
      query: testResultQuery,
      variables: {
        facilityId: "1",
        pageNumber: 0,
        pageSize: 20,
      },
    },
    result: {
      data: {
        testResults,
      },
    },
  },
];

describe("TestResultsList", () => {
  it("should render a list of tests", async () => {
    const { container, getByText } = render(
      <MemoryRouter>
        <DetachedTestResultsList
          data={{ testResults }}
          page={1}
          entriesPerPage={20}
          totalEntries={testResults.length}
        />
      </MemoryRouter>
    );
    expect(getByText("Test Results", { exact: false })).toBeInTheDocument();
    expect(getByText("Cragell, Barb Whitaker")).toBeInTheDocument();
    expect(container).toMatchSnapshot();
  });
  it("should call appropriate gql endpoints for pagination", async () => {
    render(
      <MemoryRouter>
        <Provider store={store}>
          <MockedProvider mocks={mocks}>
            <TestResultsList page={1} />
          </MockedProvider>
        </Provider>
      </MemoryRouter>
    );
    expect(
      await screen.findByText("Test Results", { exact: false })
    ).toBeInTheDocument();
    expect(
      await screen.findByText("Cragell, Barb Whitaker")
    ).toBeInTheDocument();
  });
});
