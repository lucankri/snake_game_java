package edu.lucankri.gamesnake.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.lucankri.gamesnake.services.SnakeGameService;
import edu.lucankri.gamesnake.services.SnakeGameServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;

import edu.lucankri.gamesnake.gamelogic.SnakeGame;
import edu.lucankri.gamesnake.gamelogic.SnakeGame.Point;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Controller
public class GameController {
    @GetMapping("/")
    public String startIndex() {
        return "index";
    }

    @PostMapping("/game-move-data")
    public ResponseEntity<Map<String, Object>> getGameMoveData(HttpServletRequest request,
                                                           @RequestParam("direction") String direction) {
        Map<String, Object> gameData = new HashMap<>();
        SnakeGameService snakeGameService = (SnakeGameService) request.getSession().getAttribute("player");
        if (snakeGameService != null) {
            gameData.put("move", snakeGameService.moveSnake(direction));
            gameData.put("snakeCoordinates", snakeGameService.getSnakeCoordinates());
            gameData.put("foodCoordinates", snakeGameService.getFoodCoordinates());
            gameData.put("score", snakeGameService.getScore());
            gameData.put("direction", snakeGameService.getDirection());
            return ResponseEntity.ok(gameData);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.singletonMap("error", "Игровая сессия не найдена."));
        }
    }

    @GetMapping("/initialization")
    @ResponseBody
    public void init(HttpServletRequest request,
                     @RequestParam("width") Integer width,
                     @RequestParam("height") Integer height,
                     @RequestParam("sizeFoods") Integer sizeFoods) {
        HttpSession session = request.getSession();
        SnakeGameService snakeGameService = (SnakeGameService) session.getAttribute("player");
        if (snakeGameService == null) {
            SnakeGameService service = new SnakeGameServiceImpl();
            service.initialization(width, height, sizeFoods);
            session.setAttribute("player", service);
        } else {
            snakeGameService.resize(width, height, sizeFoods);
        }
    }

}
