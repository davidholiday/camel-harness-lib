package com.davidholiday.camel.harness.config;


import com.davidholiday.camel.harness.routing.RouteBuilderHarness;

import org.apache.camel.CamelContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * handles shutdown, removal, and recreation of camel routes. invoked when arhcaius detects a property change that
 * impacts a route's behaviour.
 *
 * @implNote the constructor takes a RouteBuilderHarness class rather than the object because harnessed routeBuilders
 * will resolve connection strings when they are constructed and we want to make sure those values are up-to-date.
 *
 * TODO in cases (like MongoDB's connector) where stateful properties like host/port are stored inside the object
 * TODO  registered with JNDI it needs to be included in the list of things to restart. a future iteration of this
 * TODO  needs to add that capability
 */
public class ShutDownAndRecreateRoutesRunnable implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShutDownAndRecreateRoutesRunnable.class);

    private CamelContext camelContext;
    private Class<RouteBuilderHarness> harnessedRouteBuilderClazz;


    /**
     * @param camelContext
     * @param harnessedRouteBuilderClazz
     */
    public ShutDownAndRecreateRoutesRunnable(CamelContext camelContext,
                                             Class<RouteBuilderHarness> harnessedRouteBuilderClazz) {

        this.camelContext = camelContext;
        this.harnessedRouteBuilderClazz = harnessedRouteBuilderClazz;
    }


    public void run() {

        try {
            // create instance of new routebuilder which should come online with the new connection strings
            // embedded in its routes. also grab IDs of routes that need to be cycled
            //
            Constructor constructor = harnessedRouteBuilderClazz.getConstructor();
            RouteBuilderHarness harnessedRouteBuilder = (RouteBuilderHarness) constructor.newInstance();

            List<String> routesToRemoveList = Stream.of(
                    harnessedRouteBuilder.getBusinessLogicRouteFromName(),
                    harnessedRouteBuilder.getBusinessLogicRouteId()
            ).collect(Collectors.toList());

            if (harnessedRouteBuilder.getBusinessLogicRouteToNameOptional().isPresent()) {
                String businessLogicRouteToName = harnessedRouteBuilder.getBusinessLogicRouteToNameOptional().get();
                routesToRemoveList.add(businessLogicRouteToName);
            }


            // cycle routes
            //
            for (String routeToRemove : routesToRemoveList) {
                LOGGER.info("stopping and removing route: {}", routeToRemove);
                camelContext.stopRoute(routeToRemove);
                camelContext.removeRoute(routeToRemove);
            }

            // putting this in a for-loop so we can't end up in an infinite-loop situation.
            // fyi - the call to addRoutes() later will blow up if we somehow exit this loop w/o first removing all the
            // requested routes due to namespace violation (each route must have a unique identifier)
            for (int i = 0; i < 10; i++) {
                if (i == 9) {
                    LOGGER.warn("potential problem removing all of the requested routes...");
                }

                List<Boolean> gtgList =
                        routesToRemoveList.stream()
                                .map(routeToRemove -> camelContext.getRouteStatus(routeToRemove) == null)
                                .filter(x -> x == true)
                                .collect(Collectors.toList());

                if (gtgList.size() == routesToRemoveList.size()) {
                    break;
                } else {
                    Thread.sleep(3000);
                }
            }

            LOGGER.info("presuming routes are removed - attempting to recreate...");
            camelContext.addRoutes(harnessedRouteBuilder);

        } catch (Exception e) {
            LOGGER.error("something went wrong removing and re-injecting routes ", e);
        }
    }

}

