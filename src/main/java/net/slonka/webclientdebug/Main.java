package net.slonka.webclientdebug;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.netty.channel.DefaultChannelConfig;
import io.netty.channel.local.LocalChannel;
import org.springframework.core.ResolvableType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.codec.multipart.MultipartHttpMessageReader;
import org.springframework.http.codec.multipart.Part;
import org.springframework.http.codec.multipart.SynchronossPartHttpMessageReader;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static io.netty.handler.codec.http.HttpHeaders.Values.APPLICATION_JSON;

public class Main {
    public static final String HOST = "localhost";
    public static final String FOOS_URL = "/foos";
    public static final String CONTENT_TYPE = "Content-type";
    public static WireMockServer wireMockServer;
    public static WireMock wireMock;
    public static String MY_URL, s1, s2;

    public static void main(String[] args) {
        new Main().runMain();
    }

    public static void set_up() {
        wireMockServer = new WireMockServer(options().dynamicPort());
        wireMockServer.start();
        wireMock = new WireMock(HOST, wireMockServer.port());
        MY_URL = String.format("http://%s:%d/%s", HOST, wireMockServer.port(), FOOS_URL);

        ResponseDefinitionBuilder responseBuilder = aResponse()
                .withStatus(200)
                .withBody("[]")
                .withHeader(CONTENT_TYPE, APPLICATION_JSON);

        wireMock.register(get(FOOS_URL).willReturn(responseBuilder));
    }

    public void runMain() {
        set_up();

        WebClient webClient = WebClient.create(MY_URL);

        preload();

        s1 = easy_to_find_method_name_123(webClient);

        s2 = easy_to_find_method_name_123(webClient);

        System.out.println("s1+s2" + s1 + s2);
        System.exit(0);
    }

    static public void preload() {
        SynchronossPartHttpMessageReader partReader = new SynchronossPartHttpMessageReader();
        MultipartHttpMessageReader multipartHttpMessageReader = new MultipartHttpMessageReader(partReader);

        LocalChannel localChannel = new LocalChannel();

        DefaultChannelConfig defaultChannelConfig = new DefaultChannelConfig(localChannel);

        UriComponents uriComponents = UriComponentsBuilder.newInstance().build();

        BodyExtractors bodyExtractors = new BodyExtractors() {
            @Override
            public String toString() {
                return super.toString();
            }
        };

        ResolvableType a =
                ResolvableType.forClassWithGenerics(MultiValueMap.class, String.class, Part.class);
        ResolvableType b =
                ResolvableType.forClassWithGenerics(MultiValueMap.class, String.class, String.class);

        BodyInserters bodyInserters = new BodyInserters() {
            @Override
            public String toString() {
                return super.toString();
            }
        };

        HttpHeaders httpHeaders = new HttpHeaders();

        System.out.print(a.toString() + b.toString());
        System.out.print(multipartHttpMessageReader.toString());
        System.out.print(localChannel.toString());
        System.out.print(uriComponents.toString());
        System.out.print(bodyExtractors.toString());
        System.out.print(bodyInserters.toString());
        System.out.print(httpHeaders.toString());
        System.out.println(defaultChannelConfig.toString());
    }


    static public String easy_to_find_method_name_123(WebClient webClient) {
        System.out.println("<easy_to_find_method_name_123>");

        long before = System.nanoTime();
        String s = webClient.get().retrieve().bodyToMono(String.class).block();
        long after = System.nanoTime();

        System.out.println("</easy_to_find_method_name_123>");
        System.out.println(String.format("block(): %d ms", (after - before) / 1_000_000));
        System.out.println("--");
        return s;
    }
}
