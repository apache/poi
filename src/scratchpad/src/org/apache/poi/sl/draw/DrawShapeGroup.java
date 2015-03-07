package org.apache.poi.sl.draw;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import org.apache.poi.sl.usermodel.*;


public class DrawShapeGroup<T extends ShapeGroup<? extends Shape>> implements Drawable {

    protected final T shape;
    
    public DrawShapeGroup(T shape) {
        this.shape = shape;
    }
    
    public void applyTransform(Graphics2D context) {
    }

    public void draw(Graphics2D graphics) {

        // the coordinate system of this group of shape
        Rectangle2D interior = shape.getInteriorAnchor();
        // anchor of this group relative to the parent shape
        Rectangle2D exterior = shape.getAnchor();

        AffineTransform tx = (AffineTransform)graphics.getRenderingHint(Drawable.GROUP_TRANSFORM);
        AffineTransform tx0 = new AffineTransform(tx);

        double scaleX = interior.getWidth() == 0. ? 1.0 : exterior.getWidth() / interior.getWidth();
        double scaleY = interior.getHeight() == 0. ? 1.0 : exterior.getHeight() / interior.getHeight();

        tx.translate(exterior.getX(), exterior.getY());
        tx.scale(scaleX, scaleY);
        tx.translate(-interior.getX(), -interior.getY());

        DrawFactory drawFact = DrawFactory.getInstance(graphics);
        
        for (Shape child : shape) {
            // remember the initial transform and restore it after we are done with the drawing
            AffineTransform at = graphics.getTransform();
            graphics.setRenderingHint(Drawable.GSAVE, true);

            Drawable draw = drawFact.getDrawable(child);
            draw.applyTransform(graphics);
            draw.draw(graphics);

            // restore the coordinate system
            graphics.setTransform(at);
            graphics.setRenderingHint(Drawable.GRESTORE, true);
        }

        graphics.setRenderingHint(Drawable.GROUP_TRANSFORM, tx0);
        
    }

    public void drawContent(Graphics2D context) {
    }
}
