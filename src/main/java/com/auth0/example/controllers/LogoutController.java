package com.auth0.example.controllers;

import com.auth0.example.main.ApplicationConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
public class LogoutController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private ApplicationConfig appConfig;

    @Autowired
    public LogoutController(ApplicationConfig appConfig) {
        this.appConfig = appConfig;
    }

    @GetMapping("/logout")
    protected String logout(final HttpServletRequest request) {
        logger.debug("Performing logout");
        if (request.getSession() != null) {
            request.getSession().invalidate();
        }
        final String logoutPath = appConfig.getOnLogoutRedirectTo();
        return "redirect:" + logoutPath;
    }

}
