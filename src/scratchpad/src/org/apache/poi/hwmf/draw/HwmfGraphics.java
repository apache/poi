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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.TexturePaint;
import java.awt.font.TextAttribute;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.nio.charset.Charset;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import org.apache.poi.common.usermodel.fonts.FontInfo;
import org.apache.poi.hwmf.record.HwmfBrushStyle;
import org.apache.poi.hwmf.record.HwmfFont;
import org.apache.poi.hwmf.record.HwmfHatchStyle;
import org.apache.poi.hwmf.record.HwmfMapMode;
import org.apache.poi.hwmf.record.HwmfMisc.WmfSetBkMode.HwmfBkMode;
import org.apache.poi.hwmf.record.HwmfObjectTableEntry;
import org.apache.poi.hwmf.record.HwmfPenStyle;
import org.apache.poi.hwmf.record.HwmfPenStyle.HwmfLineDash;
import org.apache.poi.sl.draw.DrawFactory;
import org.apache.poi.sl.draw.DrawFontManager;
import org.apache.poi.util.LocaleUtil;

public class HwmfGraphics {

    private static final Charset DEFAULT_CHARSET = LocaleUtil.CHARSET_1252;
    private final Graphics2D graphicsCtx;
    private final List<HwmfDrawProperties> propStack = new LinkedList<>();
    private HwmfDrawProperties prop = new HwmfDrawProperties();
    private List<HwmfObjectTableEntry> objectTable = new ArrayList<>();
    /** Bounding box from the placeable header */ 
    private final Rectangle2D bbox;
    private final AffineTransform initialAT;

    /**
     * Initialize a graphics context for wmf rendering
     *
     * @param graphicsCtx the graphics context to delegate drawing calls
     * @param bbox the bounding box of the wmf (taken from the placeable header)
     */
    public HwmfGraphics(Graphics2D graphicsCtx, Rectangle2D bbox) {
        this.graphicsCtx = graphicsCtx;
        this.bbox = (Rectangle2D)bbox.clone();
        this.initialAT = graphicsCtx.getTransform();
        DrawFactory.getInstance(graphicsCtx).fixFonts(graphicsCtx);
    }

    public HwmfDrawProperties getProperties() {
        return prop;
    }

    public void draw(Shape shape) {
        HwmfLineDash lineDash = prop.getPenStyle().getLineDash();
        if (lineDash == HwmfLineDash.NULL) {
            // line is not drawn
            return;
        }

        BasicStroke stroke = getStroke();

        // first draw a solid background line (depending on bkmode)
        // only makes sense if the line is not solid
        if (prop.getBkMode() == HwmfBkMode.OPAQUE && (lineDash != HwmfLineDash.SOLID && lineDash != HwmfLineDash.INSIDEFRAME)) {
            graphicsCtx.setStroke(new BasicStroke(stroke.getLineWidth()));
            graphicsCtx.setColor(prop.getBackgroundColor().getColor());
            graphicsCtx.draw(shape);
        }

        // then draw the (dashed) line
        graphicsCtx.setStroke(stroke);
        graphicsCtx.setColor(prop.getPenColor().getColor());
        graphicsCtx.draw(shape);
    }

    public void fill(Shape shape) {
        if (prop.getBrushStyle() != HwmfBrushStyle.BS_NULL) {
//            GeneralPath gp = new GeneralPath(shape);
//            gp.setWindingRule(prop.getPolyfillMode().awtFlag);
            graphicsCtx.setPaint(getFill());
            graphicsCtx.fill(shape);
        }

        draw(shape);
    }

    protected BasicStroke getStroke() {
        // TODO: fix line width calculation
        float width = (float)prop.getPenWidth();
        if (width == 0) {
            width = 1;
        }
        HwmfPenStyle ps = prop.getPenStyle();
        int cap = ps.getLineCap().awtFlag;
        int join = ps.getLineJoin().awtFlag;
        float miterLimit = (float)prop.getPenMiterLimit();
        float dashes[] = ps.getLineDash().dashes;
        boolean dashAlt = ps.isAlternateDash();
        // This value is not an integer index into the dash pattern array.
        // Instead, it is a floating-point value that specifies a linear distance.
        float dashStart = (dashAlt && dashes != null && dashes.length > 1) ? dashes[0] : 0;

        return new BasicStroke(width, cap, join, miterLimit, dashes, dashStart);
    }

    protected Paint getFill() {
        switch (prop.getBrushStyle()) {
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
        }
    }

    protected Paint getSolidFill() {
        return prop.getBrushColor().getColor();
    }

