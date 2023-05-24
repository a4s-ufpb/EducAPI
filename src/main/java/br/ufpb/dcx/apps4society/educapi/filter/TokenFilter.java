package br.ufpb.dcx.apps4society.educapi.filter;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.PrematureJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class TokenFilter extends GenericFilterBean {

    public static int TOKEN_INDEX = 7;

    @Value("${app.token.key}")
    private String TOKEN_KEY;


    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")){
            ((HttpServletResponse) servletResponse).sendError(HttpServletResponse.SC_UNAUTHORIZED,
                    "Missing or badly formatted token.");
            return;
        }

        String token = header.substring(TOKEN_INDEX);
        try {
            Jwts.parser().setSigningKey(this.TOKEN_KEY).parseClaimsJws(token).getBody();
        }catch (SignatureException | ExpiredJwtException | MalformedJwtException | PrematureJwtException
                | UnsupportedJwtException | IllegalArgumentException error ){
            ((HttpServletResponse) servletResponse).sendError(HttpServletResponse.SC_UNAUTHORIZED, error.getMessage());
            return;
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }
}
