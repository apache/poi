package org.apache.poi.sl.draw;

import java.awt.Graphics2D;

import org.apache.poi.sl.usermodel.*;


public class DrawSlide<T extends Slide<? extends Shape, ? extends SlideShow, ? extends Notes<?,?>>> extends DrawSheet<T> {

    public DrawSlide(T slide) {
        super(slide);
    }
    
    public void draw(Graphics2D graphics) {
        Background bg = sheet.getBackground();
        if(bg != null) {
            DrawFactory drawFact = DrawFactory.getInstance(graphics);
            DrawBackground<Background> db = drawFact.getDrawable(bg);
            db.draw(graphics);
        }

        super.draw(graphics);
    }
}
