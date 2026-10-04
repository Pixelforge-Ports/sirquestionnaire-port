package org.portmaster.sirquestionnaire;

import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.utils.BufferUtils;
import com.orangepixel.questionnaire.World;
import com.orangepixel.questionnaire.ai.PlayerEntity;
import com.orangepixel.questionnaire.Globals;
import com.orangepixel.questionnaire.myCanvas;
import com.orangepixel.questionnaire.ui.uiquests;
import com.orangepixel.questionnaire.ui.uiinventory;
import com.orangepixel.utils.GUI;
import com.orangepixel.utils.Render;
import com.orangepixel.controller.GameInput;
import java.nio.IntBuffer;

/** Owned-game check: menus, quests, inventory, native viewport and stable font sizing. */
public final class InterfaceSmoke extends Main {
    private int frame, play, menuFrames, inventoryFrames, closedFrames, previous=-1;
    private boolean questSeen, continueSelected, hudSeen;
    private float fontHeight;
    private String output;

    public static void main(String[] args) throws Exception {
        new Lwjgl3Application(new InterfaceSmoke(),configuration());
    }
    private void key(int code,boolean down) {
        if (down) Gdx.input.getInputProcessor().keyDown(code);
        else Gdx.input.getInputProcessor().keyUp(code);
    }
    private void focusItem() {
        try {
            for (String name:new String[]{"focusedItemIDX","selectedItemIDX"}) {
                java.lang.reflect.Field field=uiinventory.class.getDeclaredField(name);
                field.setAccessible(true);field.setInt(null,0);
            }
        } catch (ReflectiveOperationException e) {throw new AssertionError(e);}
    }
    private void nextPage(TextPages pages) {
        try {
            java.lang.reflect.Field field=TextPages.class.getDeclaredField("since");
            field.setAccessible(true);field.setLong(pages,System.nanoTime()-3600000000L);
        } catch (ReflectiveOperationException e) {throw new AssertionError(e);}
    }
    private void checkCombat() {
        if (handheldHud.combatPages.rowsPerPage>3 || handheldHud.logBottom>layout.screenHeight*0.20f)
            throw new AssertionError("Combat log extends into the scene");
        float characterTop=(myPlayer.y+World.offsetY)*(float)layout.screenHeight/World.renderH;
        if (handheldHud.logBottom+8>=characterTop) throw new AssertionError("Combat log covers the character");
    }
    @Override public void create() {
        super.create(); output=System.getProperty("sirquestionnaire.output");
    }
    @Override public void render() {
        frame++;
        // Set up scenes through the owned game's entry points, without changing saves.
        if (GameState==4 && !continueSelected && ++menuFrames>=60) {
            capture(output+"/menu.png");
            myCanvas.initNewGame();GameState=6;continueSelected=true;
        }
        if (GameState==6) {
            play++;
            if (play==2) {
                World.inHatSelect=false;World.inBackpack=false;World.inQuests=false;World.inInterface=false;
                World.showNewDungeonDelay=0;activePlayer.openedCodex=true;
                World.worldProgress[0]="A tiny [YELLOW]Skeleton[] taunts you.";
                World.worldProgress[1]="You risk a [YELLOW]photo[] for your codex.";
                World.worldProgress[2]="Skeleton hurts you [YELLOW]-1hp[].";
                World.currentProgressLine=3;
                PlayerEntity.experience=16;
            }
            if (play==21) {
                World.worldProgress[0]="A very long [YELLOW]Skeleton[] statement must stay above the character and the floor. It wraps across many lines but only a few complete lines are displayed at once. The other pages remain readable without covering the gameplay scene, player stats, inventory or buttons.";
            }
            if (play==26) nextPage(handheldHud.combatPages);
            if (play==30) uiquests.init();
            if (play==150) {
                capture(output+"/quests.png");questSeen=true;
                World.inQuests=false;World.inInterface=false;
            }
            if (play==180) {
                World.inInventory=true;World.inInterface=true;
                uiinventory.initFadeIn();
                focusItem();
                int item=PlayerEntity.inventoryItemIDS[0];
                Globals.itemNames[item][1]="A longer item description must wrap inside the panel without covering the inventory cells or the controls. Additional lines scroll within the description area so all of the text remains available.";
            }
        }
        if (inventoryFrames==90) {
            // Advance the description's clock to verify a second page of complete lines.
            nextPage(handheldHud.itemPages);
        }
        GameInput.isKeyboard(); // Match PortMaster's keyboard mapper, rather than the host PC mouse.
        super.render();
        if (play==20) {
            float expected=layout.screenWidth>=1000?21:layout.screenHeight>=640?19:17;
            if (Math.abs(handheldHud.renderedCapHeight-expected)>0.01f) throw new AssertionError("Incorrect HUD font size");
            checkCombat();
            if (layout.screenWidth>layout.screenHeight && (handheldHud.logBottom==0 || handheldHud.logBottom>=handheldHud.statsTop))
                throw new AssertionError("Combat log overlaps player stats");
            if (handheldHud.xpTop<=handheldHud.xpBarBottom) throw new AssertionError("XP text overlaps its bar");
            if (handheldHud.statsBottom>World.buttonYOffset*(float)layout.screenHeight/World.renderH)
                throw new AssertionError("Player stats cover the action buttons");
            hudSeen=true;capture(output+"/hud.png");
        }
        if (play==25 || play==26) {
            checkCombat();
            if (handheldHud.combatPages.pageCount<2) throw new AssertionError("Long combat message was lost");
            if (play==26 && handheldHud.combatPages.pageIndex!=1) throw new AssertionError("Combat page did not advance");
            capture(output+(play==25?"/combat-long.png":"/combat-page.png"));
        }
        IntBuffer viewport=BufferUtils.newIntBuffer(4);
        Gdx.gl20.glGetIntegerv(GL20.GL_VIEWPORT,viewport);
        if (viewport.get(0)!=0 || viewport.get(1)!=0 || viewport.get(2)!=layout.screenWidth || viewport.get(3)!=layout.screenHeight)
            throw new AssertionError("Native viewport lost");
        if (GUI.fonts!=null && GUI.fonts[0]!=null) {
            BitmapFont font=GUI.fonts[0];
            float pixelsPerUnit=Math.max(1,(float)Math.floor(layout.screenWidth>layout.screenHeight?layout.screenHeight/320.0:layout.screenWidth/240.0));
            float height=Math.abs(font.getData().capHeight)*pixelsPerUnit;
            if (height<11.99f) throw new AssertionError("Small UI text: "+height);
            if (fontHeight!=0 && Math.abs(fontHeight-height)>0.01f) throw new AssertionError("Font scale accumulated");
            fontHeight=height;
        }
        if (GameState!=previous) {System.out.println("STATE "+frame+" "+GameState);previous=GameState;}
        if (frame%600==0) System.out.println("UI "+frame+" interface="+World.inInterface+" quests="+World.inQuests+" outfit="+World.inHatSelect+" inventory="+World.inInventory);
        if (World.inInventory) {
            inventoryFrames++;
            if (inventoryFrames==90) {
                if (layout.screenWidth==layout.screenHeight && GameState!=21) throw new AssertionError("Square inventory hidden");
                capture(output+"/inventory.png");
                if (handheldHud.descriptionBottom==0 || handheldHud.descriptionBottom>=handheldHud.hintTop)
                    throw new AssertionError("Item description overlaps controls");
                if (handheldHud.itemPages.drawnTop<handheldHud.descriptionTop || handheldHud.itemPages.drawnBottom>handheldHud.descriptionBottom)
                    throw new AssertionError("Item glyphs extend outside the description area");
            }
            if (inventoryFrames==91) {
                if (handheldHud.itemPages.pageCount<2 || handheldHud.itemPages.pageIndex!=1)
                    throw new AssertionError("Long inventory description did not advance");
                capture(output+"/inventory-scroll.png");
                key(Input.Keys.ESCAPE,true);
            }
            if (inventoryFrames==92) {
                key(Input.Keys.ESCAPE,false);
                World.inInventory=false;World.inInterface=false;GameState=6;
            }
        }
        if (inventoryFrames>=90 && !World.inInventory) {
            if (++closedFrames<60) return; // Let the game's inventory fade finish.
            capture(output+"/gameplay.png");
            if (!questSeen || !hudSeen || GameState!=6) throw new AssertionError("Interface scenes did not complete");
            System.out.println("UI_SMOKE_OK frames="+frame+" text="+fontHeight+" world="+Render.width+"x"+Render.height);
            Gdx.app.exit();
        }
        if (frame>=4800) throw new AssertionError("Menus/inventory did not complete: "+GameState);
    }
}
