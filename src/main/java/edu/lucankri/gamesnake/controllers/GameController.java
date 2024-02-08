package edu.lucankri.gamesnake.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.lucankri.gamesnake.services.SnakeGameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;

import edu.lucankri.gamesnake.game.SnakeGame;
import edu.lucankri.gamesnake.game.SnakeGame.Point;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@Controller
public class GameController {
    @Autowired
    private SnakeGameService snakeGameService;
    private int numberPage = 1;

    @GetMapping("/")
    public String startIndex(HttpServletResponse response,
                             @CookieValue(value = "playerId", defaultValue = "null") String playerId) {
        if (playerId.equals("null") || snakeGameService.getGames().get(playerId) == null) {
            System.out.println("///cuk=" + playerId);
            System.out.println("Инициализация!");
            Cookie cookie = new Cookie("playerId", snakeGameService.initialization());
            response.addCookie(cookie);
        } else {
            ++numberPage;
        }
        System.out.println("ppppppp");
        return "index";
    }


    @GetMapping("/snake-coordinates")
    @ResponseBody
    @Async
    public CompletableFuture<String> getSnake(@CookieValue(value = "playerId", defaultValue = "null") String playerId) throws JsonProcessingException {
        ConcurrentLinkedDeque<Point> snake = snakeGameService.getSnakeCoordinates(playerId);
        ObjectMapper objectMapper = new ObjectMapper();
        return CompletableFuture.completedFuture(objectMapper.writeValueAsString(snake));
    }

    @GetMapping("/food-coordinates")
    @ResponseBody
    @Async
    public CompletableFuture<String> getFood(@CookieValue(value = "playerId", defaultValue = "null") String playerId) throws JsonProcessingException {
        ConcurrentLinkedDeque<Point> foods = snakeGameService.getFoodCoordinates(playerId);
        ObjectMapper objectMapper = new ObjectMapper();
        return CompletableFuture.completedFuture(objectMapper.writeValueAsString(foods));
    }

    @GetMapping("/score")
    @ResponseBody
    @Async
    public CompletableFuture<Integer> getScore(@CookieValue(value = "playerId", defaultValue = "null") String playerId) {
        return CompletableFuture.completedFuture(snakeGameService.getScore(playerId));
    }

    @GetMapping("/direction")
    @ResponseBody
    @Async
    public CompletableFuture<String> getDirection(@CookieValue(value = "playerId", defaultValue = "null") String playerId) {
        return CompletableFuture.completedFuture(snakeGameService.getDirection(playerId));
    }

    @GetMapping("/board-size")
    @ResponseBody
    public Map<String, Integer> getBoardSize(@CookieValue(value = "playerId", defaultValue = "null") String playerId) {
        return snakeGameService.getSizeBoard(playerId);
    }

    @PostMapping("/move")
    @Async
    public CompletableFuture<ResponseEntity<String>> move(@CookieValue(value = "playerId", defaultValue = "null") String playerId,
                                               @RequestParam("direction") String direction) {
        boolean success = snakeGameService.moveSnake(playerId,
                                    SnakeGame.Direction.valueOf(direction));
        if (success) {
            return CompletableFuture.completedFuture(ResponseEntity.ok().build());
        } else {
            return CompletableFuture.completedFuture(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Game Over!"));
        }
    }

//    @GetMapping("/delete-cookie")
//    @ResponseBody
//    public void deleteCookie(HttpServletResponse response) {
//
//    }

    @DeleteMapping("/close-page")
    @ResponseBody
    @Async
    public void closePage(@CookieValue(value = "playerId", defaultValue = "null") String playerId) {
        if (numberPage == 1) {
            snakeGameService.deleteSnakeGame(playerId);
        } else {
            --numberPage;
        }
    }
}
