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

import org.apache.poi.hslf.usermodel.RichTextRun;
import org.apache.poi.util.POILogger;
import org.apache.poi.util.POILogFactory;

import java.text.AttributedString;
import java.text.AttributedCharacterIterator;
import java.awt.font.TextAttribute;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextLayout;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;

/**
 * Paint text into java.awt.Graphics2D
 * 
 * @author Yegor Kozlov
 */
public class TextPainter {
    protected POILogger logger = POILogFactory.getLogger(this.getClass());

    protected TextShape _shape;

    public TextPainter(TextShape shape){
        _shape = shape;
    }

    public AttributedString getAttributedString(TextRun txrun){
        String text = txrun.getText();
        AttributedString at = new AttributedString(text);
        RichTextRun[] rt = txrun.getRichTextRuns();
        for (int i = 0; i < rt.length; i++) {
            int start = rt[i].getStartIndex();
            int end = rt[i].getEndIndex();
            if(start == end) continue;

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

    protected RichTextRun getRichTextRunAt(int pos){
        RichTextRun[] rt = _shape.getTextRun().getRichTextRuns();
        for (int i = 0; i < rt.length; i++) {
            int start = rt[i].getStartIndex();
            int end = rt[i].getEndIndex();
            if(pos >= start && pos < end) return rt[i];
        }
        return null;
    }

    public void paint(Graphics2D graphics){
        TextRun run = _shape.getTextRun();
        if (run == null) return;

        String text = run.getText();
        if (text == null || text.equals("")) return;

        AttributedString at = getAttributedString(run);

        AttributedCharacterIterator it = at.getIterator();
        int paragraphStart = it.getBeginIndex();
        int paragraphEnd = it.getEndIndex();

        Rectangle2D anchor = _shape.getAnchor2D();

        float textHeight = 0;
        ArrayList lines = new ArrayList();
        LineBreakMeasurer measurer = new LineBreakMeasurer(it, graphics.getFontRenderContext());
        measurer.setPosition(paragraphStart);
        while (measurer.getPosition() < paragraphEnd) {
            int startIndex = measurer.getPosition();
            int nextBreak = text.indexOf('\n', measurer.getPosition() + 1);

            boolean prStart = text.charAt(startIndex) == '\n';
            if(prStart) measurer.setPosition(startIndex++);

            RichTextRun rt = getRichTextRunAt(startIndex);
            if(rt == null) {
                logger.log(POILogger.WARN,  "RichTextRun not found at pos" + startIndex + "; text.length: " + text.length());
                break;
            }

            float wrappingWidth = (float)anchor.getWidth() - _shape.getMarginLeft() - _shape.getMarginRight();
            wrappingWidth -= rt.getTextOffset();

            if (_shape.getWordWrap() == TextShape.WrapNone) {
                wrappingWidth = _shape.getSheet().getSlideShow().getPageSize().width;
            }

            TextLayout textLayout = measurer.nextLayout(wrappingWidth + 1,
                    nextBreak == -1 ? paragraphEnd : nextBreak, true);
            if (textLayout == null) {
                textLayout = measurer.nextLayout(wrappingWidth,
                    nextBreak == -1 ? paragraphEnd : nextBreak, false);
            }
            if(textLayout == null){
                logger.log(POILogger.WARN, "Failed to break text into lines: wrappingWidth: "+wrappingWidth+
                        "; text: " + rt.getText());
                measurer.setPosition(rt.getEndIndex());
                continue;
            }
            int endIndex = measurer.getPosition();

            TextElement el = new TextElement();
            el.ascent = textLayout.getAscent();
            el._startIndex = startIndex;
            el._endIndex = endIndex;
            el._align = rt.getAlignment();
            el._text = textLayout;
            el._textOffset = rt.getTextOffset();

            textHeight += textLayout.getAscent();
            if (prStart || startIndex == 0){
                int spaceBefore = rt.getSpaceBefore();
                if (spaceBefore != 0) {
                    float val = (float)(textLayout.getAscent() + textLayout.getDescent())* spaceBefore/100;
                    textHeight += val;
                    el.ascent += val;
                }
            }

            if(rt.isBullet() && (prStart || startIndex == 0)){
                it.setIndex(startIndex);

                AttributedString bat = new AttributedString(Character.toString(rt.getBulletChar()), it.getAttributes());
                int bulletSize = rt.getBulletSize();
                if (bulletSize != -1){
                    Float sz =  (Float)bat.getIterator().getAttribute(TextAttribute.SIZE);
                    if(sz != null) bat.addAttribute(TextAttribute.SIZE, new Float(sz.floatValue()*bulletSize/100));
                }

                TextLayout bulletLayout = new TextLayout(bat.getIterator(), graphics.getFontRenderContext());
                if(text.substring(startIndex, endIndex).length() > 1){
                    el._bullet = bulletLayout;
                    el._bulletOffset = rt.getBulletOffset();
                }
            }


            float descent = textLayout.getDescent();
            int lineSpacing = rt.getLineSpacing();
            if(lineSpacing != 0) descent += textLayout.getLeading()*lineSpacing/100;
            else descent = textLayout.getLeading();
            textHeight += descent;

            el.descent = descent;
            if (prStart){
                int spaceAfter = rt.getSpaceAfter();
                if (spaceAfter != 0) {
                    float val = (float)(textLayout.getAscent() + textLayout.getDescent())* spaceAfter/100;
                    textHeight += val;
                    el.descent += val;
                }
            }

            lines.add(el);
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
        for (int i = 0; i < lines.size(); i++) {
            TextElement elem = (TextElement)lines.get(i);
            y0 += elem.ascent;

            Point2D.Double pen = new Point2D.Double();
            pen.y = y0;
            switch (elem._align) {
                default:
                case TextShape.AlignLeft:
                    pen.x = anchor.getX() + _shape.getMarginLeft();
                    break;
                case TextShape.AlignCenter:
                    pen.x = anchor.getX() + _shape.getMarginLeft() +
                            (anchor.getWidth() - elem._text.getAdvance() - _shape.getMarginLeft() - _shape.getMarginRight()) / 2;
                    break;
                case TextShape.AlignRight:
                    pen.x = anchor.getX() + _shape.getMarginLeft() +
                            (anchor.getWidth() - elem._text.getAdvance() - _shape.getMarginLeft() - _shape.getMarginRight());
                    break;
            }
            if(elem._bullet != null){
                elem._bullet.draw(graphics, (float)(pen.x + elem._bulletOffset), (float)pen.y);
            }
            elem._text.draw(graphics, (float)(pen.x + elem._textOffset), (float)pen.y);

            y0 += elem.descent;
        }
    }


    static class TextElement {
        public TextLayout _text;
        public int _textOffset;
        public TextLayout _bullet;
        public int _bulletOffset;
        public int _align;
        public int _startIndex;
        public int _endIndex;
        public float _spacing;
        public float ascent, descent;
    }
}
