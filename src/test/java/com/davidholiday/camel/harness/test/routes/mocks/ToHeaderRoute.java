package com.davidholiday.camel.harness.test.routes.mocks;


import com.davidholiday.camel.harness.routing.RouteBuilderHarness;
import com.davidholiday.camel.harness.test.processor.mocks.ToHeaderProcessor;

import org.apache.camel.Processor;


/**
 * one processor route to ensure facilitate testing of the HarnessedRouteBuilderTestHarness
 */
public class ToHeaderRoute extends RouteBuilderHarness {

    private static final Processor toHeaderProcessor = new ToHeaderProcessor();

    private static final String NAME = ToHeaderRoute.class.getSimpleName();


    public ToHeaderRoute() { super(NAME, false); }


    public void configure() throws Exception {
        from("direct:in").routeId(FROM_ROUTE_ID)
                             .to(BUSINESS_LOGIC_ROUTE_FROM_NAME);

        from(BUSINESS_LOGIC_ROUTE_FROM_NAME).routeId(BUSINESS_LOGIC_ROUTE_ID)
                                            .description(BUSINESS_LOGIC_ROUTE_DESCRIPTION)
                                            .process(toHeaderProcessor);
    }

}
