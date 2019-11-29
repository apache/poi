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

package org.apache.poi.hwmf.draw;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.TexturePaint;
import java.awt.font.FontRenderContext;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.nio.charset.Charset;
import java.text.AttributedString;
import java.util.BitSet;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.BiConsumer;

import org.apache.commons.codec.Charsets;
import org.apache.poi.common.usermodel.fonts.FontCharset;
import org.apache.poi.common.usermodel.fonts.FontInfo;
import org.apache.poi.hwmf.record.HwmfBrushStyle;
import org.apache.poi.hwmf.record.HwmfFont;
import org.apache.poi.hwmf.record.HwmfHatchStyle;
import org.apache.poi.hwmf.record.HwmfMapMode;
import org.apache.poi.hwmf.record.HwmfMisc.WmfSetBkMode.HwmfBkMode;
import org.apache.poi.hwmf.record.HwmfObjectTableEntry;
import org.apache.poi.hwmf.record.HwmfPenStyle;
import org.apache.poi.hwmf.record.HwmfPenStyle.HwmfLineDash;
import org.apache.poi.hwmf.record.HwmfRegionMode;
import org.apache.poi.hwmf.record.HwmfText;
import org.apache.poi.hwmf.record.HwmfText.WmfExtTextOutOptions;
import org.apache.poi.sl.draw.BitmapImageRenderer;
import org.apache.poi.sl.draw.DrawFactory;
import org.apache.poi.sl.draw.DrawFontManager;
import org.apache.poi.sl.draw.DrawFontManagerDefault;
import org.apache.poi.sl.draw.DrawPictureShape;
import org.apache.poi.sl.draw.ImageRenderer;
import org.apache.poi.util.Internal;
import org.apache.poi.util.LocaleUtil;

public class HwmfGraphics {

    public enum FillDrawStyle {
        NONE(FillDrawStyle::fillNone),
        FILL(HwmfGraphics::fill),
        DRAW(HwmfGraphics::draw),
        FILL_DRAW(FillDrawStyle::fillDraw);

        public final BiConsumer<HwmfGraphics,Shape> handler;

        FillDrawStyle(BiConsumer<HwmfGraphics,Shape> handler) {
            this.handler = handler;
        }

        private static void fillNone(HwmfGraphics g, Shape s) {
        }

        private static void fillDraw(HwmfGraphics g, Shape s) {
            g.fill(s);
            g.draw(s);
        }
    }

    private static final Float[] WEIGHT_MAP = {
        900f, TextAttribute.WEIGHT_ULTRABOLD,
        800f, TextAttribute.WEIGHT_EXTRABOLD,
        750f, TextAttribute.WEIGHT_HEAVY,
        700f, TextAttribute.WEIGHT_BOLD,
        600f, TextAttribute.WEIGHT_DEMIBOLD,
        500f, TextAttribute.WEIGHT_MEDIUM,
        450f, TextAttribute.WEIGHT_SEMIBOLD,
        400f, TextAttribute.WEIGHT_REGULAR,
        300f, TextAttribute.WEIGHT_DEMILIGHT,
        200f, TextAttribute.WEIGHT_LIGHT,
        1f, TextAttribute.WEIGHT_EXTRA_LIGHT
    };


    private final List<HwmfDrawProperties> propStack = new LinkedList<>();
    protected HwmfDrawProperties prop;
    protected final Graphics2D graphicsCtx;
    protected final BitSet objectIndexes = new BitSet();
    protected final TreeMap<Integer,HwmfObjectTableEntry> objectTable = new TreeMap<>();
    private final AffineTransform initialAT = new AffineTransform();


    private static final Charset DEFAULT_CHARSET = LocaleUtil.CHARSET_1252;
    /** Bounding box from the placeable header */
    private final Rectangle2D bbox;

    /**
     * Initialize a graphics context for wmf rendering
     *
     * @param graphicsCtx the graphics context to delegate drawing calls
     * @param bbox the bounding box of the wmf (taken from the placeable header)
     */
    public HwmfGraphics(Graphics2D graphicsCtx, Rectangle2D bbox) {
        this.graphicsCtx = graphicsCtx;
        this.bbox = (Rectangle2D)bbox.clone();
        this.initialAT.setTransform(graphicsCtx.getTransform());
    }

