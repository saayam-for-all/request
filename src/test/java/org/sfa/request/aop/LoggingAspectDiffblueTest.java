package org.sfa.request.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.aop.aspectj.MethodInvocationProceedingJoinPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ContextConfiguration(classes = {LoggingAspect.class})
@ExtendWith(SpringExtension.class)
class LoggingAspectDiffblueTest {
    @Autowired
    private LoggingAspect loggingAspect;

    /**
     * Method under test:
     * {@link LoggingAspect#logMethodExecution(ProceedingJoinPoint)}
     */
    @Test
    @Disabled("TODO: Complete this test")
    void testLogMethodExecution() throws Throwable {
        // TODO: Diffblue Cover was only able to create a partial test for this method:
        //   Reason: No inputs found that don't throw a trivial exception.
        //   Diffblue Cover tried to run the arrange/act section, but the method under
        //   test threw
        //   java.lang.IllegalArgumentException: MethodInvocation must not be null
        //   See https://diff.blue/R013 to resolve this issue.

        // Arrange and Act
        loggingAspect.logMethodExecution(new MethodInvocationProceedingJoinPoint(null));
    }
}
