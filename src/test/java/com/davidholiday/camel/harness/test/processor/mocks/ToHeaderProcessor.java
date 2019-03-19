package com.davidholiday.camel.harness.test.processor.mocks;


import com.davidholiday.camel.harness.test.context.lifecycle.AppContextLifecycle;
import com.davidholiday.camel.harness.test.context.mocks.Bean;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * stuffs the contents of the in-message into the in-message-header
 */
public class ToHeaderProcessor implements Processor {

    private static Logger LOGGER = LoggerFactory.getLogger(ToHeaderProcessor.class);

    public static String TO_HEADER_EXCHANGE_PROPERTY_KEY = "toHeaderExchangePropertyKey";

    /**
     *
     * @param exchange
     * @throws Exception
     */
    public void process(Exchange exchange) throws IllegalArgumentException {
        String inMessageBody = (String)exchange.getIn().getBody();

        if (inMessageBody.isEmpty()) {
            LOGGER.error("message body can not be empty");
            throw new IllegalArgumentException();
        }

        LOGGER.debug("storing <{}, {}> in the in-message header cache", TO_HEADER_EXCHANGE_PROPERTY_KEY, inMessageBody);

        exchange.getIn()
                .setHeader(TO_HEADER_EXCHANGE_PROPERTY_KEY, inMessageBody);

        Bean testBean = (Bean)exchange.getContext()
                                      .getRegistry()
                                      .lookupByName(AppContextLifecycle.TEST_BEAN_NAME);

        exchange.getIn()
                .setBody(testBean.get());

    }

}
