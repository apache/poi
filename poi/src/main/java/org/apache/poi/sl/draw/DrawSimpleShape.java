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

import static org.apache.poi.sl.draw.DrawPaint.fillPaintWorkaround;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import org.apache.poi.sl.draw.geom.Context;
import org.apache.poi.sl.draw.geom.CustomGeometry;
import org.apache.poi.sl.draw.geom.Outline;
import org.apache.poi.sl.draw.geom.Path;
import org.apache.poi.sl.draw.geom.PathIf;
import org.apache.poi.sl.usermodel.LineDecoration;
import org.apache.poi.sl.usermodel.LineDecoration.DecorationShape;
import org.apache.poi.sl.usermodel.LineDecoration.DecorationSize;
import org.apache.poi.sl.usermodel.PaintStyle;
import org.apache.poi.sl.usermodel.PaintStyle.PaintModifier;
import org.apache.poi.sl.usermodel.PaintStyle.SolidPaint;
import org.apache.poi.sl.usermodel.Shadow;
import org.apache.poi.sl.usermodel.SimpleShape;
import org.apache.poi.util.Units;


public class DrawSimpleShape extends DrawShape {

    private static final double DECO_SIZE_POW = 1.5d;

    public DrawSimpleShape(SimpleShape<?,?> shape) {
        super(shape);
    }

    @Override
    public void draw(Graphics2D graphics) {
        if (getAnchor(graphics, getShape()) == null) {
            return;
        }

        Paint oldPaint = graphics.getPaint();
        Stroke oldStroke = graphics.getStroke();
        Color oldColor = graphics.getColor();

        Paint fill = getFillPaint(graphics);
        Paint line = getLinePaint(graphics);
        BasicStroke stroke = getStroke(); // the stroke applies both to the shadow and the shape
        graphics.setStroke(stroke);

        Collection<Outline> elems = computeOutlines(graphics);

        // first paint the shadow
        drawShadow(graphics, elems, fill, line);

        // then fill the shape interior
        if (fill != null) {
            final Path2D area = new Path2D.Double();
            graphics.setRenderingHint(Drawable.GRADIENT_SHAPE, area);

            Consumer<PaintModifier> fun = (pm) -> fillArea(graphics, pm, area);

            PaintModifier pm = null;
            for (Outline o : elems) {
                PathIf path = o.getPath();
                if (path.isFilled()) {
                    PaintModifier pmOld = pm;
                    pm = path.getFill();
                    if (pmOld != null && pmOld != pm) {
                        fun.accept(pmOld);
                        area.reset();
                    } else {
                        area.append(o.getOutline(), false);
                    }
                }
            }

            if (area.getCurrentPoint() != null) {
                fun.accept(pm);
            }
        }

        // then draw any content within this shape (text, image, etc.)
        drawContent(graphics);

        // then stroke the shape outline
        if(line != null) {
            graphics.setPaint(line);
            graphics.setStroke(stroke);
            for(Outline o : elems){
                if(o.getPath().isStroked()){
                    java.awt.Shape s = o.getOutline();
                    graphics.setRenderingHint(Drawable.GRADIENT_SHAPE, s);
                    graphics.draw(s);
                }
            }
        }

        // draw line decorations
        drawDecoration(graphics, line, stroke);

        graphics.setColor(oldColor);
        graphics.setPaint(oldPaint);
        graphics.setStroke(oldStroke);
    }

    private void fillArea(Graphics2D graphics, PaintModifier pm, Path2D area) {
        final SimpleShape<?, ?> ss = getShape();
        final PaintStyle ps = ss.getFillStyle().getPaint();
        final DrawPaint drawPaint = DrawFactory.getInstance(graphics).getPaint(ss);
        final Paint fillMod = drawPaint.getPaint(graphics, ps, pm);
        if (fillMod != null) {
            graphics.setPaint(fillMod);
            fillPaintWorkaround(graphics, area);
        }
    }

    protected Paint getFillPaint(Graphics2D graphics) {
        DrawPaint drawPaint = DrawFactory.getInstance(graphics).getPaint(getShape());
        return drawPaint.getPaint(graphics, getShape().getFillStyle().getPaint());
    }

    protected Paint getLinePaint(Graphics2D graphics) {
        DrawPaint drawPaint = DrawFactory.getInstance(graphics).getPaint(getShape());
        return drawPaint.getPaint(graphics, getShape().getStrokeStyle().getPaint());
    }


