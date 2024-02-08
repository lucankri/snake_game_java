const scoreElement = document.getElementById("score");
const canvas = document.getElementById("gameCanvas");
const boardContext = canvas.getContext("2d");
const gradientBoard = boardContext.createLinearGradient(0, 0, canvas.width, canvas.height);
const colorSnake = "#fefefe";

document.addEventListener("DOMContentLoaded", function () {
    let previousDirection;
    let currentDirection = "STOP";
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
        if (previousDirection === "STOP") {
            setInterval(fetchBoard, 50);
        }
    }


    let boardWidth, boardHeight;
    let cellWidth, cellHeight;
    let snakeCoordinate;
    let foodCoordinate;

    gradientBoard.addColorStop(0, "#0d163f");
    gradientBoard.addColorStop(1, "#00020a");

    function drawSnake() {
        let cellMin = Math.min(cellWidth, cellHeight);
        let radius = cellMin / 2;
        let radiusTL, radiusTR, radiusBR, radiusBL;
        let eyeX1, eyeY1, eyeX2, eyeY2; // Координаты глаз
        let xPx = snakeCoordinate[0].x * cellWidth;
        let yPx = snakeCoordinate[0].y * cellHeight;
        if (previousDirection === "UP" || previousDirection === "STOP") {
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
            drawRoundedSquare(xPx, yPx, cellWidth, cellHeight, radius, radius, radius, radius, "greenyellow");
        }
    }

    function drawBoard() {
        boardContext.clearRect(0, 0, canvas.width, canvas.height);
        boardContext.fillStyle = gradientBoard;
        boardContext.fillRect(0, 0, canvas.width, canvas.height);
        drawSnake();
        drawFood();
    }

    // function initializationGame() {
    //     fetch('/initialization')
    //         .then()
    //         .catch(error => console.error("Error: " + error));
    // }

    function getSizeBoard() {
        fetch("/board-size")
            .then(response => response.json())
            .then(size => {
                boardWidth = size.width;
                boardHeight = size.height;
                cellWidth = canvas.width / boardWidth;
                cellHeight = canvas.height / boardHeight;
            })
            .catch(error => console.error("Error fetching board size:", error));
    }

    function fetchBoard() {
        Promise.all([
            fetch("/snake-coordinates").then(response => response.json()),
            fetch("/food-coordinates").then(response => response.json()),
            fetch("/score").then(response => response.text()),
            fetch("/direction").then(response => response.text())
        ])
            .then(([snakeData, foodData, score, direction]) => {
                snakeCoordinate = snakeData;
                foodCoordinate = foodData;
                previousDirection = direction;
                updateScore(score);
                drawBoard();
                moveSnake(currentDirection);
            })
            .catch(error => console.error("Error fetching board:", error));
    }

    function moveSnake(direction) {
        fetch('/move', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: new URLSearchParams({
                direction: direction
            })
        }).then(response => {
            if (!response.ok) {
                console.error("Failed to move snake:", response.statusText);
            }
        }).catch(error => console.error("Error moving snake:", error));
    }

    // initializationGame();
    getSizeBoard();
    fetchBoard();
});


function updateScore(score) {
    scoreElement.textContent = "Очки: " + score;
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
    boardContext.fillRect(eyeX1, eyeY1, eyeSize, eyeSize); // Отрисовка глаза
    boardContext.fillRect(eyeX2, eyeY2, eyeSize, eyeSize); // Отрисовка глаза
}

window.addEventListener('beforeunload', function (event) {
    // Отправляем запрос на сервер о закрытии страницы
    fetch("/close-page", {
        method: 'DELETE'
    }).catch(error => console.error("Error moving snake:", error));
});
