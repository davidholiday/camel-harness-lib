package com.davidholiday.camel.harness.config;


import org.apache.commons.configuration.event.ConfigurationEvent;
import org.apache.commons.configuration.event.ConfigurationListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


/**
 * listens for changes to archaius app properties and tells the camelcontext to restart the routes listed in
 *
 * see:
 * http://camel.apache.org/graceful-shutdown.html
 * http://commons.apache.org/proper/commons-configuration/userguide/howto_events.html
 *
 * note:
 * Archaius allows us to associate callbacks with each DynamicProperty that will fire when the property changes. Doing
 * things this way so as to allow us to respond when one of a *collection* of properties changes w/o having to
 * add callbacks everywhere
 *
 */
public class ConfigurationChangeListener implements ConfigurationListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationChangeListener.class);

    // so we can tell when the content of a configuration value has changed as opposed to something else that might've
    // triggered the ConfigurationEvent (like replacing a properties file with an identical clone)
    private final Map<String, String> propertyValueStateStore = new HashMap<>();

    // map of property names linked to a given runnable. when value associated with property name changes we fire
    // off the runnable
    private final Map<List<String>, Runnable> propertiesNamesToRunnableMap;


    /**
     *
     * @param propertiesNamesToRunnableMap
     */
    public ConfigurationChangeListener(Map<List<String>, Runnable> propertiesNamesToRunnableMap) {
        this.propertiesNamesToRunnableMap = propertiesNamesToRunnableMap;
    }


    /**
     *
     * @param event
     */
    @Override
    public void configurationChanged(ConfigurationEvent event){
        Optional<List<Runnable>> runnableListOptional = getRunnableListForPropertyName(event.getPropertyName());

        // store previous state of property so we can later check if it's actually changed
        if (event.isBeforeUpdate() && runnableListOptional.isPresent()) {
            String currentPropertyValue = (String)event.getPropertyValue();
            propertyValueStateStore.put(event.getPropertyName(), currentPropertyValue);
        }
        // now that the property has updated itself check previous against current and fire runnable if property value
        // has changed
        else if (event.isBeforeUpdate() == false && runnableListOptional.isPresent()) {
            String currentPropertyValue = (String)event.getPropertyValue();
            String previousPropertyValue = propertyValueStateStore.get(event.getPropertyName());
            List<Runnable> runnableList = runnableListOptional.get();

            // TODO it's theoretically possible to fire off multiple runnables that end up creating a race condition if
            // TODO   those runnables are trying to cycle the same routes. in future it might be beneficial to put
            // TODO   some kind of check for this
            if (currentPropertyValue.equals(previousPropertyValue) == false) {
                runnableList.stream().forEach(runnable -> runnable.run());
            }

        }

    }


    /**
     *
     * @param propertyName
     * @return
     */
    private Optional<List<Runnable>> getRunnableListForPropertyName(String propertyName) {
        List<Runnable> runnableList = new ArrayList<>();

        for (List<String> propertyNamesList : propertiesNamesToRunnableMap.keySet()) {
            if (propertyNamesList.contains(propertyName)) {
                Runnable runnable = propertiesNamesToRunnableMap.get(propertyNamesList);
                runnableList.add(runnable);
            }
        }

        Optional<List<Runnable>> returnVal = runnableList.isEmpty() ? Optional.empty() : Optional.of(runnableList);
        return returnVal;
    }


}
