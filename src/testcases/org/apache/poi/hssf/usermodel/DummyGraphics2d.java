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


package org.apache.poi.hssf.usermodel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.io.PrintStream;
import java.text.AttributedCharacterIterator;
import java.util.Arrays;
import java.util.Map;

import org.apache.poi.util.Internal;

public class DummyGraphics2d extends Graphics2D {
    private BufferedImage bufimg;
    private final Graphics2D g2D;
    private final PrintStream log;

    public DummyGraphics2d() {
        this(System.out);
    }
    
    public DummyGraphics2d(PrintStream log) {
        bufimg = new BufferedImage(1000, 1000, 2);
        g2D = (Graphics2D)bufimg.getGraphics();
        this.log = log;
    }
    
    public DummyGraphics2d(PrintStream log, Graphics2D g2D) {
        this.g2D = g2D;
        this.log = log;
    }

    public void addRenderingHints(Map<?,?> hints) {
        String l =
            "addRenderingHinds(Map):" +
            "\n  hints = " + hints;
        log.println( l );
        g2D.addRenderingHints( hints );
    }

    public void clip(Shape s) {
        String l =
            "clip(Shape):" +
            "\n  s = " + s;
        log.println( l );
        g2D.clip( s );
    }

    public void draw(Shape s) {
        String l =
            "draw(Shape):" + 
            "\n  s = " + s;
        log.println( l );
        g2D.draw( s );
    }

    public void drawGlyphVector(GlyphVector g, float x, float y) {
        String l =
            "drawGlyphVector(GlyphVector, float, float):" +
            "\n  g = " + g +
            "\n  x = " + x +
            "\n  y = " + y;
        log.println( l );
        g2D.drawGlyphVector( g, x, y );
    }

    public void drawImage(BufferedImage img, BufferedImageOp op, int x, int y) {
        String l =
            "drawImage(BufferedImage, BufferedImageOp, x, y):" +
            "\n  img = " + img +
            "\n  op = " + op +
            "\n  x = " + x +
            "\n  y = " + y;
        log.println( l );
        g2D.drawImage( img, op, x, y );
    }

    public boolean drawImage(Image img, AffineTransform xform, ImageObserver obs) {
        String l =
            "drawImage(Image,AfflineTransform,ImageObserver):" +
            "\n  img = " + img +
            "\n  xform = " + xform +
            "\n  obs = " + obs;
        log.println( l );
        return g2D.drawImage( img, xform, obs );
    }

    public void drawRenderableImage(RenderableImage img, AffineTransform xform) {
        String l =
            "drawRenderableImage(RenderableImage, AfflineTransform):" +
            "\n  img = " + img +
            "\n  xform = " + xform;
        log.println( l );
        g2D.drawRenderableImage( img, xform );
    }

    public void drawRenderedImage(RenderedImage img, AffineTransform xform) {
        String l =
            "drawRenderedImage(RenderedImage, AffineTransform):" +
            "\n  img = " + img +
            "\n  xform = " + xform;
        log.println( l );
        g2D.drawRenderedImage( img, xform );
    }

    public void drawString(AttributedCharacterIterator iterator, float x, float y) {
        String l =
            "drawString(AttributedCharacterIterator):" +
            "\n  iterator = " + iterator +
            "\n  x = " + x +
            "\n  y = " + y;
        log.println( l );
        g2D.drawString( iterator, x, y );
    }

    public void drawString(String s, float x, float y) {
        String l =
            "drawString(s,x,y):" +
            "\n  s = " + s +
            "\n  x = " + x +
            "\n  y = " + y;
        log.println( l );
        g2D.drawString( s, x, y );
    }

    public void fill(Shape s) {
        String l =
            "fill(Shape):" +
            "\n  s = " + s;
        log.println( l );
        g2D.fill( s );
    }

    public Color getBackground() {
        log.println( "getBackground():" );
        return g2D.getBackground();
    }

