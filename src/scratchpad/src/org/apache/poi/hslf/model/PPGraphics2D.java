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
package org.apache.poi.hslf.model;


import java.awt.*;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.image.*;
import java.awt.image.renderable.RenderableImage;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.text.AttributedCharacterIterator;
import java.util.Map;
import java.util.ArrayList;

import org.apache.poi.ddf.EscherProperties;
import org.apache.poi.hslf.usermodel.RichTextRun;

/**
 * Translates Graphics2D calls into PowerPoint.
 *
 * @author Yegor Kozlov
 */
public class PPGraphics2D extends Graphics2D {
    //The group to write the graphics calls into.
    private ShapeGroup group;

    private AffineTransform transform;
    private Stroke stroke;
    private Paint paint;
    private Font font;
    private Color foreground;
    private Color background = Color.white;
    private Shape clip;
    int count = 0;
    /**
     * Construct Java Graphics object which translates graphic calls in ppt drawing layer.
     *
     * @param group           The shape group to write the graphics calls into.
     */
    public PPGraphics2D(ShapeGroup group){
        this.group = group;
        transform = new AffineTransform();
    }

    /**
     * @return  the shape group being used for drawing
     */
    public ShapeGroup getShapeGroup(){
        return group;
    }

    public Font getFont(){
        return font;
    }

    public void setFont(Font font){
        this.font = font;
    }

    public Color getColor(){
        return foreground;
    }

    public void setColor(Color color) {
        this.foreground = color;
    }

    public Stroke getStroke(){
        return stroke;
    }

    public void setStroke(Stroke s){
        this.stroke = s;
    }

    public Paint getPaint(){
        return paint;
    }

    public void setPaint(Paint paint){
        this.paint = paint;
        if (paint instanceof Color) setColor((Color)paint);
    }

    public AffineTransform getTransform(){
        return (AffineTransform)transform.clone();
    }

    public void setTransform(AffineTransform trans) {
        transform = (AffineTransform)trans.clone();
    }

    public void draw(Shape shape){
        if(clip != null) {
            java.awt.Rectangle bounds = getTransform().createTransformedShape(shape).getBounds();
            if (bounds.width == 0) bounds.width = 1;
            if (bounds.height == 0) bounds.height = 1;
            if (!clip.getBounds().contains(bounds)) {
                return;
            }
        }

        PathIterator it = shape.getPathIterator(transform);
        double[] prev = null;
        double[] coords = new double[6];
        double[] first = new double[6];
        if(!it.isDone()) it.currentSegment(first); //first point
        while(!it.isDone()){
            int type = it.currentSegment(coords);
            if (prev != null ){
                Line line = new Line(group);
                if (stroke instanceof BasicStroke){
                    BasicStroke bs = (BasicStroke)stroke;
                    line.setLineWidth(bs.getLineWidth());
                    float[] dash = bs.getDashArray();
                    if (dash != null) line.setLineDashing(Line.PEN_DASH);
                }
                if(getColor() != null) line.setLineColor(getColor());
                if (type == PathIterator.SEG_LINETO) {
                    line.setAnchor(new java.awt.Rectangle((int)prev[0],  (int)prev[1], (int)(coords[0] - prev[0]), (int)(coords[1] - prev[1])));
                } else if (type == PathIterator.SEG_CLOSE){
                    line.setAnchor(new java.awt.Rectangle((int)coords[0],  (int)coords[1], (int)(first[0] - coords[0]), (int)(first[1] - coords[1])));
                }
                group.addShape(line);
            }
            prev = new double[]{coords[0],  coords[1]};
            it.next();
        }

    }

    public void drawString(String string, float x, float y){
         TextBox txt = new TextBox(group);
         txt.getTextRun().supplySlideShow(group.getSheet().getSlideShow());
         txt.getTextRun().setSheet(group.getSheet());
         txt.setText(string);

         RichTextRun rt = txt.getTextRun().getRichTextRuns()[0];
         rt.setFontSize(font.getSize());
         rt.setFontName(font.getFamily());

        if(getColor() != null) rt.setFontColor(getColor());
        if (font.isBold()) rt.setBold(true);
        if (font.isItalic()) rt.setItalic(true);

         txt.setMarginBottom(0);
         txt.setMarginTop(0);
         txt.setMarginLeft(0);
         txt.setMarginRight(0);
         txt.setWordWrap(TextBox.WrapNone);

         if (!"".equals(string)) txt.resizeToFitText();
         int height = (int)txt.getAnchor().getHeight();

         /*
           In powerpoint anchor of a shape is its top left corner.
           Java graphics sets string coordinates by the baseline of the first character
           so we need to shift down by the height of the textbox
         */
        txt.moveTo((int)x, (int)(y - height));

        if(clip != null) {
            if (!clip.getBounds().contains(txt.getAnchor())) {
                ;//return;
            }
        }
       group.addShape(txt);
    }

