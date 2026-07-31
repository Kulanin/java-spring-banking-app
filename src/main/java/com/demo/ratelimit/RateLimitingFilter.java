package com.demo.ratelimit;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class RateLimitingFilter implements Filter {

    @Autowired
    private InMemoryRateLimiter rateLimiter;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Use IP address as the unique identifier for rate limiting
        String clientIp = httpRequest.getRemoteAddr();

        if (!rateLimiter.allowRequest(clientIp)) {
            httpResponse.setStatus(429); // 429 Too Many Requests
            httpResponse.setContentType("application/json");
            httpResponse.getWriter()
                    .write("{\"status\":\"TOO_MANY_REQUESTS\",\"message\":\"Rate limit exceeded. Try again later.\"}");
            return;
        }

        // Pass request down the chain to your Controllers
        chain.doFilter(request, response);
    }
}