package com.changsha.zhongdian.operationlog.aspect;

import com.changsha.zhongdian.auth.model.LoginUser;
import com.changsha.zhongdian.auth.service.AuthService;
import com.changsha.zhongdian.operationlog.annotation.LogOperation;
import com.changsha.zhongdian.operationlog.service.OperationLogService;
import com.changsha.zhongdian.operationlog.util.BeforeStateHolder;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

@Aspect
@Component
public class OperationLogAspect {

    private final OperationLogService operationLogService;
    private final ExpressionParser parser = new SpelExpressionParser();

    public OperationLogAspect(OperationLogService operationLogService) {
        this.operationLogService = operationLogService;
    }

    @Around("@annotation(com.changsha.zhongdian.operationlog.annotation.LogOperation)")
    public Object logOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        LogOperation annotation = method.getAnnotation(LogOperation.class);

        String operatorAccount = null;
        String operatorRole = null;

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            Object currentUser = request.getAttribute(AuthService.CURRENT_USER_REQUEST_ATTRIBUTE);
            if (currentUser instanceof LoginUser loginUser) {
                operatorAccount = loginUser.account();
                operatorRole = loginUser.role();
            }
        }

        Object result;
        Object beforeState = null;
        
        try {
            result = joinPoint.proceed();
            
            if (annotation.captureBeforeState()) {
                beforeState = BeforeStateHolder.get();
            }
        } finally {
            BeforeStateHolder.clear();
        }

        EvaluationContext context = new StandardEvaluationContext();
        Object[] args = joinPoint.getArgs();
        String[] paramNames = signature.getParameterNames();
        for (int i = 0; i < paramNames.length; i++) {
            context.setVariable(paramNames[i], args[i]);
        }
        context.setVariable("result", result);

        String targetId = evaluateSpEL(annotation.targetIdSpEL(), context);
        String targetName = evaluateSpEL(annotation.targetNameSpEL(), context);
        String stationIdStr = evaluateSpEL(annotation.stationIdSpEL(), context);
        Long stationId = parseStationId(stationIdStr);
        String remark = evaluateSpEL(annotation.remarkSpEL(), context);

        Object afterState = result;

        operationLogService.log(
                operatorAccount,
                operatorRole,
                annotation.targetType(),
                targetId,
                targetName,
                annotation.operationType(),
                beforeState,
                afterState,
                remark,
                stationId
        );
        
        return result;
    }

    private String evaluateSpEL(String expression, EvaluationContext context) {
        if (expression == null || expression.isEmpty()) {
            return null;
        }
        try {
            Object value = parser.parseExpression(expression).getValue(context);
            return value != null ? value.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private Long parseStationId(String stationIdStr) {
        if (stationIdStr == null || stationIdStr.isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(stationIdStr);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
