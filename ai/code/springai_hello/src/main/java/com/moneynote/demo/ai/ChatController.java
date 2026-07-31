package com.moneynote.demo.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;


@RestController
public class ChatController {

    private final ChatClient chatClient;

    // Spring AI 会根据你的配置自动注入 ChatClient.Builder
    public ChatController(ChatClient.Builder builder) {
        this.chatClient = builder.defaultSystem("你是一个资深Java工程师").build();
    }

    @GetMapping("/ai/chat")
    public String generate(@RequestParam(value = "message", defaultValue = "你是谁？") String message) {
        return chatClient.prompt()
                .user(message)
                .call()
                .content();
    }

    @GetMapping(value = "/ai/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> generateStream(@RequestParam(value = "message", defaultValue = "你是谁？") String message) {
        return chatClient.prompt()
                .user(message)
                .stream()
                .content();
    }

    @GetMapping(value = "/ai/ask/weather", produces = "text/html;charset=UTF-8")
    public String askWeather(String message) {
        return chatClient.prompt()
                .system("你必须调用 getWeather 函数获取天气")
                .user(message)
                .tools("getWeather")
                .call()
                .content();
    }

}
