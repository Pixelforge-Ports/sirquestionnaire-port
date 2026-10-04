package org.portmaster.sirquestionnaire;

import com.badlogic.gdx.graphics.g2d.BitmapFont;

/** Enlarge text while keeping the game's own wrapping and width measurements. */
final class ReadableFonts {
    private BitmapFont[] known = new BitmapFont[0];
    private float[] scaleX = new float[0], scaleY = new float[0], capHeight = new float[0];

    void apply(BitmapFont[] fonts, int width, int height, boolean overlay) {
        if (fonts == null) return; // The game loads fonts during its first logic frame.
        if (known.length != fonts.length) {
            known = new BitmapFont[fonts.length];
            scaleX = new float[fonts.length]; scaleY = new float[fonts.length];
            capHeight = new float[fonts.length];
        }
        // The game's high-resolution UI uses 320 vertical units in landscape,
        // and 240 horizontal units on square/portrait screens, with integer scaling.
        float pixelsPerUnit = Math.max(1, (float)Math.floor(width > height ? height/320.0 : width/240.0));
        float readableHeight = width >= 1000 ? 20f : height >= 640 ? 16f : 12f;
        for (int i=0; i<fonts.length; i++) {
            BitmapFont font = fonts[i];
            if (font == null) continue;
            BitmapFont.BitmapFontData data = font.getData();
            if (known[i] != font) {
                known[i] = font;
                scaleX[i] = data.scaleX; scaleY[i] = data.scaleY;
                capHeight[i] = Math.abs(data.capHeight);
            }
            // Size interface text in physical pixels; square screens magnify UI units
            // more than landscape screens. Leave world labels and large headings alone.
            float verticalFactor = 1f;
            if (overlay && i!=2 && capHeight[i] > 0)
                verticalFactor = readableHeight/(capHeight[i]*pixelsPerUnit);
            // Keep fixed-width labels on one line while giving small letters more height.
            float x = scaleX[i]*Math.min(1.25f, verticalFactor), y = scaleY[i]*verticalFactor;
            if (data.scaleX != x || data.scaleY != y) data.setScale(x, y);
        }
    }
}
