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
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import edu.lucankri.gamesnake.game.SnakeGame;
import edu.lucankri.gamesnake.game.SnakeGame.Point;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@Controller
public class GameController {
    @Autowired
    SnakeGameService snakeGameService;

    @GetMapping("/")
    public String startIndex() {
        return "index";
    }

    @GetMapping("/initialization")
    @ResponseBody
    public String initializeGame(HttpServletResponse response) {
        return snakeGameService.initialization();
    }

    @GetMapping("/snake-coordinates")
    @ResponseBody
    @Async
    public CompletableFuture<String> getSnake(@RequestParam("playerId") String playerId) throws JsonProcessingException {
        List<Point> snake = snakeGameService.getSnakeCoordinates(playerId);
        ObjectMapper objectMapper = new ObjectMapper();
        return CompletableFuture.completedFuture(objectMapper.writeValueAsString(snake));
    }

    @GetMapping("/food-coordinates")
    @ResponseBody
    @Async
    public CompletableFuture<String> getFood(@RequestParam("playerId") String playerId) throws JsonProcessingException {
        List<Point> foods = snakeGameService.getFoodCoordinates(playerId);
        ObjectMapper objectMapper = new ObjectMapper();
        return CompletableFuture.completedFuture(objectMapper.writeValueAsString(foods));
    }

    @GetMapping("/score")
    @ResponseBody
    @Async
    public CompletableFuture<Integer> getScore(@RequestParam("playerId") String playerId) {
        return CompletableFuture.completedFuture(snakeGameService.getScore(playerId));
    }

    @GetMapping("/direction")
    @ResponseBody
    @Async
    public CompletableFuture<String> getDirection(@RequestParam("playerId") String playerId) {
        return CompletableFuture.completedFuture(snakeGameService.getDirection(playerId));
    }

    @GetMapping("/board-size")
    @ResponseBody
    public Map<String, Integer> getBoardSize(@RequestParam("playerId") String playerId) {
        return snakeGameService.getSizeBoard(playerId);
    }

    @PostMapping("/move")
    @Async
    public CompletableFuture<ResponseEntity<String>> move(@RequestParam("playerId") String playerId,
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

    @DeleteMapping("/close-page")
    @ResponseBody
    @Async
    public void closePage(@RequestParam("playerId") String playerId) {
        snakeGameService.deleteSnakeGame(playerId);
    }
}
