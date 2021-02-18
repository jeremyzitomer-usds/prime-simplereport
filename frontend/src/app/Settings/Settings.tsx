import { Route, Redirect, RouteComponentProps } from "react-router-dom";
import ManageOrganizationContainer from "./ManageOrganizationContainer";
import ManageFacilitiesContainer from "./Facility/ManageFacilitiesContainer";
import FacilityFormContainer from "./Facility/FacilityFormContainer";
import ManageUsersContainer from "./Users/ManageUsersContainer";
import SettingsNav from "./SettingsNav";

interface Params {
  facilityId: string;
}

const Settings: React.FC<RouteComponentProps<{}>> = ({ match }) => {
  return (
    <main className="prime-home">
      <div className="grid-container">
        <SettingsNav />
        <Route exact path={match.url} component={ManageOrganizationContainer} />
        <Route
          path={match.url + "/facilities"}
          component={ManageFacilitiesContainer}
        />
        <Route
          path={match.url + "/facility/:facilityId"}
          render={({ match }: RouteComponentProps<Params>) => (
            <FacilityFormContainer facilityId={match.params.facilityId} />
          )}
        />
        <Route
          path={match.url + "/add-facility/"}
          render={({ match }: RouteComponentProps<Params>) => (
            <FacilityFormContainer facilityId={match.params.facilityId} />
          )}
        />
        <Route path={match.url + "/users"} component={ManageUsersContainer} />
        <Redirect exact to={"/settings"} />
      </div>
    </main>
  );
};

export default Settings;