package edu.lucankri.gamesnake.models;

import java.util.*;
import java.util.concurrent.*;

public class RoomImpl implements Room {
    private final String id;
    private int intervalMs;
    private int width;
    private int height;
    private boolean walls;
    private int amountFood;
    private Food food = new Food();
    private final ConcurrentLinkedDeque<Snake> snakes = new ConcurrentLinkedDeque<>();
    private final ConcurrentLinkedDeque<Point> freeCells = new ConcurrentLinkedDeque<>();
    private ScheduledExecutorService frameRater;

    public RoomImpl(int width, int height, int amountFood, int intervalMs, String roomName, boolean walls) {
        this.id = roomName != null ? roomName : UUID.randomUUID().toString();
        this.width = width;
        this.height = height;
        this.amountFood = amountFood;
        this.intervalMs = intervalMs;
        this.walls = walls;
        initRoom();
        startFrameRater();
    }

    private void initRoom() {
        freeCells.clear();
        food.deleteFoods();
        createFreeCells();
        placeFood();
    }

    private void startFrameRater() {
        frameRater = Executors.newSingleThreadScheduledExecutor();
        frameRater.scheduleAtFixedRate(() -> {
            moveSnakes();
            placeFood();
            if (frameAction != null) {
                frameAction.run();
            }
        }, 0, intervalMs, TimeUnit.MILLISECONDS);
    }

    private void placeFood() {
        int amount = amountFood - food.size();
        for (int i = 0; i < amount; ++i) {
            if (freeCells.isEmpty()) {
                createFreeCells();
            }
            if (!freeCells.isEmpty()) {
                food.addFood(Utils.removeElementAtIndex(freeCells, Utils.rand(freeCells.size())));
            }
        }
    }

    private void createFreeCells() {
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
    }

    protected Runnable frameAction;

    public void setFrameAction(Runnable frameAction) {
        this.frameAction = frameAction;
    }

    @Override
    public void resize(int width, int height, int amountFood, int intervalMs, boolean walls) {
        stopFrameRater();
        this.width = width;
        this.height = height;
        this.amountFood = amountFood;
        this.intervalMs = intervalMs;
        this.walls = walls;
        for (Snake snake : snakes) {
            snake.clearPoint();
        }
        initRoom();
        for (Snake snake : snakes) {
            snake.placeRoom(Utils.removeElementAtIndex(freeCells, Utils.rand(freeCells.size())));
        }
        startFrameRater();
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
        Snake snake = new Snake(freeCells.isEmpty() ? null :
                Utils.removeElementAtIndex(freeCells, Utils.rand(freeCells.size())));
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
            snake.placeRoom(freeCells.isEmpty() ? null :
                    Utils.removeElementAtIndex(freeCells, Utils.rand(freeCells.size())));
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
            Point head = walls ? snake.peekMove() : snake.peekMove(width, height);
            if (head != null) {
                if (freeCells.isEmpty()) {
                    createFreeCells();
                }
                try {
                    freeCells.removeFirstOccurrence(head);
                    boolean isEat = food.isEat(head);
                    if (isEat) {
                        if (!freeCells.isEmpty()) {
                            food.moveFood(Utils.removeElementAtIndex(freeCells, Utils.rand(freeCells.size())), head);
                        } else {
                            food.deleteFoods(head);
                        }
                    }
                    snake.move(head, isEat);
                } catch (Exception e) {
                    e.printStackTrace(System.out);
                }

                if (walls) {
                    if (head.x < 0 || head.x >= width || head.y < 0 || head.y >= height) {
                        clearPointSnake(snake);
                        flagGameOver = true;
                    }
                }
                if (!flagGameOver) {
                    for (Snake s : snakes) {
                        for (Point point : s.getSnake()) {
                            if (point.equals(head) && point != head) {
                                clearPointSnake(snake);
                                flagGameOver = true;
                                break;
                            }
                        }
                        if (flagGameOver) {
                            break;
                        }
                    }
                }
            }
        }
    }


//    protected void moveSnakes() {
//        for (Snake snake : snakes) {
//            boolean flagGameOver = false;
//            Point head = walls ? snake.peekMove() : snake.peekMove(width, height);
//            if (head != null) {
//                if (freeCells.isEmpty()) {
//                    createFreeCells();
//                }
//                if (walls) {
//                    if (head.x < 0 || head.x >= width || head.y < 0 || head.y >= height) {
//                        clearPointSnake(snake);
//                        flagGameOver = true;
//                    }
//                }
//                if (!flagGameOver) {
//                    for (Snake s : snakes) {
//                        if (s.getSnake().contains(head)) {
//                            clearPointSnake(snake);
//                            flagGameOver = true;
//                        }
//                    }
//                }
//                if (!flagGameOver) {
//                    try {
//                        freeCells.removeFirstOccurrence(head);
//                        boolean isEat = food.isEat(head);
//                        if (isEat) {
//                            if (!freeCells.isEmpty()) {
//                                food.moveFood(Utils.removeElementAtIndex(freeCells, Utils.rand(freeCells.size())), head);
//                            } else {
//                                food.deleteFoods(head);
//                            }
//                        }
//                        if (walls) {
//                            snake.move(isEat);
//                        } else {
//                            snake.move(isEat, width, height);
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace(System.out);
//                    }
//                }
//            }
//        }
//    }

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
    public int getIntervalMs() {
        return intervalMs;
    }

    @Override
    public boolean getWalls() {
        return walls;
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
