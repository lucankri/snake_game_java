const socket = new WebSocket("ws://localhost:8080/game-ws");
const scoreElement = document.getElementById("score");
const canvas = document.getElementById("gameCanvas");
const boardContext = canvas.getContext("2d");
const gradientBoard = boardContext.createLinearGradient(0, 0, canvas.width, canvas.height);
const logo = document.getElementById("logotype");
const containerGame = document.getElementById("containerGame");
const settingsMenu = document.getElementById("settings-menu");
const menuGame = document.getElementById("menuGame");
const buttonMenuSet1 = document.getElementById("menu-button-set-1");
const buttonMenuSet2 = document.getElementById("menu-button-set-2");
const formSnakeRoom = document.getElementById("form-snake-room");
const buttonPerform = document.getElementById("button-perform");
const buttonRestart = document.getElementById("restart");
const buttonSingleUser = document.getElementById("single-user-button");
const buttonMultiUser = document.getElementById("multi-user-button");
const buttonCreateRoom = document.getElementById("create-room-button");
const buttonEnterRoom = document.getElementById("enter-room-button");
const inputSnakeName = document.getElementById("snake-name");
const inputRoomName = document.getElementById("room-name");

const colorMySnake = "#fefefe";
const colorEnemiesSnakes = "#AA0000"
const colorFoods = "greenyellow";

let roomId, nameSnake = null;
let message;

function mainScreen() {
    logo.style.display = "grid"
    buttonMenuSet1.style.visibility = "visible";
    buttonMenuSet2.style.visibility = "hidden";
    containerGame.style.visibility = "hidden";
    settingsMenu.style.display = "none";
}

