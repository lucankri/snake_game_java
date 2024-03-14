const scoreElement = document.getElementById("score");
const canvas = document.getElementById("gameCanvas");
const boardContext = canvas.getContext("2d");
const gradientBoard = boardContext.createLinearGradient(0, 0, canvas.width, canvas.height);
const logo = document.getElementById("logotype");
const textError = document.getElementById("text-error");
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
const colorEnemiesSnakes = "#FF0000"
const colorFoods = "greenyellow";

let roomId, nameSnake = null;
let message;

function mainScreen(logotype, set1, set2, game, form, restart) {
    logotype === true ? logo.style.display = "grid" : logo.style.display = "none";
    set1 === true ? buttonMenuSet1.style.visibility = "visible" : buttonMenuSet1.style.visibility = "hidden";
    set2 === true ? buttonMenuSet2.style.visibility = "visible" : buttonMenuSet2.style.visibility = "hidden";
    game === true ? containerGame.style.visibility = "visible" : containerGame.style.visibility = "hidden";
    form === true ? formSnakeRoom.style.visibility = "visible" : formSnakeRoom.style.visibility = "hidden";
    restart === true ? buttonRestart.style.visibility = "visible" : buttonRestart.style.visibility = "hidden";
}

document.addEventListener("DOMContentLoaded", function () {
    let boardWidth = 30, boardHeight = 20, amountFood = 1, lvl = 150;
    let walls = false;
    let isGameOverProcessing = true;
    let cellWidth, cellHeight, cellMin, radius;
    let mySnake = null, enemiesSnakes = null, food;
    // let previousEnemiesSnakes, previousMySnake;
    let socket;
    setTimeout(function () {
        socket = new WebSocket("ws://localhost:8080/game-ws");

        // Обработчик события при открытии соккета
        socket.onopen = function(event) {
            console.log("WebSocket соединение установлено");
            mainScreen(true, true, false, false, false, false);
            setTextError("");
            settingsMenu.style.display = "none";
        };

        // Обработчик события при получении сообщения от сервера
        socket.onmessage = function(event) {
            // console.log("Получено сообщение от сервера:", event.data);
            let data = JSON.parse(event.data);
            if (data.type === "start-room") {
                roomId = data.roomId;
                boardWidth = data.roomWidth;
                boardHeight = data.roomHeight;
                lvl = data.intervalMs;
                walls = data.walls;
                calculateCell();
                mainScreen(false, false, false, true, false, false);
                setTextError("");
                isGameOverProcessing = true;
                if (data.creator === true) {
                    settingsMenu.style.display = "inline-block";
                }
                if (walls) {
                    canvas.style.boxShadow = "0 0 20px 15px #AA2200";
                } else {
                    canvas.style.boxShadow = "0 0 100px 1px #0099fd";
                }
            } else if (data.type === "event" || data.type === "game-over") {
                // previousEnemiesSnakes = enemiesSnakes;
                // previousMySnake = mySnake;
                // animateSnakeState();
                if (isGameOverProcessing && data.type === "game-over") {
                    mainScreen(false, false, false, true, false, true);
                    isGameOverProcessing = false;
                } else if (!isGameOverProcessing && data.type !== "game-over") {
                    mainScreen(false, false, false, true, false, false);
                    isGameOverProcessing = true;
                }
                mySnake = data.mySnake;
                enemiesSnakes = data.enemiesSnakes;
                food = data.food;
                drawBoard();
            } else if (data.type === "error") {
                mainScreen(true, true, false, false, false, false);
                setTextError(data.messageError);
                console.error(data);
            }

            // Обработчик события закрытия соединения
            socket.onclose = function(event) {
                console.log("WebSocket соединение закрыто");
                setTextError(event.reason);
                mainScreen(false, false, false, false, false, false);
            };

            // Обработчик события ошибки
            socket.onerror = function(error) {
                console.error("Произошла ошибка WebSocket:", error);
            };

        };
    }, 1000);





    // function animateSnakeState() {
    //     const duration = lvl / 1000.0; // Продолжительность анимации в секундах
    //     const interpolationSteps = Math.max(Math.ceil(lvl / 17), 1); // Количество шагов интерполяции
    //     let step = 0;
    //     if (previousMySnake !== null && previousEnemiesSnakes !== null) {
    //         const tween = gsap.to({}, {
    //             duration: duration,
    //             repeat: 0,
    //             paused: true,
    //             onUpdate: () => {
    //                 const interpolationFactor = step / interpolationSteps;
    //                 const interpolatedMySnake = interpolateSnakeState(previousMySnake, mySnake, interpolationFactor);
    //                 drawBoard();
    //                 drawSnake(interpolatedMySnake, colorMySnake);
    //                 for (let i = 0; i < previousEnemiesSnakes.length; ++i) {
    //                     const interpolatedEnemiesSnakes = interpolateSnakeState(previousEnemiesSnakes[i], enemiesSnakes[i], interpolationFactor);
    //                     drawSnake(interpolatedEnemiesSnakes, colorEnemiesSnakes);
    //                 }
    //                 step++;
    //             },
    //             onComplete: () => {
    //                 // Анимация завершена
    //             }
    //         });
    //         tween.play();
    //     } else {
    //         drawBoard();
    //         drawSnake(mySnake, colorMySnake);
    //         for (let i = 0; i < enemiesSnakes.length; ++i) {
    //             drawBoard(enemiesSnakes[i], colorEnemiesSnakes);
    //         }
    //     }
    // }
    //
    // function interpolateSnakeState(prevState, currentState, interpolationFactor) {
    //     const interpolatedPoints = [];
    //
    //     // Определяем количество точек для интерполяции
    //     const numPoints = Math.max(prevState.points.length, currentState.points.length);
    //
    //     for (let i = 0; i < numPoints; i++) {
    //         // Получаем точки из предыдущего и текущего состояний
    //         const prevPoint = prevState.points[i] || prevState.points[prevState.points.length - 1];
    //         const currentPoint = currentState.points[i] || currentState.points[currentState.points.length - 1];
    //
    //         // Интерполируем координаты точек
    //         const interpolatedX = prevPoint.x + (currentPoint.x - prevPoint.x) * interpolationFactor;
    //         const interpolatedY = prevPoint.y + (currentPoint.y - prevPoint.y) * interpolationFactor;
    //
    //         interpolatedPoints.push({ x: interpolatedX, y: interpolatedY });
    //     }
    //
    //     return { ...currentState, points: interpolatedPoints };
    // }


    gradientBoard.addColorStop(0, "#0d163f");
    gradientBoard.addColorStop(1, "#00020a");

    function calculateCell() {
        cellWidth = canvas.width / boardWidth;
        cellHeight = canvas.height / boardHeight;
        cellMin = Math.min(cellWidth, cellHeight);
        radius = cellMin / 2;
    }

    buttonSingleUser.addEventListener("click", function () {
        message = {
            type: "create-room",
            roomWidth: boardWidth,
            roomHeight: boardHeight,
            amountFood: amountFood,
            interval: lvl,
            walls: walls,
            roomId: null
        }
        socket.send(JSON.stringify(message));
        console.log(JSON.stringify(message));
        mainScreen(false, false, false, true, false, false);
    });

    buttonMultiUser.addEventListener("click", function () {
        mainScreen(true, false, true, false, false, false);
    });

    // Обработчик события при нажатии на кнопку CreateRoom
    function createRoomOnClick() {
        mainScreen(true, false, false, false, true, false);
        buttonPerform.removeEventListener("click", enterRoomHandler);
        buttonPerform.addEventListener("click", createRoomHandler);
    }

// Обработчик события при нажатии на кнопку EnterRoom
    function enterRoomOnClick() {
        mainScreen(true, false, false, false, true, false);
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
                walls: walls,
                roomId: roomId
            }
            socket.send(JSON.stringify(message));
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
                walls: walls,
                roomId: roomId
            }
            socket.send(JSON.stringify(message));
        }
    }
    buttonCreateRoom.addEventListener("click", createRoomOnClick);
    buttonEnterRoom.addEventListener("click", enterRoomOnClick);

    function drawBoard() {
        boardContext.clearRect(0, 0, canvas.width, canvas.height);
        boardContext.fillStyle = gradientBoard;
        boardContext.fillRect(0, 0, canvas.width, canvas.height);
        drawFood();
        drawSnake(mySnake, colorMySnake);
        for (let i = 0; i < enemiesSnakes.length; ++i) {
            drawSnake(enemiesSnakes[i], colorEnemiesSnakes);
        }
        updateScore(mySnake.score);
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
        for (let i = 0; i < food.points.length; ++i) {
            let xPx = food.points[i].x * cellWidth;
            let yPx = food.points[i].y * cellHeight;
            drawRoundedSquare(xPx, yPx, cellWidth, cellHeight, radius, radius, radius, radius, colorFoods);
        }
    }

    document.addEventListener("keydown", function(event) {
        if (document.activeElement.tagName !== "INPUT") {
            event.preventDefault();
            message = {
                type: "",
                direction: ""
            }
            switch(event.key) {
                case "ArrowUp":
                    message.type = "snake-dir-change";
                    message.direction = "UP";
                    break;
                case "ArrowDown":
                    message.type = "snake-dir-change";
                    message.direction = "DOWN";
                    break;
                case "ArrowLeft":
                    message.type = "snake-dir-change";
                    message.direction = "LEFT";
                    break;
                case "ArrowRight":
                    message.type = "snake-dir-change";
                    message.direction = "RIGHT";
                    break;
                case "Escape":
                    message.type = "exit-room";
                    mainScreen(true, true, false, false, false, false);
                    settingsMenu.style.display = "none";
                    setTextError("");
                    nameSnake = null;
                    roomId = null;
                    break;
                case " ":
                    let buttonRestartStyle = window.getComputedStyle(buttonRestart).getPropertyValue("visibility");
                    if (buttonRestartStyle === "visible") {
                        buttonRestart.click();
                    }
                    break;
            }
            if (message.type === "snake-dir-change" || message.type === "exit-room") {
                socket.send(JSON.stringify(message));
                console.log(JSON.stringify(message));
            }
        } else {
            if (event.key === "Enter") {
                buttonPerform.click();
            }
        }
    });

    buttonRestart.addEventListener("click", function () {
        message = {
            type: "restart-snake",
        }
        socket.send(JSON.stringify(message));
    });

    menuGame.addEventListener('click', function(event) {
        event.preventDefault();

        if (event.target.tagName === 'A') {
            let linkText = new URL(event.target.href).pathname;
            switch (linkText) {
                case "/lvl-slow":
                    lvl = 250;
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
                case "/walls-yes":
                    walls = true;
                    break;
                case "/walls-no":
                    walls = false;
                    break;
            }
            message = {
                type: "resize-room",
                roomId: roomId,
                roomWidth: boardWidth,
                roomHeight: boardHeight,
                amountFood: amountFood,
                interval: lvl,
                walls: walls
            }
            calculateCell();
            socket.send(JSON.stringify(message));
            console.log(JSON.stringify(message));
        }
    }, false);
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

function updateScore(score) {
    let text;
    if (nameSnake == null) {
         text = "Очки: " + score;
    } else {
        text = nameSnake + ": " + score;
    }
    scoreElement.textContent = text;
}

function setTextError(text) {
    textError.textContent = text;
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
