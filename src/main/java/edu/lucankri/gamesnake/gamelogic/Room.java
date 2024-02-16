package edu.lucankri.gamesnake.gamelogic;

import java.util.Collection;
import java.util.Set;

public interface Room {

    enum Direction {
        UP, DOWN, LEFT, RIGHT, GAME_START
    }

    interface Snake {
        void move(Direction direction);
    }

    Set<Snake> getSnakes();

    class Point {}
    Set<Point> getApples();

    Point addApple();

}
