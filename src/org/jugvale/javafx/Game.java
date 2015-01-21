package org.jugvale.javafx;

import javafx.scene.canvas.GraphicsContext;

/**
 *
 * @author william
 */
public abstract class Game {

    /*
        The GraphicsContext so the game can draw stuff
    */
    GraphicsContext gc;

    GameEngine engine;

    /*
     The size of the game playing area 
     */
    float MAX_W;
    float MAX_H;

    public Game(float w, float h, GraphicsContext _gc) {
        MAX_W = w;
        MAX_H = h;
        gc = _gc;
        gc.getCanvas().setWidth(w);
        gc.getCanvas().setHeight(h);
    }

    final public void setEngine(GameEngine _engine) {
        engine = _engine;
    }

    public abstract void update();

    public abstract void display();

}
