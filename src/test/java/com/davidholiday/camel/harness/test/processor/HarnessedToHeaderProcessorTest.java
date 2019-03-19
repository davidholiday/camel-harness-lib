package com.davidholiday.camel.harness.test.processor;


import com.davidholiday.camel.harness.test.context.lifecycle.HarnessedAppContextLifecycle;
import com.davidholiday.camel.harness.testing.ProcessorTestHarness;
import com.davidholiday.camel.harness.context.AppContextLifecycleHarness;
import com.davidholiday.camel.harness.test.processor.mocks.ToHeaderProcessor;

import org.apache.camel.CamelExecutionException;

import org.junit.Test;

import java.util.Optional;


/**
 * example harnessed processor test. we inject the processor we want to test and the contextlifecycle object to use for
 * this set of tests into test harness the harness' common constructor. note that the context lifecycle object in the
 * context of a test is used only to populate the registry as the camel context doesn't exist in the same way in
 * test as it does in prod. if any pre/post processing behavior necessary it can be injected by using the Harness'
 * setProcessorFor{Pre/Post}MockEndpoint() methods.
 */
public class HarnessedToHeaderProcessorTest extends ProcessorTestHarness {

    private static final ToHeaderProcessor TO_HEADER_PROCESSOR = new ToHeaderProcessor();

    private static final AppContextLifecycleHarness HARNESSED_CAMEL_CONTEXT_LIFECYCLE =
            new HarnessedAppContextLifecycle(true);


    public HarnessedToHeaderProcessorTest() {
        super(TO_HEADER_PROCESSOR, HARNESSED_CAMEL_CONTEXT_LIFECYCLE);
    }


    @Test
    public void processorTestHappyPath() throws InterruptedException {

        String messageBodyIn = "foo";
        setExpectedOutputHeader(ToHeaderProcessor.TO_HEADER_EXCHANGE_PROPERTY_KEY, messageBodyIn);
        setExpectedOutputBody("bar");

        runTest(Optional.empty(), Optional.of(messageBodyIn));
    }

    @Test(expected = CamelExecutionException.class)
    public void processorTestEmptyMessage() throws InterruptedException {
        String messageBodyIn = "";
        runTest(Optional.empty(), Optional.of(messageBodyIn));
    }


}