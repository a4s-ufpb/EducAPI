package br.ufpb.dcx.apps4society.educapi.filter;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.filter.GenericFilterBean;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.AlgorithmMismatchException;
import com.auth0.jwt.exceptions.MissingClaimException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class TokenFilter extends GenericFilterBean {

    public static int TOKEN_INDEX = 7;

    @Value("${app.token.key:token_key}")
    private String TOKEN_KEY;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            ((HttpServletResponse) servletResponse).sendError(HttpServletResponse.SC_UNAUTHORIZED,
                    "Missing or badly formatted token.");
            return;
        }

        String token = header.substring(TOKEN_INDEX);

        try {
            Algorithm algorithm = Algorithm.HMAC256(TOKEN_KEY.getBytes());
            JWT.require(algorithm).build().verify(token);
        } catch (TokenExpiredException | MissingClaimException
                | SignatureVerificationException | AlgorithmMismatchException | IllegalArgumentException error) {
            ((HttpServletResponse) servletResponse).sendError(HttpServletResponse.SC_UNAUTHORIZED, error.getMessage());
            return;
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }
}
