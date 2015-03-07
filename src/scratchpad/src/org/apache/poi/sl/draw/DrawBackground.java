package org.apache.poi.sl.draw;

import java.awt.*;
import java.awt.geom.Rectangle2D;

import org.apache.poi.sl.usermodel.*;


public class DrawBackground<T extends Background> extends DrawShape<T> {
    public DrawBackground(T shape) {
        super(shape);
    }

    public void draw(Graphics2D graphics) {
        Dimension pg = shape.getSheet().getSlideShow().getPageSize();
        final Rectangle2D anchor = new Rectangle2D.Double(0, 0, pg.getWidth(), pg.getHeight());

        PlaceableShape ps = new PlaceableShape(){
            public Rectangle2D getAnchor() { return anchor; }
            public void setAnchor(Rectangle2D anchor) {}
            public double getRotation() { return 0; }
            public void setRotation(double theta) {}
            public void setFlipHorizontal(boolean flip) {}
            public void setFlipVertical(boolean flip) {}
            public boolean getFlipHorizontal() { return false; }
            public boolean getFlipVertical() { return false; }
        };
        
        DrawFactory drawFact = DrawFactory.getInstance(graphics);
        DrawPaint dp = drawFact.getPaint(ps);
        Paint fill = dp.getPaint(graphics, shape.getFillStyle().getPaint());
        Rectangle2D anchor2 = getAnchor(graphics, anchor);
        
        if(fill != null) {
            graphics.setPaint(fill);
            graphics.fill(anchor2);
        }
    }
    
    
}
