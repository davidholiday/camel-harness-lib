package com.davidholiday.camel.harness.context;


import org.apache.camel.component.servletlistener.ServletCamelContext;

import org.apache.camel.impl.JndiRegistry;


/**
 * this wraps functions intended for use by the AppContextLifecycleHarness when the app is running in a test
 * context. by wrapping function in an implementor of this interface we can effectively treat a function as a first
 * class citizen.
 */
public interface AppContextLifecycleFunctionInterface {
    void call(ServletCamelContext camelContext, JndiRegistry registry);
}
