package com.davidholiday.camel.harness.testing;


import com.davidholiday.camel.harness.routing.RouteBuilderHarness;
import com.davidholiday.camel.harness.context.AppContextLifecycleHarness;

import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;


/**
 * base class for all processor tests. provides common test route and mock components that can be used to preprocess
 * exchange contents prior to being sent to the camel processor being tested and inject expected exchange body and
 * header contents when it exits the route.
 */
public abstract class ProcessorTestHarness extends TestHarness {

    protected final Processor processor;


    /**
     * common constructor for all implementors of this class. ensures standard naming for mocks and standard route
     * for all processor tests
     *
     * @param processor
     * @param harnessedCamelContextLifecycle
     */
    public ProcessorTestHarness(Processor processor,
                                AppContextLifecycleHarness harnessedCamelContextLifecycle) {

        this.processor = processor;
        super.harnessedCamelContextLifecycle = harnessedCamelContextLifecycle;
    }


    /**
     * standard route for all processor tests. consists of
     *
     * [in] --> [preprocessor mock endpoint] --> [processor being tested] -->
     * [postprocessor mock endpoint] --> [out mock endpoint]
     *
     * * 'in' is basically a funnel. it's the entry point to the route and does nothing other then receive exchanges
     *
     * * the preprocessor mock endpoint is what allows us to perform business logic within the scope of a camel
     * processor prior to the exchange being sent to the processor we're endeavoring to test. in other words, if you
     * want to format or munge whatever's in the exchange in an algorithmic fashion just prior to it being received by
     * the processor you're trying to test - the mock preprocessor endpoint is where and how you do that.
     *
     * * the processor being tested is whichever processor the implementor of this class passed to the common
     * constructor (the constructor in this class). is exactly what the name says - whatever camel processor you're
     * trying to test.
     *
     * * the postprocessor mock endpoint is useful for handling testing or removal of items
     * (like time/date stamps) that can't be tested against static values
     *
     * * the out mock endpoint is a sink for whatever the processor being tested passes. it provides hooks into the
     * exchange it receives that allow for validation of both header and body contents.
     *
     * @return
     */
    protected RouteBuilder createRouteBuilder() {

        RouteBuilder routeBuilder = new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from(RouteBuilderHarness.TEST_ROUTE_ENTRY_NAME).to(PREPROCESSOR_MOCK_ENDPOINT_NAME)
                                                               .process(processor)
                                                               .to(POSTPROCESSOR_MOCK_ENDPOINT_NAME)
                                                               .to(OUT_MOCK_ENDPOINT_NAME);
            }
        };

        return routeBuilder;
    }

}
