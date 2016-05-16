package com.auth0.example.main;

import com.auth0.example.filters.Auth0Filter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.*;
import org.springframework.stereotype.Component;

@Component
@Configuration
@ConfigurationProperties("auth0")
@PropertySources({
        @PropertySource("classpath:application.properties"),
        @PropertySource("classpath:auth0.properties")
})
public class ApplicationConfig {

    @Bean
    public FilterRegistrationBean someFilterRegistration() {
        final FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(authFilter());
        registration.addUrlPatterns("/portal/*");
        registration.addInitParameter("auth0.redirect_on_authentication_error", "/login");
        registration.setName("AuthFilter");
        return registration;
    }

    @Bean(name = "AuthFilter")
    public javax.servlet.Filter authFilter() {
        return new Auth0Filter();
    }

    private String clientId;
    private String clientSecret;
    private String domain;
    private String managementToken;
    private String onLogoutRedirectTo;

    private String emailLinkRedirectOnSuccess;
    private String emailLinkRedirectOnFail;
    private String accountLinkRedirectOnSuccess;
    private String accountLinkRedirectOnFail;

    private String securedRoute;

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getManagementToken() {
        return managementToken;
    }

    public void setManagementToken(String managementToken) {
        this.managementToken = managementToken;
    }

    public String getOnLogoutRedirectTo() {
        return onLogoutRedirectTo;
    }

    public void setOnLogoutRedirectTo(String onLogoutRedirectTo) {
        this.onLogoutRedirectTo = onLogoutRedirectTo;
    }

    public String getEmailLinkRedirectOnSuccess() {
        return emailLinkRedirectOnSuccess;
    }

    public void setEmailLinkRedirectOnSuccess(String emailLinkRedirectOnSuccess) {
        this.emailLinkRedirectOnSuccess = emailLinkRedirectOnSuccess;
    }

    public String getEmailLinkRedirectOnFail() {
        return emailLinkRedirectOnFail;
    }

    public void setEmailLinkRedirectOnFail(String emailLinkRedirectOnFail) {
        this.emailLinkRedirectOnFail = emailLinkRedirectOnFail;
    }

    public String getAccountLinkRedirectOnSuccess() {
        return accountLinkRedirectOnSuccess;
    }

    public void setAccountLinkRedirectOnSuccess(String accountLinkRedirectOnSuccess) {
        this.accountLinkRedirectOnSuccess = accountLinkRedirectOnSuccess;
    }

    public String getAccountLinkRedirectOnFail() {
        return accountLinkRedirectOnFail;
    }

    public void setAccountLinkRedirectOnFail(String accountLinkRedirectOnFail) {
        this.accountLinkRedirectOnFail = accountLinkRedirectOnFail;
    }

    public String getSecuredRoute() {
        return securedRoute;
    }

    public void setSecuredRoute(String securedRoute) {
        this.securedRoute = securedRoute;
    }
}
