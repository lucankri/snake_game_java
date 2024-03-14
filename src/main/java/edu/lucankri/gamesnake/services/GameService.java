package edu.lucankri.gamesnake.services;

import edu.lucankri.gamesnake.models.Food;
import edu.lucankri.gamesnake.models.Snake;

import java.util.List;

public interface GameService {
    class MessageIn {
        /**
         * "room-create": create new room
         *      fields: type, roomId, roomWidth, roomHeight, amountFood, interval, walls
         * "room-join": join existing room
         *      fields: type, roomId
         * "resize-room": change room settings
         *      fields: type, roomWidth, roomHeight, amountFood, interval, walls
         * "snake-dir-change": change snake direction
         *      fields: type, direction
         * "exit": exit the room
         *      fields: type
         * "restart": get started for the person who sent the request
         *      fields: type
         */
        public String type;
        public String roomId;
        public Integer roomWidth;
        public Integer roomHeight;
        public Integer amountFood;
        public Integer interval;
        public Boolean walls;

        /**
         * "LEFT", "RIGHT", "UP", "DOWN"
         */
        public String direction;
    }

    class MessageOut {
        /**
         * "start-room": start the game, send all the room data
         *      fields: type, roomId, roomWidth, roomHeight, amountFood, intervalMs, walls
         * "event": we are sending new data on the position of snakes and food
         *      fields: type, enemiesSnakes, mySnake, food
         * "game-over": the end of the game
         *      fields: type, enemiesSnakes, mySnake, food
         * "error": if something went wrong
         *      fields: type, messageError
         */
        public String type;
        public Boolean creator;
        public String roomId;
        public Snake mySnake;
        public List<Snake> enemiesSnakes;
        public Food food;
        public String messageError;
        public Integer roomWidth;
        public Integer roomHeight;
        public Integer amountFood;
        public Integer intervalMs;
        public Boolean walls;
    }
}
