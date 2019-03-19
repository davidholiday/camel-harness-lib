package com.davidholiday.camel.harness.routing;


import org.apache.camel.builder.RouteBuilder;

import java.util.Optional;


/**
 * attempt to enforce a paradigm where all routebuilders to implement a design pattern that allows the route builder to
 * be readily injected into the TestHarness. All routes that communicate with the outside world have that defined as a
 * distinct route that either sends or receives from the portion of the route that is purely internal. this way we can
 * always use the test harness to hook into the business logic part of the route w/o having to worry about standing up
 * whatever external dependency might be required to give the route a good beating with automated tests.
 *
 * for example, if there is a route that listens for messages from an MQ and sends them to a cache after doing some
 * business logic on it the routebuilder should have three routes in it:
 *
 * *route 1*
 *   [from: MQ] -> [BUSINESS_LOGIC_ROUTE_FROM_NAME]
 *
 * *route 2*
 *   [from: BUSINESS_LOGIC_ROUTE_FROM_NAME]
 *     -> [description: BUSINESS_LOGIC_ROUTE_DESCRIPTION]
 *       -> [processor: +applies business logics+]
 *         -> [to: BUSINESS_LOGIC_ROUTE_TO_NAME]
 *
 * *route 3*
 *   [from: BUSINESS_LOGIC_ROUTE_TO_NAME]
 *     --> [to: CACHE]
 *
 * the test harness knows how to find the route with the business logic by sniffing out the route description. it also
 * knows what the names of the entry and exit points are of that route because the implementor of this
 * has those names as either static or get-able values.
 *
 */
public abstract class RouteBuilderHarness extends RouteBuilder {

    // resolves to the name of the RouteBuilder class instance injected into the common constructor
    protected final String BASE_ID;

    // used by listener for archaius property changes to ID which routes need to be shut down/spun back up
    protected final String BUSINESS_LOGIC_ROUTE_ID;
    protected final String FROM_ROUTE_ID;
    protected final String TO_ROUTE_ID;

    // the name of the entry point into the business logic route
    protected final String BUSINESS_LOGIC_ROUTE_FROM_NAME;

    // if present, the name of the exit point out of the business logic route
    protected final Optional<String> BUSINESS_LOGIC_ROUTE_TO_NAME_OPTIONAL;

    // used by the TestHarness class to create a standard entrypoint into route and processor test routes
    public static final String TEST_ROUTE_ENTRY_NAME = "direct:testRouteIn";

    // used by the TestHarness class to figure out which of the collection of routes in a given routebuilder
    // encapsulates the business logic
    public static final String BUSINESS_LOGIC_ROUTE_DESCRIPTION = "businessLogicRoute";

    // used by route builders to define a common name scheme for the 'from' and 'to' business-logic-route routes
    public static final String FROM_ROUTE_ID_SUFFIX = "_from_route";
    public static final String TO_ROUTE_ID_SUFFIX = "_to_route";


    // used by route builders to define a common name for the hooks into and out of the business logic route
    // in the route builder. this is how the test harnes knows how to hook into the business logic route only
    public static final String BUSINESS_LOGIC_ROUTE_FROM_NAME_PREFIX = "direct:businessLogicRouteFrom_";
    public static final String BUSINESS_LOGIC_ROUTE_TO_NAME_PREFIX = "direct:businessLogicRouteTo_";


    /**
     *
     * @param routeBuilderName the simple name of the implementor of this template - used to populate BASE_ID
     *
     * @param hasBusinessLogicToRoute if the RouteBuilder represents a route that returns data to the caller it won't
     *                                have a 'to' route. It's up to the caller to make that clear via this variable.
     */
    public RouteBuilderHarness(String routeBuilderName, boolean hasBusinessLogicToRoute) {
        BASE_ID = routeBuilderName;

        BUSINESS_LOGIC_ROUTE_ID = BASE_ID + "_" + BUSINESS_LOGIC_ROUTE_DESCRIPTION;
        FROM_ROUTE_ID = BASE_ID + FROM_ROUTE_ID_SUFFIX;
        TO_ROUTE_ID = BASE_ID + TO_ROUTE_ID_SUFFIX;

        BUSINESS_LOGIC_ROUTE_FROM_NAME = BUSINESS_LOGIC_ROUTE_FROM_NAME_PREFIX + BUSINESS_LOGIC_ROUTE_ID;

        BUSINESS_LOGIC_ROUTE_TO_NAME_OPTIONAL = hasBusinessLogicToRoute
                ? Optional.of(BUSINESS_LOGIC_ROUTE_TO_NAME_PREFIX + BUSINESS_LOGIC_ROUTE_ID)
                : (Optional.empty());
    }

    /**
     *
     * @return
     */
    public String getBusinessLogicRouteId() { return BUSINESS_LOGIC_ROUTE_ID; }


    /**
     *
     * @return
     */
    public String getFromRouteId() { return FROM_ROUTE_ID; }


    /**
     *
     * @return
     */
    public String getToRouteId() { return TO_ROUTE_ID; }


    /**
     *
     * @return
     */
    public String getBaseId() { return BASE_ID; }


    /**
     *
     * @implNote here to make it easier for the test harness to grab the populated variable from an implementor instance
     *
     * @return
     */
    public String getBusinessLogicRouteFromName() {
        return BUSINESS_LOGIC_ROUTE_FROM_NAME;
    }


    /**
     * returns a copy of private variable BUSINESS_LOGIC_ROUTE_TO_NAME_OPTIONAL
     *
     * @return
     */
    public Optional<String> getBusinessLogicRouteToNameOptional() {

        String optionalContents = null;
        if (BUSINESS_LOGIC_ROUTE_TO_NAME_OPTIONAL.isPresent()) {
            optionalContents = BUSINESS_LOGIC_ROUTE_TO_NAME_OPTIONAL.get();
        }

        return Optional.ofNullable(optionalContents);
    }

}