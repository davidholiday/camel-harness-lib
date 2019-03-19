package com.davidholiday.camel.harness.helpers;


import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;


/**
 * wraps calls to archaius DynamicPropertyFactory in something that allows for an ordered list or property keys to be
 * tried before returning a default value
 *
 * TODO implement wrappers for more than just DynamicStringProperties?
 * TODO   supporting only strings might cover everything and create fewer headaches later than if we try to type
 * TODO   config vars read from a file
 */
public class DynamicPropertyHelpers {

    /**
     * will try to resolve property one. if it doesn't exist, will then try to resolve property two. default value
     * if neither is found is "".
     *
     * @param propertyNameOne
     * @param propertyNameTwo
     * @return
     */
    public static DynamicStringProperty getOrGetDynamicStringProperty(String propertyNameOne, String propertyNameTwo) {

        DynamicStringProperty dynamicStringProperty =
                DynamicPropertyFactory.getInstance().getStringProperty(
                        propertyNameOne,
                        ""
                );

        if (dynamicStringProperty.getValue().equals("")) {
            dynamicStringProperty = DynamicPropertyFactory.getInstance().getStringProperty(
                    propertyNameTwo,
                    ""
            );
        }

        return dynamicStringProperty;
    }

}
