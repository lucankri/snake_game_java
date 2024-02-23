package edu.lucankri.gamesnake.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

public class Food {
    @JsonProperty("points")
    private final ConcurrentLinkedDeque<Point> foods = new ConcurrentLinkedDeque<>();

    public Food(Deque<Point> freeCells) {
        int sizeFreeCells = freeCells.size();
        for (int i = 0; i < sizeFreeCells; ++i) {
            foods.offerLast(freeCells.pollLast());
        }
    }

    public boolean isEat(Point head) {
        return foods.contains(head);
    }

    public void moveFood(Point freeCell, Point food) {
        foods.removeFirstOccurrence(food);
        foods.offerLast(freeCell);
    }

    public void clear() {
        foods.clear();
    }

    public ConcurrentLinkedDeque<Point> getFoods() {
        return foods;
    }
}
