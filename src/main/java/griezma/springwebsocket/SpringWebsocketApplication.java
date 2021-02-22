package griezma.springwebsocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;


@SpringBootApplication
public class SpringWebsocketApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringWebsocketApplication.class, args);
    }

}

@Slf4j
class HelloHandler extends TextWebSocketHandler {
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        log.info("handleTextMessage: " + message);
       session.sendMessage(new TextMessage("Hello " + message.getPayload()));
    }
}

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
class WebSocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(helloHandler(), "/hello")
                .setAllowedOrigins("http://localhost:3000")
                .withSockJS();
        registry.addHandler(helloHandler(), "/hello-plain")
                .setAllowedOrigins("*")
                .addInterceptors(new HttpSessionHandshakeInterceptor());
    }

    @Bean
    HelloHandler helloHandler() {
        return new HelloHandler();
    }
}
