package com.davidholiday.camel.harness.testing;


import com.davidholiday.camel.harness.routing.RouteBuilderHarness;
import org.apache.camel.builder.RouteBuilder;

import org.apache.camel.model.RouteDefinition;

import java.util.Optional;

import com.davidholiday.camel.harness.context.AppContextLifecycleHarness;


/**
 * extends base TestHarness class with some route-specific magicks that allow us to hook into implementors of
 * RouteBuilderTemplate in such a way that enables us to test the 'business logic' route segment in a set of routes
 * defined by RouteBuilderTemplate.
 */
public abstract class HarnessedRouteBuilderTestHarness extends TestHarness {

    protected final RouteBuilderHarness productionRouteBuilder;


    /**
     * common constructor for all implementors of this class. ensures standard naming for mocks and standard route
     * for all processor tests
     *
     * @param productionRouteBuilder
     */
    public HarnessedRouteBuilderTestHarness(RouteBuilderHarness productionRouteBuilder,
                                            AppContextLifecycleHarness harnessedCamelContextLifecycle) {

        this.productionRouteBuilder = productionRouteBuilder;
        this.harnessedCamelContextLifecycle = harnessedCamelContextLifecycle;
    }


    /**
     *
     * @return
     */
    protected RouteBuilder createRouteBuilder() {
        return productionRouteBuilder;
    }


    /**
     * activates superclass method to setup testing mocks
     *
     * filters out all but the business logic camel route in the production routebuilder and wraps it in standard
     * testing route constructs
     *
     * [in] -> [pre-processor] -> [business logic route] -> [out]
     *
     * the pre-processor is there in case you want to inject processor behavior into the test route prior to entry
     * into the business logic route (which is what you're ostensibly trying to test)
     *
     * @throws Exception
     */
    @Override
    public void doPostSetup() throws Exception {
        super.doPostSetup();

        RouteDefinition businessLogicRouteDefinition = null;

        for (RouteDefinition routeDefinition : productionRouteBuilder.getRouteCollection().getRoutes()) {
            String routeDescriptionText = routeDefinition.getDescriptionText();

            if ((routeDescriptionText != null) && (routeDescriptionText.equals(RouteBuilderHarness.BUSINESS_LOGIC_ROUTE_DESCRIPTION))) {
                businessLogicRouteDefinition = routeDefinition;
            }

            context.removeRouteDefinition(routeDefinition);
        }

        if (businessLogicRouteDefinition == null) {
            LOGGER.error(
                    "routeBuilder must have one route with description text {} ",
                    RouteBuilderHarness.BUSINESS_LOGIC_ROUTE_DESCRIPTION
            );

            throw new IllegalArgumentException();
        }

        String businessLogicRouteEntryName = productionRouteBuilder.getBusinessLogicRouteFromName();

        Optional<String> businessLogicRouteToNameOptional =
                productionRouteBuilder.getBusinessLogicRouteToNameOptional();

        RouteBuilder testRouteBuilder =
                getTestRouteBuilder(businessLogicRouteEntryName, businessLogicRouteToNameOptional);

        context.addRoutes(testRouteBuilder);
        context.addRouteDefinition(businessLogicRouteDefinition);

    }

    /**
     * builds a test route appropriate to whether or not the businessLogicRouteDefinition routes its output to a sink
     * route identified by a 'to' name.
     *
     * @param businessLogicRouteEntryName
     * @param businessLogicRouteToNameOptional
     * @return
     */
    public RouteBuilder getTestRouteBuilder(
            String businessLogicRouteEntryName, Optional<String> businessLogicRouteToNameOptional) {

        RouteBuilder testRouteBuilder;

        if (businessLogicRouteToNameOptional.isPresent()) {

            String businessLogicRouteToName = businessLogicRouteToNameOptional.get();

            testRouteBuilder = new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from(RouteBuilderHarness.TEST_ROUTE_ENTRY_NAME).to(PREPROCESSOR_MOCK_ENDPOINT_NAME)
                                               .to(businessLogicRouteEntryName);

                    from(businessLogicRouteToName).to(POSTPROCESSOR_MOCK_ENDPOINT_NAME)
                                                  .to(OUT_MOCK_ENDPOINT_NAME);
                }
            };

        } else {

            testRouteBuilder = new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from(RouteBuilderHarness.TEST_ROUTE_ENTRY_NAME).to(PREPROCESSOR_MOCK_ENDPOINT_NAME)
                                               .to(businessLogicRouteEntryName)
                                               .to(POSTPROCESSOR_MOCK_ENDPOINT_NAME)
                                               .to(OUT_MOCK_ENDPOINT_NAME);
                }
            };

        }

        return testRouteBuilder;
    }

}
