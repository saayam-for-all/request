package org.sfa.request.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

/**
 * ClassName: LoggingAspect
 * Package: org.sfa.request.aop
 * Description:
 *
 * This class is an aspect that utilizes AspectJ to intercept and log the execution
 * of service layer methods in the Saayam For All system. It employs the `@Around`
 * annotation to wrap around the execution of service methods, logging method
 * entry, arguments, return results, and any thrown exceptions.
 *
 * Key functionalities include:
 * - Logging the class name and method name of method calls.
 * - Logging the arguments passed to methods.
 * - Logging the result returned from methods.
 * - Capturing and logging any exceptions thrown during method execution.
 * - Measuring and logging the execution time of methods.
 *
 * @author Fan Peng
 * Create 2024/7/20 16:12
 * @version 1.0
 */
@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Around("execution(* org.sfa.request.service..*.*(..))")
    public Object logMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        String className = methodSignature.getDeclaringType().getSimpleName();
        String methodName = methodSignature.getName();

        log.info("Entering: {}.{}", className, methodName);

        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            log.debug("Argument: {}", arg);
        }

        long start = System.currentTimeMillis();
        Object result;

        try {
            result = joinPoint.proceed();
            log.debug("Result: {}", result);
        } catch (Throwable throwable) {
            log.error("Exception in {}.{}: {}", className, methodName, throwable.getMessage(), throwable);
            throw throwable;
        } finally {
            long executionTime = System.currentTimeMillis() - start;
            log.info("Exiting: {}.{} - Execution time: {} ms", className, methodName, executionTime);
        }

        return result;
    }
}