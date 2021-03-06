package gov.cdc.usds.simplereport.config.authorization;

import gov.cdc.usds.simplereport.config.BeanProfiles;
import gov.cdc.usds.simplereport.config.simplereport.DemoUserConfiguration;
import gov.cdc.usds.simplereport.config.simplereport.DemoUserConfiguration.DemoUser;
import gov.cdc.usds.simplereport.idp.repository.OktaRepository;
import gov.cdc.usds.simplereport.service.AuthorizationService;
import gov.cdc.usds.simplereport.service.model.IdentityAttributes;
import gov.cdc.usds.simplereport.service.model.IdentitySupplier;
import java.util.List;
import java.util.Optional;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@Configuration
@Profile(BeanProfiles.NO_SECURITY)
@ConditionalOnWebApplication
public class DemoAuthenticationConfiguration {

  public static final String DEMO_AUTHORIZATION_FLAG = "SR-DEMO-LOGIN ";
  private static final String DEMO_BEARER_PREFIX = "Bearer " + DEMO_AUTHORIZATION_FLAG;

  private static final Logger LOG = LoggerFactory.getLogger(DemoAuthenticationConfiguration.class);

  /**
   * Creates and registers a {@link Filter} that runs before each request is processed by the
   * servlet. It checks for an Authorization header with a Bearer token that starts with {@link
   * #DEMO_AUTHORIZATION_FLAG}, and if it finds one it "logs in" a user with the username found in
   * the rest of that Authorization header. Interpreting the resulting {@link Authentication} is
   * expected to be the problem of the other beans created by this configuration class.
   */
  @Bean
  public FilterRegistrationBean<Filter> identityFilter() {
    Filter filter =
        (ServletRequest request, ServletResponse response, FilterChain chain) -> {
          SecurityContext securityContext = SecurityContextHolder.getContext();
          LOG.debug(
              "Processing request for demo authentication: current auth is {}",
              securityContext.getAuthentication());
          HttpServletRequest req2 = (HttpServletRequest) request;
          String authHeader = req2.getHeader("Authorization");
          LOG.info("Auth type is {}, header is {}", req2.getAuthType(), authHeader);
          if (authHeader != null && authHeader.startsWith(DEMO_BEARER_PREFIX)) {
            LOG.trace("Parsing authorization header [{}]", authHeader);
            String userName = authHeader.substring(DEMO_BEARER_PREFIX.length());
            securityContext.setAuthentication(
                new TestingAuthenticationToken(userName, null, List.of()));
          }
          chain.doFilter(request, response);
        };
    return new FilterRegistrationBean<>(filter);
  }

  @Bean
  public AuthorizationService getDemoAuthorizationService(
      OktaRepository oktaRepo, IdentitySupplier supplier) {
    return new DemoAuthorizationService(oktaRepo, supplier);
  }

  @Bean
  public IdentitySupplier getDemoIdentitySupplier(DemoUserConfiguration config) {
    return getCurrentDemoUserSupplier(config);
  }

  /**
   * Creates and returns a {@link IdentitySupplier} that examines the current security context,
   * finds the username of the current user, and returns the {@link IdentityAttributes} from the
   * {@link DemoUser} that matches that username. If a default user has been configured, that user
   * will be returned in the event that there is no username.
   *
   * <p>It is like that had we set out to have this originally we would have ended up with a more
   * conventional UserDetailsService, and we could eventually work our way back there, but
   * path-dependence produces weird results sometimes.
   *
   * @param config the configured list of DemoUsers, with or without a default user set.
   * @throws BadCredentialsException if a username is supplied but is invalid
   */
  public static IdentitySupplier getCurrentDemoUserSupplier(DemoUserConfiguration config) {
    return () -> {
      Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      DemoUser found;
      if (auth == null) { // have to allow defaulting to work even when there is no authentication
        LOG.warn("No authentication found: current user will be default (if available)");
        found = config.getDefaultUser();
      } else if (auth instanceof AnonymousAuthenticationToken) {
        LOG.info("Unauthenticated user in demo mode: current user will be default (if available)");
        found = config.getDefaultUser();
      } else {
        String userName = auth.getName();
        found = config.getByUsername(userName);
        if (found == null) {
          LOG.error("Invalid user {} in demo mode", userName);
          throw new BadCredentialsException("Invalid username supplied.");
        }
      }
      return null != found ? found.getIdentity() : null;
    };
  }

  /**
   * An {@link AuthorizationService} that looks up the username of the current authenticated user in
   * the list of configured demo users, falling back to the default if one is configured, then looks
   * up that user in {@link OktaRepository} to find their authorization information.
   */
  public static class DemoAuthorizationService implements AuthorizationService {

    private final IdentitySupplier _getCurrentUser;
    private final OktaRepository _oktaRepo;

    public DemoAuthorizationService(OktaRepository oktaRepo, IdentitySupplier getCurrent) {
      super();
      this._getCurrentUser = getCurrent;
      this._oktaRepo = oktaRepo;
    }

    @Override
    public List<OrganizationRoleClaims> findAllOrganizationRoles() {
      String username = Optional.ofNullable(_getCurrentUser.get()).orElseThrow().getUsername();
      Optional<OrganizationRoleClaims> claims =
          _oktaRepo.getOrganizationRoleClaimsForUser(username);
      return claims.isEmpty() ? List.of() : List.of(claims.get());
    }
  }
}
