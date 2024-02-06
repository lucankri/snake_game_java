package edu.lucankri.gamesnake.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.lucankri.gamesnake.services.PlayerServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.lucankri.gamesnake.game.SnakeGame;
import edu.lucankri.gamesnake.game.SnakeGame.Point;

@Controller
public class GameController implements WebMvcConfigurer {

    @Autowired
    private PlayerServiceImpl playerServiceImpl;

    @Autowired
    private SnakeGame snakeGame;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/web/**").addResourceLocations("classpath:/web/");
    }

    @GetMapping("/")
    public String start() {
        return "index";
    }

    @GetMapping("/snake-coordinates")
    @ResponseBody
    public String getSnake() throws JsonProcessingException {
        List<Point> snake = snakeGame.getSnake();
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(snake);
    }

    @GetMapping("/food-coordinates")
    @ResponseBody
    public String getFood() throws JsonProcessingException {
        List<Point> foods = snakeGame.getFoods();
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(foods);
    }

    @GetMapping("/score")
    @ResponseBody
    public int getScore() {
        return snakeGame.getScore();
    }

    @GetMapping("/board-size")
    @ResponseBody
    public Map<String, Integer> getBoardSize() {
        Map<String, Integer> sizeMap = new HashMap<>();
        sizeMap.put("width", SnakeGame.WIDTH);
        sizeMap.put("height", SnakeGame.HEIGHT);
        return sizeMap;
    }

    @PostMapping("/move")
    public ResponseEntity<String> move(@RequestParam("direction") String direction) {
        boolean success = snakeGame.move(SnakeGame.Direction.valueOf(direction));
        if (success) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().body("Invalid move");
        }
    }

}
