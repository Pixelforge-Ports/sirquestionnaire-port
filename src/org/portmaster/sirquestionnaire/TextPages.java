package org.portmaster.sirquestionnaire;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.utils.Align;

/** Show complete wrapped lines, including their original markup, without clipping glyphs. */
final class TextPages {
    private final GlyphLayout full = new GlyphLayout(), page = new GlyphLayout();
    private String value;
    private float width, scaleX, scaleY, inkTop, inkHeight, lineHeight;
    private int totalRows;
    private long since;
    int rowsPerPage, pageCount, pageIndex, visibleRows;
    float drawnTop, drawnBottom;

    void prepare(BitmapFont font, String text, float availableWidth, float availableHeight) {
        BitmapFont.BitmapFontData data = font.getData();
        if (!text.equals(value) || width != availableWidth || Math.abs(scaleX-data.scaleX)>0.0001f || Math.abs(scaleY-data.scaleY)>0.0001f) {
            page.runs.clear();page.colors.clear();
            full.setText(font, text, Color.WHITE, availableWidth, Align.left, true);
            value = text;width = availableWidth;scaleX = data.scaleX;scaleY = data.scaleY;
            since = System.nanoTime();lineHeight = font.getLineHeight();totalRows = 0;
            BitmapFont.Glyph capital = data.getGlyph('H');
            float reference = capital == null ? 0 : capital.yoffset;
            float top = Float.POSITIVE_INFINITY, bottom = Float.NEGATIVE_INFINITY;
            for (GlyphLayout.GlyphRun run : full.runs) {
                totalRows = Math.max(totalRows, row(font, run.y) + 1);
                for (BitmapFont.Glyph glyph : run.glyphs) {
                    if (glyph.width == 0 || glyph.height == 0) continue;
                    float y = (glyph.yoffset - reference) * scaleY;
                    top = Math.min(top, y);bottom = Math.max(bottom, y + glyph.height * scaleY);
                }
            }
            inkTop = Float.isInfinite(top) ? 0 : top;
            inkHeight = Float.isInfinite(bottom) ? font.getCapHeight() : bottom - inkTop;
        }
        rowsPerPage = availableHeight < inkHeight ? 0 : 1 + (int)Math.floor((availableHeight - inkHeight) / lineHeight);
        pageCount = rowsPerPage == 0 ? 0 : (totalRows + rowsPerPage - 1) / rowsPerPage;
        pageIndex = pageCount == 0 ? 0 : (int)(((System.nanoTime() - since) / 3500000000L) % pageCount);
        visibleRows = pageCount == 0 ? 0 : Math.min(rowsPerPage, totalRows - pageIndex * rowsPerPage);
    }

    private int row(BitmapFont font, float y) {
        return Math.round((font.isFlipped() ? y : -y) / lineHeight);
    }

    float visibleHeight() { return visibleRows == 0 ? 0 : inkHeight + (visibleRows - 1) * lineHeight; }

    void draw(BitmapFont font, Batch batch, float x, float top) {
        drawnTop = top;drawnBottom = top + visibleHeight();
        if (visibleRows == 0) return;
        // Runs are borrowed from full, so clear the view without returning them to the pool.
        page.runs.clear();page.colors.clear();page.glyphCount = 0;
        int startRow = pageIndex * rowsPerPage, endRow = startRow + visibleRows;
        int glyphIndex = 0, firstGlyph = -1, lastGlyph = 0;
        for (GlyphLayout.GlyphRun run : full.runs) {
            int row = row(font, run.y);
            if (row >= startRow && row < endRow) {
                if (firstGlyph < 0) firstGlyph = glyphIndex;
                page.runs.add(run);page.glyphCount += run.glyphs.size;
                lastGlyph = glyphIndex + run.glyphs.size;
            }
            glyphIndex += run.glyphs.size;
        }
        if (firstGlyph < 0) return;
        int firstColor = Color.WHITE.toIntBits();
        for (int i = 0; i < full.colors.size; i += 2)
            if (full.colors.get(i) <= firstGlyph) firstColor = full.colors.get(i + 1);
        page.colors.add(0);page.colors.add(firstColor);
        for (int i = 0; i < full.colors.size; i += 2) {
            int index = full.colors.get(i);
            if (index > firstGlyph && index < lastGlyph) {
                page.colors.add(index - firstGlyph);page.colors.add(full.colors.get(i + 1));
            }
        }
        page.width = width;page.height = visibleHeight();
        BitmapFont.BitmapFontData data = font.getData();
        BitmapFont.Glyph capital = data.getGlyph('H');
        float offset = data.ascent + (capital == null ? 0 : capital.yoffset * data.scaleY);
        float firstY = startRow * lineHeight * (font.isFlipped() ? 1 : -1);
        font.draw(batch, page, x, top - inkTop - offset - firstY);
    }
}
