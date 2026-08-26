package com.example.eventhub.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
@Slf4j
public class LogFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        long startTime=System.currentTimeMillis();
        String method = request.getMethod();
        String url=request.getRequestURI();
        String query=request.getQueryString();
        String fullUrl=query!=null?url+"?"+query:url;
        log.info("-> {} {}",method,fullUrl);
        try {
            filterChain.doFilter(request,response);
        }
        finally {
            log.info("<- {} {} | {} | {} ms",method,fullUrl,response.getStatus(),System.currentTimeMillis()-startTime);
        }
    }
}
