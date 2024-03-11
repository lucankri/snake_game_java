package edu.lucankri.gamesnake.services;

import edu.lucankri.gamesnake.models.Food;
import edu.lucankri.gamesnake.models.Snake;

import java.util.List;

public interface GameService {
    class MessageIn {
        /**
         * "room-create": create new room
         *   fields: roomWidth, roomHeight, foodAmount, interval
         * "room-join": join existing room
         *   fields: roomId
         * "resize-room"
         *  fields: roomWidth, roomHeight, foodAmount, interval
         * "snake-dir-change": change snake direction
         *   fields: direction
         * "exit": ----
         * "restart" ----
         */
        public String type;
        public String roomId;
        public Integer roomWidth;
        public Integer roomHeight;
        public Integer amountFood;
        public Integer interval;
        public Boolean walls;

        /**
         * "L", "R", "U", "D"
         */
        public String direction;
    }

    class MessageOut {
        /**
         * "room-id" - возвращаем ключ для подключение к комнате
         * "event" - возвращаем координаты всех змеек и еды
         * "game-over" - проиграл, но все равно отправляем координаты
         * "error" - возвращаем ошибку
         */
        public String type;
        public Boolean creator;
        public String roomId;
        public Snake mySnake;
        public List<Snake> snakes;
        public Food food;
        public String messageError;
        public Integer roomWidth;
        public Integer roomHeight;
        public Integer amountFood;
        public Integer intervalMs;
        public Boolean walls;
    }
}