    public void fill(Shape shape){
        if(clip != null) {
            java.awt.Rectangle bounds = getTransform().createTransformedShape(shape).getBounds();
            if (bounds.width == 0) bounds.width = 1;
            if (bounds.height == 0) bounds.height = 1;
             if (!clip.getBounds().contains(bounds)) {
                return;
            }
        }
        PathIterator it = shape.getPathIterator(transform);
        ArrayList pnt = new ArrayList();
        double[] coords = new double[6];
        while(!it.isDone()){
            int type = it.currentSegment(coords);
            if (type != PathIterator.SEG_CLOSE) {
                pnt.add(new Point((int)coords[0], (int)coords[1]));
            }
            it.next();
        }
        int[] xPoints= new int[pnt.size()];
        int[] yPoints= new int[pnt.size()];
        for (int i = 0; i < pnt.size(); i++) {
            Point p = (Point)pnt.get(i);
            xPoints[i] = p.x;
            yPoints[i] = p.y;
        }

        AutoShape r = new AutoShape(ShapeTypes.Rectangle);
        if (paint instanceof Color){
            Color color = (Color)paint;
            r.setFillColor(color);
        }
        if(getColor() != null) r.setLineColor(getColor());
        if (stroke instanceof BasicStroke){
            BasicStroke bs = (BasicStroke)stroke;
            r.setLineWidth(bs.getLineWidth());
            float[] dash = bs.getDashArray();
            if (dash != null) r.setLineDashing(Line.PEN_DASH);
        }

        java.awt.Rectangle bounds = transform.createTransformedShape(shape).getBounds();
        r.setAnchor(bounds);
        group.addShape(r);
    }

    public void translate(int x, int y) {
        AffineTransform at = new AffineTransform();
        at.translate(x, y);
        transform.concatenate(at);
    }

    public void clip(Shape shape) {
        this.clip = transform.createTransformedShape(shape);
        //update size of the escher group which holds the drawing
        group.setAnchor(clip.getBounds());
    }

    public Shape getClip() {
        return clip;
    }