    public Composite getComposite() {
        log.println( "getComposite():" );
        return g2D.getComposite();
    }

    public GraphicsConfiguration getDeviceConfiguration() {
        log.println( "getDeviceConfiguration():" );
        return g2D.getDeviceConfiguration();
    }

    public FontRenderContext getFontRenderContext() {
        log.println( "getFontRenderContext():" );
        return g2D.getFontRenderContext();
    }

    public Paint getPaint() {
        log.println( "getPaint():" );
        return g2D.getPaint();
    }

    public Object getRenderingHint(RenderingHints.Key hintKey) {
        String l =
            "getRenderingHint(RenderingHints.Key):" +
            "\n  hintKey = " + hintKey;
        log.println( l );
        return g2D.getRenderingHint( hintKey );
    }

    public RenderingHints getRenderingHints() {
        log.println( "getRenderingHints():" );
        return g2D.getRenderingHints();
    }

    public Stroke getStroke() {
        log.println( "getStroke():" );
        return g2D.getStroke();
    }

    public AffineTransform getTransform() {
        log.println( "getTransform():" );
        return g2D.getTransform();
    }

    public boolean hit(Rectangle rect, Shape s, boolean onStroke) {
        String l =
            "hit(Rectangle, Shape, onStroke):" +
            "\n  rect = " + rect +
            "\n  s = " + s +
            "\n  onStroke = " + onStroke;
        log.println( l );
        return g2D.hit( rect, s, onStroke );
    }

    public void rotate(double theta) {
        String l =
            "rotate(theta):" +
            "\n  theta = " + theta;
        log.println( l );
        g2D.rotate( theta );
    }

    public void rotate(double theta, double x, double y) {
        String l =
            "rotate(double,double,double):" +
            "\n  theta = " + theta +
            "\n  x = " + x +
            "\n  y = " + y;
        log.println( l );
        g2D.rotate( theta, x, y );
    }

    public void scale(double sx, double sy) {
        String l =
            "scale(double,double):" +
            "\n  sx = " + sx +
            "\n  sy";
        log.println( l );
        g2D.scale( sx, sy );
    }

    public void setBackground(Color color) {
        String l =
            "setBackground(Color):" +
            "\n  color = " + color;
        log.println( l );
        g2D.setBackground( color );
    }

    public void setComposite(Composite comp) {
        String l =
            "setComposite(Composite):" +
            "\n  comp = " + comp;
        log.println( l );
        g2D.setComposite( comp );
    }

    public void setPaint( Paint paint ) {
        String l =
            "setPaint(Paint):" +
            "\n  paint = " + paint;
        log.println( l );
        g2D.setPaint( paint );
    }

    public void setRenderingHint(RenderingHints.Key hintKey, Object hintValue) {
        String l =
            "setRenderingHint(RenderingHints.Key, Object):" +
            "\n  hintKey = " + hintKey +
            "\n  hintValue = " + hintValue;
        log.println( l );
        g2D.setRenderingHint( hintKey, hintValue );
    }

    public void setRenderingHints(Map<?,?> hints) {
        String l =
            "setRenderingHints(Map):" +
            "\n  hints = " + hints;
        log.println( l );
        g2D.setRenderingHints( hints );
    }

    public void setStroke(Stroke s) {
        String l;
        if (s instanceof BasicStroke) {
            BasicStroke bs = (BasicStroke)s;
            l = "setStroke(Stoke):" +
                "\n  s = BasicStroke(" +
                "\n    dash[]: "+Arrays.toString(bs.getDashArray()) +
                "\n    dashPhase: "+bs.getDashPhase() +
                "\n    endCap: "+bs.getEndCap() +
                "\n    lineJoin: "+bs.getLineJoin() +
                "\n    width: "+bs.getLineWidth() +
                "\n    miterLimit: "+bs.getMiterLimit() +
                "\n  )";
        } else {
            l = "setStroke(Stoke):" +
                "\n  s = " + s;
        }
        log.println( l );
        g2D.setStroke( s );
    }

