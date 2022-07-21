package com.wooteco.sokdak.support;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import com.wooteco.sokdak.support.token.AuthorizationExtractor;
import com.wooteco.sokdak.support.token.InvalidTokenException;
import com.wooteco.sokdak.support.token.TokenManager;
import com.wooteco.sokdak.support.token.TokenNotFoundException;
import java.util.Objects;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsUtils;
import javax.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final TokenManager tokenManager;

    public AuthInterceptor(TokenManager tokenManager) {
        this.tokenManager = tokenManager;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (CorsUtils.isPreFlightRequest(request)) {
            return true;
        }
        if (isGetMethodWithPostsUri(request)) {
            return true;
        }

        validateExistHeader(request);
        String token = AuthorizationExtractor.extract(request);
        validateToken(token);
        return true;
    }

    private boolean isGetMethodWithPostsUri(HttpServletRequest request) {
        return request.getRequestURI().contains("/posts") && request.getMethod().equalsIgnoreCase("GET");
    }

    private void validateExistHeader(HttpServletRequest request) {
        String authorizationHeader = request.getHeader(AUTHORIZATION);
        if (Objects.isNull(authorizationHeader)) {
            throw new TokenNotFoundException();
        }
    }

    private void validateToken(String token) {
        if (!tokenManager.isValid(token)) {
            throw new InvalidTokenException();
        }
    }
}
