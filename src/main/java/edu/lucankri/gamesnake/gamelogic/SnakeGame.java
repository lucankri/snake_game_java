package edu.lucankri.gamesnake.gamelogic;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

public class SnakeGame {
    public enum Direction {
        UP, DOWN, LEFT, RIGHT, GAME_START
    }

    public static class Point {
        @JsonProperty("x")
        int x;
        @JsonProperty("y")
        int y;

        Point(int x, int y) {
            this.x = x;
            this.y = y;
        }

        Point move(Direction direction) {
            switch (direction) {
                case UP:
                    return new Point(x, y - 1);
                case DOWN:
                    return new Point(x, y + 1);
                case LEFT:
                    return new Point(x - 1, y);
                case RIGHT:
                    return new Point(x + 1, y);
                default:
                    return new Point(x, y);
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Point)) return false;
            Point other = (Point) obj;
            return x == other.x && y == other.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }

    private int width;
    private int height;
    private int sizeFoods;
    private final ConcurrentLinkedDeque<Point> snake;
    private final ConcurrentLinkedDeque<Point> foods;
    private ConcurrentLinkedDeque<Point> freeCells;
    private int score;
    private Direction direction;

    public SnakeGame(int width, int height, int sizeFoods) {
        this.width = width;
        this.height = height;
        this.sizeFoods = sizeFoods;
        this.snake = new ConcurrentLinkedDeque<>();
        this.foods = new ConcurrentLinkedDeque<>();
        initGame();
    }

    public void resize(int width, int height, int sizeFoods) {
        this.width = width;
        this.height = height;
        this.sizeFoods = sizeFoods;
        initGame();
    }

    public void initGame() {
        this.score = 0;
        this.direction = Direction.GAME_START;
        snake.clear();
        foods.clear();
        snake.offerLast(new Point(width / 2, height / 2));
        createFreeCells();
        placeFood();
    }

    private void placeFood() {
        for (int i = 0; i < sizeFoods; ++i) {
            foods.offerLast(freeCells.pollFirst());
        }
    }

    private void createFreeCells() {
        LinkedList<Point> list = new LinkedList<>();
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                Point point = new Point(x, y);
                if (!snake.contains(point))
                    list.add(point);
            }
        }
        Collections.shuffle(list);
        freeCells = new ConcurrentLinkedDeque<>(list);
    }

    private boolean isOpposite(Direction direction) {
        return direction == Direction.UP && this.direction == Direction.DOWN ||
                direction == Direction.DOWN && this.direction == Direction.UP ||
                direction == Direction.LEFT && this.direction == Direction.RIGHT ||
                direction == Direction.RIGHT && this.direction == Direction.LEFT;
    }

    public boolean move(Direction direction) {
        if (direction != Direction.GAME_START) {
            if (freeCells.isEmpty()) {
                createFreeCells();
            }
            Point head;
            if (isOpposite(direction)) {
                head = snake.peekFirst().move(this.direction);
            } else {
                head = snake.peekFirst().move(direction);
                this.direction = direction;
            }
            if (head.x < 0 || head.x >= width || head.y < 0 || head.y >= height) {
                return false; // Game over: Out of bounds
            }

            for (Point part : snake) {
                if (part.equals(head)) {
                    return false; // Game over: Collision with snake
                }
            }

            snake.offerFirst(head);
            if (foods.contains(head)) {
                foods.removeFirstOccurrence(head);
                ++score;
                foods.offerLast(freeCells.pollFirst());
            } else {
                snake.pollLast();
            }
            freeCells.removeFirstOccurrence(head);
        }
        return true;
    }

    public ConcurrentLinkedDeque<Point> getSnake() {
        return snake;
    }

    public ConcurrentLinkedDeque<Point> getFoods() {
        return foods;
    }

    public int getScore() {
        return score;
    }

    public Map<String, Integer> getSizeWidthAndHeight() {
        Map<String, Integer> result = new HashMap<>();
        result.put("width", width);
        result.put("height", height);
        return result;
    }

    public Direction getDirection() {
        return direction;
    }
}
