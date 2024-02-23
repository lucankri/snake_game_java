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
        public String roomId;
        public Snake mySnake;
        public List<Snake> snakes;
        public Food food;
        public String messageError;
    }


    /**
     {
        "type": "abc",
        "roomId": "341rf",
        "snakes": [
             {
               "points": [
                    {1, 2},
                    {1, 3},
                    {3, 0}
                ],
                "score": 10
             },
             {
                ...
             }
        ],
         "food": [
            {10, 4},
            {2, 2}
         ]
     }


     */

}
