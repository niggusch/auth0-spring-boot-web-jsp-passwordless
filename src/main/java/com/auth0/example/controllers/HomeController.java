package com.auth0.example.controllers;

import com.auth0.Auth0User;
import com.auth0.NonceGenerator;
import com.auth0.NonceStorage;
import com.auth0.RequestNonceStorage;
import com.auth0.example.main.ApplicationConfig;
import com.auth0.example.utils.Auth0UserHelper;
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
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final NonceGenerator nonceGenerator = new NonceGenerator();

    private ApplicationConfig appConfig;

    @Autowired
    public HomeController(ApplicationConfig appConfig) {
        this.appConfig = appConfig;
    }

    @GetMapping("/portal/home")
    protected String home(final Map<String, Object> model, final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        final Auth0User user = Auth0User.get(request);
        if (user == null) {
            final String logoutPath = appConfig.getOnLogoutRedirectTo();
            return "redirect:" + logoutPath;
        }
        model.put("user", user);
        model.put("clientId", appConfig.getClientId());
        model.put("domain", appConfig.getDomain());

        handleMfaLink(user, request);
        handleLinkDropbox(user, request);
        return "home";
    }

    protected void handleMfaLink(final Auth0User user, final HttpServletRequest request) {
        final boolean hasMfa = Auth0UserHelper.isMfaEnabled(user);
        final HttpSession session = request.getSession(true);
        if (!hasMfa) {
            String mfaNonce = (String) session.getAttribute("mfaNonce");
            if (mfaNonce == null) {
                mfaNonce = nonceGenerator.generateNonce();
                session.setAttribute("mfaNonce", mfaNonce);
            }
        } else {
            session.removeAttribute("mfaNonce");
        }
        request.setAttribute("hasMfa", hasMfa);
    }

    protected void handleLinkDropbox(final Auth0User user, final HttpServletRequest request) {
        final List<String> linkedAccounts = Auth0UserHelper.getLinkedAccountsInfo(user);
        boolean linkDropbox = true;
        for(String acct: linkedAccounts) {
           if (acct.contains("dropbox")) {
               linkDropbox = false;
               String [] parts = acct.split("\\|");
               if (parts.length > 1) {
                   request.setAttribute("dropboxEmail", parts[1]);
               }
            }
        }
        if (linkDropbox) {
            setNonce(request);
        }
        request.setAttribute("linkDropbox", linkDropbox);
    }

    protected void setNonce(final HttpServletRequest request) {
        final NonceStorage nonceStorage = new RequestNonceStorage(request);
        String nonce = nonceStorage.getState();
        if (nonce == null) {
            nonce = nonceGenerator.generateNonce();
            nonceStorage.setState(nonce);
        }
        request.setAttribute("state", "nonce=" + nonce);
        logger.debug("Nonce (set in state): " + nonce);
    }
}
