package com.auth0.example.controllers.callbacks;

import com.auth0.Auth0User;
import com.auth0.NonceStorage;
import com.auth0.RequestNonceStorage;
import com.auth0.Tokens;
import com.auth0.example.main.ApplicationConfig;
import org.apache.commons.lang3.Validate;
import us.monoid.json.JSONObject;
import us.monoid.web.JSONResource;
import us.monoid.web.Resty;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.Map;

import static us.monoid.web.Resty.content;


public abstract class AbstractCallbackController {

    protected String redirectOnSuccess;
    protected String redirectOnFail;
    protected ApplicationConfig appConfig;

    public AbstractCallbackController(final ApplicationConfig appConfig, final String redirectOnSuccess, final String redirectOnFail) {
        this.appConfig = appConfig;
        this.redirectOnSuccess = redirectOnSuccess;
        this.redirectOnFail = redirectOnFail;
    }

    protected void onSuccess(final HttpServletRequest req, final HttpServletResponse resp)
            throws ServletException, IOException {
        resp.sendRedirect(req.getContextPath() + redirectOnSuccess);
    }

    protected void onFailure(final HttpServletRequest req, final HttpServletResponse resp,
                             Exception ex) throws ServletException, IOException {
        ex.printStackTrace();
        final String redirectOnFailLocation = req.getContextPath() + redirectOnFail;
        resp.sendRedirect(redirectOnFailLocation);
    }

    protected void store(final Tokens tokens, final Auth0User user, final HttpServletRequest req) {
        final HttpSession session = req.getSession(true);
        // Save tokens on a persistent session
        session.setAttribute("auth0tokens", tokens);
        session.setAttribute("user", user);
    }

    protected Tokens fetchTokens(final HttpServletRequest req) throws IOException {
        final String authorizationCode = getAuthorizationCode(req);
        final Resty resty = createResty();
        final String tokenUri = getTokenUri();
        final JSONObject json = new JSONObject();
        try {
            json.put("client_id", appConfig.getClientId());
            json.put("client_secret", appConfig.getClientSecret());
            json.put("redirect_uri", req.getRequestURL().toString());
            json.put("grant_type", "authorization_code");
            json.put("code", authorizationCode);

            final JSONResource tokenInfo = resty.json(tokenUri, content(json));
            return new Tokens(tokenInfo.toObject());
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot get Token from Auth0", ex);
        }
    }

    protected Auth0User fetchUser(final Tokens tokens) {
        final Resty resty = createResty();
        final String userInfoUri = getUserInfoUri(tokens.getAccessToken());
        try {
            final JSONResource json = resty.json(userInfoUri);
            return new Auth0User(json.toObject());
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot get User from Auth0", ex);
        }
    }

    protected String getTokenUri() {
        return getUri("/oauth/token");
    }

    protected String getUserInfoUri(final String accessToken) {
        return getUri("/userinfo?access_token=" + accessToken);
    }

    protected String getUri(final String path) {
        return String.format("https://%s%s", appConfig.getDomain(), path);
    }

    protected String getAuthorizationCode(final HttpServletRequest req) {
        final String code = req.getParameter("code");
        Validate.notNull(code);
        return code;
    }

    /**
     * Override this method to specify a different Resty client. For example, if
     * you want to add a proxy, this would be the place to set it
     *
     * @return {@link Resty} that will be used to perform all requests to Auth0
     */
    protected Resty createResty() {
        return new Resty();
    }

    protected boolean isValidRequest(final HttpServletRequest req, final HttpServletResponse res) throws IOException {
        if (hasError(req) || !isValidState(req)) {
            return false;
        }
        return true;
    }

    protected static boolean hasError(final HttpServletRequest req) {
        return req.getParameter("error") != null;
    }

    protected NonceStorage getNonceStorage(final HttpServletRequest request) {
        return new RequestNonceStorage(request);
    }

    protected boolean isValidState(final HttpServletRequest req) {
        final String stateValue = req.getParameter("state");
        try {
            final Map<String, String> pairs = splitQuery(stateValue);
            final String state = pairs.get("nonce");
            return state != null && state.equals(getNonceStorage(req).getState());
        } catch (UnsupportedEncodingException e) {
            return false;
        }
    }

    protected static Map<String, String> splitQuery(final String query) throws UnsupportedEncodingException {
        if (query == null) {
            throw new NullPointerException("query cannot be null");
        }
        final Map<String, String> query_pairs = new LinkedHashMap<>();
        final String[] pairs = query.split("&");
        for (String pair : pairs) {
            final int idx = pair.indexOf("=");
            query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"),
                    URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
        }
        return query_pairs;
    }


}
