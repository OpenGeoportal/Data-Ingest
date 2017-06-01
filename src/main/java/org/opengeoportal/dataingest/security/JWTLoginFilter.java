package org.opengeoportal.dataingest.security;

import java.io.IOException;
import java.util.Collections;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The Class JWTLoginFilter.
 */
public class JWTLoginFilter extends AbstractAuthenticationProcessingFilter {

    /**
     * Instantiates a new JWT login filter.
     *
     * @param url the url
     * @param authManager the auth manager
     */
    public JWTLoginFilter(final String url,
            final AuthenticationManager authManager) {
        super(new AntPathRequestMatcher(url));
        this.setAuthenticationManager(authManager);
    }

    /* (non-Javadoc)
     * @see org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter#attemptAuthentication(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public Authentication attemptAuthentication(final HttpServletRequest req,
            final HttpServletResponse res)
            throws AuthenticationException, IOException, ServletException {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(Feature.AUTO_CLOSE_SOURCE, true);
        AccountCredentials creds = new AccountCredentials();
        try {
            creds = objectMapper.readValue(req.getInputStream(),
                    AccountCredentials.class);
        } catch (final JsonMappingException e) {
            // throws this exception for anonymous api methods
        }

        return this.getAuthenticationManager()
                .authenticate(new UsernamePasswordAuthenticationToken(
                        creds.getUsername(), creds.getPassword(),
                        Collections.emptyList()));
    }

    /* (non-Javadoc)
     * @see org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter#successfulAuthentication(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, javax.servlet.FilterChain, org.springframework.security.core.Authentication)
     */
    @Override
    protected void successfulAuthentication(final HttpServletRequest req,
            final HttpServletResponse res, final FilterChain chain,
            final Authentication auth) throws IOException, ServletException {
        TokenAuthenticationService.addAuthentication(res, auth.getName());
    }
}
