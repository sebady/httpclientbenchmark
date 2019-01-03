package com.ss.benchmark.httpclient.rxnetty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelOption;
import io.reactivex.netty.client.Host;
import io.reactivex.netty.client.pool.PoolConfig;
import io.reactivex.netty.client.pool.SingleHostPoolingProviderFactory;
import io.reactivex.netty.protocol.http.client.HttpClient;
import io.reactivex.netty.protocol.http.client.HttpClientRequest;
import rx.Observable;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by ssrinivasa on 1/2/19.
 */
public class RxNettyApp {

    public static void main(String[] args) throws InterruptedException {

        int executions = 10_000;
        // If we use a ConnectionProvider with 200, the app 'hangs' my Macbook.  If I wait long enough,
        // think_ the application will finish.
        int poolSize = 200;
        int connReqTimeout = 10_000; // in ms
        int connTimeout = 2_000;     // in ms
        int readTimeout = 2_000;     // in ms

        //Bounded connection pool
        PoolConfig poolConfig = new PoolConfig()
                .maxConnections(poolSize).maxIdleTimeoutMillis(60000);

        HttpClient client = HttpClient
                .newClient(SingleHostPoolingProviderFactory.<ByteBuf, ByteBuf>create(poolConfig), Observable
                        .just(new Host(new InetSocketAddress("localhost", 8080))))
                .readTimeOut(readTimeout, TimeUnit.MILLISECONDS)
                .channelOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, connTimeout);


        CountDownLatch latch = new CountDownLatch(executions);
        HttpClientRequest<ByteBuf, ByteBuf> request = client.createGet("/echodelayserv/echo/long");

        for (int i = 0; i < executions; i++) {
            final StringBuffer content = new StringBuffer();
            final int ctr = i;
            request
                    .flatMap(resp -> {
                        if(resp.getStatus().code() != 200) {
                            throw new IllegalStateException("Unexpected response code: " + resp.getStatus().code());
                        }
                        return resp.getContent();
                    })
                    .map( byteBuf -> byteBuf.toString(Charset.defaultCharset()))
                    .collect(() -> {return content;}, (StringBuffer sb, String s) -> {sb.append(s);})
                    .doOnTerminate( () -> {if(latch != null) latch.countDown();})
                    .subscribe(s -> System.out.println((ctr + 1) + ". " + s.length()));
        }
        latch.await();
    }
}
