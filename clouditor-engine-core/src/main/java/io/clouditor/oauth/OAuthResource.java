package io.clouditor.oauth;

import io.clouditor.Engine;
import io.clouditor.auth.AuthenticationService;
import io.clouditor.auth.LoginResponse;
import io.clouditor.auth.User;
import io.clouditor.util.PersistenceManager;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;
import javax.inject.Inject;
import javax.ws.rs.GET;
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
      @QueryParam("redirect_uri") String redirectUri,
      @QueryParam("scope") String scope,
      @QueryParam("state") String state,
      @Context ContainerRequestContext context) {

    // TODO: check response type (only code?)

    // check, if clientId is empty
    if (clientId == null || clientId.isBlank()) {
      // TODO: what kind of error?
      return Response.status(Status.BAD_REQUEST).build();
    }

    // check, if clientId is in the database
    var client = this.service.getClient(clientId);
    if (client == null) {
      // TODO: what kind of error?
      return Response.status(Status.BAD_REQUEST).build();
    }

    // check, if redirectUri is empty
    // TODO: do we need to allow empty redirect urls for device code?
    if (redirectUri == null || redirectUri.isBlank()) {
      // TODO: what kind of error?
      return Response.status(Status.BAD_REQUEST).build();
    }

    // check, if redirectUri is a valid URI
    try {
      var url = new URI(redirectUri);
    } catch (URISyntaxException e) {
      // TODO: what kind of error?
      return Response.status(Status.BAD_REQUEST).build();
    }

    if (!client.getRedirectUrls().contains(redirectUri)) {
      // TODO: what kind of error?
      return Response.status(Status.BAD_REQUEST).build();
    }

    // TODO: do we need scopes?

    // TODO: reject, if state is empty?

    LOGGER.info(
        "Got OAuth 2.0 authorize call with response_type={}, client_id={}, redirect_uri={}, scope={}, state={}",
        responseType,
        clientId,
        redirectUri,
        scope,
        state);

    // TODO: check cookie or redirect to login page
    return Response.temporaryRedirect(URI.create("/#?login")).build();
  }

  @GET
  @Path("token")
  public Response token(
      @QueryParam("grant_type") String grantType,
      @QueryParam("code") String code,
      @QueryParam("redirect_uri") String redirectUri,
      @QueryParam("client_id") String client_id,
      @QueryParam("code_verifier") String codeVerifier) {

    // TODO: exchange code with access token

    return Response.ok().build();
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

    // TODO: max age, etc.
    /* angular is particular about the hash! it needs to be included.
    we cannot use UriBuilder, since it will remove the hash */
    var uri = URI.create("/#?token=" + payload.getToken());

    return Response.temporaryRedirect(uri)
        .cookie(new NewCookie("authorization", payload.getToken()))
        .build();
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
