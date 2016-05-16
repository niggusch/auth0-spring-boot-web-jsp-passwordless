package com.auth0.example.controllers;

import com.auth0.NonceGenerator;
import com.auth0.NonceStorage;
import com.auth0.RequestNonceStorage;
import com.auth0.example.main.ApplicationConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;

@Controller
public class LoginController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final NonceGenerator nonceGenerator = new NonceGenerator();

    private ApplicationConfig appConfig;

    @Autowired
    public LoginController(ApplicationConfig appConfig) {
        this.appConfig = appConfig;
    }

    @RequestMapping(value="/login", method = RequestMethod.GET)
    protected String login(final Map<String, Object> model, final HttpServletRequest request) throws ServletException, IOException {
        logger.debug("Performing login");
        if (model.get("error") != null) {
            model.put("error", true);
        } else {
            model.put("error", false);
        }
        final NonceStorage nonceStorage = new RequestNonceStorage(request);
        String nonce = nonceStorage.getState();
        if (nonce == null) {
            nonce = nonceGenerator.generateNonce();
            nonceStorage.setState(nonce);
        }
        model.put("clientId", appConfig.getClientId());
        model.put("domain", appConfig.getDomain());
        model.put("state", "nonce=" + nonce);
        logger.debug("Nonce (set in state): " + nonce);
        return "login";
    }


}
