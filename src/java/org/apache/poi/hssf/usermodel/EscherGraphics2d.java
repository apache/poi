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

import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.text.AttributedCharacterIterator;
import java.util.Map;

/**
 * Translates Graphics2d calls into escher calls.  The translation is lossy so
 * many features are not supported and some just aren't implemented yet.  If
 * in doubt test the specific calls you wish to make. Graphics calls are
 * always drawn into an EscherGroup so one will need to be created.
 * <p>
 * <b>Important:</b>
 * <blockquote>
 * One important concept worth considering is that of font size.  One of the
 * difficulties in converting Graphics calls into escher drawing calls is that
 * Excel does not have the concept of absolute pixel positions.  It measures
 * it's cell widths in 'characters' and the cell heights in points.
 * Unfortunately it's not defined exactly what a type of character it's
 * measuring.  Presumably this is due to the fact that the Excel will be
 * using different fonts on different platforms or even within the same
 * platform.
 * <p>
 * Because of this constraint you have to calculate the verticalPointsPerPixel.
 * This the amount the font should be scaled by when
 * you issue commands such as drawString().  A good way to calculate this
 * is to use the follow formula:
 * <p>
 * <pre>
 *      multipler = groupHeightInPoints / heightOfGroup
 * </pre>
 * <p>
 * The height of the group is calculated fairly simply by calculating the
 * difference between the y coordinates of the bounding box of the shape.  The
 * height of the group can be calculated by using a convenience called
 * <code>HSSFClientAnchor.getAnchorHeightInPoints()</code>.
 * </blockquote>
 */
public final class EscherGraphics2d extends Graphics2D {
    private EscherGraphics _escherGraphics;
    private BufferedImage _img;
    private AffineTransform _trans;
    private Stroke _stroke;
    private Paint _paint;
    private Shape _deviceclip;
    private POILogger logger = POILogFactory.getLogger(getClass());

    /**
     * Constructs one escher graphics object from an escher graphics object.
     *
     * @param escherGraphics    the original EscherGraphics2d object to copy
     */
    public EscherGraphics2d(EscherGraphics escherGraphics)
    {
        this._escherGraphics = escherGraphics;
        setImg( new BufferedImage(1, 1, 2) );
        setColor(Color.black);
    }

    public void addRenderingHints(Map<?, ?> map)
    {
        getG2D().addRenderingHints(map);
    }

    public void clearRect(int i, int j, int k, int l)
    {
        Paint paint1 = getPaint();
        setColor(getBackground());
        fillRect(i, j, k, l);
        setPaint(paint1);
    }

    public void clip(Shape shape)
    {
        if(getDeviceclip() != null)
        {
            Area area = new Area(getClip());
            if(shape != null)
                area.intersect(new Area(shape));
            shape = area;
        }
        setClip(shape);
    }

    public void clipRect(int x, int y, int width, int height)
    {
        clip(new Rectangle(x,y,width,height));
    }

    public void copyArea(int x, int y, int width, int height,
				  int dx, int dy)
    {
        getG2D().copyArea(x,y,width,height,dx,dy);
    }

    public Graphics create()
    {
        return new EscherGraphics2d(_escherGraphics);
    }

    public void dispose()
    {
        getEscherGraphics().dispose();
        getG2D().dispose();
        getImg().flush();
    }

    public void draw(Shape shape)
    {
        if (shape instanceof Line2D)
        {
            Line2D shape2d = (Line2D) shape;

            int width = 0;
            if (_stroke != null && _stroke instanceof BasicStroke) {
                width = (int) ((BasicStroke)_stroke).getLineWidth() * 12700;
            }

            drawLine((int)shape2d.getX1(), (int)shape2d.getY1(), (int)shape2d.getX2(), (int)shape2d.getY2(), width);
        }
        else
        {
            if (logger.check(POILogger.WARN))
                logger.log(POILogger.WARN, "draw not fully supported");
        }
    }

    public void drawArc(int x, int y, int width, int height,
				 int startAngle, int arcAngle)
    {
        draw(new java.awt.geom.Arc2D.Float(x, y, width, height, startAngle, arcAngle, 0));
    }

