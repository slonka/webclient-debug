package net.slonka.webclientdebug;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import static net.slonka.webclientdebug.Main.*;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.netty.handler.codec.http.HttpHeaders.Values.APPLICATION_JSON;

public class WebClientFirstRequestTest {
    @Before
    public void setUp() {
        set_up();
    }

    @Test
    public void webclientRequest() {
        String s1, s2;

        WebClient webClient = WebClient.create(MY_URL);

        s1 = easy_to_find_method_name_123(webClient);

        s2 = easy_to_find_method_name_123(webClient);

        System.out.println(s1 + s2);

        wireMock.verifyThat(getRequestedFor(urlEqualTo(FOOS_URL)));
    }

    @Test
    public void webclientRequestFromDifferentInstances() {
        String s1, s2, s3, s4;

        WebClient webClient1 = WebClient.create(MY_URL);
        WebClient webClient2 = WebClient.create(MY_URL);

        s1 = easy_to_find_method_name_123(webClient1);
        s2 = easy_to_find_method_name_123(webClient1);

        s3 = easy_to_find_method_name_123(webClient2);
        s4 = easy_to_find_method_name_123(webClient2);

        System.out.println(s1 + s2 + s3 + s4);

        wireMock.verifyThat(getRequestedFor(urlEqualTo(FOOS_URL)));
    }

    @Test
    public void webclientRequestWithPreload() {
        String s1, s2;

        preload();

        WebClient webClient = WebClient.create(MY_URL);

        s1 = easy_to_find_method_name_123(webClient);

        s2 = easy_to_find_method_name_123(webClient);

        System.out.println(s1 + s2);

        wireMock.verifyThat(getRequestedFor(urlEqualTo(FOOS_URL)));
    }

    @Test
    public void restTemplateRequest() {
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