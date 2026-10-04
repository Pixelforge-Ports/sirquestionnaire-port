package org.portmaster.sirquestionnaire;

/** Native viewport and input coordinates must agree, including after a resize. */
public final class DisplayLayoutTest {
    public static void main(String[] args) {
        DisplayLayout layout = new DisplayLayout();
        int[][] sizes = {{640,480},{720,480},{720,720},{1024,768},{1280,720}};
        for (int[] size : sizes) {
            int w = size[0], h = size[1];
            layout.resize(w,h);
            check(layout.gameWidth==w && layout.gameHeight==h, "Wrong game aspect");
            check(layout.x==0 && layout.y==0 && layout.width==w && layout.height==h, "Letterboxing");
            for (int x : new int[]{0,w/2,w-1}) {
                check(layout.viewportX(x)==x && layout.inputX(x)==x && layout.cursorX(x)==x, "Horizontal input mismatch");
            }
            for (int y : new int[]{0,h/2,h-1}) {
                check(layout.viewportY(y)==y && layout.inputY(y)==y && layout.cursorY(y)==y, "Vertical input mismatch");
            }
            check(layout.viewportWidth(w)==w && layout.viewportHeight(h)==h, "Viewport size mismatch");
            System.out.println("NATIVE_VIEW_OK "+w+"x"+h);
        }
        layout.resize(0,0);
        check(layout.gameWidth==1280 && layout.gameHeight==720, "Invalid resize changed layout");
    }
    private static void check(boolean pass,String message) {
        if (!pass) throw new AssertionError(message);
    }
}
