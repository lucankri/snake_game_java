package edu.lucankri.gamesnake.models;

import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedDeque;

public class Utils {
    private static final Random random = new Random();

    public static int rand(int bound) {
        return random.nextInt(bound);
    }

    public static Point removeElementAtIndex(ConcurrentLinkedDeque<Point> freeCells, int index) {
        Iterator<Point> iterator = freeCells.iterator();

        for (int i = 0; i < index; i++) {
            if (iterator.hasNext()) {
                iterator.next();
            } else {
                throw new IndexOutOfBoundsException("Index " + index + " is out of bounds");
            }
        }

        if (iterator.hasNext()) {
            Point removedElement = iterator.next();
            iterator.remove();
            return removedElement;
        } else {
            throw new IndexOutOfBoundsException("Index " + index + " is out of bounds");
        }
    }
}