    public HwmfDrawProperties getProperties() {
        if (prop == null) {
            prop = newProperties(null);
        }
        return prop;
    }

    protected HwmfDrawProperties newProperties(HwmfDrawProperties oldProps) {
        return (oldProps == null) ? new HwmfDrawProperties() : new HwmfDrawProperties(oldProps);
    }

    public void draw(Shape shape) {
        HwmfPenStyle ps = getProperties().getPenStyle();
        if (ps == null) {
            return;
        }
        HwmfLineDash lineDash = ps.getLineDash();
        if (lineDash == HwmfLineDash.NULL) {
            // line is not drawn
            return;
        }

        BasicStroke stroke = getStroke();

        // first draw a solid background line (depending on bkmode)
        // only makes sense if the line is not solid
        if (getProperties().getBkMode() == HwmfBkMode.OPAQUE && (lineDash != HwmfLineDash.SOLID && lineDash != HwmfLineDash.INSIDEFRAME)) {
            graphicsCtx.setStroke(new BasicStroke(stroke.getLineWidth()));
            graphicsCtx.setColor(getProperties().getBackgroundColor().getColor());
            graphicsCtx.draw(shape);
        }

        // then draw the (dashed) line
        graphicsCtx.setStroke(stroke);
        graphicsCtx.setColor(getProperties().getPenColor().getColor());
        graphicsCtx.draw(shape);
    }

    public void fill(Shape shape) {
        HwmfDrawProperties prop = getProperties();

        Composite old = graphicsCtx.getComposite();
        graphicsCtx.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
        if (prop.getBrushStyle() != HwmfBrushStyle.BS_NULL) {
            if (prop.getBkMode() == HwmfBkMode.OPAQUE) {
                graphicsCtx.setPaint(prop.getBackgroundColor().getColor());
                graphicsCtx.fill(shape);
            }

            graphicsCtx.setPaint(getFill());
            graphicsCtx.fill(shape);
        }
        graphicsCtx.setComposite(old);

        draw(shape);
    }

    protected BasicStroke getStroke() {
        HwmfDrawProperties prop = getProperties();
        HwmfPenStyle ps = prop.getPenStyle();
        // TODO: fix line width calculation
        float width = (float)prop.getPenWidth();
        if (width == 0) {
            width = 1;
        }
        int cap = ps.getLineCap().awtFlag;
        int join = ps.getLineJoin().awtFlag;
        float miterLimit = (float)prop.getPenMiterLimit();
        float[] dashes = ps.getLineDashes();
        boolean dashAlt = ps.isAlternateDash();
        // This value is not an integer index into the dash pattern array.
        // Instead, it is a floating-point value that specifies a linear distance.
        float dashStart = (dashAlt && dashes != null && dashes.length > 1) ? dashes[0] : 0;

        return new BasicStroke(width, cap, join, miterLimit, dashes, dashStart);
    }

    protected Paint getFill() {
        switch (getProperties().getBrushStyle()) {
        default:
        case BS_INDEXED:
        case BS_PATTERN8X8:
        case BS_DIBPATTERN8X8:
        case BS_MONOPATTERN:
        case BS_NULL: return null;
        case BS_PATTERN:
        case BS_DIBPATTERN:
        case BS_DIBPATTERNPT: return getPatternPaint();
        case BS_SOLID: return getSolidFill();
        case BS_HATCHED: return getHatchedFill();
        case BS_LINEAR_GRADIENT: return getLinearGradient();
        }
    }

    protected Paint getLinearGradient() {
        return null;
    }

    protected Paint getSolidFill() {
        return getProperties().getBrushColor().getColor();
    }