    public void drawGlyphVector(GlyphVector g, float x, float y)
    {
        fill(g.getOutline(x, y));
    }

    public boolean drawImage(Image image, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1,
            int sx2, int sy2, Color bgColor, ImageObserver imageobserver)
    {
        if (logger.check( POILogger.WARN ))
            logger.log(POILogger.WARN,"drawImage() not supported");
        return true;
    }

    public boolean drawImage(Image image, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1,
            int sx2, int sy2, ImageObserver imageobserver)
    {
        if (logger.check( POILogger.WARN ))
            logger.log(POILogger.WARN,"drawImage() not supported");
        return drawImage(image, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, null, imageobserver);
    }
    public boolean drawImage(Image image, int dx1, int dy1, int dx2, int dy2, Color bgColor, ImageObserver imageobserver)
    {
        if (logger.check( POILogger.WARN ))
            logger.log(POILogger.WARN,"drawImage() not supported");
        return true;
    }

    public boolean drawImage(Image img, int x, int y,
				      int width, int height,
				      ImageObserver observer)
    {
        return drawImage(img, x,y,width,height, null, observer);
    }

    public boolean drawImage(Image image, int x, int y, Color bgColor, ImageObserver imageobserver)
    {
        return drawImage(image, x, y, image.getWidth(imageobserver), image.getHeight(imageobserver), bgColor, imageobserver);
    }

    public boolean drawImage(Image image, int x, int y, ImageObserver imageobserver)
    {
        return drawImage(image, x, y, image.getWidth(imageobserver), image.getHeight(imageobserver), imageobserver);
    }

    public boolean drawImage(Image image, AffineTransform affinetransform, ImageObserver imageobserver)
    {
        AffineTransform affinetransform1 = (AffineTransform)getTrans().clone();
        getTrans().concatenate(affinetransform);
        drawImage(image, 0, 0, imageobserver);
        setTrans( affinetransform1 );
        return true;
    }

    public void drawImage(BufferedImage bufferedimage, BufferedImageOp op, int x, int y)
    {
        BufferedImage img = op.filter(bufferedimage, null);
        drawImage(img, new AffineTransform(1.0F, 0.0F, 0.0F, 1.0F, x, y), null);
    }

    public void drawLine(int x1, int y1, int x2, int y2, int width)
    {
        getEscherGraphics().drawLine(x1,y1,x2,y2, width);
    }

    public void drawLine(int x1, int y1, int x2, int y2)
    {
        int width = 0;
        if (_stroke != null && _stroke instanceof BasicStroke) {
            width = (int) ((BasicStroke)_stroke).getLineWidth() * 12700;
        }
        getEscherGraphics().drawLine(x1,y1,x2,y2, width);
//        draw(new GeneralPath(new java.awt.geom.Line2D.Float(x1, y1, x2, y2)));
    }

    public void drawOval(int x, int y, int width, int height)
    {
        getEscherGraphics().drawOval(x,y,width,height);
//        draw(new java.awt.geom.Ellipse2D.Float(x, y, width, height));
    }

    public void drawPolygon(int[] xPoints, int[] yPoints,
                            int nPoints)
    {
        getEscherGraphics().drawPolygon(xPoints, yPoints, nPoints);
    }

    public void drawPolyline(int[] xPoints, int[] yPoints, int nPoints)
    {
        if(nPoints > 0)
        {
            GeneralPath generalpath = new GeneralPath();
            generalpath.moveTo(xPoints[0], yPoints[0]);
            for(int j = 1; j < nPoints; j++)
                generalpath.lineTo(xPoints[j], yPoints[j]);

            draw(generalpath);
        }
    }

    public void drawRect(int x, int y, int width, int height)
    {
        _escherGraphics.drawRect(x,y,width,height);
    }

    public void drawRenderableImage(RenderableImage renderableimage, AffineTransform affinetransform)
    {
        drawRenderedImage(renderableimage.createDefaultRendering(), affinetransform);
    }

