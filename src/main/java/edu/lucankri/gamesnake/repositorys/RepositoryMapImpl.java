package edu.lucankri.gamesnake.repositorys;

import edu.lucankri.gamesnake.models.Room;
import edu.lucankri.gamesnake.models.RoomImpl;
import edu.lucankri.gamesnake.models.Snake;
import org.springframework.web.socket.WebSocketSession;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

//Map
public class RepositoryMapImpl implements Repository {

    protected static class ClientInfo {
        public WebSocketSession session;
        public Room room;
        public Snake snake;
    }

    private final Map<String, ClientInfo> clients = new ConcurrentHashMap<>();
    protected final Map<String, Room> rooms = new ConcurrentHashMap<>();
    protected final Map<String, List<WebSocketSession>> roomMembers = new ConcurrentHashMap<>();

    private void deletePointers(WebSocketSession session) {
        ClientInfo client = clients.get(session.getId());
        if (client != null && client.room != null) {
            List<WebSocketSession> sessions = roomMembers.get(client.room.getId());
            if (sessions != null) {
                sessions.remove(session);
                if (sessions.isEmpty()) {
                    roomMembers.remove(client.room.getId());
                    rooms.remove(client.room.getId()).stopFrameRater();
                    System.out.println("///В комнате никого нет - удаляем!");
                }
            }
        }
        System.out.println("///Количество комнат=" + rooms.size());
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
        System.out.println("///Клиент ушел!");
        System.out.println("///Количество клиентов=" + clients.size());
    }

    @Override
    public void exitRoom(WebSocketSession session) {
        ClientInfo client = clients.get(session.getId());
        deletePointers(session);
        System.out.println("///Клиент вышел из комнаты!");
        System.out.println("///Количество клиентов в комнате=" + (roomMembers.get(client.room.getId())
                        == null ? 0 : roomMembers.get(client.room.getId()).size()));
        client.room = null;
        client.snake = null;
    }

    @Override
    public void bind(WebSocketSession session, Room room, Snake snake) {
        ClientInfo clientInfo = clients.get(session.getId());
        if (clientInfo == null) {
            // impossible
            throw new IllegalStateException(session.getId() + " not found");
        }
        clientInfo.snake = snake;
        System.out.println("///Клиент вошел в комнату!");
        clientInfo.room = room;
        List<WebSocketSession> sessions = roomMembers.computeIfAbsent(room.getId(), k -> new ArrayList<>());
        sessions.add(session);
        System.out.println("///Количество клиентов в комнате= " + sessions.size());
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
    public Room createRoom(int width, int height, int foodAmount, int frameIntervalMs, String nameRoom) {
        Room room = new RoomImpl(width, height, foodAmount, frameIntervalMs, nameRoom);
        System.out.println("///Клиент создал команту!" + room.getId());
        rooms.put(room.getId(), room);
        return room;
    }

    @Override
    public Snake findSnake(WebSocketSession client) {
        ClientInfo clientInfo = clients.get(client.getId());
        return clientInfo.snake;
    }

    @Override
    public List<WebSocketSession> getRoomMembers(String roomId) {
        return roomMembers.get(roomId);
    }
}
