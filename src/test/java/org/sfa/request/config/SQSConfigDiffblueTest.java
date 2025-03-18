//package org.sfa.request.config;
//
//import static org.mockito.Mockito.mock;
//
//import org.junit.jupiter.api.Disabled;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.aot.DisabledInAotMode;
//import org.springframework.test.context.junit.jupiter.SpringExtension;
//import software.amazon.awssdk.services.sqs.SqsAsyncClient;
//
//@ContextConfiguration(classes = {SQSConfig.class})
//@ExtendWith(SpringExtension.class)
//@DisabledInAotMode
//class SQSConfigDiffblueTest {
//    @Autowired
//    private SQSConfig sQSConfig;
//
//    @MockBean
//    private SqsAsyncClient sqsAsyncClient;
//
//    /**
//     * Method under test: {@link SQSConfig#sqsAsyncClient()}
//     */
//    @Test
//    @Disabled("TODO: Complete this test")
//    void testSqsAsyncClient() {
//        // TODO: Diffblue Cover was only able to create a partial test for this method:
//        //   Reason: Failed to create Spring context.
//        //   Attempt to initialize test context failed with
//        //   java.lang.IllegalStateException: ApplicationContext failure threshold (1) exceeded: skipping repeated attempt to load context for [MergedContextConfiguration@3ef287d2 testClass = org.sfa.request.config.DiffblueFakeClass11, locations = [], classes = [org.sfa.request.config.SQSConfig], contextInitializerClasses = [], activeProfiles = [], propertySourceDescriptors = [], propertySourceProperties = [], contextCustomizers = [org.springframework.boot.test.context.filter.ExcludeFilterContextCustomizer@24e707c7, org.springframework.boot.test.json.DuplicateJsonObjectContextCustomizerFactory$DuplicateJsonObjectContextCustomizer@2d7c9cd2, org.springframework.boot.test.mock.mockito.MockitoContextCustomizer@0, org.springframework.boot.test.autoconfigure.actuate.observability.ObservabilityContextCustomizerFactory$DisableObservabilityContextCustomizer@1f, org.springframework.boot.test.autoconfigure.properties.PropertyMappingContextCustomizer@0, org.springframework.boot.test.autoconfigure.web.servlet.WebDriverContextCustomizer@4422d5e6], contextLoader = org.springframework.test.context.support.DelegatingSmartContextLoader, parent = null]
//        //       at org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate.loadContext(DefaultCacheAwareContextLoaderDelegate.java:145)
//        //       at org.springframework.test.context.support.DefaultTestContext.getApplicationContext(DefaultTestContext.java:130)
//        //       at java.base/java.util.stream.ReferencePipeline$3$1.accept(ReferencePipeline.java:197)
//        //       at java.base/java.util.ArrayList$ArrayListSpliterator.forEachRemaining(ArrayList.java:1625)
//        //       at java.base/java.util.stream.AbstractPipeline.copyInto(AbstractPipeline.java:509)
//        //       at java.base/java.util.stream.AbstractPipeline.wrapAndCopyInto(AbstractPipeline.java:499)
//        //       at java.base/java.util.stream.ReduceOps$ReduceOp.evaluateSequential(ReduceOps.java:921)
//        //       at java.base/java.util.stream.AbstractPipeline.evaluate(AbstractPipeline.java:234)
//        //       at java.base/java.util.stream.ReferencePipeline.collect(ReferencePipeline.java:682)
//        //   See https://diff.blue/R026 to resolve this issue.
//
//        // Arrange and Act
//        sQSConfig.sqsAsyncClient();
//    }
//
//    /**
//     * Method under test: {@link SQSConfig#sqsTemplate(SqsAsyncClient)}
//     */
//    @Test
//    @Disabled("TODO: Complete this test")
//    void testSqsTemplate() {
//        // TODO: Diffblue Cover was only able to create a partial test for this method:
//        //   Reason: Failed to create Spring context.
//        //   Attempt to initialize test context failed with
//        //   java.lang.IllegalStateException: ApplicationContext failure threshold (1) exceeded: skipping repeated attempt to load context for [MergedContextConfiguration@4ce7d182 testClass = org.sfa.request.config.DiffblueFakeClass12, locations = [], classes = [org.sfa.request.config.SQSConfig], contextInitializerClasses = [], activeProfiles = [], propertySourceDescriptors = [], propertySourceProperties = [], contextCustomizers = [org.springframework.boot.test.context.filter.ExcludeFilterContextCustomizer@24e707c7, org.springframework.boot.test.json.DuplicateJsonObjectContextCustomizerFactory$DuplicateJsonObjectContextCustomizer@2d7c9cd2, org.springframework.boot.test.mock.mockito.MockitoContextCustomizer@0, org.springframework.boot.test.autoconfigure.actuate.observability.ObservabilityContextCustomizerFactory$DisableObservabilityContextCustomizer@1f, org.springframework.boot.test.autoconfigure.properties.PropertyMappingContextCustomizer@0, org.springframework.boot.test.autoconfigure.web.servlet.WebDriverContextCustomizer@4422d5e6], contextLoader = org.springframework.test.context.support.DelegatingSmartContextLoader, parent = null]
//        //       at org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate.loadContext(DefaultCacheAwareContextLoaderDelegate.java:145)
//        //       at org.springframework.test.context.support.DefaultTestContext.getApplicationContext(DefaultTestContext.java:130)
//        //       at java.base/java.util.stream.ReferencePipeline$3$1.accept(ReferencePipeline.java:197)
//        //       at java.base/java.util.ArrayList$ArrayListSpliterator.forEachRemaining(ArrayList.java:1625)
//        //       at java.base/java.util.stream.AbstractPipeline.copyInto(AbstractPipeline.java:509)
//        //       at java.base/java.util.stream.AbstractPipeline.wrapAndCopyInto(AbstractPipeline.java:499)
//        //       at java.base/java.util.stream.ReduceOps$ReduceOp.evaluateSequential(ReduceOps.java:921)
//        //       at java.base/java.util.stream.AbstractPipeline.evaluate(AbstractPipeline.java:234)
//        //       at java.base/java.util.stream.ReferencePipeline.collect(ReferencePipeline.java:682)
//        //   See https://diff.blue/R026 to resolve this issue.
//
//        // Arrange and Act
//        sQSConfig.sqsTemplate(mock(SqsAsyncClient.class));
//    }
//
//    /**
//     * Method under test:
//     * {@link SQSConfig#defaultSqsListenerContainerFactory(SqsAsyncClient)}
//     */
//    @Test
//    void testDefaultSqsListenerContainerFactory() {
//        // TODO: Diffblue Cover was only able to create a partial test for this method:
//        //   Diffblue AI was unable to find a test
//
//        // Arrange and Act
//        sQSConfig.defaultSqsListenerContainerFactory(mock(SqsAsyncClient.class));
//    }
//}
