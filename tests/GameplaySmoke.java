package org.portmaster.sirquestionnaire;
import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.orangepixel.questionnaire.World;
import com.orangepixel.questionnaire.PlayerProfileSaveGame;
public class GameplaySmoke extends Main {
 boolean resumeSeen,continueSelected; int frame,play,previous=-1,lastRoom=-1,roomChanges; long started=System.nanoTime();
 public static void main(String[] args) throws Exception {new Lwjgl3Application(new GameplaySmoke(),configuration());}
 void key(int code,boolean down) {if(down)Gdx.input.getInputProcessor().keyDown(code);else Gdx.input.getInputProcessor().keyUp(code);}
 Object read(String name)throws Exception {java.lang.reflect.Field f=com.orangepixel.questionnaire.myCanvas.class.getDeclaredField(name);f.setAccessible(true);return f.get(java.lang.reflect.Modifier.isStatic(f.getModifiers())?null:this);}
 boolean existingSave() {return com.orangepixel.questionnaire.PlayerProfile.hasSaveGame();}
 void saveReadback() throws Exception {
    com.orangepixel.questionnaire.PlayerProfile.saveGame();
    int room=World.currentRoom;
    PlayerProfileSaveGame.floorcurrentRoom=-999;
    com.orangepixel.questionnaire.PlayerProfile.loadGame(false);
    if(!existingSave() || PlayerProfileSaveGame.floorcurrentRoom!=room)throw new IllegalStateException("Save readback failed");
    System.out.println("SAVE_READBACK_OK");
 }
 @Override public void render() {
  frame++;
  if(GameState==23 && !continueSelected){key(Input.Keys.LEFT,true);continueSelected=true;}
  else if(continueSelected && play==0)key(Input.Keys.LEFT,false);
  if(frame==240 && Boolean.getBoolean("sirquestionnaire.expectResume")){try{resumeSeen=existingSave();if(!resumeSeen)throw new IllegalStateException("Save missing on restart");System.out.println("RESUME_DATA_FOUND");}catch(Exception e){throw new IllegalStateException(e);}}
  if(frame>240 && GameState!=6){
   if(frame%90==0){key(Input.Keys.X,true);key(Input.Keys.ENTER,true);}
  }
  if(frame%90==3){key(Input.Keys.X,false);key(Input.Keys.ENTER,false);}
  if(GameState==6){
   play++;
   if(play%90==15)key(World.inInterface?Input.Keys.X:((play/90)%2==0?Input.Keys.RIGHT:Input.Keys.LEFT),true);
   if(play%90==18){key(Input.Keys.RIGHT,false);key(Input.Keys.LEFT,false);key(Input.Keys.X,false);}
  }
  super.render();
  if(GameState==6 && World.currentRoom!=lastRoom){if(lastRoom!=-1)roomChanges++;lastRoom=World.currentRoom;System.out.println("ROOM "+lastRoom+" transitions="+roomChanges);}
  java.nio.IntBuffer v=com.badlogic.gdx.utils.BufferUtils.newIntBuffer(4);Gdx.gl20.glGetIntegerv(com.badlogic.gdx.graphics.GL20.GL_VIEWPORT,v);
  if(v.get(0)!=layout.x || v.get(1)!=layout.y || v.get(2)!=layout.width || v.get(3)!=layout.height)throw new IllegalStateException("Viewport lost");
  if(physicalGraphics.getWidth()!=Integer.getInteger("sirquestionnaire.width",640) || physicalGraphics.getHeight()!=Integer.getInteger("sirquestionnaire.height",480))throw new IllegalStateException("Physical size changed");
  if(GameState!=previous){System.out.println("STATE "+frame+" "+GameState);previous=GameState;}
  if(frame==240 || frame%600==0)capture(System.getProperty("sirquestionnaire.output")+"/frame"+frame+".png");
  if((play>=600 && roomChanges>=2) || frame>=5400){
   capture(System.getProperty("sirquestionnaire.output")+"/final.png");
   if(play<600 || roomChanges<2)throw new IllegalStateException("Insufficient gameplay or room progression: "+play+" / "+roomChanges);
   double seconds=(System.nanoTime()-started)/1e9;if(seconds<(frame-2)/60.0)throw new IllegalStateException("Frame cap exceeded");
   if(Boolean.getBoolean("sirquestionnaire.testSave")){try{saveReadback();}catch(Exception e){throw new IllegalStateException(e);}}
   System.out.println("GAMEPLAY_OK frames="+frame+" play="+play+" seconds="+seconds);Gdx.app.exit();
  }
 }
}