    public void setTransform(AffineTransform Tx) {
        String l =
            "setTransform():" +
            "\n  Tx = " + Tx;
        log.println( l );
        g2D.setTransform( Tx );
    }

    public void shear(double shx, double shy) {
        String l =
            "shear(shx, dhy):" +
            "\n  shx = " + shx +
            "\n  shy = " + shy;
        log.println( l );
        g2D.shear( shx, shy );
    }

    public void transform(AffineTransform Tx) {
        String l =
            "transform(AffineTransform):" +
            "\n  Tx = " + Tx;
        log.println( l );
        g2D.transform( Tx );
    }

    public void translate(double tx, double ty) {
        String l =
            "translate(double, double):" +
            "\n  tx = " + tx +
            "\n  ty = " + ty;
        log.println( l );
        g2D.translate( tx, ty );
    }

    public void clearRect(int x, int y, int width, int height) {
        String l =
            "clearRect(int,int,int,int):" +
            "\n  x = " + x +
            "\n  y = " + y +
            "\n  width = " + width +
            "\n  height = " + height;
        log.println( l );
        g2D.clearRect( x, y, width, height );
    }

    public void clipRect(int x, int y, int width, int height) {
        String l =
            "clipRect(int, int, int, int):" +
            "\n  x = " + x +
            "\n  y = " + y +
            "\n  width = " + width +
            "height = " + height;
        log.println( l );
        g2D.clipRect( x, y, width, height );
    }

    public void copyArea(int x, int y, int width, int height, int dx, int dy) {
        String l =
            "copyArea(int,int,int,int):" +
            "\n  x = " + x +
            "\n  y = " + y +
            "\n  width = " + width +
            "\n  height = " + height;
        log.println( l );
        g2D.copyArea( x, y, width, height, dx, dy );
    }

    public Graphics create() {
        log.println( "create():" );
        return g2D.create();
    }

    public Graphics create(int x, int y, int width, int height) {
        String l =
            "create(int,int,int,int):" +
            "\n  x = " + x +
            "\n  y = " + y +
            "\n  width = " + width +
            "\n  height = " + height;
        log.println( l );
        return g2D.create( x, y, width, height );
    }

    public void dispose() {
        log.println( "dispose():" );
        g2D.dispose();
    }

    public void draw3DRect(int x, int y, int width, int height, boolean raised) {
        String l =
            "draw3DRect(int,int,int,int,boolean):" +
            "\n  x = " + x +
            "\n  y = " + y +
            "\n  width = " + width +
            "\n  height = " + height +
            "\n  raised = " + raised;
        log.println( l );
        g2D.draw3DRect( x, y, width, height, raised );
    }

