package edu.lucankri.gamesnake.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class Point {
    @JsonProperty("x")
    int x;
    @JsonProperty("y")
    int y;

    Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    Point move(String direction) {
        switch (direction) {
            case "UP":
                return new Point(x, y - 1);
            case "DOWN":
                return new Point(x, y + 1);
            case "LEFT":
                return new Point(x - 1, y);
            case "RIGHT":
                return new Point(x + 1, y);
            default:
                return new Point(x, y);
        }
    }

    @Override
    public String toString() {
        return "Point{" +
                "x=" + x +
                ", y=" + y +
                '}';
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