    protected Paint getHatchedFill() {
        int dim = 7, mid = 3;
        BufferedImage bi = new BufferedImage(dim, dim, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g = bi.createGraphics();
        Color c = (prop.getBkMode() == HwmfBkMode.TRANSPARENT)
            ? new Color(0, true)
            : prop.getBackgroundColor().getColor();
        g.setColor(c);
        g.fillRect(0, 0, dim, dim);
        g.setColor(prop.getBrushColor().getColor());
        HwmfHatchStyle h = prop.getBrushHatch();
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
        g.dispose();
        return new TexturePaint(bi, new Rectangle(0,0,dim,dim));
    }

    protected Paint getPatternPaint() {
        BufferedImage bi = prop.getBrushBitmap();
        return (bi == null) ? null
            : new TexturePaint(bi, new Rectangle(0,0,bi.getWidth(),bi.getHeight()));
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
        ListIterator<HwmfObjectTableEntry> oIter = objectTable.listIterator();
        while (oIter.hasNext()) {
            HwmfObjectTableEntry tableEntry = oIter.next();
            if (tableEntry == null) {
                oIter.set(entry);
                return;
            }
        }
        objectTable.add(entry);
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
        objectTable.set(index, null);
    }
    
    /**
     * Saves the current properties to the stack
     */
    public void saveProperties() {
        propStack.add(prop);
        prop = new HwmfDrawProperties(prop);  
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
            int curIdx = propStack.indexOf(prop);
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
        prop = propStack.get(stackIndex);
    }

    /**
     * After setting various window and viewport related properties,
     * the underlying graphics context needs to be adapted.
     * This methods gathers and sets the corresponding graphics transformations.
     */
    public void updateWindowMapMode() {
        Rectangle2D win = prop.getWindow();
        HwmfMapMode mapMode = prop.getMapMode();
        graphicsCtx.setTransform(initialAT);

        switch (mapMode) {
        default:
        case MM_ANISOTROPIC:
            // scale window bounds to output bounds
            graphicsCtx.scale(bbox.getWidth()/win.getWidth(), bbox.getHeight()/win.getHeight());
            graphicsCtx.translate(-win.getX(), -win.getY());
            break;
        case MM_ISOTROPIC:
            // TODO: to be validated ...
            // like anisotropic, but use x-axis as reference
            graphicsCtx.scale(bbox.getWidth()/win.getWidth(), bbox.getWidth()/win.getWidth());
            graphicsCtx.translate(-win.getX(), -win.getY());
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

    public void drawString(byte[] text, Rectangle2D bounds) {
        drawString(text, bounds, null);
    }

    public void drawString(byte[] text, Rectangle2D bounds, int dx[]) {
        HwmfFont font = prop.getFont();
        if (font == null || text == null || text.length == 0) {
            return;
        }
        
        double fontH = getFontHeight(font);
        // TODO: another approx. ...
        double fontW = fontH/1.8;
        
        Charset charset = (font.getCharset().getCharset() == null)?
                DEFAULT_CHARSET : font.getCharset().getCharset();
        String textString = new String(text, charset);
        AttributedString as = new AttributedString(textString);
        if (dx == null || dx.length == 0) {
            addAttributes(as, font);
        } else {
            int[] dxNormed = dx;
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
            if (textString.length() != text.length) {
                int codePoints = textString.codePointCount(0, textString.length());
                dxNormed = new int[codePoints];
                int dxPosition = 0;
                for (int offset = 0; offset < textString.length(); ) {
                    dxNormed[offset] = dx[dxPosition];
                    int[] chars = new int[1];
                    int cp = textString.codePointAt(offset);
                    chars[0] = cp;
                    //now figure out how many bytes it takes to encode that
                    //code point in the charset
                    int byteLength = new String(chars, 0, chars.length).getBytes(charset).length;
                    dxPosition += byteLength;
                    offset += Character.charCount(cp);
                }
            }
            for (int i = 0; i < dxNormed.length; i++) {
                addAttributes(as, font);
                // Tracking works as a prefix/advance space on characters whereas
                // dx[...] is the complete width of the current char
                // therefore we need to add the additional/suffix width to the next char
                if (i < dxNormed.length - 1) {
                    as.addAttribute(TextAttribute.TRACKING, (dxNormed[i] - fontW) / fontH, i + 1, i + 2);
                }
            }
        }
        
        
        double angle = Math.toRadians(-font.getEscapement()/10.);
        
        
        final AffineTransform at = graphicsCtx.getTransform();
        try {
            graphicsCtx.translate(bounds.getX(), bounds.getY()+fontH);
            graphicsCtx.rotate(angle);
            if (prop.getBkMode() == HwmfBkMode.OPAQUE) {
                // TODO: validate bounds
                graphicsCtx.setBackground(prop.getBackgroundColor().getColor());
                graphicsCtx.fill(new Rectangle2D.Double(0, 0, bounds.getWidth(), bounds.getHeight()));
            }
            graphicsCtx.setColor(prop.getTextColor().getColor());
            graphicsCtx.drawString(as.getIterator(), 0, 0); // (float)bounds.getX(), (float)bounds.getY());
        } finally {
            graphicsCtx.setTransform(at);
        }
    }
    
    private void addAttributes(AttributedString as, HwmfFont font) {
        DrawFontManager fontHandler = DrawFactory.getInstance(graphicsCtx).getFontManager(graphicsCtx);
        FontInfo fontInfo = fontHandler.getMappedFont(graphicsCtx, font);
        
        as.addAttribute(TextAttribute.FAMILY, fontInfo.getTypeface());
        as.addAttribute(TextAttribute.SIZE, getFontHeight(font));
        as.addAttribute(TextAttribute.STRIKETHROUGH, font.isStrikeOut());
        if (font.isUnderline()) {
            as.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
        }
        if (font.isItalic()) {
            as.addAttribute(TextAttribute.POSTURE, TextAttribute.POSTURE_OBLIQUE);
        }
        as.addAttribute(TextAttribute.WEIGHT, font.getWeight());
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
}
