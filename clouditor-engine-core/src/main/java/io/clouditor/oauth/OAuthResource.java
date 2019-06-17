package io.clouditor.oauth;

import io.clouditor.Engine;
import io.clouditor.auth.AuthenticationService;
import io.clouditor.auth.LoginResponse;
import io.clouditor.auth.User;
import io.clouditor.util.PersistenceManager;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("")
public class OAuthResource {

  private final AuthenticationService service;

  private final Engine engine;

  private static final Logger LOGGER = LoggerFactory.getLogger(OAuthResource.class);

  // TODO: extract to AuthenticationService?
  private Map<String, String> codes = new HashMap<>();

  @Inject
  public OAuthResource(AuthenticationService service, Engine engine) {
    this.service = service;
    this.engine = engine;
  }

  @GET
  @Path("profile")
  public Profile getProfile() {
    var profile = new Profile();
    profile.setEnabled(
        this.engine.getOAuthClientId() != null
            && this.engine.getOAuthClientSecret() != null
            && this.engine.getOAuthAuthUrl() != null
            && this.engine.getOAuthTokenUrl() != null
            && this.engine.getOAuthJwtSecret() != null);

    return profile;
  }

  @GET
  @Path("authorize")
  public Response authorize(
      @QueryParam("response_type") String responseType,
      @QueryParam("client_id") String clientId,
      @QueryParam("redirect_uri") String redirectUriString,
      @QueryParam("scope") String scope,
      @QueryParam("state") String state,
      @Context ContainerRequestContext context) {

    LOGGER.info(
        "Got OAuth 2.0 authorize call with response_type={}, client_id={}, redirect_uri={}, scope={}, state={}",
        responseType,
        clientId,
        redirectUriString,
        scope,
        state);

    // check, if clientId is empty
    if (clientId == null || clientId.isBlank()) {
      return Response.status(Status.BAD_REQUEST).entity("client_id was not specified").build();
    }

    // check, if clientId is in the database
    var client = this.service.getClient(clientId);
    if (client == null) {
      return Response.status(Status.BAD_REQUEST)
          .entity("Cannot find a valid client with specified client_id")
          .build();
    }

    // check, if redirectUri is empty
    // TODO: do we need to allow empty redirect urls for device code?
    if (redirectUriString == null || redirectUriString.isBlank()) {
      return Response.status(Status.BAD_REQUEST).entity("redirect_uri was empty").build();
    }

    // check, if redirectUri is a valid URI
    URI redirectUri;
    try {
      redirectUri = new URI(redirectUriString);
    } catch (URISyntaxException e) {
      return Response.status(Status.BAD_REQUEST).entity("redirect_uri is not a valid URL").build();
    }

    if (!client.getRedirectUrls().contains(redirectUri)) {
      return Response.status(Status.BAD_REQUEST)
          .entity("Client does not contain specified redirect_uri")
          .build();
    }

    // check, if responseType is empty
    if (responseType == null || responseType.isBlank()) {
      return redirectWithError(redirectUri, "invalid_request", null);
    }

    // check, if scope is empty
    if (scope == null || scope.isBlank()) {
      return redirectWithError(redirectUri, "invalid_scope", null);
    }

    // check, if state is empty
    // TODO: check if this is standard-compliant
    if (scope == null || scope.isBlank()) {
      return redirectWithError(redirectUri, "invalid_request", null);
    }

    // TODO: do we need scopes?

    // TODO: reject, if state is empty?

    var authorization = context.getCookies().get("authorization");

    if (authorization == null) {
      return redirectToLogin(responseType, clientId, redirectUri, scope, state);
    }

    // TODO: merge this with code in the AuthenticationFilter
    try {
      var user = this.service.verifyToken(authorization.getValue());

      // TODO: generate code and store somewhere
      var code = "mycode";

      this.codes.put(code, user.getId());

      return redirectToCallback(redirectUri, code);
    } catch (NotAuthorizedException ex) {
      return redirectWithError(redirectUri, "unauthorized_client", null);
    }
  }

  private Response redirectWithError(URI redirectUri, String error, String errorDescription) {
    var builder = UriBuilder.fromUri(redirectUri).queryParam("error", error);

    if (errorDescription != null) {
      builder.queryParam("error_description", errorDescription);
    }

    return Response.temporaryRedirect(builder.build()).build();
  }

  private Response redirectToCallback(URI redirectUri, String code) {
    var uri = UriBuilder.fromUri(redirectUri).queryParam("code", code).build();

    return Response.temporaryRedirect(uri).build();
  }

