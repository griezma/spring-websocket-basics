package com.example.springplainwebsocket;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.time.LocalDateTime;

@Configuration
@EnableWebSocketMessageBroker
class WsMessageBrokerConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-stomp")
                .setAllowedOrigins("http://localhost:3000")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/messaging");
        registry.enableSimpleBroker("/topic", "/queue");
    }
}

@Data
class HelloMessage {
    private String name;
}

@Data
class Greeting {
    private final String content;
}

@Slf4j
@Controller
@RequiredArgsConstructor
class HelloStompController {
    private final SimpMessagingTemplate stomp;

    @MessageMapping("/hello")   // maps to /stomp/hello
    @SendTo("/topic/greeting")
    Greeting helloMessage(HelloMessage message) throws InterruptedException {
        log.info("Message via messaging: " + message);
        Thread.sleep(1000);
        return new Greeting(message.getName());
    }

    @PostMapping("/api/hello")
    @ResponseBody
    void postHello(@RequestBody HelloMessage message) throws InterruptedException {
        log.info("Posted to /api/broker-hello: " + message);
        Thread.sleep(1000);
        stomp.convertAndSend("/topic/greeting", new Greeting(message.getName()));
    }
}
