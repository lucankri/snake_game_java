package edu.lucankri.gamesnake.services;

import edu.lucankri.gamesnake.controllers.GameController;
import edu.lucankri.gamesnake.game.SnakeGame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class SnakeGameServiceImpl implements SnakeGameService {
    @Autowired
    private PlayerService playerService;

    private final ConcurrentMap<String, SnakeGame> games = new ConcurrentHashMap<>();

    @Override
    public String initialization() {
        String id = playerService.generatePlayerId();
        getOrCreateGame(id);
        return id;
    }

    @Override
    public boolean moveSnake(String playerId, SnakeGame.Direction direction) {
        return getOrCreateGame(playerId).move(direction);
    }

    @Override
    public List<SnakeGame.Point> getSnakeCoordinates(String playerId) {
        return getOrCreateGame(playerId).getSnake();
    }

    @Override
    public List<SnakeGame.Point> getFoodCoordinates(String playerId) {
        return getOrCreateGame(playerId).getFoods();
    }

    @Override
    public int getScore(String playerId) {
        return getOrCreateGame(playerId).getScore();
    }

    @Override
    public Map<String, Integer> getSizeBoard(String playerId) {
        System.out.println(games);
        return getOrCreateGame(playerId).getSizeWidthAndHeight();
    }

    @Override
    public String getDirection(String playerId) {
        return getOrCreateGame(playerId).getDirection().toString();
    }

    @Override
    public void deleteSnakeGame(String playerId) {
        SnakeGame removedGame = games.remove(playerId);
        if (removedGame != null) {
            System.out.println("Игрок ушел! " + playerId + " Их=" + games.size());
        } else {
            System.out.println("Игрок " + playerId + " не найден.");
        }
        System.out.println(games);
    }

    private SnakeGame getOrCreateGame(String playerId) {
        return games.computeIfAbsent(playerId, k -> new SnakeGame());
    }
}
