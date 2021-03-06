import React from "react";
import ReactDOM from "react-dom";
import { Provider } from "react-redux";
import {
  ApolloClient,
  ApolloProvider,
  ApolloLink,
  InMemoryCache,
  concat,
} from "@apollo/client";
import { Switch, Route, BrowserRouter as Router } from "react-router-dom";
import { createUploadLink } from "apollo-upload-client";
import { onError } from "@apollo/client/link/error";
import { toast } from "react-toastify";
import Modal from "react-modal";

import App from "./app/App";
import PatientApp from "./patientApp/PatientApp";
import HealthChecks from "./app/HealthChecks";
import * as serviceWorker from "./serviceWorker";
import "./styles/App.css";
import { store } from "./app/store";
import { showError } from "./app/utils";

// Define the root element for modals
if (process.env.NODE_ENV !== "test") {
  Modal.setAppElement("#root");
}

if (window.location.hash) {
  const params = new URLSearchParams(window.location.hash.slice(1));
  const bearerToken = params.get("access_token");
  if (bearerToken) {
    localStorage.setItem("access_token", bearerToken);
  }
  // We need to store the ID token in order for logout to work correctly.
  const idToken = params.get("id_token");
  if (idToken) {
    localStorage.setItem("id_token", idToken);
  }
}

const httpLink = createUploadLink({
  uri: `${process.env.REACT_APP_BACKEND_URL}/graphql`,
});

const authMiddleware = new ApolloLink((operation, forward) => {
  operation.setContext({
    headers: {
      "Access-Control-Request-Headers": "Authorization",
      Authorization: `Bearer ${localStorage.getItem("access_token")}`,
    },
  });
  return forward(operation);
});

const logoutLink = onError(({ networkError, graphQLErrors }) => {
  if (networkError && process.env.REACT_APP_BASE_URL) {
    console.error("network error", networkError);
    console.log("redirecting to", process.env.REACT_APP_BASE_URL);
    window.location.replace(process.env.REACT_APP_BASE_URL);
  }
  if (graphQLErrors) {
    const messages = graphQLErrors.map(({ message, locations, path }) => {
      console.error(
        `[GraphQL error]: Message: ${message}, Location: ${locations}, Path: ${path}`
      );
      return message;
    });
    showError(
      toast,
      "Please check for errors and try again",
      messages.join(" ")
    );
    console.error("graphql error", graphQLErrors);
  }
});

const client = new ApolloClient({
  cache: new InMemoryCache(),
  link: logoutLink.concat(concat(authMiddleware, httpLink)),
});

ReactDOM.render(
  <ApolloProvider client={client}>
    <React.StrictMode>
      <Provider store={store}>
        <Router basename={process.env.PUBLIC_URL}>
          <Switch>
            <Route path="/health" component={HealthChecks} />
            <Route path="/pxp" component={PatientApp} />
            <Route path="/" component={App} />
          </Switch>
        </Router>
      </Provider>
    </React.StrictMode>
  </ApolloProvider>,
  document.getElementById("root")
);

// If you want your app to work offline and load faster, you can change
// unregister() to register() below. Note this comes with some pitfalls.
// Learn more about service workers: https://bit.ly/CRA-PWA
serviceWorker.unregister();
