package org.jugvale.javafx;

import java.util.Random;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

class StackerGame extends Game {

    /*
     The number of points that can be awarded
     */
    int MAX_POINTS = 15;
    int MIN_POINTS = 5;

    /*
     Indicates when the player lose the game
     */
    public BooleanProperty gameOver;

    /*
     The total of points
     */
    public IntegerProperty score;

    int INITIAL_COL = 3;
    int INITIAL_LINES = 2;

    /*
     Initial number of lines and columns which will change according to the game level
     */
    int _LINES = INITIAL_LINES;
    int _COLUMNS = INITIAL_COL;

    /*
     The size of each rectangle, which will change according to the number of lines and columns
     */
    float _W;
    float _H;

    /*
     The higest level which someone could reach
     */
    int MAX_LEVEL = 20;

    /*
     Level which will be increased as soon as the user reaches the top
     */
    public IntegerProperty level;

    /*
     The current line which the rectangle will be moving 
     */
    int MOVING_LINE;

    /*
     The direction of the rectangle movement
     */
    int direction = 1;

    /*
     The matrix of our "squares"(actually rectangles) which size is dinamic
     */
    boolean[][] squares;

    boolean mousePressed = false;

    Random random;

    StackerGame(float w, float h, GraphicsContext _gc) {
        super(w, h, _gc);
        _gc.getCanvas().setOnMousePressed(e -> {
            mousePressed = true;
        });
        gameOver = new SimpleBooleanProperty(false);
        score = new SimpleIntegerProperty(0);
        level = new SimpleIntegerProperty(1);
        random = new Random();
        updateMatrix();
    }

    @Override
    public void update() {
        if (gameOver.get()) {
            if (mousePressed) {
                mousePressed = false;
                restart();
            }
            return;
        }
        int curPos;
        // update the square's position
        // we will not always move the square, the pos update will be faster according to how close user gets to the top
        int levelIncrease = level.get();
        levelIncrease += 1 + _LINES - MOVING_LINE;

        levelIncrease = constrain(levelIncrease, 0, MAX_LEVEL);
        int rate = (int) map(levelIncrease, 1, MAX_LEVEL, engine.getFrameRate(), engine.getFrameRate() / 20);
        boolean updatePos = engine.getFrameCount() % rate == 0;
        for (curPos = 0; curPos < _COLUMNS && updatePos; curPos++) {
            if (squares[curPos][MOVING_LINE]) {
                if (curPos == 0) {
                    direction = 1;
                } else if (curPos == _COLUMNS - 1) {
                    direction = -1;
                }
                // update the square matrix
                squares[curPos][MOVING_LINE] = false;
                if (_COLUMNS != 1) {
                    squares[curPos + direction][MOVING_LINE] = true;
                }
                break;
            }
        }
        // if user press a key, move to the line above
        if (mousePressed) {
            checkStack();
            if (MOVING_LINE == 0) {
                level.set(level.get() + 1);
                updateMatrix();
            } else {
                MOVING_LINE--;
                squares[(int) random.nextInt(_COLUMNS - 1)][MOVING_LINE] = true;
            }
            mousePressed = false;
        }
    }

    void drawSquares() {
        for (int x = 0; x < _COLUMNS; x++) {
            for (int y = 0; y < _LINES; y++) {
                if (squares[x][y]) {
                    gc.setFill(Color.RED);
                    gc.fillRect(x * _W, y * _H, _W, _H);
                    if (y != _LINES - 1 && y != MOVING_LINE) {
                        int leftColumn = x == 0 ? -1 : x - 1;
                        int rightColumn = (x == _COLUMNS - 1) ? -1 : x + 1;
                        int line = y + 1;
                        if (leftColumn != -1 || rightColumn != -1) {
                            gc.setFill(Color.color(1, 0, 0, 0.5));
                            gc.fillRect(x * _W, line * _H, _W, _H);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void display() {
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, MAX_W, MAX_H);
        drawGrid();
        drawSquares();
    }

    void drawGrid() {
        gc.setStroke(Color.gray(0, 0.2));
        for (int x = 0; x < _COLUMNS; x++) {
            for (int y = 0; y < _LINES; y++) {
                gc.strokeRect(x * _W, y * _H, _W, _H);
            }
        }
    }

    private void updateMatrix() {
        _LINES = level.get() * 2;
        _COLUMNS += level.get() % 3 == 0 ? 1 : 0;
        squares = new boolean[_COLUMNS][_LINES];
        squares[0][_LINES - 1] = true;
        MOVING_LINE = _LINES - 1;
        _W = MAX_W / _COLUMNS;
        _H = MAX_H / _LINES;

    }

    void checkStack() {
        // no need to check at the first line
        if (MOVING_LINE == _LINES - 1) {
            return;
        }
        for (int i = 0; i < _COLUMNS; i++) {
            if (squares[i][MOVING_LINE]) {
                int lineToCheck = MOVING_LINE + 1;
                int leftColumn = i == 0 ? -1 : i - 1;
                int rightColumn = (i == _COLUMNS - 1) ? -1 : i + 1;
                // perfect stack, highest score
                if (squares[i][lineToCheck]) {
                    score.set(score.get() + MAX_POINTS);
                } else if ((leftColumn != -1 && squares[leftColumn][lineToCheck])
                        || (rightColumn != -1 && squares[rightColumn][lineToCheck])) {
                    score.set(score.get() + MIN_POINTS);
                } else {
                    gameOver.setValue(true);
                }
            }
        }
    }

    public void restart() {
        level.set(1);
        score.set(0);
        gameOver.setValue(false);
        _COLUMNS = INITIAL_COL;
        _LINES = INITIAL_LINES;
        updateMatrix();
    }

    /*
     Utility methods from Processing
     */
    int constrain(int v, int min, int max) {
        if (v < min) {
            return min;
        }
        if (v > max) {
            return max;
        } else {
            return v;
        }
    }

    private float map(float value,
            float start1, float stop1,
            float start2, float stop2) {
        return start2 + (stop2 - start2) * ((value - start1) / (stop1 - start1));
    }

}