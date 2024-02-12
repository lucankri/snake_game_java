package edu.lucankri.gamesnake.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.lucankri.gamesnake.services.SnakeGameService;
import edu.lucankri.gamesnake.services.SnakeGameServiceImpl;
import org.springframework.stereotype.Controller;

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

    @GetMapping("/initialization")
    @ResponseBody
    public void init(HttpServletRequest request,
                     @RequestParam("width") Integer width,
                     @RequestParam("height") Integer height,
                     @RequestParam("sizeFoods") Integer sizeFoods) {
        HttpSession session = request.getSession();
        SnakeGameService snakeGameService = (SnakeGameService) session.getAttribute("player");
        if (snakeGameService == null) {
            System.out.println("Новая сессия!!!");
            SnakeGameService service = new SnakeGameServiceImpl();
            service.initialization(width, height, sizeFoods);
            session.setAttribute("player", service);
        } else {
            snakeGameService.resize(width, height, sizeFoods);
            System.out.println("Сессия уже существует!");
        }
    }

    @GetMapping("/restart")
    @ResponseBody
    public void restart(HttpServletRequest request) {
        HttpSession session = request.getSession();
        SnakeGameService snakeGameService = (SnakeGameService) session.getAttribute("player");
        snakeGameService.restart();
    }


    @GetMapping(path = "/snake-coordinates")
    @ResponseBody
    public String getSnake(HttpServletRequest request) throws JsonProcessingException {
        SnakeGameService gameSession = (SnakeGameService) request.getSession().getAttribute("player");
        ConcurrentLinkedDeque<Point> snake = gameSession.getSnakeCoordinates();
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(snake);
    }

    @GetMapping("/food-coordinates")
    @ResponseBody
    public String getFood(HttpServletRequest request) throws JsonProcessingException {
        SnakeGameService gameSession = (SnakeGameService) request.getSession().getAttribute("player");
        ConcurrentLinkedDeque<Point> foods = gameSession.getFoodCoordinates();
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(foods);
    }

    @GetMapping("/score")
    @ResponseBody
    public Integer getScore(HttpServletRequest request) {
        SnakeGameService gameSession = (SnakeGameService) request.getSession().getAttribute("player");
        return gameSession.getScore();
    }

    @GetMapping("/direction")
    @ResponseBody
    public String getDirection(HttpServletRequest request) {
        SnakeGameService gameSession = (SnakeGameService) request.getSession().getAttribute("player");
        return gameSession.getDirection();
    }

    @GetMapping("/board-size")
    @ResponseBody
    public Map<String, Integer> getBoardSize(HttpServletRequest request) {
        SnakeGameService gameSession = (SnakeGameService) request.getSession().getAttribute("player");
        return gameSession.getSizeBoard();
    }

    @PostMapping("/move")
    @ResponseBody
    public boolean move(HttpServletRequest request,
                                       @RequestParam("direction") String direction) {
        SnakeGameService gameSession = (SnakeGameService) request.getSession().getAttribute("player");
        return gameSession.moveSnake(SnakeGame.Direction.valueOf(direction));
    }
}
