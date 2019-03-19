package com.davidholiday.camel.harness.test.context.mocks;


/**
 * dummy bean for testing
 */
public class BarBean implements Bean {

    public static String GET_VALUE = "bar";

    public String get() {
        return GET_VALUE;
    }

}
