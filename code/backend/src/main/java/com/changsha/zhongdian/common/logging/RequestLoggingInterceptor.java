package com.changsha.zhongdian.common.logging;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingInterceptor.class);
    private static final String REQUEST_START_TIME_ATTRIBUTE = "requestStartTime";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(REQUEST_START_TIME_ATTRIBUTE, System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception exception
    ) {
        Object startTime = request.getAttribute(REQUEST_START_TIME_ATTRIBUTE);
        long costMillis = startTime instanceof Long value ? System.currentTimeMillis() - value : -1L;

        log.info(
                "Request completed: method={}, path={}, status={}, cost={}ms",
                request.getMethod(),
                request.getRequestURI(),
                response.getStatus(),
                costMillis
        );
    }
}
