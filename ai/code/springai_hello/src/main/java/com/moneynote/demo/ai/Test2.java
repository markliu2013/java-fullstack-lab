package com.moneynote.demo.ai;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class Test2 {

    private final static String baseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1";
    private final static String apiKey = System.getenv("DASHSCOPE_API_KEY");

    public static void main(String[] args) throws InterruptedException {

        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("请先设置环境变量 DASHSCOPE_API_KEY");
            return;
        }

        WebClient client = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + apiKey
                )
                .build();

        Map<String, Object> request = Map.of(
                "model", "qwen3.8-omni-flash",
                "messages", List.of(
                        Map.of(
                                "role", "user",
                                "content", "你好，请随便输出500字，我测试使用，不需要深度思考。"
                        )
                ),
                "stream", true
        );

        CountDownLatch latch = new CountDownLatch(1);

        Flux<ServerSentEvent<String>> response = client.post()
                .uri("/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .bodyValue(request)
                .retrieve()
                .bodyToFlux(new ParameterizedTypeReference<ServerSentEvent<String>>() {
                });

        response.subscribe(
                event -> {
                    String eventType = event.event();
                    String data = event.data();
                    String id = event.id();
                    System.out.println("eventType: " + eventType);
                    System.out.println("data: " + data);
                },
                error -> {
                    System.err.println("请求异常：" + error.getMessage());
                    latch.countDown();
                },
                () -> {
                    System.out.println("流式响应结束");
                    latch.countDown();
                }
        );

        latch.await(); // 阻塞主线程，等待流式响应结束后再退出 JVM

    }

}
