package edu.lucankri.gamesnake.repositorys;

import edu.lucankri.gamesnake.models.Room;
import edu.lucankri.gamesnake.models.RoomImpl;
import edu.lucankri.gamesnake.models.Snake;
import edu.lucankri.gamesnake.models.Utils;
import org.springframework.web.socket.WebSocketSession;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

//Map
public class RepositoryMapImpl implements Repository {

    protected static class ClientInfo {
        public WebSocketSession session;
        public Room room;
        public Snake snake;
        public  Boolean creator;
    }

    private final Map<String, ClientInfo> clients = new ConcurrentHashMap<>();
    protected final Map<String, Room> rooms = new ConcurrentHashMap<>();
    protected final Map<String, ConcurrentLinkedDeque<WebSocketSession>> roomMembers = new ConcurrentHashMap<>();

    private void deletePointers(WebSocketSession session) {
        ClientInfo client = clients.get(session.getId());
        if (client != null && client.room != null) {
            ConcurrentLinkedDeque<WebSocketSession> sessions = roomMembers.get(client.room.getId());
            if (sessions != null) {
                sessions.remove(session);
                if (sessions.isEmpty()) {
                    roomMembers.remove(client.room.getId());
                    rooms.remove(client.room.getId()).stopFrameRater();
                    System.out.println("///В комнате никого нет - удаляем!, количество комнат=" + rooms.size());
                } else if (client.creator != null && client.creator) {
                    clients.get(sessions.peekFirst().getId()).creator = true;
                }
            }
        }
    }

    @Override
    public void clientAdded(WebSocketSession session) {
        ClientInfo client = new ClientInfo();
        client.session = session;
        clients.put(session.getId(), client);
    }
    @Override
    public void clientRemoved(WebSocketSession session) {
        deletePointers(session);
        clients.remove(session.getId());
    }

    @Override
    public void exitRoom(WebSocketSession session) {
        ClientInfo client = clients.get(session.getId());
        deletePointers(session);
        client.room = null;
        client.snake = null;
        client.creator = null;
    }

    @Override
    public void bind(WebSocketSession session, Room room, Snake snake) {
        ClientInfo client = clients.get(session.getId());
        if (client == null) {
            // impossible
            throw new IllegalStateException(session.getId() + " not found");
        }
        if (client.creator == null) {
            client.creator = false;
        }
        client.snake = snake;
        client.room = room;
        ConcurrentLinkedDeque<WebSocketSession> sessions = roomMembers.computeIfAbsent(room.getId(), k -> new ConcurrentLinkedDeque<>());
        sessions.add(session);
    }

    @Override
    public Room findRoom(String roomId) {
        return roomId == null ? null : rooms.get(roomId);
    }

    @Override
    public Room findRoom(WebSocketSession session) {
        ClientInfo client = clients.get(session.getId());
        return client.room;
    }

    @Override
    public Room createRoom(WebSocketSession session, int width,
                           int height, int foodAmount, int frameIntervalMs, String nameRoom, boolean walls) {
        ClientInfo client = clients.get(session.getId());
        Room room = null;
        if (client != null) {
            client.creator = true;
            room = new RoomImpl(width, height, foodAmount, frameIntervalMs, nameRoom, walls);
            rooms.put(room.getId(), room);
        }
        return room;
    }

    @Override
    public Snake findSnake(WebSocketSession client) {
        ClientInfo clientInfo = clients.get(client.getId());
        return clientInfo != null ? clientInfo.snake : null;
    }

    @Override
    public ConcurrentLinkedDeque<WebSocketSession> getRoomMembers(String roomId) {
        return roomId == null ? null : roomMembers.get(roomId);
    }

    @Override
    public Boolean getCreator(WebSocketSession session) {
        ClientInfo clientInfo = clients.get(session.getId());
        return clientInfo.creator;
    }
}
