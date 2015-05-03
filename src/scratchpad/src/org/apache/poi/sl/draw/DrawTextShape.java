package org.apache.poi.sl.draw;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Iterator;

import org.apache.poi.sl.usermodel.*;

public class DrawTextShape<T extends TextShape<? extends TextParagraph<? extends TextRun>>> extends DrawSimpleShape<T> {

    public DrawTextShape(T shape) {
        super(shape);
    }

    @Override
    public void drawContent(Graphics2D graphics) {
        Rectangle2D anchor = DrawShape.getAnchor(graphics, shape);
        Insets2D insets = shape.getInsets();
        double x = anchor.getX() + insets.left;
        double y = anchor.getY();

        // remember the initial transform
        AffineTransform tx = graphics.getTransform();

        // Transform of text in flipped shapes is special.
        // At this point the flip and rotation transform is already applied
        // (see DrawShape#applyTransform ), but we need to restore it to avoid painting "upside down".
        // See Bugzilla 54210.

        if(shape.getFlipVertical()){
            graphics.translate(anchor.getX(), anchor.getY() + anchor.getHeight());
            graphics.scale(1, -1);
            graphics.translate(-anchor.getX(), -anchor.getY());

            // text in vertically flipped shapes is rotated by 180 degrees
            double centerX = anchor.getX() + anchor.getWidth()/2;
            double centerY = anchor.getY() + anchor.getHeight()/2;
            graphics.translate(centerX, centerY);
            graphics.rotate(Math.toRadians(180));
            graphics.translate(-centerX, -centerY);
        }

        // Horizontal flipping applies only to shape outline and not to the text in the shape.
        // Applying flip second time restores the original not-flipped transform
        if(shape.getFlipHorizontal()){
            graphics.translate(anchor.getX() + anchor.getWidth(), anchor.getY());
            graphics.scale(-1, 1);
            graphics.translate(-anchor.getX() , -anchor.getY());
        }


        // first dry-run to calculate the total height of the text
        double textHeight = shape.getTextHeight();

        switch (shape.getVerticalAlignment()){
            case TOP:
                y += insets.top;
                break;
            case BOTTOM:
                y += anchor.getHeight() - textHeight - insets.bottom;
                break;
            default:
            case MIDDLE:
                double delta = anchor.getHeight() - textHeight - insets.top - insets.bottom;
                y += insets.top + delta/2;
                break;
        }

        drawParagraphs(graphics, x, y);

        // restore the transform
        graphics.setTransform(tx);
    }

    /**
     * paint the paragraphs starting from top left (x,y)
     *
     * @return  the vertical advance, i.e. the cumulative space occupied by the text
     */
    public double drawParagraphs(Graphics2D graphics, double x, double y) {
        DrawFactory fact = DrawFactory.getInstance(graphics);
        Insets2D shapePadding = shape.getInsets();

        double y0 = y;
        Iterator<? extends TextParagraph<? extends TextRun>> paragraphs = shape.iterator();
        
        boolean isFirstLine = true;
        while (paragraphs.hasNext()){
            TextParagraph<? extends TextRun> p = paragraphs.next();
            DrawTextParagraph<? extends TextRun> dp = fact.getDrawable(p);
            dp.setInsets(shapePadding);
            dp.breakText(graphics);

            if (!isFirstLine) {
                // the amount of vertical white space before the paragraph
                double spaceBefore = p.getSpaceBefore();
                if(spaceBefore > 0) {
                    // positive value means percentage spacing of the height of the first line, e.g.
                    // the higher the first line, the bigger the space before the paragraph
                    y += spaceBefore*0.01*dp.getFirstLineHeight();
                } else {
                    // negative value means the absolute spacing in points
                    y += -spaceBefore;
                }
            }
            isFirstLine = false;
            
            dp.setPosition(x, y);
            dp.draw(graphics);
            y += dp.getY();

            if (paragraphs.hasNext()) {
                double spaceAfter = p.getSpaceAfter();
                if(spaceAfter > 0) {
                    // positive value means percentage spacing of the height of the last line, e.g.
                    // the higher the last line, the bigger the space after the paragraph
                    y += spaceAfter*0.01*dp.getLastLineHeight();
                } else {
                    // negative value means the absolute spacing in points
                    y += -spaceAfter;
                }
            }
        }
        return y - y0;
    }

    /**
     * Compute the cumulative height occupied by the text
     */
    public double getTextHeight(){
        // dry-run in a 1x1 image and return the vertical advance
        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = img.createGraphics();
        return drawParagraphs(graphics, 0, 0);
    }
}
