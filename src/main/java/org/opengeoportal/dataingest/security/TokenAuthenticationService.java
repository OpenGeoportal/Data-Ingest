package org.opengeoportal.dataingest.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.Date;

/**
 * The Class TokenAuthenticationService.
 */
class TokenAuthenticationService {

    /**
     * The Constant EXPIRATIONTIME.
     */
    static final long EXPIRATIONTIME = 864_000_000; // 10 days

    /**
     * The Constant SECRET.
     */
    static final String SECRET = "ThisIsASecret";

    /**
     * The Constant TOKEN_PREFIX.
     */
    static final String TOKEN_PREFIX = "Bearer";

    /**
     * The Constant HEADER_STRING.
     */
    static final String HEADER_STRING = "Authorization";

    /**
     * Adds the authentication.
     *
     * @param res      the res
     * @param username the username
     */
    static void addAuthentication(final HttpServletResponse res,
                                  final String username) {
        final String JWT = Jwts.builder().setSubject(username)
            .setExpiration(new Date(System.currentTimeMillis()
                + TokenAuthenticationService.EXPIRATIONTIME))
            .signWith(SignatureAlgorithm.HS512,
                TokenAuthenticationService.SECRET)
            .compact();
        res.addHeader(TokenAuthenticationService.HEADER_STRING,
            TokenAuthenticationService.TOKEN_PREFIX + " " + JWT);
    }

    /**
     * Gets the authentication.
     *
     * @param request the request
     * @return the authentication
     */
    static Authentication getAuthentication(final HttpServletRequest request) {
        final String token = request
            .getHeader(TokenAuthenticationService.HEADER_STRING);
        if (token != null) {
            // parse the token.
            final String user = Jwts.parser()
                .setSigningKey(TokenAuthenticationService.SECRET)
                .parseClaimsJws(token.replace(
                    TokenAuthenticationService.TOKEN_PREFIX, ""))
                .getBody().getSubject();

            return user != null ? new UsernamePasswordAuthenticationToken(user,
                null, Collections.emptyList()) : null;
        }
        return null;
    }
}
