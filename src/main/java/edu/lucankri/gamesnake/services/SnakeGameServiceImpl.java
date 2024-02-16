package edu.lucankri.gamesnake.services;

import edu.lucankri.gamesnake.gamelogic.SnakeGame;

import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;

public class SnakeGameServiceImpl implements SnakeGameService {

    private SnakeGame snakeGame;

    @Override
    public void initialization(int width, int height, int sizeFoods) {
        snakeGame = new SnakeGame(width, height, sizeFoods);
    }

    @Override
    public boolean moveSnake(String direction) {
        return snakeGame.move(SnakeGame.Direction.valueOf(direction));
    }

    @Override
    public ConcurrentLinkedDeque<SnakeGame.Point> getSnakeCoordinates() {
        return snakeGame.getSnake();
    }

    @Override
    public ConcurrentLinkedDeque<SnakeGame.Point> getFoodCoordinates() {
        return snakeGame.getFoods();
    }

    @Override
    public int getScore() {
        return snakeGame.getScore();
    }


    @Override
    public String getDirection() {
        return snakeGame.getDirection().toString();
    }

    @Override
    public void restart() {
        snakeGame.initGame();
    }

    @Override
    public void resize(int width, int height, int sizeFoods) {
        snakeGame.resize(width, height, sizeFoods);
    }
}