    public void drawRenderedImage(RenderedImage renderedimage, AffineTransform affinetransform)
    {
        BufferedImage bufferedimage = new BufferedImage(renderedimage.getColorModel(), renderedimage.getData().createCompatibleWritableRaster(), false, null);
        bufferedimage.setData(renderedimage.getData());
        drawImage(bufferedimage, affinetransform, null);
    }

    public void drawRoundRect(int i, int j, int k, int l, int i1, int j1)
    {
        draw(new java.awt.geom.RoundRectangle2D.Float(i, j, k, l, i1, j1));
    }

    public void drawString(String string, float x, float y)
    {
        getEscherGraphics().drawString(string, (int)x, (int)y);
    }

    public void drawString(String string, int x, int y)
    {
        getEscherGraphics().drawString(string, x, y);
    }

    public void drawString(AttributedCharacterIterator attributedcharacteriterator, float x, float y)
    {
        TextLayout textlayout = new TextLayout(attributedcharacteriterator, getFontRenderContext());
        Paint paint1 = getPaint();
        setColor(getColor());
        fill(textlayout.getOutline(AffineTransform.getTranslateInstance(x, y)));
        setPaint(paint1);
    }

    public void drawString(AttributedCharacterIterator attributedcharacteriterator, int x, int y)
    {
        getEscherGraphics().drawString(attributedcharacteriterator, x, y);
    }

    public void fill(Shape shape)
    {
        if (logger.check( POILogger.WARN ))
            logger.log(POILogger.WARN,"fill(Shape) not supported");
    }

    public void fillArc(int i, int j, int k, int l, int i1, int j1)
    {
        fill(new java.awt.geom.Arc2D.Float(i, j, k, l, i1, j1, 2));
    }

    public void fillOval(int x, int y, int width, int height)
    {
        _escherGraphics.fillOval(x,y,width,height);
    }

