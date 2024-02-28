package edu.lucankri.gamesnake.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedDeque;

public class Snake {
    public enum Direction {
        UP, DOWN, LEFT, RIGHT, GAME_START
    }
    private Direction directionFuture = Direction.GAME_START;
    @JsonProperty("direction")
    private Direction directionCurrent = Direction.GAME_START;
    @JsonProperty("score")
    private int score = 0;
    @JsonProperty("points")
    private final ConcurrentLinkedDeque<Point> snake = new ConcurrentLinkedDeque<>();

    private boolean isOpposite(Direction direction) {
        return direction == Direction.UP && this.directionCurrent == Direction.DOWN ||
                direction == Direction.DOWN && this.directionCurrent == Direction.UP ||
                direction == Direction.LEFT && this.directionCurrent == Direction.RIGHT ||
                direction == Direction.RIGHT && this.directionCurrent == Direction.LEFT;
    }

    public Snake() {}

    public Snake(Point freeCells) {
        placeRoom(freeCells);
    }

    public void clearPoint() {
        snake.clear();
    }

    public void placeRoom(Point freeCell) {
        if (snake.isEmpty() && freeCell != null) {
            directionCurrent = Direction.GAME_START;
            directionFuture = Direction.GAME_START;
            snake.offerLast(freeCell);
            score = 0;
        }
    }

    public Point peekMove() {
        if (!snake.isEmpty() && this.directionFuture != Direction.GAME_START) {
            directionCurrent = directionFuture;
            return this.snake.peekFirst().move(this.directionFuture.name());
        }
        return null;
    }

    public void move(boolean grow) {
        snake.offerFirst(this.snake.peekFirst().move(this.directionCurrent.name()));
        if (grow) {
            ++score;
        } else {
            this.snake.pollLast();
        }
    }

    public void setDirection(String directionFuture) {
        try {
            Direction dir = Direction.valueOf(directionFuture);
            if (!isOpposite(dir)) {
                this.directionFuture = dir;
            }
        } catch (IllegalArgumentException ignore) {}
    }

    public ConcurrentLinkedDeque<Point> getSnake() {
        return snake;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Snake snake1 = (Snake) o;
        return score == snake1.score && directionCurrent == snake1.directionCurrent && Objects.equals(snake, snake1.snake);
    }

    @Override
    public int hashCode() {
        return Objects.hash(directionCurrent, score, snake);
    }
}