    public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
        String l =
            "drawArc(int,int,int,int,int,int):" +
            "\n  x = " + x +
            "\n  y = " + y +
            "\n  width = " + width +
            "\n  height = " + height +
            "\n  startAngle = " + startAngle +
            "\n  arcAngle = " + arcAngle;
        log.println( l );
        g2D.drawArc( x, y, width, height, startAngle, arcAngle );
    }

    public void drawBytes(byte data[], int offset, int length, int x, int y) {
        String l =
            "drawBytes(byte[],int,int,int,int):" +
            "\n  data = " + Arrays.toString(data) +
            "\n  offset = " + offset +
            "\n  length = " + length +
            "\n  x = " + x +
            "\n  y = " + y;
        log.println( l );
        g2D.drawBytes( data, offset, length, x, y );
    }

    public void drawChars(char data[], int offset, int length, int x, int y) {
        String l =
            "drawChars(data,int,int,int,int):" +
            "\n  data = " + Arrays.toString(data) +
            "\n  offset = " + offset +
            "\n  length = " + length +
            "\n  x = " + x +
            "\n  y = " + y;
        log.println( l );
        g2D.drawChars( data, offset, length, x, y );
    }

    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, ImageObserver observer) {
        String l =
            "drawImage(Image,int,int,int,int,int,int,int,int,ImageObserver):" +
            "\n  img = " + img +
            "\n  dx1 = " + dx1 +
            "\n  dy1 = " + dy1 +
            "\n  dx2 = " + dx2 +
            "\n  dy2 = " + dy2 +
            "\n  sx1 = " + sx1 +
            "\n  sy1 = " + sy1 +
            "\n  sx2 = " + sx2 +
            "\n  sy2 = " + sy2 +
            "\n  observer = " + observer;
        log.println( l );
        return g2D.drawImage( img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, observer );
    }

    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, Color bgcolor, ImageObserver observer) {
        String l =
            "drawImage(Image,int,int,int,int,int,int,int,int,Color,ImageObserver):" +
            "\n  img = " + img +
            "\n  dx1 = " + dx1 +
            "\n  dy1 = " + dy1 +
            "\n  dx2 = " + dx2 +
            "\n  dy2 = " + dy2 +
            "\n  sx1 = " + sx1 +
            "\n  sy1 = " + sy1 +
            "\n  sx2 = " + sx2 +
            "\n  sy2 = " + sy2 +
            "\n  bgcolor = " + bgcolor +
            "\n  observer = " + observer;
        log.println( l );
        return g2D.drawImage( img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, bgcolor, observer );
    }

    public boolean drawImage(Image img, int x, int y, Color bgcolor, ImageObserver observer) {
        String l =
            "drawImage(Image,int,int,Color,ImageObserver):" +
            "\n  img = " + img +
            "\n  x = " + x +
            "\n  y = " + y +
            "\n  bgcolor = " + bgcolor +
            "\n  observer = " + observer;
        log.println( l );
        return g2D.drawImage( img, x, y, bgcolor, observer );
    }

    public boolean drawImage(Image img, int x, int y, ImageObserver observer) {
        String l =
            "drawImage(Image,int,int,observer):" +
            "\n  img = " + img +
            "\n  x = " + x +
            "\n  y = " + y +
            "\n  observer = " + observer;
        log.println( l );
        return g2D.drawImage( img, x, y, observer );
    }

    public boolean drawImage(Image img, int x, int y, int width, int height, Color bgcolor, ImageObserver observer) {
        String l =
            "drawImage(Image,int,int,int,int,Color,ImageObserver):" +
            "\n  img = " + img +
            "\n  x = " + x +
            "\n  y = " + y +
            "\n  width = " + width +
            "\n  height = " + height +
            "\n  bgcolor = " + bgcolor +
            "\n  observer = " + observer;
        log.println( l );
        return g2D.drawImage( img, x, y, width, height, bgcolor, observer );
    }

    public boolean drawImage(Image img, int x, int y, int width, int height, ImageObserver observer) {
        String l =
            "drawImage(Image,int,int,width,height,observer):" +
            "\n  img = " + img +
            "\n  x = " + x +
            "\n  y = " + y +
            "\n  width = " + width +
            "\n  height = " + height +
            "\n  observer = " + observer;
        log.println( l );
        return g2D.drawImage( img, x, y, width, height, observer );
    }

    public void drawLine(int x1, int y1, int x2, int y2) {
        String l =
            "drawLine(int,int,int,int):" +
            "\n  x1 = " + x1 +
            "\n  y1 = " + y1 +
            "\n  x2 = " + x2 +
            "\n  y2 = " + y2;
        log.println( l );
        g2D.drawLine( x1, y1, x2, y2 );
    }

    public void drawOval(int x, int y, int width, int height) {
        String l =
            "drawOval(int,int,int,int):" +
            "\n  x = " + x +
            "\n  y = " + y +
            "\n  width = " + width +
            "\n  height = " + height;
        log.println( l );
        g2D.drawOval( x, y, width, height );
    }

    public void drawPolygon(Polygon p) {
        String l =
            "drawPolygon(Polygon):" +
            "\n  p = " + p;
        log.println( l );
        g2D.drawPolygon( p );
    }

    public void drawPolygon(int xPoints[], int yPoints[], int nPoints) {
        String l =
            "drawPolygon(int[],int[],int):" +
            "\n  xPoints = " + Arrays.toString(xPoints) +
            "\n  yPoints = " + Arrays.toString(yPoints) +
            "\n  nPoints = " + nPoints;
        log.println( l );
        g2D.drawPolygon( xPoints, yPoints, nPoints );
    }

    public void drawPolyline(int xPoints[], int yPoints[], int nPoints) {
        String l =
            "drawPolyline(int[],int[],int):" +
            "\n  xPoints = " + Arrays.toString(xPoints) +
            "\n  yPoints = " + Arrays.toString(yPoints) +
            "\n  nPoints = " + nPoints;
        log.println( l );
        g2D.drawPolyline( xPoints, yPoints, nPoints );
    }

    public void drawRect(int x, int y, int width, int height) {
        String l =
            "drawRect(int,int,int,int):" +
            "\n  x = " + x +
            "\n  y = " + y +
            "\n  width = " + width +
            "\n  height = " + height;
        log.println( l );
        g2D.drawRect( x, y, width, height );
    }

    public void drawRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
        String l =
            "drawRoundRect(int,int,int,int,int,int):" +
            "\n  x = " + x +
            "\n  y = " + y +
            "\n  width = " + width +
            "\n  height = " + height +
            "\n  arcWidth = " + arcWidth +
            "\n  arcHeight = " + arcHeight;
        log.println( l );
        g2D.drawRoundRect( x, y, width, height, arcWidth, arcHeight );
    }

    public void drawString(AttributedCharacterIterator iterator, int x, int y) {
        String l =
            "drawString(AttributedCharacterIterator,int,int):" +
            "\n  iterator = " + iterator +
            "\n  x = " + x +
            "\n  y = " + y;
        log.println( l );
        g2D.drawString( iterator, x, y );
    }

    public void drawString(String str, int x, int y) {
        String l =
            "drawString(str,int,int):" +
            "\n  str = " + str +
            "\n  x = " + x +
            "\n  y = " + y;
        log.println( l );
        g2D.drawString( str, x, y );
    }

    public void fill3DRect(int x, int y, int width, int height, boolean raised) {
        String l =
            "fill3DRect(int,int,int,int,boolean):" +
            "\n  x = " + x +
            "\n  y = " + y +
            "\n  width = " + width +
            "\n  height = " + height +
            "\n  raised = " + raised;
        log.println( l );
        g2D.fill3DRect( x, y, width, height, raised );
    }

    public void fillArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
        String l =
            "fillArc(int,int,int,int,int,int):" +
            "\n  x = " + x +
            "\n  y = " + y +
            "\n  width = " + width +
            "\n  height = " + height +
            "\n  startAngle = " + startAngle +
            "\n  arcAngle = " + arcAngle;
        log.println( l );
        g2D.fillArc( x, y, width, height, startAngle, arcAngle );
    }

    public void fillOval(int x, int y, int width, int height) {
        String l =
            "fillOval(int,int,int,int):" +
            "\n  x = " + x +
            "\n  y = " + y +
            "\n  width = " + width +
            "\n  height = " + height;
        log.println( l );
        g2D.fillOval( x, y, width, height );
    }

    public void fillPolygon(Polygon p) {
        String l =
            "fillPolygon(Polygon):" +
            "\n  p = " + p;
        log.println( l );
        g2D.fillPolygon( p );
    }

    public void fillPolygon(int xPoints[], int yPoints[], int nPoints) {
        String l =
            "fillPolygon(int[],int[],int):" +
            "\n  xPoints = " + Arrays.toString(xPoints) +
            "\n  yPoints = " + Arrays.toString(yPoints) +
            "\n  nPoints = " + nPoints;
        log.println( l );
        g2D.fillPolygon( xPoints, yPoints, nPoints );
    }

    public void fillRect(int x, int y, int width, int height) {
        String l =
            "fillRect(int,int,int,int):" +
            "\n  x = " + x +
            "\n  y = " + y +
            "\n  width = " + width +
            "\n  height = " + height;
        log.println( l );
        g2D.fillRect( x, y, width, height );
    }

    public void fillRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
        String l =
            "fillRoundRect(int,int,int,int,int,int):" +
            "\n  x = " + x +
            "\n  y = " + y +
            "\n  width = " + width +
            "\n  height = " + height;
        log.println( l );
        g2D.fillRoundRect( x, y, width, height, arcWidth, arcHeight );
    }

    // FIXME: should be protected
    // FindBugs, category MALICIOUS_CODE, FI_PUBLIC_SHOULD_BE_PROTECTED
    // A class's finalize() method should have protected access, not public
    @Internal
    @Override
    public final void finalize() {
        log.println( "finalize():" );
        g2D.finalize(); // NOSOLAR
        super.finalize();
    }

    public Shape getClip() {
        log.println( "getClip():" );
        return g2D.getClip();
    }

    public Rectangle getClipBounds() {
        log.println( "getClipBounds():" );
        return g2D.getClipBounds();
    }

    public Rectangle getClipBounds(Rectangle r) {
        String l =
            "getClipBounds(Rectangle):" +
            "\n  r = " + r;
        log.println( l );
        return g2D.getClipBounds( r );
    }

    public Color getColor() {
        log.println( "getColor():" );
        return g2D.getColor();
    }

    public Font getFont() {
        log.println( "getFont():" );
        return g2D.getFont();
    }

    public FontMetrics getFontMetrics() {
        log.println( "getFontMetrics():" );
        return g2D.getFontMetrics();
    }

    public FontMetrics getFontMetrics(Font f) {
        log.println( "getFontMetrics():" );
        return g2D.getFontMetrics( f );
    }

    public boolean hitClip(int x, int y, int width, int height) {
        String l =
            "hitClip(int,int,int,int):" +
            "\n  x = " + x +
            "\n  y = " + y +
            "\n  width = " + width +
            "\n  height = " + height;
        log.println( l );
        return g2D.hitClip( x, y, width, height );
    }

    public void setClip(Shape clip) {
        String l =
            "setClip(Shape):" +
            "\n  clip = " + clip;
        log.println( l );
        g2D.setClip( clip );
    }

    public void setClip(int x, int y, int width, int height) {
        String l =
            "setClip(int,int,int,int):" +
            "\n  x = " + x +
            "\n  y = " + y +
            "\n  width = " + width +
            "\n  height = " + height;
        log.println( l );
        g2D.setClip( x, y, width, height );
    }

    public void setColor(Color c) {
        String l =
            "setColor():" +
            "\n  c = " + c;
        log.println( l );
        g2D.setColor( c );
    }

    public void setFont(Font font) {
        String l =
            "setFont(Font):" +
            "\n  font = " + font;
        log.println( l );
        g2D.setFont( font );
    }

    public void setPaintMode() {
        log.println( "setPaintMode():" );
        g2D.setPaintMode();
    }

    public void setXORMode(Color c1) {
        String l =
            "setXORMode(Color):" +
            "\n  c1 = " + c1;
        log.println( l );
        g2D.setXORMode( c1 );
    }

    public String toString() {
        log.println( "toString():" );
        return g2D.toString();
    }

    public void translate(int x, int y) {
        String l =
            "translate(int,int):" +
            "\n  x = " + x +
            "\n  y = " + y;
        log.println( l );
        g2D.translate( x, y );
    }
}
