package com.shaobingmm.wallaby.test.crawler;

import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;

import java.util.concurrent.CountDownLatch;

/**
* This example demonstrates a fully asynchronous execution of multiple HTTP
* exchanges where the result of an individual operation is reported using a
* callback interface.
*/
public class AsyncClientHttpExchangeFutureCallback {
    private String username;
    private int    age;

    public static void main(final String[] args) throws Exception {
        RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(3000)
            .setConnectTimeout(3000).build();

        CloseableHttpAsyncClient httpclient = HttpAsyncClients.custom()
            .setDefaultRequestConfig(requestConfig).build();
        try {
            httpclient.start();
            final HttpGet[] requests = new HttpGet[] { new HttpGet("http://www.apache.org/"),
                                                       new HttpGet("https://www.verisign.com/"),
                                                       new HttpGet("http://www.google.com/"),
                                                       new HttpGet("http://www.baidu.com/") };
            final CountDownLatch latch = new CountDownLatch(requests.length);
            for (final HttpGet request : requests) {
                httpclient.execute(request, new FutureCallback<HttpResponse>() {
                    //无论完成还是失败都调用countDown()
                    @Override
                    public void completed(final HttpResponse response) {
                        latch.countDown();
                        System.out
                            .println(request.getRequestLine() + "->" + response.getStatusLine());
                    }

                    @Override
                    public void failed(final Exception ex) {
                        latch.countDown();
                        System.out.println(request.getRequestLine() + "->" + ex);
                    }

                    @Override
                    public void cancelled() {
                        latch.countDown();
                        System.out.println(request.getRequestLine() + " cancelled");
                    }
                });
            }
            latch.await();
            System.out.println("Shutting down");
        } finally {
            httpclient.close();
        }
        System.out.println("Done");
    }
}