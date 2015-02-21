package org.apache.poi.sl.draw;

import java.awt.Graphics2D;
import java.awt.font.TextLayout;
import java.text.*;

import org.apache.poi.xslf.usermodel.XSLFRenderingHint;

public class DrawTextFragment implements Drawable  {
    final TextLayout layout;
    final AttributedString str;
    double x, y;
    
    public DrawTextFragment(TextLayout layout, AttributedString str) {
        this.layout = layout;
        this.str = str;
    }

    public void setPosition(double x, double y) {
        // TODO: replace it, by applyTransform????
        this.x = x;
        this.y = y;
    }

    public void draw(Graphics2D graphics){
        if(str == null) {
            return;
        }

        double yBaseline = y + layout.getAscent();

        Integer textMode = (Integer)graphics.getRenderingHint(XSLFRenderingHint.TEXT_RENDERING_MODE);
        if(textMode != null && textMode == XSLFRenderingHint.TEXT_AS_SHAPES){
            layout.draw(graphics, (float)x, (float)yBaseline);
        } else {
            graphics.drawString(str.getIterator(), (float)x, (float)yBaseline );
        }
    }

    public void applyTransform(Graphics2D graphics) {
        // TODO Auto-generated method stub
        
    }

    public void drawContent(Graphics2D graphics) {
        // TODO Auto-generated method stub
        
    }
    
    public TextLayout getLayout() {
        return layout;
    }

    public AttributedString getAttributedString() {
        return str;
    }
    
    /**
     * @return full height of this text run which is sum of ascent, descent and leading
     */
    public float getHeight(){
        double h = Math.ceil(layout.getAscent()) + Math.ceil(layout.getDescent()) + layout.getLeading();
        return (float)h;
    }

    /**
     *
     * @return width if this text run
     */
    public float getWidth(){
        return layout.getAdvance();
    }

    /**
     *
     * @return the string to be painted
     */
    public String getString(){
        if (str == null) return "";

        AttributedCharacterIterator it = str.getIterator();
         StringBuilder buf = new StringBuilder();
         for (char c = it.first(); c != CharacterIterator.DONE; c = it.next()) {
             buf.append(c);
         }
        return buf.toString();
    }

    @Override
    public String toString(){
        return "[" + getClass().getSimpleName() + "] " + getString();
    }
    
}
