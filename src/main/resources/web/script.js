const scoreElement = document.getElementById("score");
const canvas = document.getElementById("gameCanvas");
const buttonRestart = document.getElementById("restart");
const boardContext = canvas.getContext("2d");
const gradientBoard = boardContext.createLinearGradient(0, 0, canvas.width, canvas.height);
const menu = document.getElementById("menu");


const colorSnake = "#fefefe";
const colorFoods = "greenyellow";


let intervalId = null;

document.addEventListener("DOMContentLoaded", function () {
    let previousDirection;
    let currentDirection;
    let boardWidth = 30, boardHeight = 20, sizeFoods = 1;
    let cellWidth, cellHeight;
    let snakeCoordinate;
    let foodCoordinate;
    let lvl = 200;

    document.addEventListener("keydown", function(event) {
        event.preventDefault();
        switch(event.key) {
            case "ArrowUp":
                currentDirection = "UP";
                break;
            case "ArrowDown":
                currentDirection = "DOWN";
                break;
            case "ArrowLeft":
                currentDirection = "LEFT";
                break;
            case "ArrowRight":
                currentDirection = "RIGHT";
                break;
        }
        fetchBoardStart();
    });

    function fetchBoardStart() {
        if (intervalId === null) {
            intervalId = setInterval(fetchBoard, lvl);
        }
    }

    function fetchBoardStop() {
        if (intervalId !== null) {
            clearInterval(intervalId);
            intervalId = null;
        }
    }


    function initialization() {
        return fetch(`/initialization?width=${boardWidth}&height=${boardHeight}&sizeFoods=${sizeFoods}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error(`Ошибка HTTP: ${response.status}`);
                }
                cellWidth = canvas.width / boardWidth;
                cellHeight = canvas.height / boardHeight;
                previousDirection = "";
                currentDirection = "GAME_START";
        }).catch(error => {
                window.location.href = "/";
                console.error("Error ", error)
        });
    }

    menu.addEventListener('click', function(event) {
        event.preventDefault();

        if (event.target.tagName === 'A') {
            let linkText = new URL(event.target.href).pathname;

            switch (linkText) {
                case "/lvl-slow":
                    lvl = 200;
                    break;
                case "/lvl-average":
                    lvl = 120;
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
                    sizeFoods = 1;
                    break;
                case "/foods-3":
                    sizeFoods = 3;
                    break;
                case "/foods-6":
                    sizeFoods = 6;
                    break;
            }
            fetchBoardStop();
            initialization().then(() => {
                fetchBoard();
                buttonRestart.style.visibility = "hidden";
            });
        }
    }, false);

    gradientBoard.addColorStop(0, "#0d163f");
    gradientBoard.addColorStop(1, "#00020a");

    function drawSnake() {
        let cellMin = Math.min(cellWidth, cellHeight);
        let radius = cellMin / 2;
        let radiusTL, radiusTR, radiusBR, radiusBL;
        let eyeX1, eyeY1, eyeX2, eyeY2; // Координаты глаз
        let xPx = snakeCoordinate[0].x * cellWidth;
        let yPx = snakeCoordinate[0].y * cellHeight;
        if (previousDirection === "UP" || previousDirection === "GAME_START") {
            radiusBR = radiusBL = 0;
            radiusTL = radiusTR = radius;
            eyeX1 = xPx + cellWidth / 4;
            eyeY1 = eyeY2 = yPx + cellHeight / 4;
            eyeX2 = xPx + 3 * cellWidth / 4;
        } else if (previousDirection === "DOWN") {
            radiusTL = radiusTR = 0;
            radiusBR = radiusBL = radius;
            eyeX1 = xPx + cellWidth / 4;
            eyeY1 = eyeY2 = yPx + cellHeight / 4 * 3;
            eyeX2 = xPx + 3 * cellWidth / 4;
        } else if (previousDirection === "LEFT") {
            radiusTR = radiusBR = 0;
            radiusTL = radiusBL = radius;
            eyeX1 = eyeX2 = xPx + cellWidth / 4;
            eyeY1 = yPx + cellHeight / 4 * 3;
            eyeY2 = yPx + cellHeight / 4;
        } else if (previousDirection === "RIGHT") {
            radiusTL = radiusBL = 0;
            radiusTR = radiusBR = radius;
            eyeX1 = eyeX2 = xPx + cellWidth / 4 * 3;
            eyeY1 = yPx + cellHeight / 4 * 3;
            eyeY2 = yPx + cellHeight / 4;
        }
        drawRoundedSquare(xPx, yPx, cellWidth, cellHeight, radiusTL, radiusTR, radiusBR, radiusBL, colorSnake);
        eyeRendering(cellMin / 5, eyeX1, eyeY1, eyeX2, eyeY2, "#000");
        boardContext.fillStyle = colorSnake;
        for (let i = 1; i < snakeCoordinate.length; ++i) {
            xPx = snakeCoordinate[i].x * cellWidth;
            yPx = snakeCoordinate[i].y * cellHeight;
            boardContext.fillRect(xPx, yPx, cellWidth, cellHeight);
        }
    }

    function drawFood() {
        let radius = Math.min(cellWidth, cellHeight) / 2;
        for (let i = 0; i < foodCoordinate.length; ++i) {
            let xPx = foodCoordinate[i].x * cellWidth;
            let yPx = foodCoordinate[i].y * cellHeight;
            drawRoundedSquare(xPx, yPx, cellWidth, cellHeight, radius, radius, radius, radius, colorFoods);
        }
    }

    function drawBoard() {
        boardContext.clearRect(0, 0, canvas.width, canvas.height);
        boardContext.fillStyle = gradientBoard;
        boardContext.fillRect(0, 0, canvas.width, canvas.height);
        drawSnake();
        drawFood();
    }

    function fetchBoard() {
        return fetch('/game-move-data', {
            method: 'POST',
            body: new URLSearchParams({
                direction: currentDirection
            })
        })
        .then(response => {
            if (!response.ok) {
                throw new Error("Ошибка при получении данных игры");
            }
            return response.json();
        })
        .then(data => {
            snakeCoordinate = data.snakeCoordinates;
            foodCoordinate = data.foodCoordinates;
            previousDirection = data.direction;
            if (data.move === false) {
                fetchBoardStop();
                intervalId = "0";
                buttonRestart.style.visibility = "visible";
            }
            updateScore(data.score);
            drawBoard();
        })
        .catch(error => {
            fetchBoardStop();
            initialization();
        });
    }


    buttonRestart.addEventListener("click", function () {
        buttonRestart.style.visibility = "hidden";
        intervalId = null;
        initialization().then(() => {
            fetchBoard().then(() => {
                checkingPageUpdate(previousDirection, intervalId);
            });
        });
    });

    initialization().then(() => {
        fetchBoard().then(() => {
            checkingPageUpdate(previousDirection, intervalId);
        });
    });
});

function updateScore(score) {
    scoreElement.textContent = "Очки: " + score;
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
