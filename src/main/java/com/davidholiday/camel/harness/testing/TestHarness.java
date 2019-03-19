package com.davidholiday.camel.harness.testing;


import com.davidholiday.camel.harness.routing.RouteBuilderHarness;
import org.apache.camel.CamelContext;
import org.apache.camel.Processor;

import org.apache.camel.builder.RouteBuilder;

import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.component.servletlistener.ServletCamelContext;

import org.apache.camel.impl.JndiRegistry;

import org.apache.camel.test.junit4.CamelTestSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


import com.davidholiday.camel.harness.context.AppContextLifecycleHarness;


/**
 * base class for all testing involving camel routes and processors. provides a common structure for tests as well as
 * common test methods.
 */
public abstract class TestHarness extends CamelTestSupport {

    protected static final Logger LOGGER = LoggerFactory.getLogger(TestHarness.class);

    protected AppContextLifecycleHarness harnessedCamelContextLifecycle;

    private MockEndpoint preprocessorMockEndpoint;
    private MockEndpoint postprocessorMockEndpoint;
    private MockEndpoint outMockEndpoint;

    public static final String PREPROCESSOR_MOCK_ENDPOINT_NAME = "mock:preprocessor";
    public static final String POSTPROCESSOR_MOCK_ENDPOINT_NAME = "mock:postprocessor";
    public static final String OUT_MOCK_ENDPOINT_NAME = "mock:out";


    /**
     * defines the route to be used for testing - implementors must include the out mock endpoint provided by this class
     *
     * @return
     */
    protected abstract RouteBuilder createRouteBuilder() throws Exception;


    /**
     * overrides creation of DefaultCamelContext in favor of one that matches the type used in prod
     *
     * @return
     * @throws Exception
     */
    protected CamelContext createCamelContext() throws Exception {
        ServletCamelContext context = new ServletCamelContext(createRegistry(), null);
        return context;
    }


    /**
     * used in test context as an easy means of creating a test-context appropriate JNDI registry that is based on the
     * one used for prod.
     *
     * @param jndi
     * @return
     * @throws Exception
     */
    public JndiRegistry populateTestJndiRegistry(JndiRegistry jndi) throws Exception {

        harnessedCamelContextLifecycle.beforeStart((ServletCamelContext)context, jndi);
        harnessedCamelContextLifecycle.afterStart((ServletCamelContext)context, jndi);
        harnessedCamelContextLifecycle.beforeAddRoutes((ServletCamelContext)context, jndi);
        harnessedCamelContextLifecycle.afterAddRoutes((ServletCamelContext)context, jndi);
        harnessedCamelContextLifecycle.beforeStop((ServletCamelContext)context, jndi);
        harnessedCamelContextLifecycle.afterStop((ServletCamelContext)context, jndi);

        return jndi;
    }


    /**
     * we can't add mocks until the camel context has been set up. this enables that setup to occur after the
     * camel context is up but before the test is executed.
     */
    @Override
    public void doPostSetup() throws Exception {
        JndiRegistry registry = context().getRegistry(JndiRegistry.class);
        populateTestJndiRegistry(registry);

        this.outMockEndpoint = getMockEndpoint(OUT_MOCK_ENDPOINT_NAME);
        this.preprocessorMockEndpoint = getMockEndpoint(PREPROCESSOR_MOCK_ENDPOINT_NAME);
        this.postprocessorMockEndpoint = getMockEndpoint(POSTPROCESSOR_MOCK_ENDPOINT_NAME);
    }

    /**
     * injects behavior into mock preprocessor node in the route
     *
     * @param processor
     */
    public void setProcessorForPreprocessorMockEndpoint(Processor processor) {
        preprocessorMockEndpoint.whenAnyExchangeReceived(processor);
    }

    /**
     * injects behavior into mock postprocessor node in route
     *
     * @param processor
     */
    public void setProcessorForPostprocessorMockEndpoint(Processor processor) {
        postprocessorMockEndpoint.whenAnyExchangeReceived(processor);
    }

    /**
     * sets expectation for exchange header K V pair after exit from route
     *
     * @param expectedKey
     * @param expectedValue
     */
    public void setExpectedOutputHeader(String expectedKey, Object expectedValue) {
        outMockEndpoint.expectedHeaderReceived(expectedKey, expectedValue);
    }

    /**
     * sets expectation for exchange body state after exit from route
     *
     * @param expectedOutputBody
     */
    public void setExpectedOutputBody(Object expectedOutputBody) {
        outMockEndpoint.expectedBodiesReceived(expectedOutputBody);
    }

    /**
     * kicks off the test by publishing an empty exchange to the test route's entry point.
     */
    public void runTest() throws InterruptedException {
        runTest(Optional.empty(), Optional.empty());
    }

    /**
     * kicks off the test by publishing an exchange to the test route's entry point. if populated optional is passed the
     * contents will be attached to the exchange body.
     *
     * @throws InterruptedException
     */
    public void runTest(Optional<Map<String, Object>> headersOptional,
                        Optional<Object> bodyOptional) throws InterruptedException {

        Object body = bodyOptional.isPresent() ? bodyOptional.get() : new Object();

        Map<String, Object> headers =
                headersOptional.isPresent() ? headersOptional.get() : new HashMap<>();

        template.sendBodyAndHeaders(RouteBuilderHarness.TEST_ROUTE_ENTRY_NAME, body, headers);
        assertMockEndpointsSatisfied();
    }

    /**
     * kicks off the test by publishing an exchange to the test route's entry point and parrots the Object response
     * from the route
     *
     * @param headersOptional
     * @param bodyOptional
     * @throws InterruptedException
     */
    public Object runTestAndGetOutput(Optional<Map<String, Object>> headersOptional, Optional<Object> bodyOptional) {

        Object body = bodyOptional.isPresent() ? bodyOptional.get() : new Object();

        Map<String, Object> headers =
                headersOptional.isPresent() ? headersOptional.get() : new HashMap<>();

        Object output = template.requestBodyAndHeaders(RouteBuilderHarness.TEST_ROUTE_ENTRY_NAME, body, headers);
        return output;
    }
}
