package com.auth0.example.controllers;

import com.auth0.Auth0User;
import com.auth0.example.main.ApplicationConfig;
import com.auth0.example.utils.Auth0UserHelper;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;

@Controller
public class MfaSignupController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private ApplicationConfig appConfig;

    protected String getUri(final String auth0Domain, final String path) {
        return String.format("https://%s%s", auth0Domain, path);
    }

    @Autowired
    public MfaSignupController(ApplicationConfig appConfig) {
        this.appConfig = appConfig;
    }

    @GetMapping("/portal/mfa")
    protected void mfaSignup(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        final Auth0User user = Auth0User.get(request);
        if (user == null) {
            final String logoutPath = appConfig.getOnLogoutRedirectTo();
            response.sendRedirect(logoutPath);
        }
        request.setAttribute("user", user);
        final boolean hasMfa = Auth0UserHelper.isMfaEnabled(user);
        if (hasMfa) {
            request.getRequestDispatcher("/home").forward(request, response);
            return;
        } else {
            final HttpSession session = request.getSession(true);
            final String requestMfaNonce = request.getParameter("mfaNonce");
            final String sessionMfaNonce = (String) session.getAttribute("mfaNonce");
            if (requestMfaNonce == null || sessionMfaNonce == null) {
                request.getRequestDispatcher("/home").forward(request, response);
                return;
            } else if (!sessionMfaNonce.equals(requestMfaNonce)) {
                request.getRequestDispatcher("/home").forward(request, response);
                return;
            }

            logger.info("Registering user with MFA");
            final String auth0Domain = appConfig.getDomain();
            final String managementToken = appConfig.getManagementToken();
            final String mfaPayload = "{\"app_metadata\":{\"mfa\":true}}";
            final String accountUserId = user.getUserId();

            try {
                final String encodedAccountUserId = URLEncoder.encode(accountUserId, "UTF-8");
                final String endpoint = getUri(auth0Domain, "/api/v2/users/" + encodedAccountUserId);
                final OkHttpClient client = new OkHttpClient();
                final MediaType mediaType = MediaType.parse("application/json");
                final RequestBody body = RequestBody.create(mediaType, mfaPayload);
                final Request clientRequest = new Request.Builder()
                    .url(endpoint)
                    .patch(body)
                    .addHeader("authorization", "Bearer " + managementToken)
                    .addHeader("content-type", "application/json")
                    .build();
                final Response clientResponse = client.newCall(clientRequest).execute();
                if (clientResponse.code() != 200) {
                    // TODO - handle error
                    throw new IllegalStateException("Error occurred setting up MFA signup");
                }
            } catch (Exception ex) {
                throw new IllegalStateException("Error occurred setting up MFA signup: ", ex);
            }
            request.getRequestDispatcher("/logout").forward(request, response);
//            return "/logout";
        }
    }

}
