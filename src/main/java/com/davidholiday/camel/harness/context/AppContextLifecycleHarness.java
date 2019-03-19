package com.davidholiday.camel.harness.context;


import org.apache.camel.component.servletlistener.CamelContextLifecycle;
import org.apache.camel.component.servletlistener.ServletCamelContext;

import org.apache.camel.impl.JndiRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;


/**
 * harness for CamelContextLifecycle object that adds capabilities to invoke different behavior depending on whether or
 * not constructor parameter inAlternateContext is true. useful for creating alternate versions of the lifecycle object
 * for testing or for different runtime environments.
 *
 */
public abstract class AppContextLifecycleHarness implements CamelContextLifecycle<JndiRegistry> {

    protected static final Logger LOGGER = LoggerFactory.getLogger(AppContextLifecycleHarness.class);

    // the context lifecycle object we're plugging into this harness
    private final CamelContextLifecycle contextLifecycle;

    // boolean indicating whether or not we're bootstrapping in a test mode
    private final boolean inAlternateContext;

    // a map of substitute functions to be executed if running in test context
    private final Map<String, AppContextLifecycleFunctionInterface> alternateContextFunctionMap = new HashMap<>();

    // the set of valid keys for alternateContextFunctionMap
    public static final String BEFORE_START_FUNCTION_ALTERNATE_CONTEXT_KEY = "beforeStartFunctionAlternateContext";
    public static final String AFTER_START_FUNCTION_ALTERNATE_CONTEXT_KEY = "afterStartFunctionAlternateContext";

    public static final String BEFORE_STOP_FUNCTION_ALTERNATE_CONTEXT_KEY = "beforeStopFunctionAlternateContext";
    public static final String AFTER_STOP_FUNCTION_ALTERNATE_CONTEXT_KEY = "afterStopFunctionAlternateContext";

    public static final String 
            BEFORE_ADD_ROUTES_FUNCTION_ALTERNATE_CONTEXT_KEY = "beforeAddRoutesFunctionAlternateContext";
    
    public static final String 
            AFTER_ADD_ROUTES_FUNCTION_ALTERNATE_CONTEXT_KEY = "afterAddRoutesFunctionAlternateContext";


    /**
     *
     * @param contextLifecycle
     * @param inAlternateContext
     * @param alternateContextFunctionMap
     */
    public AppContextLifecycleHarness(CamelContextLifecycle<JndiRegistry> contextLifecycle,
                                      boolean inAlternateContext,
                                      Map<String, AppContextLifecycleFunctionInterface> alternateContextFunctionMap){

        super();
        this.contextLifecycle = contextLifecycle;
        this.inAlternateContext = inAlternateContext;
        this.alternateContextFunctionMap.putAll(alternateContextFunctionMap);
    }

    /**
     * local helper that pulls text context functions out of the text context function map and executes them
     *
     * @param camelContext
     * @param registry
     * @param alternateContextFunctionName
     */
    private void callAlternateContextFunction(ServletCamelContext camelContext,
                                              JndiRegistry registry,
                                              String alternateContextFunctionName) {

        AppContextLifecycleFunctionInterface alternateContextFunction =
                alternateContextFunctionMap.get(alternateContextFunctionName);
        
        alternateContextFunction.call(camelContext, registry);
    }


    /*
    overridden AppContextLifecycleInterface methods to allow us to execute differently when running in a test execution context
     */

    @Override
    public void beforeStart(ServletCamelContext camelContext, JndiRegistry registry) throws Exception {
        if (inAlternateContext && alternateContextFunctionMap.containsKey(BEFORE_START_FUNCTION_ALTERNATE_CONTEXT_KEY)){
            callAlternateContextFunction(camelContext, registry, BEFORE_START_FUNCTION_ALTERNATE_CONTEXT_KEY);
        }
        else { contextLifecycle.beforeStart(camelContext, registry); }
    }


    @Override
    public void afterStart(ServletCamelContext camelContext, JndiRegistry registry) throws Exception {
        if (inAlternateContext && alternateContextFunctionMap.containsKey(AFTER_START_FUNCTION_ALTERNATE_CONTEXT_KEY)) {
            callAlternateContextFunction(camelContext, registry, AFTER_START_FUNCTION_ALTERNATE_CONTEXT_KEY);
        }
        else { contextLifecycle.afterStart(camelContext, registry); }
    }

    @Override
    public void beforeStop(ServletCamelContext camelContext, JndiRegistry registry) throws Exception {
        if (inAlternateContext && alternateContextFunctionMap.containsKey(BEFORE_STOP_FUNCTION_ALTERNATE_CONTEXT_KEY)) {
            callAlternateContextFunction(camelContext, registry, BEFORE_STOP_FUNCTION_ALTERNATE_CONTEXT_KEY);
        }
        else { contextLifecycle.beforeStop(camelContext, registry); }
    }

    @Override
    public void afterStop(ServletCamelContext camelContext, JndiRegistry registry) throws Exception {
        if (inAlternateContext && alternateContextFunctionMap.containsKey(AFTER_STOP_FUNCTION_ALTERNATE_CONTEXT_KEY)) {
            callAlternateContextFunction(camelContext, registry, AFTER_STOP_FUNCTION_ALTERNATE_CONTEXT_KEY);
        }
        else { contextLifecycle.afterStop(camelContext, registry); }
    }

    @Override
    public void beforeAddRoutes(ServletCamelContext camelContext, JndiRegistry registry) throws Exception {
        boolean alternateContextFunctionMapContainsKey =
                alternateContextFunctionMap.containsKey(BEFORE_ADD_ROUTES_FUNCTION_ALTERNATE_CONTEXT_KEY);

        if (inAlternateContext && alternateContextFunctionMapContainsKey) {
            callAlternateContextFunction(camelContext, registry, BEFORE_ADD_ROUTES_FUNCTION_ALTERNATE_CONTEXT_KEY);
        }
        else { contextLifecycle.beforeAddRoutes(camelContext, registry); }
    }

    @Override
    public void afterAddRoutes(ServletCamelContext camelContext, JndiRegistry registry) throws Exception {
        boolean alternateContextFunctionMapContainsKey =
                alternateContextFunctionMap.containsKey(AFTER_ADD_ROUTES_FUNCTION_ALTERNATE_CONTEXT_KEY);

        if (inAlternateContext && alternateContextFunctionMapContainsKey) {
            callAlternateContextFunction(camelContext, registry, AFTER_ADD_ROUTES_FUNCTION_ALTERNATE_CONTEXT_KEY);
        }
        else { contextLifecycle.afterAddRoutes(camelContext, registry); }
    }

}
