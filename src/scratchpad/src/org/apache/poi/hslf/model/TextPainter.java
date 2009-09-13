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

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hslf.record.TextRulerAtom;
import org.apache.poi.hslf.usermodel.RichTextRun;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

/**
 * Paint text into java.awt.Graphics2D
 *
 * @author Yegor Kozlov
 */
public final class TextPainter {
    protected POILogger logger = POILogFactory.getLogger(this.getClass());

    /**
     * Display unicode square if a bullet char can't be displayed,
     * for example, if Wingdings font is used.
     * TODO: map Wingdngs and Symbol to unicode Arial
     */
    protected static final char DEFAULT_BULLET_CHAR = '\u25a0';

    protected TextShape _shape;

    public TextPainter(TextShape shape){
        _shape = shape;
    }

    /**
     * Convert the underlying set of rich text runs into java.text.AttributedString
     */
    public AttributedString getAttributedString(TextRun txrun){
        String text = txrun.getText();
        //TODO: properly process tabs
        text = text.replace('\t', ' ');
        text = text.replace((char)160, ' ');

        AttributedString at = new AttributedString(text);
        RichTextRun[] rt = txrun.getRichTextRuns();
        for (int i = 0; i < rt.length; i++) {
            int start = rt[i].getStartIndex();
            int end = rt[i].getEndIndex();
            if(start == end) {
                logger.log(POILogger.INFO,  "Skipping RichTextRun with zero length");
                continue;
            }

            at.addAttribute(TextAttribute.FAMILY, rt[i].getFontName(), start, end);
            at.addAttribute(TextAttribute.SIZE, new Float(rt[i].getFontSize()), start, end);
            at.addAttribute(TextAttribute.FOREGROUND, rt[i].getFontColor(), start, end);
            if(rt[i].isBold()) at.addAttribute(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD, start, end);
            if(rt[i].isItalic()) at.addAttribute(TextAttribute.POSTURE, TextAttribute.POSTURE_OBLIQUE, start, end);
            if(rt[i].isUnderlined()) {
                at.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON, start, end);
                at.addAttribute(TextAttribute.INPUT_METHOD_UNDERLINE, TextAttribute.UNDERLINE_LOW_TWO_PIXEL, start, end);
            }
            if(rt[i].isStrikethrough()) at.addAttribute(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON, start, end);
            int superScript = rt[i].getSuperscript();
            if(superScript != 0) at.addAttribute(TextAttribute.SUPERSCRIPT, superScript > 0 ? TextAttribute.SUPERSCRIPT_SUPER : TextAttribute.SUPERSCRIPT_SUB, start, end);

        }
        return at;
    }

    public void paint(Graphics2D graphics){
        Rectangle2D anchor = _shape.getLogicalAnchor2D();
        TextElement[] elem = getTextElements((float)anchor.getWidth(), graphics.getFontRenderContext());
        if(elem == null) return;

        float textHeight = 0;
        for (int i = 0; i < elem.length; i++) {
            textHeight += elem[i].ascent + elem[i].descent;
        }

        int valign = _shape.getVerticalAlignment();
        double y0 = anchor.getY();
        switch (valign){
            case TextShape.AnchorTopBaseline:
            case TextShape.AnchorTop:
                y0 += _shape.getMarginTop();
                break;
            case TextShape.AnchorBottom:
                y0 += anchor.getHeight() - textHeight - _shape.getMarginBottom();
                break;
            default:
            case TextShape.AnchorMiddle:
                float delta =  (float)anchor.getHeight() - textHeight - _shape.getMarginTop() - _shape.getMarginBottom();
                y0 += _shape.getMarginTop()  + delta/2;
                break;
        }

        //finally draw the text fragments
        for (int i = 0; i < elem.length; i++) {
            y0 += elem[i].ascent;

            Point2D.Double pen = new Point2D.Double();
            pen.y = y0;
            switch (elem[i]._align) {
                default:
                case TextShape.AlignLeft:
                    pen.x = anchor.getX() + _shape.getMarginLeft();
                    break;
                case TextShape.AlignCenter:
                    pen.x = anchor.getX() + _shape.getMarginLeft() +
                            (anchor.getWidth() - elem[i].advance - _shape.getMarginLeft() - _shape.getMarginRight()) / 2;
                    break;
                case TextShape.AlignRight:
                    pen.x = anchor.getX() + _shape.getMarginLeft() +
                            (anchor.getWidth() - elem[i].advance - _shape.getMarginLeft() - _shape.getMarginRight());
                    break;
            }
            if(elem[i]._bullet != null){
                graphics.drawString(elem[i]._bullet.getIterator(), (float)(pen.x + elem[i]._bulletOffset), (float)pen.y);
            }
            AttributedCharacterIterator chIt = elem[i]._text.getIterator();
            if(chIt.getEndIndex() > chIt.getBeginIndex()) {
                graphics.drawString(chIt, (float)(pen.x + elem[i]._textOffset), (float)pen.y);
            }
            y0 += elem[i].descent;
        }
    }

    public TextElement[] getTextElements(float textWidth, FontRenderContext frc){
        TextRun run = _shape.getTextRun();
        if (run == null) return null;

        String text = run.getText();
        if (text == null || text.equals("")) return null;

        AttributedString at = getAttributedString(run);

        AttributedCharacterIterator it = at.getIterator();
        int paragraphStart = it.getBeginIndex();
        int paragraphEnd = it.getEndIndex();

        List<TextElement> lines = new ArrayList<TextElement>();
        LineBreakMeasurer measurer = new LineBreakMeasurer(it, frc);
        measurer.setPosition(paragraphStart);
        while (measurer.getPosition() < paragraphEnd) {
            int startIndex = measurer.getPosition();
            int nextBreak = text.indexOf('\n', measurer.getPosition() + 1);

            boolean prStart = text.charAt(startIndex) == '\n';
            if(prStart) measurer.setPosition(startIndex++);

            RichTextRun rt = run.getRichTextRunAt(startIndex == text.length() ? (startIndex-1) : startIndex);
            if(rt == null) {
                logger.log(POILogger.WARN,  "RichTextRun not found at pos" + startIndex + "; text.length: " + text.length());
                break;
            }

            float wrappingWidth = textWidth - _shape.getMarginLeft() - _shape.getMarginRight();
            int bulletOffset = rt.getBulletOffset();
            int textOffset = rt.getTextOffset();
            int indent = rt.getIndentLevel();

            TextRulerAtom ruler = run.getTextRuler();
            if(ruler != null) {
                int bullet_val = ruler.getBulletOffsets()[indent]*Shape.POINT_DPI/Shape.MASTER_DPI;
                int text_val = ruler.getTextOffsets()[indent]*Shape.POINT_DPI/Shape.MASTER_DPI;
                if(bullet_val > text_val){
                    int a = bullet_val;
                    bullet_val = text_val;
                    text_val = a;
                }
                if(bullet_val != 0 ) bulletOffset = bullet_val;
                if(text_val != 0) textOffset = text_val;
            }

            if(bulletOffset > 0 || prStart || startIndex == 0) wrappingWidth -= textOffset;

            if (_shape.getWordWrap() == TextShape.WrapNone) {
                wrappingWidth = _shape.getSheet().getSlideShow().getPageSize().width;
            }

            TextLayout textLayout = measurer.nextLayout(wrappingWidth + 1,
                    nextBreak == -1 ? paragraphEnd : nextBreak, true);
            if (textLayout == null) {
                textLayout = measurer.nextLayout(textWidth,
                    nextBreak == -1 ? paragraphEnd : nextBreak, false);
            }
            if(textLayout == null){
                logger.log(POILogger.WARN, "Failed to break text into lines: wrappingWidth: "+wrappingWidth+
                        "; text: " + rt.getText());
                measurer.setPosition(rt.getEndIndex());
                continue;
            }
            int endIndex = measurer.getPosition();

            float lineHeight = (float)textLayout.getBounds().getHeight();
            int linespacing = rt.getLineSpacing();
            if(linespacing == 0) linespacing = 100;

            TextElement el = new TextElement();
            if(linespacing >= 0){
                el.ascent = textLayout.getAscent()*linespacing/100;
            } else {
                el.ascent = -linespacing*Shape.POINT_DPI/Shape.MASTER_DPI;
            }

            el._align = rt.getAlignment();
            el.advance = textLayout.getAdvance();
            el._textOffset = textOffset;
            el._text = new AttributedString(it, startIndex, endIndex);
            el.textStartIndex = startIndex;
            el.textEndIndex = endIndex;

            if (prStart){
                int sp = rt.getSpaceBefore();
                float spaceBefore;
                if(sp >= 0){
                    spaceBefore = lineHeight * sp/100;
                } else {
                    spaceBefore = -sp*Shape.POINT_DPI/Shape.MASTER_DPI;
                }
                el.ascent += spaceBefore;
            }

            float descent;
            if(linespacing >= 0){
                descent = (textLayout.getDescent() + textLayout.getLeading())*linespacing/100;
            } else {
                descent = -linespacing*Shape.POINT_DPI/Shape.MASTER_DPI;
            }
            if (prStart){
                int sp = rt.getSpaceAfter();
                float spaceAfter;
                if(sp >= 0){
                    spaceAfter = lineHeight * sp/100;
                } else {
                    spaceAfter = -sp*Shape.POINT_DPI/Shape.MASTER_DPI;
                }
                el.ascent += spaceAfter;
            }
            el.descent = descent;

            if(rt.isBullet() && (prStart || startIndex == 0)){
                it.setIndex(startIndex);

                AttributedString bat = new AttributedString(Character.toString(rt.getBulletChar()));
                Color clr = rt.getBulletColor();
                if (clr != null) bat.addAttribute(TextAttribute.FOREGROUND, clr);
                else bat.addAttribute(TextAttribute.FOREGROUND, it.getAttribute(TextAttribute.FOREGROUND));

                int fontIdx = rt.getBulletFont();
                if(fontIdx == -1) fontIdx = rt.getFontIndex();
                PPFont bulletFont = _shape.getSheet().getSlideShow().getFont(fontIdx);
                bat.addAttribute(TextAttribute.FAMILY, bulletFont.getFontName());

                int bulletSize = rt.getBulletSize();
                int fontSize = rt.getFontSize();
                if(bulletSize != -1) fontSize = Math.round(fontSize*bulletSize*0.01f);
                bat.addAttribute(TextAttribute.SIZE, new Float(fontSize));

                if(!new Font(bulletFont.getFontName(), Font.PLAIN, 1).canDisplay(rt.getBulletChar())){
                    bat.addAttribute(TextAttribute.FAMILY, "Arial");
                    bat = new AttributedString("" + DEFAULT_BULLET_CHAR, bat.getIterator().getAttributes());
                }

                if(text.substring(startIndex, endIndex).length() > 1){
                    el._bullet = bat;
                    el._bulletOffset = bulletOffset;
                }
            }
            lines.add(el);
        }

        //finally draw the text fragments
        TextElement[] elems = new TextElement[lines.size()];
        return lines.toArray(elems);
    }

    public static class TextElement {
        public AttributedString _text;
        public int _textOffset;
        public AttributedString _bullet;
        public int _bulletOffset;
        public int _align;
        public float ascent, descent;
        public float advance;
        public int textStartIndex, textEndIndex;
    }
}
