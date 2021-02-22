package griezma.springwebsocket;


import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.broker.BrokerAvailabilityEvent;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompReactorNettyCodec;
import org.springframework.messaging.tcp.reactor.ReactorNettyTcpClient;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Slf4j
@Configuration
@EnableWebSocketMessageBroker
class WsMessageBrokerConfig implements WebSocketMessageBrokerConfigurer {
    @Value("${stomp.relay.host:simple}")
    private String relayHost;

    @Value("${stomp.relay.port:61613}")
    private int relayPort;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-stomp")
                .setAllowedOrigins("http://localhost:3000")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/messaging");
        if ("simple".equals(relayHost)) {
            log.info("Using simple broker");
            registry.enableSimpleBroker("/topic", "/queue");
        } else {
            log.info("Using broker relay> {}:{}", relayHost, relayPort);
            registry.enableStompBrokerRelay("/topic", "/queue")
                    .setRelayHost(relayHost)
                    .setRelayPort(relayPort);
        }
    }

    @Bean
    ApplicationListener<BrokerAvailabilityEvent> brokerAvailable() {
        return ev -> log.info("Broker available: " + ev);
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
