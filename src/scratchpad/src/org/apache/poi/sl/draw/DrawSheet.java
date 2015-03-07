package org.apache.poi.sl.draw;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import org.apache.poi.sl.usermodel.*;


public class DrawSheet<T extends Sheet<? extends Shape>> implements Drawable {

    protected final T sheet;
    
    public DrawSheet(T sheet) {
        this.sheet = sheet;
    }
    
    public void applyTransform(Graphics2D context) {
        // TODO Auto-generated method stub
        
    }

    public void draw(Graphics2D graphics) {
        DrawFactory drawFact = DrawFactory.getInstance(graphics);
        MasterSheet<? extends Shape> master = sheet.getMasterSheet();
        
        if(sheet.getFollowMasterGraphics() && master != null) {
            Drawable drawer = drawFact.getDrawable(master);
            drawer.draw(graphics);
        }
        
        graphics.setRenderingHint(Drawable.GROUP_TRANSFORM, new AffineTransform());

        for (Shape shape : sheet.getShapes()) {
            if(!canDraw(shape)) continue;
            
            // remember the initial transform and restore it after we are done with drawing
            AffineTransform at = graphics.getTransform();

            // concrete implementations can make sense of this hint,
            // for example PSGraphics2D or PDFGraphics2D would call gsave() / grestore
            graphics.setRenderingHint(Drawable.GSAVE, true);

            // apply rotation and flipping
            Drawable drawer = drawFact.getDrawable(shape);
            drawer.applyTransform(graphics);
            // draw stuff
            drawer.draw(graphics);

            // restore the coordinate system
            graphics.setTransform(at);

            graphics.setRenderingHint(Drawable.GRESTORE, true);
        }
    }

    public void drawContent(Graphics2D context) {
        // TODO Auto-generated method stub
        
    }

    /**
     * Checks if this <code>sheet</code> displays the specified shape.
     *
     * Subclasses can override it and skip certain shapes from drawings,
     * for instance, slide masters and layouts don't display placeholders
     */
    protected boolean canDraw(Shape shape){
        return true;
    }
}
