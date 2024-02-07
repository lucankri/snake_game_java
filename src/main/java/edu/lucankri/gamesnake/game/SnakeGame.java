package edu.lucankri.gamesnake.game;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.*;

public class SnakeGame {
    public enum Direction {
        UP, DOWN, LEFT, RIGHT, STOP
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

    private static final int WIDTH = 30;
    private static final int HEIGHT =20;
    private final List<Point> snake;
    private final List<Point> foods;
    private final Random random;
    private int score;
    private Direction direction;

    public SnakeGame() {
        this.snake = new LinkedList<>();
        this.foods = new LinkedList<>();
        this.random = new Random();
        this.score = 0;
        this.direction = Direction.STOP;
        initGame();
    }

    private void initGame() {
        snake.clear();
        snake.add(new Point(WIDTH / 2, HEIGHT / 2)); // Initial position of snake
        placeFood();
    }

    private void placeFood() {
        int x = random.nextInt(WIDTH);
        int y = random.nextInt(HEIGHT);
        foods.add(new Point(x, y));
    }

    private boolean isOpposite(Direction direction) {
        return direction == Direction.UP && this.direction == Direction.DOWN ||
                direction == Direction.DOWN && this.direction == Direction.UP ||
                direction == Direction.LEFT && this.direction == Direction.RIGHT ||
                direction == Direction.RIGHT && this.direction == Direction.LEFT;
    }

    public boolean move(Direction direction) {
        if (direction != Direction.STOP) {
            Point head;
            if (isOpposite(direction)) {
                head = snake.get(0).move(this.direction);
            } else {
                head = snake.get(0).move(direction);
                this.direction = direction;
            }
            if (head.x < 0 || head.x >= WIDTH || head.y < 0 || head.y >= HEIGHT) {
                return false; // Game over: Out of bounds
            }

            for (Point part : snake) {
                if (part.equals(head)) {
                    return false; // Game over: Collision with snake
                }
            }

            snake.add(0, head);
            if (foods.contains(head)) {
                foods.remove(head);
                ++score;
                placeFood();
            } else {
                snake.remove(snake.size() - 1);
            }
        }
        return true;
    }

    public List<Point> getSnake() {
        return snake;
    }

    public List<Point> getFoods() {
        return foods;
    }

    public int getScore() {
        return score;
    }

    public Map<String, Integer> getSizeWidthAndHeight() {
        Map<String, Integer> result = new HashMap<>();
        result.put("width", WIDTH);
        result.put("height", HEIGHT);
        return result;
    }

    public Direction getDirection() {
        return direction;
    }
}
