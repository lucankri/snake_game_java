package edu.lucankri.gamesnake.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

public class Food {
    @JsonProperty("points")
    private final ConcurrentLinkedDeque<Point> foodPoints = new ConcurrentLinkedDeque<>();

    public Food(Deque<Point> freeCells) {
        int sizeFreeCells = freeCells.size();
        for (int i = 0; i < sizeFreeCells; ++i) {
            foodPoints.offerLast(freeCells.pollLast());
        }
    }

    public boolean isEat(Point head) {
        return foodPoints.contains(head);
    }

    public void moveFood(Point freeCell, Point food) {
        if (food != null) {
            foodPoints.removeFirstOccurrence(food);
        }
        if (freeCell != null) {
            foodPoints.offerLast(freeCell);
        }
    }

    public ConcurrentLinkedDeque<Point> getFoodPoints() {
        return foodPoints;
    }
}
