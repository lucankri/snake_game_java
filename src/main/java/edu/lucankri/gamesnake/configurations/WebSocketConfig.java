package edu.lucankri.gamesnake.configurations;

import edu.lucankri.gamesnake.services.GameService;
import edu.lucankri.gamesnake.services.GameServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@ComponentScan("edu.lucankri.gamesnake")
@PropertySource("classpath:application.properties")
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    @Bean
    public GameServiceImpl getGameServiceBean() {
        return new GameServiceImpl();
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(getGameServiceBean(), "/game-ws").setAllowedOrigins("*");
    }
}
