import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { Provider } from "react-redux";
import { MemoryRouter } from "react-router";
import createMockStore from "redux-mock-store";

import Header from "../Header";

const mockStore = createMockStore([]);

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
});

describe("Header.tsx", () => {
  const OLD_ENV = process.env;

  beforeEach(() => {
    jest.resetModules();
    process.env = { ...OLD_ENV };
  });

  afterAll(() => {
    process.env = OLD_ENV;
  });

  const WrappedHeader: React.FC = () => (
    <MemoryRouter>
      <Provider store={store}>
        <Header />
      </Provider>
    </MemoryRouter>
  );

  const ALERT_TEXT =
    "This is a training site with fake data to be used for training purposes only";

  const MODAL_TEXT = "Welcome to the SimpleReport";

  it("displays the training header and modal and dismisses the modal", async () => {
    process.env.REACT_APP_IS_TRAINING_SITE = "true";
    render(<WrappedHeader />);
    expect(await screen.findByText(ALERT_TEXT)).toBeInTheDocument();
    const trainingWelcome = await screen.findByText(MODAL_TEXT, {
      exact: false,
    });
    expect(trainingWelcome).toBeInTheDocument();
    await waitFor(() => {
      fireEvent.click(screen.getByText("Got it", { exact: false }));
    });
    expect(trainingWelcome).not.toBeInTheDocument();
  });
  it("does not display training notifications outside the training environment", () => {
    process.env.REACT_APP_IS_TRAINING_SITE = "false";
    expect(screen.queryByText(ALERT_TEXT)).not.toBeInTheDocument();
    expect(screen.queryByText(MODAL_TEXT)).not.toBeInTheDocument();
  });
});
