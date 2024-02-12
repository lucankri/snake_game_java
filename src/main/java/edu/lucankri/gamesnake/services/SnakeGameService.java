package edu.lucankri.gamesnake.services;

import edu.lucankri.gamesnake.gamelogic.SnakeGame;

import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;

public interface SnakeGameService {
    void initialization(int width, int height, int sizeFoods);
    boolean moveSnake(SnakeGame.Direction direction);
    ConcurrentLinkedDeque<SnakeGame.Point> getSnakeCoordinates();
    ConcurrentLinkedDeque<SnakeGame.Point> getFoodCoordinates();
    int getScore();
    Map<String, Integer> getSizeBoard();
    String getDirection();
    void restart();
    void resize(int width, int height, int sizeFoods);
}