    protected void drawDecoration(Graphics2D graphics, Paint line, BasicStroke stroke) {
        if(line == null) {
            return;
        }
        graphics.setPaint(line);

        List<Outline> lst = new ArrayList<>();
        LineDecoration deco = getShape().getLineDecoration();
        Outline head = getHeadDecoration(graphics, deco, stroke);
        if (head != null) {
            lst.add(head);
        }
        Outline tail = getTailDecoration(graphics, deco, stroke);
        if (tail != null) {
            lst.add(tail);
        }


        for(Outline o : lst){
            java.awt.Shape s = o.getOutline();
            PathIf p = o.getPath();
            graphics.setRenderingHint(Drawable.GRADIENT_SHAPE, s);

            if(p.isFilled()) {
                graphics.fill(s);
            }
            if(p.isStroked()) {
                graphics.draw(s);
            }
        }
    }

    protected Outline getTailDecoration(Graphics2D graphics, LineDecoration deco, BasicStroke stroke) {
        if (deco == null || stroke == null) {
            return null;
        }
        DecorationSize tailLength = deco.getTailLength();
        if (tailLength == null) {
            tailLength = DecorationSize.MEDIUM;
        }
        DecorationSize tailWidth = deco.getTailWidth();
        if (tailWidth == null) {
            tailWidth = DecorationSize.MEDIUM;
        }

        double lineWidth = Math.max(2.5, stroke.getLineWidth());

        Rectangle2D anchor = getAnchor(graphics, getShape());
        double x2 = 0, y2 = 0, alpha = 0;
        if (anchor != null) {
            x2 = anchor.getX() + anchor.getWidth();
            y2 = anchor.getY() + anchor.getHeight();
            alpha = Math.atan(anchor.getHeight() / anchor.getWidth());
        }

        AffineTransform at = new AffineTransform();
        java.awt.Shape tailShape = null;
        Path p = null;
        Rectangle2D bounds;
        final double scaleY = Math.pow(DECO_SIZE_POW, tailWidth.ordinal()+1.);
        final double scaleX = Math.pow(DECO_SIZE_POW, tailLength.ordinal()+1.);

        DecorationShape tailShapeEnum = deco.getTailShape();

        if (tailShapeEnum == null) {
            return null;
        }

        switch (tailShapeEnum) {
            case OVAL:
                p = new Path();
                tailShape = new Ellipse2D.Double(0, 0, lineWidth * scaleX, lineWidth * scaleY);
                bounds = tailShape.getBounds2D();
                at.translate(x2 - bounds.getWidth() / 2, y2 - bounds.getHeight() / 2);
                at.rotate(alpha, bounds.getX() + bounds.getWidth() / 2, bounds.getY() + bounds.getHeight() / 2);
                break;
            case STEALTH:
            case ARROW:
                p = new Path();
                p.setFill(PaintModifier.NONE);
                p.setStroke(true);
                Path2D.Double arrow = new Path2D.Double();
                arrow.moveTo((-lineWidth * scaleX), (-lineWidth * scaleY / 2));
                arrow.lineTo(0, 0);
                arrow.lineTo((-lineWidth * scaleX), (lineWidth * scaleY / 2));
                tailShape = arrow;
                at.translate(x2, y2);
                at.rotate(alpha);
                break;
            case TRIANGLE:
                p = new Path();
                Path2D.Double triangle = new Path2D.Double();
                triangle.moveTo((-lineWidth * scaleX), (-lineWidth * scaleY / 2));
                triangle.lineTo(0, 0);
                triangle.lineTo((-lineWidth * scaleX), (lineWidth * scaleY / 2));
                triangle.closePath();
                tailShape = triangle;
                at.translate(x2, y2);
                at.rotate(alpha);
                break;
            default:
                break;
        }

        if (tailShape != null) {
            tailShape = at.createTransformedShape(tailShape);
        }
        return tailShape == null ? null : new Outline(tailShape, p);
    }