document.addEventListener("DOMContentLoaded", function () {
    let boardWidth = 30, boardHeight = 20, amountFood = 1, lvl = 500;
    let cellWidth, cellHeight, cellMin, radius;
    let mySnake, enemiesSnakes, food;

    gradientBoard.addColorStop(0, "#0d163f");
    gradientBoard.addColorStop(1, "#00020a");

    function calculateCell() {
        cellWidth = canvas.width / boardWidth;
        cellHeight = canvas.height / boardHeight;
        cellMin = Math.min(cellWidth, cellHeight);
        radius = cellMin / 2;
    }

    // Обработчик события при открытии соккета
    socket.onopen = function(event) {
        console.log("WebSocket соединение установлено");
        mainScreen();
    };

    buttonSingleUser.addEventListener("click", function () {
        buttonMenuSet1.style.visibility = "hidden";
        message = {
            type: "create-room",
            roomWidth: boardWidth,
            roomHeight: boardHeight,
            amountFood: amountFood,
            interval: lvl,
            roomId: null
        }
        socket.send(JSON.stringify(message));
        containerGame.style.visibility = "visible";
        settingsMenu.style.display = "inline-block";
    });

    buttonMultiUser.addEventListener("click", function () {
        buttonMenuSet1.style.visibility = "hidden";
        buttonMenuSet2.style.visibility = "visible"
    });

    // Обработчик события при нажатии на кнопку CreateRoom
    function createRoomOnClick() {
        buttonMenuSet2.style.visibility = "hidden";
        formSnakeRoom.style.visibility = "visible";
        buttonPerform.removeEventListener("click", enterRoomHandler);
        buttonPerform.addEventListener("click", createRoomHandler);
    }

// Обработчик события при нажатии на кнопку EnterRoom
    function enterRoomOnClick() {
        buttonMenuSet2.style.visibility = "hidden";
        formSnakeRoom.style.visibility = "visible";
        buttonPerform.removeEventListener("click", createRoomHandler);
        buttonPerform.addEventListener("click", enterRoomHandler);
    }

    function createRoomHandler() {
        if (checkInputEmpty()) {
            message = {
                type: "create-room",
                roomWidth: boardWidth,
                roomHeight: boardHeight,
                amountFood: amountFood,
                interval: lvl,
                roomId: roomId
            }
            socket.send(JSON.stringify(message));
            formSnakeRoom.style.visibility = "hidden";
            containerGame.style.visibility = "visible";
            settingsMenu.style.display = "inline-block";
        }
    }

    function enterRoomHandler() {
        if (checkInputEmpty()) {
            message = {
                type: "join-room",
                roomWidth: boardWidth,
                roomHeight: boardHeight,
                amountFood: amountFood,
                interval: lvl,
                roomId: roomId
            }
            socket.send(JSON.stringify(message));
            formSnakeRoom.style.visibility = "hidden";
            containerGame.style.visibility = "visible";
        }
    }
    buttonCreateRoom.addEventListener("click", createRoomOnClick);
    buttonEnterRoom.addEventListener("click", enterRoomOnClick);

    let isMessageProcessing = true;
// Обработчик события при получении сообщения от сервера
    socket.onmessage = function(event) {
        if (!isMessageProcessing) {
            return;
        }
        console.log("Получено сообщение от сервера:", event.data);
        let data = JSON.parse(event.data);
        if (data.type === "room-id") {
            roomId = data.roomId;
            calculateCell();
        } else if (data.type === "event") {
            mySnake = data.mySnake;
            enemiesSnakes = data.snakes;
            food = data.food;
            drawBoard();
        } else if (data.type === "game-over") {
            isMessageProcessing = false;
            buttonRestart.style.visibility = "visible";
        } else if (data.type === "error") {
            console.error(data);
        }


        // Добавьте здесь обработку полученных данных от сервера

    };

// Обработчик события закрытия соединения
    socket.onclose = function(event) {
        console.log("WebSocket соединение закрыто");
    };

// Обработчик события ошибки
    socket.onerror = function(error) {
        console.error("Произошла ошибка WebSocket:", error);
    };


    function drawBoard() {
        boardContext.clearRect(0, 0, canvas.width, canvas.height);
        boardContext.fillStyle = gradientBoard;
        boardContext.fillRect(0, 0, canvas.width, canvas.height);
        drawSnake(mySnake, colorMySnake);
        for (let i = 0; i < enemiesSnakes.length; ++i) {
            drawSnake(enemiesSnakes[i], colorEnemiesSnakes);
        }
        drawFood();
        // updateScore(mySnake.score, nameSnake);
    }

    function drawSnake(snake, color) {
        if (snake.points.length !== 0) {
            let radiusTL, radiusTR, radiusBR, radiusBL;
            let eyeX1, eyeY1, eyeX2, eyeY2; // Координаты глаз
            let xPx = snake.points[0].x * cellWidth;
            let yPx = snake.points[0].y * cellHeight;
            if (snake.direction === "UP" || snake.direction === "GAME_START") {
                radiusBR = radiusBL = 0;
                radiusTL = radiusTR = radius;
                eyeX1 = xPx + cellWidth / 4;
                eyeY1 = eyeY2 = yPx + cellHeight / 4;
                eyeX2 = xPx + 3 * cellWidth / 4;
            } else if (snake.direction === "DOWN") {
                radiusTL = radiusTR = 0;
                radiusBR = radiusBL = radius;
                eyeX1 = xPx + cellWidth / 4;
                eyeY1 = eyeY2 = yPx + cellHeight / 4 * 3;
                eyeX2 = xPx + 3 * cellWidth / 4;
            } else if (snake.direction === "LEFT") {
                radiusTR = radiusBR = 0;
                radiusTL = radiusBL = radius;
                eyeX1 = eyeX2 = xPx + cellWidth / 4;
                eyeY1 = yPx + cellHeight / 4 * 3;
                eyeY2 = yPx + cellHeight / 4;
            } else if (snake.direction === "RIGHT") {
                radiusTL = radiusBL = 0;
                radiusTR = radiusBR = radius;
                eyeX1 = eyeX2 = xPx + cellWidth / 4 * 3;
                eyeY1 = yPx + cellHeight / 4 * 3;
                eyeY2 = yPx + cellHeight / 4;
            }
            console.log(xPx, yPx, cellWidth, cellHeight, radiusTL, radiusTR, radiusBR, radiusBL, color);
            drawRoundedSquare(xPx, yPx, cellWidth, cellHeight, radiusTL, radiusTR, radiusBR, radiusBL, color);
            eyeRendering(cellMin / 5, eyeX1, eyeY1, eyeX2, eyeY2, "#000");
            boardContext.fillStyle = color;
            for (let i = 1; i < snake.points.length; ++i) {
                xPx = snake.points[i].x * cellWidth;
                yPx = snake.points[i].y * cellHeight;
                boardContext.fillRect(xPx, yPx, cellWidth, cellHeight);
            }
        }
    }

    function drawFood() {
        let radius = Math.min(cellWidth, cellHeight) / 2;
        for (let i = 0; i < food.points.length; ++i) {
            let xPx = food.points[i].x * cellWidth;
            let yPx = food.points[i].y * cellHeight;
            drawRoundedSquare(xPx, yPx, cellWidth, cellHeight, radius, radius, radius, radius, colorFoods);
        }
    }

    document.addEventListener("keydown", function(event) {
        if (document.activeElement.tagName !== "INPUT") {
            event.preventDefault();
        }
        let direction;
        switch(event.key) {
            case "ArrowUp":
                direction = "UP";
                break;
            case "ArrowDown":
                direction = "DOWN";
                break;
            case "ArrowLeft":
                direction = "LEFT";
                break;
            case "ArrowRight":
                direction = "RIGHT";
                break;
        }
        message = {
            type: "snake-dir-change",
            direction: direction
        }
        if (message.type === "snake-dir-change") {
            socket.send(JSON.stringify(message));
            console.log(JSON.stringify(message));
        }
    });

    buttonRestart.addEventListener("click", function () {
        buttonRestart.style.visibility = "hidden";
        message = {
            type: "restart-room",
            roomId: roomId
        }
        socket.send(JSON.stringify(message));
        isMessageProcessing = true;
    });

    menuGame.addEventListener('click', function(event) {
        event.preventDefault();

        if (event.target.tagName === 'A') {
            let linkText = new URL(event.target.href).pathname;
            switch (linkText) {
                case "/lvl-slow":
                    lvl = 200;
                    break;
                case "/lvl-average":
                    lvl = 150;
                    break;
                case "/lvl-fast":
                    lvl = 70;
                    break;
                case "/board-little":
                    boardWidth = 15;
                    boardHeight = 10;
                    break;
                case "/board-average":
                    boardWidth = 30;
                    boardHeight = 20;
                    break;
                case "/board-big":
                    boardWidth = 60;
                    boardHeight = 40;
                    break;
                case "/foods-1":
                    amountFood = 1;
                    break;
                case "/foods-3":
                    amountFood = 3;
                    break;
                case "/foods-6":
                    amountFood = 6;
                    break;
            }
            if (linkText === "/exit") {
                message = {
                    type: "exit-room"
                }
                settingsMenu.style.display = "none";
                containerGame.style.visibility = "hidden";
                buttonMenuSet1.style.visibility = "visible"
                buttonRestart.style.visibility = "hidden";
            } else {
                message = {
                    type: "resize-room",
                    roomId: roomId,
                    roomWidth: boardWidth,
                    roomHeight: boardHeight,
                    amountFood: amountFood,
                    interval: lvl
                }
                calculateCell();
            }
            socket.send(JSON.stringify(message));
            console.log(JSON.stringify(message));
        }
    }, false);



    // let previousDirection;
    // let currentDirection;
    // let snakeCoordinate;
    // let foodCoordinate;

    // document.addEventListener("keydown", function(event) {
    //     event.preventDefault();
    //     switch(event.key) {
    //         case "ArrowUp":
    //             currentDirection = "UP";
    //             break;
    //         case "ArrowDown":
    //             currentDirection = "DOWN";
    //             break;
    //         case "ArrowLeft":
    //             currentDirection = "LEFT";
    //             break;
    //         case "ArrowRight":
    //             currentDirection = "RIGHT";
    //             break;
    //     }
    // });

    // function fetchBoardStart() {
    //     if (intervalId === null) {
    //         intervalId = setInterval(fetchBoard, lvl);
    //     }
    // }
    //
    // function fetchBoardStop() {
    //     if (intervalId !== null) {
    //         clearInterval(intervalId);
    //         intervalId = null;
    //     }
    // }
    //
    //
    // function initialization() {
    //     return fetch(`/initialization?width=${boardWidth}&height=${boardHeight}&sizeFoods=${sizeFoods}`)
    //         .then(response => {
    //             if (!response.ok) {
    //                 throw new Error(`Ошибка HTTP: ${response.status}`);
    //             }
    //             cellWidth = canvas.width / boardWidth;
    //             cellHeight = canvas.height / boardHeight;
    //             previousDirection = "";
    //             currentDirection = "GAME_START";
    //     }).catch(error => {
    //             window.location.href = "/";
    //             console.error("Error ", error)
    //     });
    // }
    //
    //


    // function drawBoard() {
    //     boardContext.clearRect(0, 0, canvas.width, canvas.height);
    //     boardContext.fillStyle = gradientBoard;
    //     boardContext.fillRect(0, 0, canvas.width, canvas.height);
    //     drawSnake();
    //     drawFood();
    // }

    // function fetchBoard() {
    //     return fetch('/game-move-data', {
    //         method: 'POST',
    //         body: new URLSearchParams({
    //             direction: currentDirection
    //         })
    //     })
    //     .then(response => {
    //         if (!response.ok) {
    //             throw new Error("Ошибка при получении данных игры");
    //         }
    //         return response.json();
    //     })
    //     .then(data => {
    //         snakeCoordinate = data.snakeCoordinates;
    //         foodCoordinate = data.foodCoordinates;
    //         previousDirection = data.direction;
    //         if (data.move === false) {
    //             fetchBoardStop();
    //             intervalId = "0";
    //             buttonRestart.style.visibility = "visible";
    //         }
    //         updateScore(data.score);
    //         drawBoard();
    //     })
    //     .catch(error => {
    //         fetchBoardStop();
    //         initialization();
    //     });
    // }
    //
    // buttonRestart.addEventListener("click", function () {
    //     buttonRestart.style.visibility = "hidden";
    //     intervalId = null;
    //     initialization().then(() => {
    //         fetchBoard().then(() => {
    //             checkingPageUpdate(previousDirection, intervalId);
    //         });
    //     });
    // });
    // initialization().then(() => {
    //     fetchBoard().then(() => {
    //         checkingPageUpdate(previousDirection, intervalId);
    //     });
    // });

    // message = {
    //     type: "room-create",
    //     roomWidth: 30,
    //     roomHeight: 20,
    //     foodAmount: 3,
    //     interval: 2000
    // };
    // socket.send(JSON.stringify(message));

// Обработчик события открытия соединения


});

