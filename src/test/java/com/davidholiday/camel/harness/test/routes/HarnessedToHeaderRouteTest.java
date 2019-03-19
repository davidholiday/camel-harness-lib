package com.davidholiday.camel.harness.test.routes;


import com.davidholiday.camel.harness.routing.RouteBuilderHarness;
import com.davidholiday.camel.harness.test.context.lifecycle.HarnessedAppContextLifecycle;
import com.davidholiday.camel.harness.testing.HarnessedRouteBuilderTestHarness;
import com.davidholiday.camel.harness.context.AppContextLifecycleHarness;

import com.davidholiday.camel.harness.test.routes.mocks.ToHeaderRoute;

import org.junit.Assert;
import org.junit.Test;

import java.util.Optional;


/**
 * example Harnessed route test. we use the constructor to inject the route we want to test and the contextlifecycle
 * object we want used for this set of tests. note that the context lifecycle object in the context of a test is used
 * only to populate the registry as the camel context doesn't exist in the same way in test as it does in prod. if any
 * pre/post processing behavior necessary it can be injected by using the Harness'
 * setProcessorFor{Pre/Post}MockEndpoint() methods.
 */
public class HarnessedToHeaderRouteTest extends HarnessedRouteBuilderTestHarness {

    private static final RouteBuilderHarness ROUTE_TO_TEST = new ToHeaderRoute();

    private static final AppContextLifecycleHarness HARNESSED_CAMEL_CONTEXT_LIFECYCLE =
            new HarnessedAppContextLifecycle(true);


    public HarnessedToHeaderRouteTest() { super(ROUTE_TO_TEST, HARNESSED_CAMEL_CONTEXT_LIFECYCLE); }


    @Test
    public void toHeaderRouteTestHappyPath() throws Exception {
        String expectedOutputAsString = "bar";
        String actualOutputAsString = (String)runTestAndGetOutput(Optional.empty(), Optional.of("foo"));
        Assert.assertEquals(
                "what came back wasn't what we expected!",
                expectedOutputAsString,
                actualOutputAsString
        );
    }

}