    protected Paint getHatchedFill() {
        int dim = 7, mid = 3;
        BufferedImage bi = new BufferedImage(dim, dim, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g = bi.createGraphics();
        Color c = (getProperties().getBkMode() == HwmfBkMode.TRANSPARENT)
            ? new Color(0, true)
            : getProperties().getBackgroundColor().getColor();
        g.setColor(c);
        g.fillRect(0, 0, dim, dim);
        g.setColor(getProperties().getBrushColor().getColor());
        HwmfHatchStyle h = getProperties().getBrushHatch();
        if (h == HwmfHatchStyle.HS_HORIZONTAL || h == HwmfHatchStyle.HS_CROSS) {
            g.drawLine(0, mid, dim, mid);
        }
        if (h == HwmfHatchStyle.HS_VERTICAL || h == HwmfHatchStyle.HS_CROSS) {
            g.drawLine(mid, 0, mid, dim);
        }
        if (h == HwmfHatchStyle.HS_FDIAGONAL || h == HwmfHatchStyle.HS_DIAGCROSS) {
            g.drawLine(0, 0, dim, dim);
        }
        if (h == HwmfHatchStyle.HS_BDIAGONAL || h == HwmfHatchStyle.HS_DIAGCROSS) {
            g.drawLine(0, dim, dim, 0);
        }
        // TODO: handle new HS_* enumeration values
        g.dispose();
        return new TexturePaint(bi, new Rectangle(0,0,dim,dim));
    }

    protected Paint getPatternPaint() {
        HwmfDrawProperties prop = getProperties();
        ImageRenderer bb = prop.getBrushBitmap();
        if (bb == null) {
            return null;
        }

        Dimension2D dim = bb.getDimension();
        Rectangle2D rect = new Rectangle2D.Double(0, 0, dim.getWidth(), dim.getHeight());
        rect = prop.getBrushTransform().createTransformedShape(rect).getBounds2D();

        return new TexturePaint(bb.getImage(), rect);
    }

    /**
     * Adds an record of type {@link HwmfObjectTableEntry} to the object table.
     *
     * Every object is assigned the lowest available index-that is, the smallest
     * numerical value-in the WMF Object Table. This binding happens at object creation,
     * not when the object is used.
     * Moreover, each object table index uniquely refers to an object.
     * Indexes in the WMF Object Table always start at 0.
     *
     * @param entry
     */
    public void addObjectTableEntry(HwmfObjectTableEntry entry) {
        int objIdx = objectIndexes.nextClearBit(0);
        objectIndexes.set(objIdx);
        objectTable.put(objIdx, entry);
    }

    /**
     * Applies the object table entry
     *
     * @param index the index of the object table entry (0-based)
     * 
     * @throws IndexOutOfBoundsException if the index is out of range
     * @throws NoSuchElementException if the entry was deleted before
     */
    public void applyObjectTableEntry(int index) {
        HwmfObjectTableEntry ote = objectTable.get(index);
        if (ote == null) {
            throw new NoSuchElementException("WMF reference exception - object table entry on index "+index+" was deleted before.");
        }
        ote.applyObject(this);
    }
    
    /**
     * Unsets (deletes) the object table entry for further usage
     * 
     * When a META_DELETEOBJECT record (section 2.3.4.7) is received that specifies this
     * object's particular index, the object's resources are released, the binding to its
     * WMF Object Table index is ended, and the index value is returned to the pool of
     * available indexes. The index will be reused, if needed, by a subsequent object
     * created by another Object Record Type record.
     *
     * @param index the index (0-based)
     * 
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    public void unsetObjectTableEntry(int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException("Invalid index: "+index);
        }
        // sometime emfs remove object table entries before they set them
        // so ignore requests, if the table entry doesn't exist
        objectTable.remove(index);
        objectIndexes.clear(index);
    }
    
    /**
     * Saves the current properties to the stack
     */
    public void saveProperties() {
        final HwmfDrawProperties p = getProperties();
        assert(p != null);
        p.setTransform(graphicsCtx.getTransform());
        p.setClip(graphicsCtx.getClip());
        propStack.add(p);
        prop = newProperties(p);
    }
    
    /**
     * Restores the properties from the stack
     *
     * @param index if the index is positive, the n-th element from the start is activated.
     *      If the index is negative, the n-th previous element relative to the current properties element is activated.
     */
    public void restoreProperties(int index) {
        if (index == 0) {
            return;
        }
        int stackIndex = index;
        if (stackIndex < 0) {
            int curIdx = propStack.indexOf(getProperties());
            if (curIdx == -1) {
                // the current element is not pushed to the stacked, i.e. it's the last
                curIdx = propStack.size();
            }
            stackIndex = curIdx + index;
        }
        if (stackIndex == -1) {
            // roll to last when curIdx == 0
            stackIndex = propStack.size()-1;
        }

        // The playback device context is restored by popping state information off a stack that was created by
        // prior SAVEDC records
        // ... so because being a stack, we will remove all entries having a greater index than the stackIndex
        for (int i=propStack.size()-1; i>=stackIndex; i--) {
            prop = propStack.remove(i);
        }

        graphicsCtx.setTransform(prop.getTransform());
        graphicsCtx.setClip(prop.getClip());
    }

    /**
     * After setting various window and viewport related properties,
     * the underlying graphics context needs to be adapted.
     * This methods gathers and sets the corresponding graphics transformations.
     */
    public void updateWindowMapMode() {
        Rectangle2D win = getProperties().getWindow();
        Rectangle2D view = getProperties().getViewport();
        HwmfMapMode mapMode = getProperties().getMapMode();
        graphicsCtx.setTransform(getInitTransform());

        switch (mapMode) {
        default:
        case MM_ANISOTROPIC:
            // scale window bounds to output bounds
            if (view != null) {
                graphicsCtx.translate(view.getCenterX(), view.getCenterY());
                graphicsCtx.scale(view.getWidth() / win.getWidth(), view.getHeight() / win.getHeight());
                graphicsCtx.translate(-win.getCenterX(), -win.getCenterY());
            }
            break;
        case MM_ISOTROPIC:
            // TODO: to be validated ...
            // like anisotropic, but use x-axis as reference
            graphicsCtx.translate(bbox.getCenterX(), bbox.getCenterY());
            graphicsCtx.scale(bbox.getWidth()/win.getWidth(), bbox.getWidth()/win.getWidth());
            graphicsCtx.translate(-win.getCenterX(), -win.getCenterY());
            break;
        case MM_LOMETRIC:
        case MM_HIMETRIC:
        case MM_LOENGLISH:
        case MM_HIENGLISH:
        case MM_TWIPS: {
            // TODO: to be validated ...
            GraphicsConfiguration gc = graphicsCtx.getDeviceConfiguration();
            graphicsCtx.transform(gc.getNormalizingTransform());
            graphicsCtx.scale(1./mapMode.scale, -1./mapMode.scale);
            graphicsCtx.translate(-win.getX(), -win.getY());
            break;
        }
        case MM_TEXT:
            // TODO: to be validated ...
            break;
        }
    }

    public void drawString(byte[] text, int length, Point2D reference) {
        drawString(text, length, reference, null, null, null, null, false);
    }

    public void drawString(byte[] text, int length, Point2D reference, Dimension2D scale, Rectangle2D clip, WmfExtTextOutOptions opts, List<Integer> dx, boolean isUnicode) {
        final HwmfDrawProperties prop = getProperties();

        final AffineTransform at = graphicsCtx.getTransform();

        try {
            at.createInverse();
        } catch (NoninvertibleTransformException e) {
            return;
        }

        HwmfFont font = prop.getFont();
        if (font == null || text == null || text.length == 0) {
            return;
        }
        
        double fontH = getFontHeight(font);
        // TODO: another approx. ...
        double fontW = fontH/1.8;
        
        Charset charset;
        if (isUnicode) {
            charset = Charsets.UTF_16LE;
        } else {
            charset = font.getCharset().getCharset();
            if (charset == null) {
                charset = DEFAULT_CHARSET;
            }
        }

        int trimLen;
        for (trimLen=0; trimLen<text.length; trimLen+=2) {
            if (trimLen == text.length-1) {
                if (text[trimLen] != 0) {
                    trimLen++;
                }
                break;
            } else if ((text[trimLen] == -1 && text[trimLen+1] == -1) ||
                ((text[trimLen] & 0xE0) == 0 && text[trimLen+1] == 0)) {
                break;
            }
        }

        String textString = new String(text, 0, trimLen, charset);
        textString = textString.substring(0, Math.min(textString.length(), length));

        if (textString.isEmpty()) {
            return;
        }

        DrawFontManager fontHandler = DrawFactory.getInstance(graphicsCtx).getFontManager(graphicsCtx);
        FontInfo fontInfo = fontHandler.getMappedFont(graphicsCtx, font);
        if (fontInfo.getCharset() == FontCharset.SYMBOL) {
            textString = DrawFontManagerDefault.mapSymbolChars(textString);
        }

        AttributedString as = new AttributedString(textString);
        addAttributes(as, font, fontInfo.getTypeface());

        // disabled for the time being, as the results aren't promising
        /*
        if (dx != null && !dx.isEmpty()) {
            //for multi-byte encodings (e.g. Shift_JIS), the byte length
            //might not equal the string length().
            //The x information is stored in dx[], an array parallel to the
            //byte array text[].  dx[] stores the x info in the
            //first byte of a multibyte character, but dx[] stores 0
            //for the other bytes in that character.
            //We need to map this information to the String offsets
            //dx[0] = 13 text[0] = -125
            //dx[1] = 0  text[1] = 118
            //dx[2] = 14 text[2] = -125
            //dx[3] = 0  text[3] = -115
            // needs to be remapped as:
            //dxNormed[0] = 13 textString.get(0) = U+30D7
            //dxNormed[1] = 14 textString.get(1) = U+30ED

            final int cps = textString.codePointCount(0, textString.length());
            final int unicodeSteps = Math.max(dx.size()/cps, 1);
            int dxPosition = 0, lastDxPosition = 0;
            int beginIndex = 0;
            while (beginIndex < textString.length() && dxPosition < dx.size()) {
                int endIndex = textString.offsetByCodePoints(beginIndex, 1);
                if (beginIndex > 0) {
                    // Tracking works as a prefix/advance space on characters whereas
                    // dx[...] is the complete width of the current char
                    // therefore we need to add the additional/suffix width to the next char

                    as.addAttribute(TextAttribute.TRACKING, (float)((dx.get(lastDxPosition) - fontW) / fontH), beginIndex, endIndex);
                }
                lastDxPosition = dxPosition;
                dxPosition += (isUnicode) ? unicodeSteps : (endIndex-beginIndex);
                beginIndex = endIndex;
            }
        }
        */

        double angle = Math.toRadians(-font.getEscapement()/10.);

        final HwmfText.HwmfTextAlignment align = prop.getTextAlignLatin();
        final HwmfText.HwmfTextVerticalAlignment valign = prop.getTextVAlignLatin();
        final FontRenderContext frc = graphicsCtx.getFontRenderContext();
        final TextLayout layout = new TextLayout(as.getIterator(), frc);

        final Rectangle2D pixelBounds = layout.getBounds();

        AffineTransform tx = new AffineTransform();
        switch (align) {
            default:
            case LEFT:
                break;
            case CENTER:
                tx.translate(-pixelBounds.getWidth() / 2., 0);
                break;
            case RIGHT:
                tx.translate(-layout.getAdvance(), 0);
                break;
        }

        // TODO: check min/max orientation
        switch (valign) {
            case TOP:
                tx.translate(0, layout.getAscent());
            default:
            case BASELINE:
                break;
            case BOTTOM:
                tx.translate(0, -(pixelBounds.getHeight()-layout.getDescent()));
                break;
        }
        tx.rotate(angle);
        Point2D src = new Point2D.Double();
        Point2D dst = new Point2D.Double();
        tx.transform(src, dst);

        final Shape clipShape = graphicsCtx.getClip();
        try {
            if (clip != null && !clip.getBounds2D().isEmpty()) {
                graphicsCtx.translate(-clip.getCenterX(), -clip.getCenterY());
                graphicsCtx.rotate(angle);
                graphicsCtx.translate(clip.getCenterX(), clip.getCenterY());
                if (prop.getBkMode() == HwmfBkMode.OPAQUE && opts.isOpaque()) {
                    graphicsCtx.setPaint(prop.getBackgroundColor().getColor());
                    graphicsCtx.fill(clip);
                }
                if (opts.isClipped()) {
                    graphicsCtx.setClip(clip);
                }
                graphicsCtx.setTransform(at);
            }

            graphicsCtx.translate(reference.getX(), reference.getY());
            graphicsCtx.rotate(angle);
            if (scale != null) {
                graphicsCtx.scale(scale.getWidth() < 0 ? -1 : 1, scale.getHeight() < 0 ? -1 : 1);
            }
            graphicsCtx.translate(dst.getX(), dst.getY());
            graphicsCtx.setColor(prop.getTextColor().getColor());
            graphicsCtx.drawString(as.getIterator(), 0, 0);
        } finally {
            graphicsCtx.setTransform(at);
            graphicsCtx.setClip(clipShape);
        }
    }

    private void addAttributes(AttributedString as, HwmfFont font, String typeface) {
        as.addAttribute(TextAttribute.FAMILY, typeface);
        as.addAttribute(TextAttribute.SIZE, getFontHeight(font));
        if (font.isStrikeOut()) {
            as.addAttribute(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
        }
        if (font.isUnderline()) {
            as.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
        }
        if (font.isItalic()) {
            as.addAttribute(TextAttribute.POSTURE, TextAttribute.POSTURE_OBLIQUE);
        }
        // convert font weight to awt font weight - usually a font weight of 400 is regarded as regular
        final int fw = font.getWeight();
        Float awtFW = TextAttribute.WEIGHT_REGULAR;
        for (int i=0; i<WEIGHT_MAP.length; i+=2) {
            if (fw >= WEIGHT_MAP[i]) {
                awtFW = WEIGHT_MAP[i+1];
                break;
            }
        }
        as.addAttribute(TextAttribute.WEIGHT, awtFW);
    }
    
    private double getFontHeight(HwmfFont font) {
        // see HwmfFont#height for details
        double fontHeight = font.getHeight();
        if (fontHeight == 0) {
            return 12;
        } else if (fontHeight < 0) {
            return -fontHeight;
        } else {
            // TODO: fix font height calculation 
            // the height is given as font size + ascent + descent
            // as an approximation we reduce the height by a static factor 
            return fontHeight*3/4;
        }
    }

    public void drawImage(BufferedImage img, Rectangle2D srcBounds, Rectangle2D dstBounds) {
        drawImage(new BufferedImageRenderer(img), srcBounds, dstBounds);
    }

    public void drawImage(ImageRenderer img, Rectangle2D srcBounds, Rectangle2D dstBounds) {
        if (srcBounds.isEmpty()) {
            return;
        }
        HwmfDrawProperties prop = getProperties();

        // handle raster op
        // currently the raster op as described in https://docs.microsoft.com/en-us/windows/desktop/gdi/ternary-raster-operations
        // are not supported, as we would need to extract the destination image area from the underlying buffered image
        // and therefore would make it mandatory that the graphics context must be from a buffered image
        // furthermore I doubt the purpose of bitwise image operations on non-black/white images
        switch (prop.getRasterOp()) {
            case D:
                // keep destination, i.e. do nothing
                break;
            case PATCOPY:
                graphicsCtx.setPaint(getFill());
                graphicsCtx.fill(dstBounds);
                break;
            case BLACKNESS:
                graphicsCtx.setPaint(Color.BLACK);
                graphicsCtx.fill(dstBounds);
                break;
            case WHITENESS:
                graphicsCtx.setPaint(Color.WHITE);
                graphicsCtx.fill(dstBounds);
                break;
            default:
            case SRCCOPY:
                if (img == null) {
                    return;
                }

                final Shape oldClip = graphicsCtx.getClip();
                final AffineTransform oldTrans = graphicsCtx.getTransform();

                // add clipping in case of a source subimage, i.e. a clipped source image
                // some dstBounds are horizontal or vertical flipped, so we need to normalize the images
                Rectangle2D normBounds = normalizeRect(dstBounds);
                // graphicsCtx.clip(normBounds);

                if (prop.getBkMode() == HwmfBkMode.OPAQUE) {
                    Paint oldPaint = graphicsCtx.getPaint();
                    graphicsCtx.setPaint(prop.getBackgroundColor().getColor());
                    graphicsCtx.fill(dstBounds);
                    graphicsCtx.setPaint(oldPaint);
                }

                graphicsCtx.translate(normBounds.getCenterX(), normBounds.getCenterY());
                graphicsCtx.scale(Math.signum(dstBounds.getWidth()), Math.signum(dstBounds.getHeight()));
                graphicsCtx.translate(-normBounds.getCenterX(), -normBounds.getCenterY());

                // this is similar to drawing bitmaps with a clipping
                // see {@link BitmapImageRenderer#drawImage(Graphics2D,Rectangle2D,Insets)}
                // the difference is, that clippings are 0-based, whereas the srcBounds are absolute in the user-space
                // of the referenced image and can be also negative
                Composite old = graphicsCtx.getComposite();
                graphicsCtx.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));

                boolean useDeviceBounds = (img instanceof HwmfImageRenderer);

                img.drawImage(graphicsCtx, normBounds,
                      getSubImageInsets(srcBounds, useDeviceBounds ? img.getNativeBounds() : img.getBounds()));
                graphicsCtx.setComposite(old);

                graphicsCtx.setTransform(oldTrans);
                graphicsCtx.setClip(oldClip);
                break;
        }

    }