    protected Outline getHeadDecoration(Graphics2D graphics, LineDecoration deco, BasicStroke stroke) {
        if (deco == null || stroke == null) {
            return null;
        }
        DecorationSize headLength = deco.getHeadLength();
        if (headLength == null) {
            headLength = DecorationSize.MEDIUM;
        }
        DecorationSize headWidth = deco.getHeadWidth();
        if (headWidth == null) {
            headWidth = DecorationSize.MEDIUM;
        }

        double lineWidth = Math.max(2.5, stroke.getLineWidth());

        Rectangle2D anchor = getAnchor(graphics, getShape());
        double x1 = 0, y1 = 0, alpha = 0;
        if (anchor != null) {
            x1 = anchor.getX();
            y1 = anchor.getY();
            alpha = Math.atan(anchor.getHeight() / anchor.getWidth());
        }

        AffineTransform at = new AffineTransform();
        java.awt.Shape headShape = null;
        Path p = null;
        Rectangle2D bounds;
        final double scaleY = Math.pow(DECO_SIZE_POW, headWidth.ordinal()+1.);
        final double scaleX = Math.pow(DECO_SIZE_POW, headLength.ordinal()+1.);
        DecorationShape headShapeEnum = deco.getHeadShape();

        if (headShapeEnum == null) {
            return null;
        }

        switch (headShapeEnum) {
            case OVAL:
                p = new Path();
                headShape = new Ellipse2D.Double(0, 0, lineWidth * scaleX, lineWidth * scaleY);
                bounds = headShape.getBounds2D();
                at.translate(x1 - bounds.getWidth() / 2, y1 - bounds.getHeight() / 2);
                at.rotate(alpha, bounds.getX() + bounds.getWidth() / 2, bounds.getY() + bounds.getHeight() / 2);
                break;
            case STEALTH:
            case ARROW:
                p = new Path();
                p.setFill(PaintModifier.NONE);
                p.setStroke(true);
                Path2D.Double arrow = new Path2D.Double();
                arrow.moveTo((lineWidth * scaleX), (-lineWidth * scaleY / 2));
                arrow.lineTo(0, 0);
                arrow.lineTo((lineWidth * scaleX), (lineWidth * scaleY / 2));
                headShape = arrow;
                at.translate(x1, y1);
                at.rotate(alpha);
                break;
            case TRIANGLE:
                p = new Path();
                Path2D.Double triangle = new Path2D.Double();
                triangle.moveTo((lineWidth * scaleX), (-lineWidth * scaleY / 2));
                triangle.lineTo(0, 0);
                triangle.lineTo((lineWidth * scaleX), (lineWidth * scaleY / 2));
                triangle.closePath();
                headShape = triangle;
                at.translate(x1, y1);
                at.rotate(alpha);
                break;
            default:
                break;
        }

        if (headShape != null) {
            headShape = at.createTransformedShape(headShape);
        }
        return headShape == null ? null : new Outline(headShape, p);
    }

    public BasicStroke getStroke() {
        return getStroke(getShape().getStrokeStyle());
    }

    protected void drawShadow(
        Graphics2D graphics
      , Collection<Outline> outlines
      , Paint fill
      , Paint line
    ) {
        Shadow<?,?> shadow = getShape().getShadow();
        if (shadow == null || (fill == null && line == null)) {
            return;
        }

        SolidPaint shadowPaint = shadow.getFillStyle();
        Color shadowColor = DrawPaint.applyColorTransform(shadowPaint.getSolidColor());

        double shapeRotation = getShape().getRotation();
        if (getShape().getFlipVertical()) {
            shapeRotation += 180;
        }
        double angle = shadow.getAngle() - shapeRotation;
        double dist = shadow.getDistance();
        double dx = dist * Math.cos(Math.toRadians(angle));
        double dy = dist * Math.sin(Math.toRadians(angle));

        graphics.translate(dx, dy);

        for (Outline o : outlines) {
            java.awt.Shape s = o.getOutline();
            PathIf p = o.getPath();
            graphics.setRenderingHint(Drawable.GRADIENT_SHAPE, s);
            graphics.setPaint(shadowColor);

            if (fill != null && p.isFilled()) {
                fillPaintWorkaround(graphics, s);
            } else if (line != null && p.isStroked()) {
                graphics.draw(s);
            }
        }

        graphics.translate(-dx, -dy);
    }

    protected Collection<Outline> computeOutlines(Graphics2D graphics) {
        final SimpleShape<?,?> sh = getShape();

        List<Outline> lst = new ArrayList<>();
        CustomGeometry geom = sh.getGeometry();
        if(geom == null) {
            return lst;
        }

        Rectangle2D anchor = getAnchor(graphics, sh);
        if(anchor == null) {
            return lst;
        }
        for (PathIf p : geom) {

            double w = p.getW(), h = p.getH(), scaleX, scaleY;
            if (w == -1) {
                w = Units.toEMU(anchor.getWidth());
                scaleX = Units.toPoints(1);
            } else if (anchor.getWidth() == 0) {
                scaleX = 1;
            } else {
                scaleX = anchor.getWidth() / w;
            }
            if (h == -1) {
                h = Units.toEMU(anchor.getHeight());
                scaleY = Units.toPoints(1);
            } else if (anchor.getHeight() == 0) {
                scaleY = 1;
            } else {
                scaleY = anchor.getHeight() / h;
            }

            // the guides in the shape definitions are all defined relative to each other,
            // so we build the path starting from (0,0).
            final Rectangle2D pathAnchor = new Rectangle2D.Double(0,0,w,h);

            Context ctx = new Context(geom, pathAnchor, sh);

            java.awt.Shape gp = p.getPath(ctx);

            // translate the result to the canvas coordinates in points
            AffineTransform at = new AffineTransform();
            at.translate(anchor.getX(), anchor.getY());
            at.scale(scaleX, scaleY);

            java.awt.Shape canvasShape = at.createTransformedShape(gp);

            lst.add(new Outline(canvasShape, p));
        }

        return lst;
    }

    @Override
    protected SimpleShape<?,?> getShape() {
        return (SimpleShape<?,?>)shape;
    }
}
