package org.apache.poi.sl.draw;

import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

import org.apache.poi.sl.usermodel.PictureData;
import org.apache.poi.sl.usermodel.PictureShape;


public class DrawPictureShape<T extends PictureShape> extends DrawSimpleShape<T> {
    public DrawPictureShape(T shape) {
        super(shape);
    }
    
    @Override
    public void drawContent(Graphics2D graphics) {
        PictureData data = shape.getPictureData();
        if(data == null) return;

        ImageRenderer renderer = (ImageRenderer)graphics.getRenderingHint(Drawable.IMAGE_RENDERER);
        if (renderer == null) renderer = new ImageRenderer();
        
        Rectangle2D anchor = getAnchor(graphics, shape);

        Insets insets = shape.getClipping();

        try {
            renderer.loadImage(data.getData(), data.getContentType());
            renderer.drawImage(graphics, anchor, insets);
        } catch (IOException e) {
            // TODO: draw specific runtime exception?
            throw new RuntimeException(e);
        }
    }    
}
