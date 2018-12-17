/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.apache.poi.sl.draw;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Iterator;

import org.apache.poi.sl.usermodel.Insets2D;
import org.apache.poi.sl.usermodel.PlaceableShape;
import org.apache.poi.sl.usermodel.ShapeContainer;
import org.apache.poi.sl.usermodel.TextParagraph;
import org.apache.poi.sl.usermodel.TextParagraph.BulletStyle;
import org.apache.poi.sl.usermodel.TextRun;
import org.apache.poi.sl.usermodel.TextShape;
import org.apache.poi.sl.usermodel.TextShape.TextDirection;

public class DrawTextShape extends DrawSimpleShape {

    public DrawTextShape(TextShape<?,?> shape) {
        super(shape);
    }

    @Override
    public void drawContent(Graphics2D graphics) {
        TextShape<?,?> s = getShape();
        
        Rectangle2D anchor = DrawShape.getAnchor(graphics, s);
        if(anchor == null) {
            return;
        }

        Insets2D insets = s.getInsets();
        double x = anchor.getX() + insets.left;
        double y = anchor.getY();

        // remember the initial transform
        AffineTransform tx = graphics.getTransform();
    
        // Transform of text in flipped shapes is special.
        // At this point the flip and rotation transform is already applied
        // (see DrawShape#applyTransform ), but we need to restore it to avoid painting "upside down".
        // See Bugzilla 54210.

        boolean vertFlip = s.getFlipVertical();
        boolean horzFlip = s.getFlipHorizontal();
        ShapeContainer<?,?> sc = s.getParent();
        while (sc instanceof PlaceableShape) {
            PlaceableShape<?,?> ps = (PlaceableShape<?,?>)sc;
            vertFlip ^= ps.getFlipVertical();
            horzFlip ^= ps.getFlipHorizontal();
            sc = ps.getParent();
        }
        
        // Horizontal flipping applies only to shape outline and not to the text in the shape.
        // Applying flip second time restores the original not-flipped transform
        if (horzFlip ^ vertFlip) {
            final double ax = anchor.getX();
            final double ay = anchor.getY();
            graphics.translate(ax + anchor.getWidth(), ay);
            graphics.scale(-1, 1);
            graphics.translate(-ax, -ay);
        }

        Double textRot = s.getTextRotation();
        if (textRot != null && textRot != 0) {
            final double cx = anchor.getCenterX();
            final double cy = anchor.getCenterY();
            graphics.translate(cx, cy);
            graphics.rotate(Math.toRadians(textRot));
            graphics.translate(-cx, -cy);
        }
        
        // first dry-run to calculate the total height of the text
        double textHeight;

        switch (s.getVerticalAlignment()){
            default:
            case TOP:
                y += insets.top;
                break;
            case BOTTOM:
                textHeight = getTextHeight(graphics);
                y += anchor.getHeight() - textHeight - insets.bottom;
                break;
            case MIDDLE:
                textHeight = getTextHeight(graphics);
                double delta = anchor.getHeight() - textHeight - insets.top - insets.bottom;
                y += insets.top + delta/2;
                break;
        }

        TextDirection textDir = s.getTextDirection();
        if (textDir == TextDirection.VERTICAL || textDir == TextDirection.VERTICAL_270) {
            final double deg = (textDir == TextDirection.VERTICAL) ? 90 : 270;
            final double cx = anchor.getCenterX();
            final double cy = anchor.getCenterY();
            graphics.translate(cx, cy);
            graphics.rotate(Math.toRadians(deg));
            graphics.translate(-cx, -cy);
            
            // old top/left edge is now bottom/left or top/right - as we operate on the already
            // rotated drawing context, both verticals can be moved in the same direction
            final double w = anchor.getWidth();
            final double h = anchor.getHeight();
            final double dx = (w-h)/2d;
            graphics.translate(dx,-dx);
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

        double y0 = y;
        Iterator<? extends TextParagraph<?,?,? extends TextRun>> paragraphs = getShape().iterator();
        
        boolean isFirstLine = true;
        for (int autoNbrIdx=0; paragraphs.hasNext(); autoNbrIdx++){
            TextParagraph<?,?,? extends TextRun> p = paragraphs.next();
            DrawTextParagraph dp = fact.getDrawable(p);
            BulletStyle bs = p.getBulletStyle();
            if (bs == null || bs.getAutoNumberingScheme() == null) {
                autoNbrIdx = -1;
            } else {
                Integer startAt = bs.getAutoNumberingStartAt();
                if (startAt == null) startAt = 1;
                // TODO: handle reset auto number indexes
                if (startAt > autoNbrIdx) autoNbrIdx = startAt;
            }
            dp.setAutoNumberingIdx(autoNbrIdx);
            dp.breakText(graphics);

            if (isFirstLine) {
                y += dp.getFirstLineLeading();
            } else {
                // the amount of vertical white space before the paragraph
                Double spaceBefore = p.getSpaceBefore();
                if (spaceBefore == null) spaceBefore = 0d;
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
                Double spaceAfter = p.getSpaceAfter();
                if (spaceAfter == null) spaceAfter = 0d;
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
     * 
     * @return the height in points
     */
    public double getTextHeight() {
        return getTextHeight(null);
    }
    
    /**
     * Compute the cumulative height occupied by the text
     *
     * @param oldGraphics the graphics context, which properties are to be copied, may be null
     * @return the height in points
     */
    public double getTextHeight(Graphics2D oldGraphics) {
        // dry-run in a 1x1 image and return the vertical advance
        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = img.createGraphics();
        if (oldGraphics != null) {
            graphics.addRenderingHints(oldGraphics.getRenderingHints());
            graphics.setTransform(oldGraphics.getTransform());
        }
        return drawParagraphs(graphics, 0, 0);
    }

    @Override
    protected TextShape<?,? extends TextParagraph<?,?,? extends TextRun>> getShape() {
        return (TextShape<?,? extends TextParagraph<?,?,? extends TextRun>>)shape;
    }
}
