package com.davidholiday.camel.harness.test.context.lifecycle;


import com.davidholiday.camel.harness.test.context.mocks.FooBean;

import org.apache.camel.component.servletlistener.CamelContextLifecycle;
import org.apache.camel.component.servletlistener.ServletCamelContext;

import org.apache.camel.impl.JndiRegistry;


/**
 * basic context lifecycle that registers bean(s) with JNDI
 */
public class AppContextLifecycle implements CamelContextLifecycle<JndiRegistry> {

    public static String TEST_BEAN_NAME = "testBeanName";


    @Override
    public void beforeStart(ServletCamelContext camelContext, JndiRegistry registry) throws Exception {
        registry.bind(TEST_BEAN_NAME, new FooBean());
    }

    @Override
    public void afterStart(ServletCamelContext camelContext, JndiRegistry registry) throws Exception {}

    @Override
    public void beforeStop(ServletCamelContext camelContext, JndiRegistry registry) throws Exception {}

    @Override
    public void afterStop(ServletCamelContext camelContext, JndiRegistry registry) throws Exception {}

    @Override
    public void beforeAddRoutes(ServletCamelContext camelContext, JndiRegistry registry) throws Exception {}

    @Override
    public void afterAddRoutes(ServletCamelContext camelContext, JndiRegistry registry) throws Exception {}

}