    private static Rectangle2D normalizeRect(Rectangle2D dstBounds) {
        return new Rectangle2D.Double(
            dstBounds.getWidth() >= 0 ? dstBounds.getMinX() : dstBounds.getMaxX(),
            dstBounds.getHeight() >= 0 ? dstBounds.getMinY() : dstBounds.getMaxY(),
            Math.abs(dstBounds.getWidth()),
            Math.abs(dstBounds.getHeight()));
    }

    private static Insets getSubImageInsets(Rectangle2D srcBounds, Rectangle2D nativeBounds) {
        // Todo: check if we need to normalize srcBounds x/y, in case of flipped images
        // for now we assume the width/height is positive
        int left = (int)Math.round((srcBounds.getX()-nativeBounds.getX())/nativeBounds.getWidth()*100_000.);
        int top = (int)Math.round((srcBounds.getY()-nativeBounds.getY())/nativeBounds.getHeight()*100_000.);
        int right = (int)Math.round((nativeBounds.getMaxX()-srcBounds.getMaxX())/nativeBounds.getWidth()*100_000.);
        int bottom = (int)Math.round((nativeBounds.getMaxY()-srcBounds.getMaxY())/nativeBounds.getHeight()*100_000.);

        return new Insets(top, left, bottom, right);
    }

