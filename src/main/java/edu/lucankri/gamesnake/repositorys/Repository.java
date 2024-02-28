package edu.lucankri.gamesnake.repositorys;

import edu.lucankri.gamesnake.models.Room;
import edu.lucankri.gamesnake.models.Snake;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;

public interface Repository {
    void clientAdded(WebSocketSession session);
    void clientRemoved(WebSocketSession session);
    void bind(WebSocketSession session, Room room, Snake snake);
    Room findRoom(String roomId);
    Room findRoom(WebSocketSession session);
    Room createRoom(WebSocketSession session, int width, int height, int foodAmount, int frameIntervalMs, String nameRoom);
    Snake findSnake(WebSocketSession session);
    Boolean getCreator(WebSocketSession session);

    void exitRoom(WebSocketSession session);

    ConcurrentLinkedDeque<WebSocketSession> getRoomMembers(String roomId);
}
