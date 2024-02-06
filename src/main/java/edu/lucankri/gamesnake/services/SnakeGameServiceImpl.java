package edu.lucankri.gamesnake.services;

import edu.lucankri.gamesnake.game.SnakeGame;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class SnakeGameServiceImpl implements SnakeGameService {
    private final ConcurrentMap<String, SnakeGame> games = new ConcurrentHashMap<>();
    private final PlayerService playerService = new PlayerServiceImpl();

    public void startGame(String playerId) {
        // Логика начала игры
    }

    public void moveSnake(String playerId, SnakeGame.Direction direction) {
        // Логика перемещения змейки
    }

    public void processCollision(String playerId) {
        // Логика обработки столкновений
    }

    private SnakeGame getOrCreateGame() {
        return games.computeIfAbsent(playerService.generatePlayerId(), k -> new SnakeGame());
    }

}
