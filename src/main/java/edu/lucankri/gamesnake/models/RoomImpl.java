package edu.lucankri.gamesnake.models;

import java.util.*;
import java.util.concurrent.*;

public class RoomImpl implements Room {
    private final String id;
    private int width;
    private int height;
    private Food food;
    private int amountFood;
    private final ConcurrentLinkedDeque<Snake> snakes = new ConcurrentLinkedDeque<>();
    private final ConcurrentLinkedDeque<Point> freeCells = new ConcurrentLinkedDeque<>();
    private ScheduledExecutorService frameRater;

    public RoomImpl(int width, int height, int amountFood, int frameIntervalMs, String roomName) {
        this.id = roomName != null ? roomName : UUID.randomUUID().toString();
        this.width = width;
        this.height = height;
        this.amountFood = amountFood;
        initRoom();
        startFrameRater(frameIntervalMs);
    }

    private void initRoom() {
        freeCells.clear();
        createFreeCells();
        placeFood();
    }

    private void startFrameRater(int frameIntervalMs) {
        frameRater = Executors.newSingleThreadScheduledExecutor();
        frameRater.scheduleAtFixedRate(() -> {
            moveSnakes();
            if (frameAction != null) {
                frameAction.run();
            }
        }, 0, frameIntervalMs, TimeUnit.MILLISECONDS);
    }

    private void placeFood() {
        Deque<Point> freeCellsFoods = new ArrayDeque<>();
        for (int i = 0; i < amountFood; ++i) {
            freeCellsFoods.offerLast(Utils.removeElementAtIndex(freeCells, Utils.rand(freeCells.size())));
        }
        this.food = new Food(freeCellsFoods);
    }

    private boolean createFreeCells() {
        Set<Point> occupiedPoints = new HashSet<>();
        for (Snake snake : snakes) {
            occupiedPoints.addAll(snake.getSnake());
        }
        if (food != null) {
            occupiedPoints.addAll(food.getFoodPoints());
        }
        for (int i = 0; i < height; ++i) {
            for (int j = 0; j < width; ++j) {
                Point point = new Point(j, i);
                if (!occupiedPoints.contains(point)) {
                    freeCells.offerLast(point);
                }
            }
        }
        return !freeCells.isEmpty();
    }

    protected Runnable frameAction;

    public void setFrameAction(Runnable frameAction) {
        this.frameAction = frameAction;
    }

    @Override
    public void resize(int width, int height, int amountFood, int frameIntervalMs) {
        stopFrameRater();
        this.width = width;
        this.height = height;
        this.amountFood = amountFood;
        initRoom();
        for (Snake snake : snakes) {
            snake.clearPoint();
            snake.placeRoom(Utils.removeElementAtIndex(freeCells, Utils.rand(freeCells.size())));
        }
        startFrameRater(frameIntervalMs);
    }

    @Override
    public void stopFrameRater() {
        frameRater.shutdownNow();
    }

    @Override
    public Snake addSnakeRoom() {
        if (freeCells.isEmpty()) {
            createFreeCells();
        }
        Snake snake = new Snake(Utils.removeElementAtIndex(freeCells, Utils.rand(freeCells.size())));
        snakes.offerLast(snake);
        return snake;
    }

    @Override
    public void restartSnake(Snake snake) {
        if (snake != null) {
            snake.clearPoint();
            if (freeCells.isEmpty()) {
                createFreeCells();
            }
            snake.placeRoom(Utils.removeElementAtIndex(freeCells, Utils.rand(freeCells.size())));
        }
    }

    @Override
    public void clearPointSnake(Snake snake) {
        snake.clearPoint();
    }

    @Override
    public void deleteSnake(Snake snake) {
        if (snake != null) {
            clearPointSnake(snake);
            snakes.removeFirstOccurrence(snake);
        }
    }

    protected void moveSnakes() {
        for (Snake snake : snakes) {
            boolean flagGameOver = false;
            Point head = snake.peekMove();
            if (head != null) {
                if (freeCells.isEmpty()) {
                    createFreeCells();
                }
                if (head.x < 0 || head.x >= width || head.y < 0 || head.y >= height) {
                    clearPointSnake(snake);
                    flagGameOver = true;
                }
                if (!flagGameOver) {
                    for (Snake s : snakes) {
                        if (s.getSnake().contains(head)) {
                            clearPointSnake(snake);
                            flagGameOver = true;
                        }
                    }
                }
                if (!flagGameOver) {
                    freeCells.removeFirstOccurrence(head);
                    boolean isEat = food.isEat(head);
                    if (isEat) {
                        food.moveFood(Utils.removeElementAtIndex(freeCells, Utils.rand(freeCells.size())), head);
                    }
                    snake.move(isEat);
                }
            }
        }
    }

    @Override
    public ConcurrentLinkedDeque<Snake> getSnakes() {
        return snakes;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Food getFood() {
        return food;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoomImpl room = (RoomImpl) o;
        return Objects.equals(id, room.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
