package com.auth0.example.filters;


import com.auth0.Auth0RequestWrapper;
import com.auth0.Auth0User;
import com.auth0.Tokens;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class Auth0Filter implements Filter {

    private String onFailRedirectTo;

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        onFailRedirectTo = filterConfig.getInitParameter("auth0.redirect_on_authentication_error");
        if (onFailRedirectTo == null) {
            throw new IllegalArgumentException("auth0.redirect_on_authentication_error parameter of " + this.getClass().getName() + " cannot be null");
        }
    }

    protected Tokens loadTokens(final ServletRequest req, final ServletResponse res) {
        final HttpSession session = ((HttpServletRequest) req).getSession();
        return (Tokens) session.getAttribute("auth0tokens");
    }

    protected Auth0User loadUser(final ServletRequest req) {
        final HttpSession session = ((HttpServletRequest) req).getSession();
        return (Auth0User) session.getAttribute("user");
    }

    protected void onSuccess(final ServletRequest req, final ServletResponse res, final FilterChain next, final Auth0User user) throws IOException, ServletException {
        final Auth0RequestWrapper auth0RequestWrapper = new Auth0RequestWrapper(user, (HttpServletRequest) req);
        next.doFilter(auth0RequestWrapper, res);
    }

    protected void onReject(final ServletRequest request, final ServletResponse response, final FilterChain next) throws IOException, ServletException {
        final HttpServletResponse res = (HttpServletResponse) response;
        final HttpServletRequest req = (HttpServletRequest) request;
        res.sendRedirect(onFailRedirectTo);
    }

    @Override
    public void doFilter(final ServletRequest req, final ServletResponse res, final FilterChain next) throws IOException, ServletException {
        final Tokens tokens = loadTokens(req, res);
        final Auth0User user = loadUser(req);
        // Reject if not accessToken or idToken are found
        if (tokens == null || !tokens.exist()) {
            onReject(req, res, next);
            return;
        }
        onSuccess(req, res, next, user);
    }

    @Override
    public void destroy() {
    }
}
