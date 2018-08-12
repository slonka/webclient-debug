package com.zeroturnaround.callspy;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static io.netty.handler.codec.http.HttpHeaders.Values.APPLICATION_JSON;

public class Main {
    public static final String HOST = "localhost";
    public static final String FOOS_URL = "/foos";
    public static final String CONTENT_TYPE = "Content-type";
    public static WireMockServer wireMockServer;
    public static WireMock wireMock;
    public static String MY_URL;

    public static void main(String[] args) {
        new Main().runMain();
    }

    public void runMain() {
        wireMockServer = new WireMockServer(options().dynamicPort());
        wireMockServer.start();
        wireMock = new WireMock(HOST, wireMockServer.port());
        MY_URL = String.format("http://%s:%d/%s", HOST, wireMockServer.port(), FOOS_URL);

        ResponseDefinitionBuilder responseBuilder = aResponse()
                .withStatus(200)
                .withBody("[]")
                .withHeader(CONTENT_TYPE, APPLICATION_JSON);

        wireMock.register(get(FOOS_URL).willReturn(responseBuilder));

        boolean a = true;
        String s1 = "", s2 = "";
        WebClient webClient = WebClient.create(MY_URL);

        s1 = easy_to_find_method_name_123(webClient);

//    s2 = easy_to_find_method_name_123(webClient);

        System.out.println("s1+s2" + s1 + s2);
        System.exit(0);
    }

    public String easy_to_find_method_name_123(WebClient webClient) {
        long before = System.nanoTime();
        WebClient.RequestHeadersUriSpec<?> get = webClient.get();
        long after = System.nanoTime();
        System.out.println(String.format("webClinet.get(): %d ms", (after - before) / 1_000_000));

        before = System.nanoTime();
        WebClient.ResponseSpec retrieve = get.retrieve();
        after = System.nanoTime();
        System.out.println(String.format("retrieve(): %d ms", (after - before) / 1_000_000));

        before = System.nanoTime();
        Mono<String> stringMono = retrieve.bodyToMono(String.class);
        after = System.nanoTime();
        System.out.println(String.format("bodyToMono(): %d ms", (after - before) / 1_000_000));

//        before = System.nanoTime();
//        CompletableFuture<String> stringCompletableFuture = stringMono.toFuture();
//        after = System.nanoTime();
//        System.out.println(String.format("toFuture(): %d ms", (after - before) / 1_000_000));

        before = System.nanoTime();
//        String s = stringCompletableFuture.get();
        String s = stringMono.block();
        after = System.nanoTime();
        System.out.println(String.format("block(): %d ms", (after - before) / 1_000_000));
        System.out.println("--");
        return s;
    }
}
