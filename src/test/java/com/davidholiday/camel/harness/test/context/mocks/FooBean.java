package com.davidholiday.camel.harness.test.context.mocks;


/**
 * dummy bean for testing
 */
public class FooBean implements Bean {

    public static String GET_VALUE = "foo";

    public String get() {
        return GET_VALUE;
    }

}
