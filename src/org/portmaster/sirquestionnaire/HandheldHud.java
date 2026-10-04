package org.portmaster.sirquestionnaire;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Align;
import com.orangepixel.questionnaire.World;
import com.orangepixel.utils.ArcadeCanvas;
import com.orangepixel.utils.GUI;
import com.orangepixel.utils.Render;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/** Reflow the compact gameplay HUD, retaining the game's text, markup and controls. */
final class HandheldHud {
    private final DisplayLayout screen;
    private BitmapFont[] installed;
    private Field layoutField;
    private boolean overlay;
    private SpriteBatch batch;
    private Texture pixel;
    private final Matrix4 projection = new Matrix4();
    private final GlyphLayout measure = new GlyphLayout();
    private final Color savedColor = new Color();
    private final List<String> messages = new ArrayList<String>();
    final TextPages combatPages = new TextPages(), itemPages = new TextPages();
    private String floor, level, xp, itemName, itemDescription, turns, useAction;
    private boolean canDrop, canBack;
    private float floorX, floorY, levelX, statsScale, inventoryX, inventoryTop, inventoryScale;
    private int xpParts;
    float renderedCapHeight;
    float logTop, logBottom, statsTop, statsBottom, xpTop, xpBarBottom, descriptionTop, descriptionBottom, hintTop;

    HandheldHud(DisplayLayout screen) { this.screen=screen; }

    void beginFrame() {
        messages.clear();floor=level=xp=itemName=itemDescription=turns=useAction=null;xpParts=0;
        canDrop=canBack=false;
        logTop=logBottom=statsTop=statsBottom=xpTop=xpBarBottom=descriptionTop=descriptionBottom=hintTop=0;
    }
    void prepare(boolean overlay) {
        this.overlay=overlay;
        BitmapFont[] fonts=GUI.fonts;
        if (fonts==null) return;
        if (installed!=fonts) {
            try {
                if (layoutField==null) {layoutField=GUI.class.getDeclaredField("layout");layoutField.setAccessible(true);}
                layoutField.set(null,new CapturedLayout());
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Cannot adapt the supported game's text layout",e);
            }
            installed=fonts;
        }
        if (fonts.length>0 && fonts[0]!=null && !(fonts[0] instanceof HudFont))
            fonts[0]=new HudFont(fonts[0]);
    }

    private boolean capture(CapturedLayout text,float x,float y) {
        if (!overlay || (ArcadeCanvas.GameState!=6 && ArcadeCanvas.GameState!=21)) return false;
        float sx=(float)screen.screenWidth/Render.width, sy=(float)screen.screenHeight/Render.height;
        String raw=text.text;
        if (text.target==240 && !World.inInterface && isMessage(raw)) {
            messages.add(raw);return true;
        }
        if (text.target==162) {
            if (itemName==null) {
                itemName=raw;inventoryX=x*sx-5*sx;inventoryTop=y*sy-7*sy;
                inventoryScale=(float)screen.screenHeight/Math.max(1,World.renderH);
            } else itemDescription=raw;
            return true;
        }
        if (itemName!=null && raw.endsWith(" turns")) {turns=raw;return true;}
        // GUI splits keyboard glyphs from their captions before drawing them.
        if (World.inInventory && itemName!=null && raw.startsWith(" : ")) {
            String action=raw.substring(3);
            if (text.target==48 && action.equals("drop")) canDrop=true;
            else if (text.target==200 && action.equals("back")) canBack=true;
            else if (text.target==48) useAction=action;
            else return false;
            return true;
        }
        if (raw.startsWith("Floor:")) {floor=raw;floorX=Math.max(12,x*sx);floorY=y*sy;statsScale=sy;return true;}
        if (floor!=null && raw.startsWith("Level:")) {level=raw;levelX=x*sx;xpParts=2;return true;}
        if (xpParts>0 && isNumber(raw)) {
            xp=(xp==null?"":xp)+raw;xpParts--;return true;
        }
        return false;
    }
    private boolean isMessage(String text) {
        for (int i=0;i<World.currentProgressLine;i++)
            if (text.equals(World.worldProgress[i])) return true;
        return false;
    }
    private boolean isNumber(String text) {
        int start=text.startsWith("/")?1:0;
        if (text.length()==start) return false;
        for (int i=start;i<text.length();i++)
            if (text.charAt(i)<'0' || text.charAt(i)>'9') return false;
        return true;
    }

