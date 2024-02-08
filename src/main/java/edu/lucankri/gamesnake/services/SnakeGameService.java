package edu.lucankri.gamesnake.services;

import edu.lucankri.gamesnake.game.SnakeGame;

import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;

public interface SnakeGameService {
    String initialization();
    boolean moveSnake(String playerId, SnakeGame.Direction direction);
    void deleteSnakeGame(String playerId);
    ConcurrentLinkedDeque<SnakeGame.Point> getSnakeCoordinates(String playerId);
    ConcurrentLinkedDeque<SnakeGame.Point> getFoodCoordinates(String playerId);
    int getScore(String playerId);
    Map<String, Integer> getSizeBoard(String playerId);
    String getDirection(String playerId);
    Map<String, SnakeGame> getGames();
}
