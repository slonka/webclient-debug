package com.example.demo;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static io.netty.handler.codec.http.HttpHeaders.Values.APPLICATION_JSON;

public class WebClientFirstRequestTest {

    private static final String HOST = "localhost";
    private static final String FOOS_URL = "/foos";
    private static final String CONTENT_TYPE = "Content-type";
    private WireMockServer wireMockServer;
    private WireMock wireMock;
    public String MY_URL;

    @Before
    public void setUp() throws Exception {
        wireMockServer = new WireMockServer(options().dynamicPort());
        wireMockServer.start();
        wireMock = new WireMock(HOST, wireMockServer.port());
        MY_URL = String.format("http://%s:%d/%s", HOST, wireMockServer.port(), FOOS_URL);
    }

    @Test
    public void simpleRequest() throws Exception {

        ResponseDefinitionBuilder responseBuilder = aResponse()
                .withStatus(200)
                .withBody("[]")
                .withHeader(CONTENT_TYPE, APPLICATION_JSON);

        wireMock.register(get(FOOS_URL).willReturn(responseBuilder));

        boolean a = true;
        String s1 = "", s2 = "";
        WebClient webClient = WebClient.builder().build();

        s1 = getString(webClient, "http://google.com");

        s2 = getString(webClient, MY_URL);

        System.out.println(s1 + s2);

        wireMock.verifyThat(getRequestedFor(urlEqualTo(FOOS_URL)));
    }

    private String getString(WebClient webClient, String uri) throws InterruptedException, java.util.concurrent.ExecutionException {
        long before = System.nanoTime();
        WebClient.RequestHeadersSpec<?> get = webClient.get().uri(uri);
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

    @Test
    public void simpleRequest2() throws Exception {
        RestTemplate restTemplate = new RestTemplate();

        ResponseDefinitionBuilder responseBuilder = aResponse()
                .withStatus(200)
                .withBody("[]")
                .withHeader(CONTENT_TYPE, APPLICATION_JSON);

        wireMock.register(get(FOOS_URL).willReturn(responseBuilder));

        long start1 = System.nanoTime();
        String body = restTemplate.getForEntity(MY_URL, String.class).getBody();
        long end1 = System.nanoTime();

        long start2 = System.nanoTime();
        String body2 = restTemplate.getForEntity(MY_URL, String.class).getBody();
        long end2 = System.nanoTime();

        System.out.println(body + body2);

        System.out.println(String.format("First took: %s ms", ((end1 - start1) / 1_000_000)));
        System.out.println(String.format("Second took: %s ms", ((end2 - start2) / 1_000_000)));
        System.out.println(String.format("Difference in time: %s ms", ((end1 - start1) - (end2 - start2)) / 1_000_000));

        wireMock.verifyThat(getRequestedFor(urlEqualTo(FOOS_URL)));
    }
}