    /**
     * Fills a (closed) polygon, as defined by a pair of arrays, which
     *  hold the <i>x</i> and <i>y</i> coordinates.
     * <p>
     * This draws the polygon, with <code>nPoint</code> line segments.
     * The first <code>nPoint&nbsp;-&nbsp;1</code> line segments are
     *  drawn between sequential points
     *  (<code>xPoints[i],yPoints[i],xPoints[i+1],yPoints[i+1]</code>).
     * The final line segment is a closing one, from the last point to
     *  the first (assuming they are different).
     * <p>
     * The area inside of the polygon is defined by using an
     *  even-odd fill rule (also known as the alternating rule), and
     *  the area inside of it is filled.
     * @param xPoints array of the <code>x</code> coordinates.
     * @param yPoints array of the <code>y</code> coordinates.
     * @param nPoints the total number of points in the polygon.
     * @see   java.awt.Graphics#drawPolygon(int[], int[], int)
     */
    public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints)
    {
        _escherGraphics.fillPolygon(xPoints, yPoints, nPoints);
    }

    public void fillRect(int x, int y, int width, int height)
    {
        getEscherGraphics().fillRect(x,y,width,height);
    }

    public void fillRoundRect(int x, int y, int width, int height,
				       int arcWidth, int arcHeight)
    {
        fill(new java.awt.geom.RoundRectangle2D.Float(x, y, width, height, arcWidth, arcHeight));
    }

    public Color getBackground()
    {
        return getEscherGraphics().getBackground();
    }

    public Shape getClip()
    {
        try
        {
            return getTrans().createInverse().createTransformedShape(getDeviceclip());
        }
        catch(Exception _ex)
        {
            return null;
        }
    }

    public Rectangle getClipBounds()
    {
        if(getDeviceclip() != null) {
            final Shape clip = getClip();
            return clip != null ? clip.getBounds() : null;
        }
        return null;
    }

    public Color getColor()
    {
        return _escherGraphics.getColor();
    }

    public Composite getComposite()
    {
        return getG2D().getComposite();
    }

    public GraphicsConfiguration getDeviceConfiguration()
    {
        return getG2D().getDeviceConfiguration();
    }

    public Font getFont()
    {
        return getEscherGraphics().getFont();
    }

    public FontMetrics getFontMetrics(Font font)
    {
        return getEscherGraphics().getFontMetrics(font);
    }

    public FontRenderContext getFontRenderContext()
    {
        getG2D().setTransform(getTrans());
        return getG2D().getFontRenderContext();
    }

    public Paint getPaint()
    {
        return _paint;
    }

    public Object getRenderingHint(java.awt.RenderingHints.Key key)
    {
        return getG2D().getRenderingHint(key);
    }

    public RenderingHints getRenderingHints()
    {
        return getG2D().getRenderingHints();
    }

    public Stroke getStroke()
    {
        return _stroke;
    }

    public AffineTransform getTransform()
    {
        return (AffineTransform)getTrans().clone();
    }

    public boolean hit(Rectangle rectangle, Shape shape, boolean flag)
    {
        getG2D().setTransform(getTrans());
        getG2D().setStroke(getStroke());
        getG2D().setClip(getClip());
        return getG2D().hit(rectangle, shape, flag);
    }

    public void rotate(double d)
    {
        getTrans().rotate(d);
    }

    public void rotate(double d, double d1, double d2)
    {
        getTrans().rotate(d, d1, d2);
    }

    public void scale(double d, double d1)
    {
        getTrans().scale(d, d1);
    }

    public void setBackground(Color c)
    {
        getEscherGraphics().setBackground(c);
    }

    public void setClip(int i, int j, int k, int l)
    {
        setClip(new Rectangle(i, j, k, l));
    }

    public void setClip(Shape shape)
    {
        setDeviceclip( getTrans().createTransformedShape(shape) );
    }

    public void setColor(Color c)
    {
        _escherGraphics.setColor(c);
    }

    public void setComposite(Composite composite)
    {
        getG2D().setComposite(composite);
    }

    public void setFont(Font font)
    {
        getEscherGraphics().setFont(font);
    }

    public void setPaint(Paint paint1)
    {
        if(paint1 != null)
        {
            _paint = paint1;
            if(paint1 instanceof Color)
                setColor( (Color)paint1 );
        }
    }

    public void setPaintMode()
    {
        getEscherGraphics().setPaintMode();
    }

    public void setRenderingHint(java.awt.RenderingHints.Key key, Object obj)
    {
        getG2D().setRenderingHint(key, obj);
    }

    public void setRenderingHints(Map<?, ?> map)
    {
        getG2D().setRenderingHints(map);
    }

    public void setStroke(Stroke s)
    {
        _stroke = s;
    }

    public void setTransform(AffineTransform affinetransform)
    {
        setTrans( (AffineTransform)affinetransform.clone() );
    }

    public void setXORMode(Color color1)
    {
        getEscherGraphics().setXORMode(color1);
    }

    public void shear(double d, double d1)
    {
        getTrans().shear(d, d1);
    }

    public void transform(AffineTransform affinetransform)
    {
        getTrans().concatenate(affinetransform);
    }

//    Image transformImage(Image image, Rectangle rectangle, Rectangle rectangle1, ImageObserver imageobserver, Color color1)
//    {
//        logger.log(POILogger.WARN,"transformImage() not supported");
//        return null;
//    }
//
//    Image transformImage(Image image, int ai[], Rectangle rectangle, ImageObserver imageobserver, Color color1)
//    {
//        logger.log(POILogger.WARN,"transformImage() not supported");
//        return null;
//    }

    public void translate(double d, double d1)
    {
        getTrans().translate(d, d1);
    }

    public void translate(int i, int j)
    {
        getTrans().translate(i, j);
    }

    private EscherGraphics getEscherGraphics()
    {
        return _escherGraphics;
    }

    private BufferedImage getImg()
    {
        return _img;
    }

    private void setImg( BufferedImage img )
    {
        this._img = img;
    }

    private Graphics2D getG2D()
    {
        return (Graphics2D) _img.getGraphics();
    }

    private AffineTransform getTrans()
    {
        return _trans;
    }

    private void setTrans( AffineTransform trans )
    {
        this._trans = trans;
    }

    private Shape getDeviceclip()
    {
        return _deviceclip;
    }

    private void setDeviceclip( Shape deviceclip )
    {
        this._deviceclip = deviceclip;
    }

}
