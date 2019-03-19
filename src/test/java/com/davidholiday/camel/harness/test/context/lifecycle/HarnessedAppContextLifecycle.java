package com.davidholiday.camel.harness.test.context.lifecycle;


import com.davidholiday.camel.harness.context.AppContextLifecycleHarness;
import com.davidholiday.camel.harness.context.AppContextLifecycleFunctionInterface;

import com.davidholiday.camel.harness.test.context.mocks.BarBean;

import org.apache.camel.component.servletlistener.ServletCamelContext;
import org.apache.camel.impl.JndiRegistry;

import javax.naming.NamingException;

import java.util.HashMap;
import java.util.Map;


/**
 * harnessed context lifecyle object to run some basic tests against FOO and BAR beans. depending on boolean passed to
 * constructor, either the FOO or BAR bean will be registered against name AppContextLifecycleInterface.TEST_BEAN_NAME
 */
public class HarnessedAppContextLifecycle extends AppContextLifecycleHarness {

    // function to override default beforeStart() behavior in underlying ContextLifecycle object
    private static AppContextLifecycleFunctionInterface beforeStartFunction =
            (ServletCamelContext contextLifecycle, JndiRegistry registry) -> {

        try {
            registry.getContext().unbind(AppContextLifecycle.TEST_BEAN_NAME);
            registry.getContext().bind(AppContextLifecycle.TEST_BEAN_NAME, new BarBean());
        } catch (NamingException e) {
            LOGGER.error("caught naming exception - the bean we are trying to rebind should already be there", e);
            throw new AssertionError();
        }
    };

    // map to be passed to harness constructor keying the alternateContextFunctions to names defining when in the
    // lifecycle they should be invoked (assuming of course the flag for alternate context is set)
    private static final Map<String, AppContextLifecycleFunctionInterface> alternateContextFunctionName =
            new HashMap<String, AppContextLifecycleFunctionInterface>() {{
                put(BEFORE_START_FUNCTION_ALTERNATE_CONTEXT_KEY, beforeStartFunction);
            }};


    public HarnessedAppContextLifecycle(boolean inTestContext) {
        super(new AppContextLifecycle(), inTestContext, alternateContextFunctionName);
    }

}