const backgroundColorInput = window.getComputedStyle(inputSnakeName).backgroundColor;
function checkInputEmpty() {
    let snake = inputSnakeName.value;
    let room = inputRoomName.value;
    if (snake.trim() !== "" && room.trim() !== "") {
        roomId = room;
        nameSnake = snake;
        return true;
    }
    if (snake.trim() === "") {
        inputSnakeName.style.backgroundColor = "#502235";
    } else {
        inputSnakeName.style.backgroundColor = backgroundColorInput;
    }
    if (room.trim() === "") {
        inputRoomName.style.backgroundColor = "#502235";
    } else {
        inputRoomName.style.backgroundColor = backgroundColorInput;
    }
    return false;
}

function updateScore(score, nameSnake) {
    let text;
    if (nameSnake == null) {
         text = "Очки: " + score;
    } else {
        text = nameSnake + ": " + score;
    }
    scoreElement.textContent = text;
}

function checkingPageUpdate(direction) {
    if (direction !== "GAME_START") {
        buttonRestart.style.visibility = "visible";
        intervalId = "0";
    }
}

function drawRoundedSquare(x, y, width, height, radiusTL, radiusTR, radiusBR, radiusBL, color) {
    boardContext.fillStyle = color;
    boardContext.beginPath();

    // Верхняя левая дуга
    boardContext.arc(x + radiusTL, y + radiusTL, radiusTL, -Math.PI, -Math.PI / 2);

    // Верхняя сторона
    boardContext.lineTo(x + width - radiusTR, y);

    // Верхняя правая дуга
    boardContext.arc(x + width - radiusTR, y + radiusTR, radiusTR, -Math.PI / 2, 0);

    // Правая сторона
    boardContext.lineTo(x + width, y + height - radiusBR);

    // Нижняя правая дуга
    boardContext.arc(x + width - radiusBR, y + height - radiusBR, radiusBR, 0, Math.PI / 2);

    // Нижняя сторона
    boardContext.lineTo(x + radiusBL, y + height);

    // Нижняя левая дуга
    boardContext.arc(x + radiusBL, y + height - radiusBL, radiusBL, Math.PI / 2, Math.PI);

    // Левая сторона
    boardContext.lineTo(x, y + radiusTL);

    // Закрываем контур
    boardContext.closePath();

    // Заливаем фигуру
    boardContext.fill();
}

function eyeRendering(eyeSize, eyeX1, eyeY1, eyeX2, eyeY2, color) {
    boardContext.fillStyle = color; // Цвет глаз
    boardContext.fillRect(eyeX1, eyeY1, eyeSize, eyeSize); // Отрисовка глаз
    boardContext.fillRect(eyeX2, eyeY2, eyeSize, eyeSize); // Отрисовка глаз
}
