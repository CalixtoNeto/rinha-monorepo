package com.rinhadeloadbalancer;

import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.proxy.LoadBalancingProxyClient;
import io.undertow.server.handlers.proxy.ProxyHandler;

import java.net.URI;
import java.net.URISyntaxException;

public class LoadBalancerApplication {
    public static void main(String[] args) throws URISyntaxException {
        String backend1 = System.getenv("BACKEND1_URL") != null ? System.getenv("BACKEND1_URL") : "http://backend1:8080";
        String backend2 = System.getenv("BACKEND2_URL") != null ? System.getenv("BACKEND2_URL") : "http://backend2:8080";

        LoadBalancingProxyClient proxy = new LoadBalancingProxyClient()
                .addHost(new URI(backend1))
                .addHost(new URI(backend2))
                .setConnectionsPerThread(20);

        // Create the proxy handler using the configured proxy client
        HttpHandler proxyHandler = new ProxyHandler(proxy);

        Undertow server = Undertow.builder()
                .addHttpListener(80, "0.0.0.0")
                .setHandler(proxyHandler)
                .build();
        server.start();
    }
}
