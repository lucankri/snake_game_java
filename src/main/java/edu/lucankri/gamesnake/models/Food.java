package edu.lucankri.gamesnake.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.concurrent.ConcurrentLinkedDeque;

public class Food {
    @JsonProperty("points")
    private final ConcurrentLinkedDeque<Point> foodPoints = new ConcurrentLinkedDeque<>();

    public Food() {}

    public void addFood(Point freeCell) {
        foodPoints.offerLast(freeCell);
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

    public void deleteFoods(Point food) {
        foodPoints.removeFirstOccurrence(food);
    }

    public void deleteFoods() {
        foodPoints.clear();
    }

    public ConcurrentLinkedDeque<Point> getFoodPoints() {
        return foodPoints;
    }

    public int size() {
        return foodPoints.size();
    }
}
