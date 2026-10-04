package org.portmaster.sirquestionnaire;

/** Give the game the device's native aspect ratio without letterboxing or stretching. */
public final class DisplayLayout {
    public int screenWidth = 640, screenHeight = 480;
    public int gameWidth = 640, gameHeight = 480;
    public int x, y, width = 640, height = 480;
    public void resize(int w, int h) {
        if (w < 160 || h < 160) return;
        screenWidth = w; screenHeight = h;
        gameWidth = width = w; gameHeight = height = h;
        x = y = 0;
    }
    public int viewportX(int value) { return x + (int)Math.round((double)value * width / gameWidth); }
    public int viewportY(int value) { return y + (int)Math.round((double)value * height / gameHeight); }
    public int viewportWidth(int value) { return (int)Math.round((double)value * width / gameWidth); }
    public int viewportHeight(int value) { return (int)Math.round((double)value * height / gameHeight); }
    public int inputX(int value) { return (int)((double)(value - x) * gameWidth / width); }
    public int inputY(int value) {
        return (int)((double)(value - (screenHeight - y - height)) * gameHeight / height);
    }
    public int cursorX(int value) { return viewportX(value); }
    public int cursorY(int value) { return screenHeight - y - height + viewportHeight(value); }
}