    private final class CapturedLayout extends GlyphLayout {
        String text="";float target;
        @Override public void setText(BitmapFont font,CharSequence value,int start,int end,Color color,
                                      float width,int align,boolean wrap,String truncate) {
            text=value instanceof String && start==0 && end==value.length() ? (String)value : value.subSequence(start,end).toString();
            target=width;
            super.setText(font,value,start,end,color,width,align,wrap,truncate);
        }
    }
    private final class HudFont extends BitmapFont {
        final BitmapFont original;
        private boolean disposed;
        HudFont(BitmapFont original) {super(original.getData(),original.getRegions(),original.usesIntegerPositions());this.original=original;}
        @Override public void draw(Batch batch,GlyphLayout text,float x,float y) {
            if (!(text instanceof CapturedLayout) || !capture((CapturedLayout)text,x,y)) super.draw(batch,text,x,y);
        }
        @Override public void dispose() {if (!disposed) {disposed=true;super.dispose();original.dispose();}}
    }

    void render() {
        if (messages.isEmpty() && floor==null && itemName==null) return;
        if (batch==null) {
            batch=new SpriteBatch();
            Pixmap map=new Pixmap(1,1,Pixmap.Format.RGBA8888);
            try {map.setColor(Color.WHITE);map.fill();pixel=new Texture(map);} finally {map.dispose();}
        }
        BitmapFont font=((HudFont)GUI.fonts[0]).original;
        BitmapFont.BitmapFontData data=font.getData();
        float oldX=data.scaleX, oldY=data.scaleY;
        savedColor.set(font.getColor());
        float size=screen.screenWidth>=1000 ? 21f : screen.screenHeight>=640 ? 19f : 17f;
        float scale=data.scaleY*size/Math.abs(data.capHeight);
        data.setScale(scale,scale);font.setColor(Color.WHITE);renderedCapHeight=Math.abs(data.capHeight);
        projection.setToOrtho(0,screen.screenWidth,screen.screenHeight,0,-1,1);
        batch.setProjectionMatrix(projection);batch.begin();
        try {
            drawMessages(font);
            drawStats(font);
            drawInventory(font);
        } finally {
            batch.end();data.setScale(oldX,oldY);font.setColor(savedColor);
        }
    }
    private float height(BitmapFont font,String text,float width) {
        measure.setText(font,text,Color.WHITE,width,Align.left,true);return measure.height;
    }
    private void text(BitmapFont font,String text,float x,float top,float width) {
        BitmapFont.Glyph capital=font.getData().getGlyph('H');
        float offset=font.getData().ascent+(capital==null?0:capital.yoffset*font.getData().scaleY);
        font.draw(batch,text,x,top-offset,width,Align.left,true);
    }
    private void panel(float x,float y,float w,float h) {
        batch.setColor(0.025f,0.035f,0.065f,0.9f);batch.draw(pixel,x,y,w,h);batch.setColor(Color.WHITE);
    }
    private void drawMessages(BitmapFont font) {
        if (messages.isEmpty()) return;
        float edge=screen.screenWidth>screen.screenHeight ? screen.screenWidth/2f : screen.screenWidth;
        float x=screen.screenWidth*(screen.screenWidth>screen.screenHeight?0.125f:0.30f);
        float y=10, width=Math.max(80,edge-x-12);
        StringBuilder content=new StringBuilder();
        for (String line:messages) {if (content.length()>0) content.append('\n');content.append(line);}
        float maximum=Math.min(screen.screenHeight*0.20f-y, font.getLineHeight()*3);
        combatPages.prepare(font,content.toString(),width,maximum);
        logTop=y;logBottom=y+combatPages.visibleHeight();
        panel(x-5,y-4,width+10,combatPages.visibleHeight()+8);
        combatPages.draw(font,batch,x,y);
        pageDots(combatPages,x,y+maximum+5);
    }
    private void drawStats(BitmapFont font) {
        if (floor==null) return;
        float top=World.inInventory && screen.screenWidth<=screen.screenHeight
            ? Math.max(8,floorY-font.getCapHeight()-10) : floorY+32*statsScale+8;
        statsTop=top;
        statsBottom=top+font.getCapHeight()+Math.abs(font.getDescent());
        float floorWidth=heightWidth(font,floor);
        panel(floorX-4,top-4,floorWidth+8,font.getCapHeight()+8);
        text(font,floor,floorX,top,floorWidth+1);
        if (level!=null) {
            float edge=screen.screenWidth>screen.screenHeight ? screen.screenWidth/2f-12 : screen.screenWidth-12;
            String summary=level+(xp==null?"":"  "+xp);
            float available=edge-floorX-floorWidth-12;
            if (heightWidth(font,summary)>available) summary=summary.replace("Level:","Lv:");
            float width=heightWidth(font,summary);
            float x=Math.max(floorX+floorWidth+12,Math.min(levelX,edge-width));
            panel(x-4,top-4,width+8,font.getCapHeight()+8);
            text(font,summary,x,top,width+1);
            xpBarBottom=floorY+18*statsScale;xpTop=top;
        }
    }
    private float heightWidth(BitmapFont font,String text) {
        measure.setText(font,text);return measure.width;
    }
    private void drawInventory(BitmapFont font) {
        if (itemName==null) return;
        float width=Math.min(162*inventoryScale,screen.screenWidth-inventoryX-8);
        float gridBottom=inventoryTop+142*inventoryScale;
        float top=gridBottom+8, bottom=screen.screenHeight-8;
        batch.setColor(0.025f,0.035f,0.065f,1);batch.draw(pixel,inventoryX,top,width,Math.max(0,bottom-top));batch.setColor(Color.WHITE);
        float x=inventoryX+10, available=width-20, y=top+6;
        float usage=turns==null?0:heightWidth(font,turns);
        float nameWidth=Math.max(60,available-(usage>0?usage+12:0));
        text(font,itemName,x,y,nameWidth);
        if (turns!=null) text(font,turns,x+available-usage,y,usage+1);
        y+=height(font,itemName,nameWidth)+6;
        float footer=World.inInventory?font.getCapHeight()+8:0;
        float bodyBottom=bottom-footer-6;
        descriptionTop=y;descriptionBottom=bodyBottom;hintTop=bottom-font.getCapHeight()-4;
        if (itemDescription!=null && bodyBottom>y) {
            itemPages.prepare(font,itemDescription,available,bodyBottom-y-8);
            itemPages.draw(font,batch,x,y);
            pageDots(itemPages,x,bodyBottom-3);
        }
        if (World.inInventory) {
            String hint=(canBack?"B: Back":"")+(canDrop?"   X: Drop":"")+(useAction!=null?"   A: "+useAction:"");
            text(font,hint,x,hintTop,available);
        }
    }
    private void pageDots(TextPages pages,float x,float y) {
        if (pages.pageCount<=1) return;
        for (int i=0;i<pages.pageCount;i++) {
            batch.setColor(1,1,1,i==pages.pageIndex?1:0.3f);batch.draw(pixel,x+i*7,y,3,3);
        }
        batch.setColor(Color.WHITE);
    }
    void dispose() {if (batch!=null) batch.dispose();if (pixel!=null) pixel.dispose();}
}
