package edu.lucankri.gamesnake.services;

import com.fasterxml.jackson.core.JsonProcessingException;
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

import java.util.List;
import java.util.LinkedList;


public class GameServiceImpl extends TextWebSocketHandler implements GameService {

    private final ObjectMapper mapper = new ObjectMapper();
    private final Repository repository = new RepositoryMapImpl();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        repository.clientAdded(session);
        System.out.println("Подключился игрок id=" + session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        Room room = repository.findRoom(session);
        if (room != null) {
            room.deleteSnake(repository.findSnake(session));
        }
        repository.clientRemoved(session);
        System.out.println("Отключился игрок id=" + session.getId() + "статус=" + status);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        super.handleTextMessage(session, message);
        MessageIn messageIn;
        try {
            messageIn = new ObjectMapper().readValue(message.getPayload(), MessageIn.class);
        } catch (JsonMappingException e) {
            MessageOut messageOut = new MessageOut();
            messageOut.type = "error";
            messageOut.messageError = e.getMessage();
            sendMessage(session, messageOut);
            return;
        }
        if (messageIn != null && messageIn.type != null) {
            if ("create-room".equals(messageIn.type) || "join-room".equals(messageIn.type)) {
                createOrJoinProcessing(session, messageIn);
            } else if ("snake-dir-change".equals(messageIn.type)) {
                Snake snake = repository.findSnake(session);
                if (snake != null) {
                    snake.setDirection(messageIn.direction);
                }
            } else if ("restart-room".equals(messageIn.type)) {
                Room room = repository.findRoom(session);
                room.restartSnake(repository.findSnake(session));
            } else if ("resize-room".equals(messageIn.type)) {
                Room room = repository.findRoom(session);
                room.resize(messageIn.roomWidth, messageIn.roomHeight, messageIn.amountFood, messageIn.interval);
            } else if ("exit-room".equals(messageIn.type)) {
                System.out.println("///Пытаемся выйти!!!");
                Room room = repository.findRoom(session);
                if (room != null) {
                    room.deleteSnake(repository.findSnake(session));
                    repository.exitRoom(session);
                }
            }
        } else {
            MessageOut messageOut = new MessageOut();
            messageOut.type = "error";
            messageOut.messageError = "The message or message type is null";
            sendMessage(session, messageOut);
        }

    }

    private void createOrJoinProcessing(WebSocketSession session, MessageIn messageIn) {
        MessageOut messageOut = new MessageOut();
        Room room = null;
        if ("create-room".equals(messageIn.type) || "resize-room".equals(messageIn.type)) {
            if (messageIn.roomWidth != null && messageIn.roomHeight != null
                    && messageIn.amountFood != null && messageIn.interval != null) {
                if ("create-room".equals(messageIn.type)) {
                    room = repository.findRoom(messageIn.roomId);
                    if (room == null) {
                        room = repository.createRoom(messageIn.roomWidth, messageIn.roomHeight,
                                messageIn.amountFood, messageIn.interval, messageIn.roomId);
                        Room finalRoom = room;
                        room.setFrameAction(() -> prepareDataCoordinateRoomClients(finalRoom));
                    } else {
                        messageOut.type = "error";
                        messageOut.messageError = "There is a room with that name!";
                        sendMessage(session, messageOut);
                        room = null;
                    }
                } else {
                    room = repository.findRoom(session);
                    if (room != null) {
                        room.resize(messageIn.roomWidth, messageIn.roomHeight, messageIn.amountFood, messageIn.interval);
                    } else {
                        messageOut.type = "error";
                        messageOut.messageError = "You are not in the room!";
                        sendMessage(session, messageOut);
                    }
                }
            } else {
                messageOut.type = "error";
                messageOut.messageError = "There is not enough data, I need: roomWidth," +
                        "roomHeight, amountFood, interval, roomId";
                sendMessage(session, messageOut);
            }
        } else if ("join-room".equals(messageIn.type)) {
            if (messageIn.roomId != null) {
                room = repository.findRoom(messageIn.roomId);
                if (room == null) {
                    messageOut.type = "error";
                    messageOut.messageError = "The room was not found!";
                    sendMessage(session, messageOut);
                }
            } else {
                messageOut.type = "error";
                messageOut.messageError = "There is not enough data, I need: roomId";
                sendMessage(session, messageOut);
            }
        }
        if (room != null && !"resize-room".equals(messageIn.type)) {
            Snake snake = room.addSnakeRoom();
            messageOut.type = "room-id";
            messageOut.roomId = room.getId();
            sendMessage(session, messageOut);
            repository.bind(session, room, snake);
            System.out.println("///Вошли в комнату!");
        }
    }

    private void prepareDataCoordinateRoomClients(Room room) {
//        System.out.println("Отправляем данные!!!");
        List<WebSocketSession> sockets = repository.getRoomMembers(room.getId());
        if (sockets != null) {
            for (WebSocketSession socket : sockets) {
                MessageOut message = new MessageOut();
                Snake snake = repository.findSnake(socket);
                if (snake.getSnake().isEmpty()) {
                    message.type = "game-over";
                } else {
                    message.type = "event";
                }
                message.snakes = new LinkedList<>(room.getSnakes());
                message.snakes.remove(snake);
                message.mySnake = snake;
                message.food = room.getFood();

                sendMessage(socket, message);
            }
        } else {
            System.out.println("Некому отправить сообщение!!!");
        }
    }

    private void sendMessage(WebSocketSession session, Object object) {
        try {
            session.sendMessage(new TextMessage(mapper.writeValueAsString(object)));
        } catch (Exception e) {
            e.getMessage();
        }
    }
}
