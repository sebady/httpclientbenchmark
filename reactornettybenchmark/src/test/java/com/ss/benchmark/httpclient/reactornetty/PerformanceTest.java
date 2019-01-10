package com.ss.benchmark.httpclient.reactornetty;

import com.ss.benchmark.httpclient.HttpClientEngine;
import com.ss.benchmark.httpclient.BasePerformanceTest;
import org.testng.annotations.Test;

/**
 * @author sharath.srinivasa
 */
@Test(groups = "performance")
public class PerformanceTest extends BasePerformanceTest {

    @Override
    protected HttpClientEngine getClient() {
        return new ReactorNettyEngine();
    }
}

