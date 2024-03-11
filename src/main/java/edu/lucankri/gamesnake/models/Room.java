package edu.lucankri.gamesnake.models;

import java.util.concurrent.ConcurrentLinkedDeque;

public interface Room {
    void resize(int width, int height, int amountFood, int intervalMs, boolean walls);
    void restartSnake(Snake snake);
    Snake addSnakeRoom();
    void clearPointSnake(Snake snake);
    void deleteSnake(Snake snake);
    ConcurrentLinkedDeque<Snake> getSnakes();

    String getId();

    void setFrameAction(Runnable frameAction);
    void stopFrameRater();
    Food getFood();
    int getWidth();
    int getHeight();
    int getIntervalMs();
    boolean getWalls();
}