    /**
     * @return the initial AffineTransform, when this graphics context was created
     */
    public AffineTransform getInitTransform() {
        return new AffineTransform(initialAT);
    }

    /**
     * @return the current AffineTransform
     */
    public AffineTransform getTransform() {
        return new AffineTransform(graphicsCtx.getTransform());
    }

    /**
     * Set the current AffineTransform
     * @param tx the current AffineTransform
     */
    public void setTransform(AffineTransform tx) {
        graphicsCtx.setTransform(tx);
    }

    /**
     * Set the new clipping region
     *
     * @param clip the next clipping region to be processed
     * @param regionMode the mode and operation of how to apply the next clipping region
     * @param useInitialAT if true, the clipping is applied on the initial (world) coordinate system
     */
    public void setClip(Shape clip, HwmfRegionMode regionMode, boolean useInitialAT) {
        final AffineTransform at = graphicsCtx.getTransform();
        if (useInitialAT) {
            graphicsCtx.setTransform(getInitTransform());
        }

        final Shape oldClip = graphicsCtx.getClip();
        final Shape newClip = regionMode.applyOp(oldClip, clip);
        if (!Objects.equals(oldClip, newClip)) {
            graphicsCtx.setClip(newClip);
        }

        if (useInitialAT) {
            graphicsCtx.setTransform(at);
        }
        prop.setClip(graphicsCtx.getClip());
    }

    public ImageRenderer getImageRenderer(String contentType) {
        // TODO: refactor DrawPictureShape method to POI Common
        return DrawPictureShape.getImageRenderer(graphicsCtx, contentType);
    }


    @Internal
    static class BufferedImageRenderer extends BitmapImageRenderer {
        public BufferedImageRenderer(BufferedImage img) {
            this.img = img;
        }
    }
}
