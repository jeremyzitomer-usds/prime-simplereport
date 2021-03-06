package gov.cdc.usds.simplereport.api.apiuser;

import gov.cdc.usds.simplereport.api.model.User;
import gov.cdc.usds.simplereport.service.ApiUserService;
import graphql.kickstart.tools.GraphQLQueryResolver;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/** Created by jeremyzitomer-usds on 1/7/21 */
@Component
public class ApiUserResolver implements GraphQLQueryResolver {

  private ApiUserService _userService;

  public ApiUserResolver(ApiUserService userService) {
    _userService = userService;
  }

  public User getWhoami() {
    return new User(_userService.getCurrentUserInfo());
  }

  public List<User> getUsers() {
    return _userService.getUsersInCurrentOrg().stream().map(User::new).collect(Collectors.toList());
  }
}