    public void scale(double sx, double sy) {
        AffineTransform at = new AffineTransform();
        at.scale(sx, sy);
        transform.concatenate(at);
    }
    //===============================================
    public void drawRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
        throw new RuntimeException("Not implemented");
    }

    public void drawString(String str, int x, int y) {
        throw new RuntimeException("Not implemented");
    }

    public void fillOval(int x, int y, int width, int height) {
        throw new RuntimeException("Not implemented");
    }

    public void fillRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
        throw new RuntimeException("Not implemented");
    }

    public void fillArc(int x, int y, int width, int height,
                        int startAngle, int arcAngle) {
        throw new RuntimeException("Not implemented");
    }

    public void setPaintMode() {
        throw new RuntimeException("Not implemented");
    }

    public void drawArc(int x, int y, int width, int height,
                        int startAngle, int arcAngle) {
        throw new RuntimeException("Not implemented");
    }


    public void drawPolyline(int xPoints[], int yPoints[],
                             int nPoints) {
        throw new RuntimeException("Not implemented");
    }

    public Graphics create() {
        throw new RuntimeException("Not implemented");
    }

    public void drawOval(int x, int y, int width, int height) {
        AutoShape ellipse = new AutoShape(ShapeTypes.Ellipse);
        ellipse.setAnchor(new java.awt.Rectangle(x-width/2, y-height/2, width, height));
        if (stroke instanceof BasicStroke){
            BasicStroke bs = (BasicStroke)stroke;
            ellipse.setLineWidth(bs.getLineWidth());
        }
        if(getColor() != null) ellipse.setLineColor(getColor());
        if (paint instanceof Color){
            Color color = (Color)paint;
            ellipse.setFillColor(color);
        }

        group.addShape(ellipse);
    }

    public void setXORMode(Color color1) {
        throw new RuntimeException("Not implemented");
    }


    public boolean drawImage(Image img, int x, int y,
                             Color bgcolor,
                             ImageObserver observer) {
        throw new RuntimeException("Not implemented");
    }

    public boolean drawImage(Image img, int x, int y,
                             int width, int height,
                             Color bgcolor,
                             ImageObserver observer) {
        throw new RuntimeException("Not implemented");
    }


    public boolean drawImage(Image img,
                             int dx1, int dy1, int dx2, int dy2,
                             int sx1, int sy1, int sx2, int sy2,
                             ImageObserver observer) {
        throw new RuntimeException("Not implemented");
    }

    public boolean drawImage(Image img,
                             int dx1, int dy1, int dx2, int dy2,
                             int sx1, int sy1, int sx2, int sy2,
                             Color bgcolor,
                             ImageObserver observer) {
        throw new RuntimeException("Not implemented");
    }

    public boolean drawImage(Image img, int x, int y,
                             ImageObserver observer) {
        throw new RuntimeException("Not implemented");
    }

    public boolean drawImage(Image img, int x, int y,
                             int width, int height,
                             ImageObserver observer) {
        throw new RuntimeException("Not implemented");
    }

    public void dispose() {
        throw new RuntimeException("Not implemented");
    }

    public void drawLine(int x1, int y1, int x2, int y2) {
        Line line = new Line();
        line.setAnchor(new java.awt.Rectangle(x1, y1, x2-x1, y2-y1));
        if (stroke instanceof BasicStroke){
            BasicStroke bs = (BasicStroke)stroke;
            line.setLineWidth(bs.getLineWidth());
        }
        if(getColor() != null) line.setLineColor(getColor());
        group.addShape(line);
    }

    public void fillPolygon(int xPoints[], int yPoints[],
                            int nPoints) {
        throw new RuntimeException("Not implemented");
    }

    public FontMetrics getFontMetrics(Font f) {
        throw new RuntimeException("Not implemented");
    }

    public void fillRect(int x, int y, int width, int height) {
        throw new RuntimeException("Not implemented");
    }

    public void drawPolygon(int xPoints[], int yPoints[],
                            int nPoints) {
        throw new RuntimeException("Not implemented");
    }

    public void clipRect(int x, int y, int width, int height) {
        throw new RuntimeException("Not implemented");
    }

    public void setClip(Shape clip) {
        throw new RuntimeException("Not implemented");
    }

    public java.awt.Rectangle getClipBounds() {
        throw new RuntimeException("Not implemented");
    }

    public void drawString(AttributedCharacterIterator iterator, int x, int y) {
        throw new RuntimeException("Not implemented");
    }

    public void clearRect(int x, int y, int width, int height) {
        throw new RuntimeException("Not implemented");
    }

    public void copyArea(int x, int y, int width, int height, int dx, int dy) {
        throw new RuntimeException("Not implemented");
    }

    public void setClip(int x, int y, int width, int height) {
        throw new RuntimeException("Not implemented");
    }

    public void rotate(double d) {
        throw new RuntimeException("Not implemented");

    }

    public void rotate(double d, double d1, double d2) {
        throw new RuntimeException("Not implemented");
    }

    public void shear(double d, double d1) {
        throw new RuntimeException("Not implemented");
    }

    public FontRenderContext getFontRenderContext() {
        return new FontRenderContext(transform, true, true);
    }

    public void transform(AffineTransform affinetransform) {
        throw new RuntimeException("Not implemented");
    }

    public void drawImage(BufferedImage bufferedimage, BufferedImageOp op, int x, int y) {
        throw new RuntimeException("Not implemented");
    }

    public void setBackground(Color c) {
        throw new RuntimeException("Not implemented");
    }

    public void drawRenderedImage(RenderedImage renderedimage, AffineTransform affinetransform) {
        throw new RuntimeException("Not implemented");
    }

    public Color getBackground() {
        throw new RuntimeException("Not implemented");
    }

    public void setComposite(Composite composite) {
        throw new RuntimeException("Not implemented");

    }

    public Composite getComposite() {
        throw new RuntimeException("Not implemented");
    }

    public Object getRenderingHint(java.awt.RenderingHints.Key key) {
        throw new RuntimeException("Not implemented");
    }

    public boolean drawImage(Image image, AffineTransform affinetransform, ImageObserver imageobserver) {
        throw new RuntimeException("Not implemented");
    }

    public void setRenderingHint(java.awt.RenderingHints.Key key, Object obj) {
        throw new RuntimeException("Not implemented");
    }


    public void drawGlyphVector(GlyphVector g, float x, float y) {
        throw new RuntimeException("Not implemented");

    }

    public GraphicsConfiguration getDeviceConfiguration() {
        throw new RuntimeException("Not implemented");
    }

    public void addRenderingHints(Map map) {
        throw new RuntimeException("Not implemented");
    }

    public void translate(double d, double d1) {

        throw new RuntimeException("Not implemented");
    }

    public void drawString(AttributedCharacterIterator attributedcharacteriterator, float x, float y) {
        throw new RuntimeException("Not implemented");
    }

    public boolean hit(java.awt.Rectangle rectangle, Shape shape, boolean flag) {
        throw new RuntimeException("Not implemented");
    }

    public RenderingHints getRenderingHints() {
        throw new RuntimeException("Not implemented");
    }

    public void setRenderingHints(Map map) {
        throw new RuntimeException("Not implemented");

    }

    public void drawRenderableImage(RenderableImage renderableimage, AffineTransform affinetransform) {
        throw new RuntimeException("Not implemented");
    }
}
