package edu.lucankri.gamesnake.services;

import edu.lucankri.gamesnake.gamelogic.SnakeGame;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;

@Component
public class SnakeGameServiceImpl implements SnakeGameService {

    private SnakeGame snakeGame;

    @Override
    public void initialization(int width, int height, int sizeFoods) {
        snakeGame = new SnakeGame(width, height, sizeFoods);
    }

    @Override
    public boolean moveSnake(SnakeGame.Direction direction) {
        return snakeGame.move(direction);
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
    public Map<String, Integer> getSizeBoard() {
        return snakeGame.getSizeWidthAndHeight();
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