  private Response redirectToLogin(
      String responseType, String clientId, URI redirectUri, String scope, String state) {
    var returnUrl =
        UriBuilder.fromUri("/oauth2/authorize")
            .queryParam("response_type", responseType)
            .queryParam("client_id", clientId)
            .queryParam("redirect_uri", redirectUri)
            .queryParam("scope", scope)
            .queryParam("state", state)
            .build();

    /* angular is particular about the hash! it needs to be included.
    we cannot use UriBuilder, since it will remove the hash */
    var uri =
        URI.create(
            "/#/login?returnUrl="
                + URLEncoder.encode(returnUrl.toString(), Charset.defaultCharset()));

    return Response.temporaryRedirect(uri).build();
  }

  @POST
  @Path("token")
  public Response token(
      @QueryParam("grant_type") String grantType,
      @QueryParam("code") String code,
      @QueryParam("redirect_uri") String redirectUri,
      @QueryParam("client_id") String clientId,
      @QueryParam("code_verifier") String codeVerifier) {

    LOGGER.info(
        "Got OAuth 2.0 token call with grant_type={}, code={}, redirect_uri={}, client_id={}, code_verifier={}",
        grantType,
        code,
        redirectUri,
        clientId,
        codeVerifier);

    // TODO: properly exchange code with access token

    var userId = this.codes.get(code);
    var accessToken = this.service.createToken(userId);

    return Response.ok().entity("{\"access_token\": \"" + accessToken + "\"}").build();
  }

  @GET
  @Path("callback")
  public Response callback(@QueryParam("code") String code) {
    var token = retrieveAccessToken(code);

    var user = token.decode(this.engine.getOAuthJwtSecret(), this.engine.getoAuthJwtIssuer());

    if (user == null) {
      // redirect back to the beginning
      return buildRedirect();
    }

    LOGGER.info("Decoded access token as user {}", user.getUsername());

    // try to find the user
    var ref = PersistenceManager.getInstance().getById(User.class, user.getId());

    if (ref == null) {
      service.createUser(user);
    }

    // issue token for our API
    var payload = new LoginResponse();

    payload.setToken(service.createToken(user.getUsername()));

    var cookie =
        new NewCookie(
            "authorization", payload.getToken(), "/", null, "", NewCookie.DEFAULT_MAX_AGE, false);

    /* angular is particular about the hash! it needs to be included.
    we cannot use UriBuilder, since it will remove the hash */
    var uri = URI.create("/#?token=" + payload.getToken());

    return Response.temporaryRedirect(uri).cookie(cookie).build();
  }

  private TokenResponse retrieveAccessToken(String code) {
    var tokenUrl = this.engine.getOAuthTokenUrl();

    LOGGER.info("Trying to retrieve access token from {}", tokenUrl);

    var uri =
        UriBuilder.fromUri(tokenUrl)
            .queryParam("grant_type", "authorization_code")
            .queryParam("code", code)
            .build();

    var client =
        ClientBuilder.newClient()
            .register(
                (ClientRequestFilter)
                    requestContext -> {
                      var headers = requestContext.getHeaders();
                      headers.add(
                          "Authorization",
                          "Basic "
                              + Base64.getEncoder()
                                  .encodeToString(
                                      (this.engine.getOAuthClientId()
                                              + ":"
                                              + this.engine.getOAuthClientSecret())
                                          .getBytes()));
                    })
            .register(
                (ClientResponseFilter)
                    (requestContext, responseContext) -> {
                      // stupid workaround because some oauth servers incorrectly sends two
                      // Content-Type
                      // headers! fix it!
                      var contentType = responseContext.getHeaders().getFirst("Content-Type");
                      responseContext.getHeaders().putSingle("Content-Type", contentType);
                    });

    return client.target(uri).request().post(null, TokenResponse.class);
  }

  private Response buildRedirect() {
    var nonce = 25;

    var uri =
        UriBuilder.fromUri(this.engine.getOAuthAuthUrl())
            .queryParam("redirect_uri", this.engine.getBaseUrl() + "/oauth2/callback")
            .queryParam("client_id", this.engine.getOAuthClientId())
            .queryParam("response_type", "code")
            .queryParam("scope", "openid email full_name")
            .queryParam("nonce", nonce)
            .build();

    return Response.temporaryRedirect(uri).build();
  }

  @GET
  @Path("login")
  public Response login() {
    return buildRedirect();
  }
}
