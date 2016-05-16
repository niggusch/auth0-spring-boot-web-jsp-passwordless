package com.auth0.example.controllers.callbacks;

import com.auth0.Auth0User;
import com.auth0.NonceStorage;
import com.auth0.RequestNonceStorage;
import com.auth0.Tokens;
import com.auth0.example.main.ApplicationConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
public class EmailLinkCallbackController extends AbstractCallbackController {

    @Autowired
    public EmailLinkCallbackController(ApplicationConfig appConfig) {
        super(appConfig, appConfig.getEmailLinkRedirectOnSuccess(), appConfig.getEmailLinkRedirectOnFail());
    }

    @GetMapping("/mcallback")
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
            throws ServletException, IOException {
        if (isValidRequest(req, resp)) {
            try {
                final Tokens tokens = fetchTokens(req);
                Auth0User user = fetchUser(tokens);
                store(tokens, user, req);
                final NonceStorage nonceStorage = new RequestNonceStorage(req);
                nonceStorage.setState(null);
                onSuccess(req, resp);
            } catch (IllegalArgumentException ex) {
                onFailure(req, resp, ex);
            } catch (IllegalStateException ex) {
                onFailure(req, resp, ex);
            }
        } else {
            onFailure(req, resp, new IllegalStateException("Invalid state or error"));
        }
    }

}
