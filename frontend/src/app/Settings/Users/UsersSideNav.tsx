import React from "react";
import classnames from "classnames";

import { displayFullName } from "../../utils";

import { SettingsUser } from "./ManageUsersContainer";

import "./ManageUsers.scss";

interface Props {
  activeUserId: string;
  users: SettingsUser[];
  onChangeActiveUser: (userId: string) => void;
}

const UsersSideNav: React.FC<Props> = ({
  activeUserId,
  users,
  onChangeActiveUser,
}) => {
  return (
    <div className="display-block users-sidenav">
      <h3>Users</h3>
      <ul className="usa-sidenav">
        {users.map((user: SettingsUser) => {
          return (
            <li
              className="usa-sidenav__item users-sidenav-item"
              onClick={() => onChangeActiveUser(user.id)}
              key={user.id}
            >
              <button
                style={{ cursor: "pointer" }}
                className={classnames(
                  "usa-button--unstyled",
                  "text-ink",
                  "text-no-underline",
                  "padding-105 padding-right-2",
                  activeUserId === user.id && "usa-current"
                )}
              >
                <span className="sidenav-user-name">
                  {displayFullName(
                    user.firstName,
                    user.middleName,
                    user.lastName
                  )}
                </span>
                <br />
                <span className="sidenav-user-email">{user.email}</span>
              </button>
            </li>
          );
        })}
      </ul>
    </div>
  );
};

export default UsersSideNav;
