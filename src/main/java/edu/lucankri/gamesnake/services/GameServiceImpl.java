package edu.lucankri.gamesnake.services;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import edu.lucankri.gamesnake.repositorys.Repository;
import edu.lucankri.gamesnake.repositorys.RepositoryMapImpl;
import edu.lucankri.gamesnake.models.Snake;
import edu.lucankri.gamesnake.models.Room;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedDeque;


public class GameServiceImpl extends TextWebSocketHandler implements GameService {

    private final ObjectMapper mapper = new ObjectMapper();
    private final Repository repository = new RepositoryMapImpl();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        repository.clientAdded(session);
        System.out.println("///Подключился игрок id=" + session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        Room room = repository.findRoom(session);
        if (room != null) {
            room.deleteSnake(repository.findSnake(session));
        }
        repository.clientRemoved(session);
        sendStartMessageRoom(room);
        System.out.println("///Отключился игрок id=" + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        super.handleTextMessage(session, message);
        MessageIn messageIn;
        try {
            messageIn = new ObjectMapper().readValue(message.getPayload(), MessageIn.class);
        } catch (JsonMappingException e) {
            prepareError(session, e.getMessage());
            return;
        }
        if (messageIn != null && messageIn.type != null) {
            switch (messageIn.type) {
                case "create-room":
                case "join-room":
                case "resize-room":
                    createOrJoinOrResizeProcessing(session, messageIn);
                    break;
                case "snake-dir-change":
                    Snake snake = repository.findSnake(session);
                    if (snake != null) {
                        snake.setDirection(messageIn.direction);
                    }
                    break;
                case "restart-snake": {
                    Room room = repository.findRoom(session);
                    if (room != null) {
                        room.restartSnake(repository.findSnake(session));
                    }
                    break;
                }
                case "exit-room": {
                    Room room = repository.findRoom(session);
                    if (room != null) {
                        room.deleteSnake(repository.findSnake(session));
                        repository.exitRoom(session);
                        sendStartMessageRoom(room);
                    }
                    break;
                }
            }
        } else {
            prepareError(session, "The message or message type is null");
        }

    }

    private void createOrJoinOrResizeProcessing(WebSocketSession session, MessageIn messageIn) {
        Room room = null;
        if ("create-room".equals(messageIn.type) || "resize-room".equals(messageIn.type)) {
            if (messageIn.roomWidth != null && messageIn.roomHeight != null
                    && messageIn.amountFood != null && messageIn.interval != null && messageIn.walls != null) {
                if ("create-room".equals(messageIn.type)) {
                    room = repository.findRoom(messageIn.roomId);
                    if (room == null) {
                        room = repository.createRoom(session, messageIn.roomWidth, messageIn.roomHeight,
                                messageIn.amountFood, messageIn.interval, messageIn.roomId, messageIn.walls);
                        Room finalRoom = room;
                        room.setFrameAction(() -> prepareDataCoordinateRoomClients(finalRoom));
                        System.out.println("///Игрок= " + session.getId() + " создал комнату id=" + room.getId());
                    } else {
                        prepareError(session, "There is a room with that name!");
                        room = null;
                    }
                } else {
                    room = repository.findRoom(session);
                    if (room != null) {
                        room.resize(messageIn.roomWidth, messageIn.roomHeight, messageIn.amountFood, messageIn.interval, messageIn.walls);
                    } else {
                        prepareError(session, "You are not in the room!");
                    }
                }
            } else {
                prepareError(session, "There is not enough data, I need: roomWidth," +
                        "roomHeight, amountFood, interval, roomId, walls");
            }
        } else if ("join-room".equals(messageIn.type)) {
            if (messageIn.roomId != null) {
                room = repository.findRoom(messageIn.roomId);
                if (room == null) {
                    prepareError(session, "The room was not found!");
                }
            } else {
                prepareError(session, "There is not enough data, I need: roomId");
            }
        }
        if (room != null) {
            if (!"resize-room".equals(messageIn.type)) {
                repository.bind(session, room, room.addSnakeRoom());
                System.out.println("///Игрок= " + session.getId() + " присоединился к комнате id=" + room.getId());
            }
            sendStartMessageRoom(room);
        }
    }

    private void sendStartMessageRoom(Room room) {
        if (room != null) {
            ConcurrentLinkedDeque<WebSocketSession> sessions = repository.getRoomMembers(room.getId());
            if (sessions != null) {
                MessageOut message = new MessageOut();
                message.type = "start-room";
                message.roomId = room.getId();
                message.roomWidth = room.getWidth();
                message.roomHeight = room.getHeight();
                message.amountFood = room.getFood().getFoodPoints().size();
                message.intervalMs = room.getIntervalMs();
                message.walls = room.getWalls();
                for (WebSocketSession session : sessions) {
                    message.creator = repository.getCreator(session);
                    sendMessage(session, message);
                }
            }
        }
    }

    private void prepareDataCoordinateRoomClients(Room room) {
        ConcurrentLinkedDeque<WebSocketSession> sessions = repository.getRoomMembers(room.getId());
        if (sessions != null) {
            for (WebSocketSession session : sessions) {
                MessageOut message = new MessageOut();
                Snake snake = repository.findSnake(session);
                if (snake.getSnake().isEmpty()) {
                    message.type = "game-over";
                } else {
                    message.type = "event";
                }
                message.enemiesSnakes = new LinkedList<>(room.getSnakes());
                message.enemiesSnakes.remove(snake);
                message.mySnake = snake;
                message.food = room.getFood();

                sendMessage(session, message);
            }
        }
    }

    private void prepareError(WebSocketSession session, String error) {
        MessageOut message = new MessageOut();
        message.type = "error";
        message.messageError = error;
        sendMessage(session, message);
    }

    private synchronized void sendMessage(WebSocketSession session, Object object) {
        try {
            session.sendMessage(new TextMessage(mapper.writeValueAsString(object)));
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
