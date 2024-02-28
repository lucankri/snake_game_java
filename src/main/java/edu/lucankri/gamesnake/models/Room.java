package edu.lucankri.gamesnake.models;

import java.util.concurrent.ConcurrentLinkedDeque;

public interface Room {
    void resize(int width, int height, int amountFood, int frameIntervalMs);
    void restartSnake(Snake snake);
    Snake addSnakeRoom();
    void clearPointSnake(Snake snake);
    void deleteSnake(Snake snake);
    ConcurrentLinkedDeque<Snake> getSnakes();

    String getId();

    void setFrameAction(Runnable frameAction);

    Food getFood();

    void stopFrameRater();
    int getWidth();
    int getHeight();
}