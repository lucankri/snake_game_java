package edu.lucankri.gamesnake.services;

import edu.lucankri.gamesnake.game.SnakeGame;

import java.util.List;
import java.util.Map;

public interface SnakeGameService {
    String initialization();
    boolean moveSnake(String playerId, SnakeGame.Direction direction);
    void deleteSnakeGame(String playerId);
    List<SnakeGame.Point> getSnakeCoordinates(String playerId);
    List<SnakeGame.Point> getFoodCoordinates(String playerId);
    int getScore(String playerId);
    Map<String, Integer> getSizeBoard(String playerId);
    String getDirection(String playerId);
}
