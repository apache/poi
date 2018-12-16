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

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.io.InvalidObjectException;
import java.text.AttributedCharacterIterator;
import java.text.AttributedCharacterIterator.Attribute;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.poi.common.usermodel.fonts.FontGroup;
import org.apache.poi.common.usermodel.fonts.FontGroup.FontGroupRange;
import org.apache.poi.common.usermodel.fonts.FontInfo;
import org.apache.poi.sl.usermodel.AutoNumberingScheme;
import org.apache.poi.sl.usermodel.Hyperlink;
import org.apache.poi.sl.usermodel.Insets2D;
import org.apache.poi.sl.usermodel.PaintStyle;
import org.apache.poi.sl.usermodel.PlaceableShape;
import org.apache.poi.sl.usermodel.ShapeContainer;
import org.apache.poi.sl.usermodel.Sheet;
import org.apache.poi.sl.usermodel.Slide;
import org.apache.poi.sl.usermodel.TextParagraph;
import org.apache.poi.sl.usermodel.TextParagraph.BulletStyle;
import org.apache.poi.sl.usermodel.TextParagraph.TextAlign;
import org.apache.poi.sl.usermodel.TextRun;
import org.apache.poi.sl.usermodel.TextRun.FieldType;
import org.apache.poi.sl.usermodel.TextShape;
import org.apache.poi.sl.usermodel.TextShape.TextDirection;
import org.apache.poi.util.Internal;
import org.apache.poi.util.LocaleUtil;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.poi.util.Units;

public class DrawTextParagraph implements Drawable {
    private static final POILogger LOG = POILogFactory.getLogger(DrawTextParagraph.class);
    
    /** Keys for passing hyperlinks to the graphics context */
    public static final XlinkAttribute HYPERLINK_HREF = new XlinkAttribute("href");
    public static final XlinkAttribute HYPERLINK_LABEL = new XlinkAttribute("label");

    protected TextParagraph<?,?,?> paragraph;
    double x, y;
    protected List<DrawTextFragment> lines = new ArrayList<>();
    protected String rawText;
    protected DrawTextFragment bullet;
    protected int autoNbrIdx;

    /**
     * the highest line in this paragraph. Used for line spacing.
     */
    protected double maxLineHeight;

    /**
     * Defines an attribute used for storing the hyperlink associated with
     * some renderable text.
     */
    private static class XlinkAttribute extends Attribute {

        XlinkAttribute(String name) {
            super(name);
        }

        /**
         * Resolves instances being deserialized to the predefined constants.
         */
        @Override
        protected Object readResolve() throws InvalidObjectException {
            if (HYPERLINK_HREF.getName().equals(getName())) {
                return HYPERLINK_HREF;
            }
            if (HYPERLINK_LABEL.getName().equals(getName())) {
                return HYPERLINK_LABEL;
            }
            throw new InvalidObjectException("unknown attribute name");
        }
    }


    public DrawTextParagraph(TextParagraph<?,?,?> paragraph) {
        this.paragraph = paragraph;
    }

    public void setPosition(double x, double y) {
        // TODO: replace it, by applyTransform????
        this.x = x;
        this.y = y;
    }

    public double getY() {
        return y;
    }

    /**
     * Sets the auto numbering index of the handled paragraph
     * @param index the auto numbering index
     */
    public void setAutoNumberingIdx(int index) {
        autoNbrIdx = index;
    }

    @Override
    public void draw(Graphics2D graphics){
        if (lines.isEmpty()) {
            return;
        }

        double penY = y;

        boolean firstLine = true;
        int indentLevel = paragraph.getIndentLevel();
        Double leftMargin = paragraph.getLeftMargin();
        if (leftMargin == null) {
            // if the marL attribute is omitted, then a value of 347663 is implied
            leftMargin = Units.toPoints(347663L*indentLevel);
        }
        Double indent = paragraph.getIndent();
        if (indent == null) {
            indent = Units.toPoints(347663L*indentLevel);
        }
        if (isHSLF()) {
            // special handling for HSLF
            indent -= leftMargin;
        }

//        Double rightMargin = paragraph.getRightMargin();
//        if (rightMargin == null) {
//            rightMargin = 0d;
//        }

        //The vertical line spacing
        Double spacing = paragraph.getLineSpacing();
        if (spacing == null) {
            spacing = 100d;
        }

        for(DrawTextFragment line : lines){
            double penX;

            if(firstLine) {
                if (!isEmptyParagraph()) {
                    // TODO: find out character style for empty, but bulleted/numbered lines
                    bullet = getBullet(graphics, line.getAttributedString().getIterator());
                }

                if (bullet != null){
                    bullet.setPosition(x+leftMargin+indent, penY);
                    bullet.draw(graphics);
                    // don't let text overlay the bullet and advance by the bullet width
                    double bulletWidth = bullet.getLayout().getAdvance() + 1;
                    penX = x + Math.max(leftMargin, leftMargin+indent+bulletWidth);
                } else {
                    penX = x + leftMargin;
                }
            } else {
                penX = x + leftMargin;
            }

            Rectangle2D anchor = DrawShape.getAnchor(graphics, paragraph.getParentShape());
            // Insets are already applied on DrawTextShape.drawContent
            // but (outer) anchor need to be adjusted
            Insets2D insets = paragraph.getParentShape().getInsets();
            double leftInset = insets.left;
            double rightInset = insets.right;

            TextAlign ta = paragraph.getTextAlign();
            if (ta == null) {
                ta = TextAlign.LEFT;
            }
            switch (ta) {
                case CENTER:
                    penX += (anchor.getWidth() - line.getWidth() - leftInset - rightInset - leftMargin) / 2;
                    break;
                case RIGHT:
                    penX += (anchor.getWidth() - line.getWidth() - leftInset - rightInset);
                    break;
                default:
                    break;
            }

            line.setPosition(penX, penY);
            line.draw(graphics);
            
            if(spacing > 0) {
                // If linespacing >= 0, then linespacing is a percentage of normal line height.
                penY += spacing*0.01* line.getHeight();
            } else {
                // negative value means absolute spacing in points
                penY += -spacing;
            }

            firstLine = false;
        }

        y = penY - y;
    }

    public float getFirstLineLeading() {
        return (lines.isEmpty()) ? 0 : lines.get(0).getLeading();
    }

    public float getFirstLineHeight() {
        return (lines.isEmpty()) ? 0 : lines.get(0).getHeight();
    }

    public float getLastLineHeight() {
        return (lines.isEmpty()) ? 0 : lines.get(lines.size()-1).getHeight();
    }

    public boolean isEmptyParagraph() {
        return (lines.isEmpty() || rawText.trim().isEmpty());
    }

    @Override
    public void applyTransform(Graphics2D graphics) {
    }

    @Override
    public void drawContent(Graphics2D graphics) {
    }

    /**
     * break text into lines, each representing a line of text that fits in the wrapping width
     *
     * @param graphics The drawing context for computing text-lengths.
     */
    protected void breakText(Graphics2D graphics){
        lines.clear();

        DrawFactory fact = DrawFactory.getInstance(graphics);
        StringBuilder text = new StringBuilder();
        AttributedString at = getAttributedString(graphics, text);
        boolean emptyParagraph = text.toString().trim().isEmpty();

        AttributedCharacterIterator it = at.getIterator();
        LineBreakMeasurer measurer = new LineBreakMeasurer(it, graphics.getFontRenderContext());
        for (;;) {
            int startIndex = measurer.getPosition();

            // add a pixel to compensate rounding errors
            double wrappingWidth = getWrappingWidth(lines.isEmpty(), graphics) + 1;
            // shape width can be smaller that the sum of insets (this was proved by a test file)
            if(wrappingWidth < 0) {
                wrappingWidth = 1;
            }

            int nextBreak = text.indexOf("\n", startIndex + 1);
            if (nextBreak == -1) {
                nextBreak = it.getEndIndex();
            }

            TextLayout layout = measurer.nextLayout((float)wrappingWidth, nextBreak, true);
            if (layout == null) {
                 // layout can be null if the entire word at the current position
                 // does not fit within the wrapping width. Try with requireNextWord=false.
                 layout = measurer.nextLayout((float)wrappingWidth, nextBreak, false);
            }

            if(layout == null) {
                // exit if can't break any more
                break;
            }

            int endIndex = measurer.getPosition();
            // skip over new line breaks (we paint 'clear' text runs not starting or ending with \n)
            if(endIndex < it.getEndIndex() && text.charAt(endIndex) == '\n'){
                measurer.setPosition(endIndex + 1);
            }

            TextAlign hAlign = paragraph.getTextAlign();
            if(hAlign == TextAlign.JUSTIFY || hAlign == TextAlign.JUSTIFY_LOW) {
                layout = layout.getJustifiedLayout((float)wrappingWidth);
            }

            AttributedString str = (emptyParagraph)
                ? null // we will not paint empty paragraphs
                : new AttributedString(it, startIndex, endIndex);
            DrawTextFragment line = fact.getTextFragment(layout, str);
            lines.add(line);

            maxLineHeight = Math.max(maxLineHeight, line.getHeight());

            if(endIndex == it.getEndIndex()) {
                break;
            }
        }

        rawText = text.toString();
    }

    protected DrawTextFragment getBullet(Graphics2D graphics, AttributedCharacterIterator firstLineAttr) {
        BulletStyle bulletStyle = paragraph.getBulletStyle();
        if (bulletStyle == null) {
            return null;
        }

        String buCharacter;
        AutoNumberingScheme ans = bulletStyle.getAutoNumberingScheme();
        if (ans != null) {
            buCharacter = ans.format(autoNbrIdx);
        } else {
            buCharacter = bulletStyle.getBulletCharacter();
        }
        if (buCharacter == null) {
            return null;
        }

        PlaceableShape<?,?> ps = getParagraphShape();
        PaintStyle fgPaintStyle = bulletStyle.getBulletFontColor();
        Paint fgPaint;
        if (fgPaintStyle == null) {
            fgPaint = (Paint)firstLineAttr.getAttribute(TextAttribute.FOREGROUND);
        } else {
            fgPaint = new DrawPaint(ps).getPaint(graphics, fgPaintStyle);
        }

        float fontSize = (Float)firstLineAttr.getAttribute(TextAttribute.SIZE);
        Double buSz = bulletStyle.getBulletFontSize();
        if (buSz == null) {
            buSz = 100d;
        }
        if (buSz > 0) {
            fontSize *= buSz* 0.01;
        } else {
            fontSize = (float)-buSz;
        }

        String buFontStr = bulletStyle.getBulletFont();
        if (buFontStr == null) {
            buFontStr = paragraph.getDefaultFontFamily();
        }
        assert(buFontStr != null);
        FontInfo buFont = new DrawFontInfo(buFontStr);


        DrawFontManager dfm = DrawFactory.getInstance(graphics).getFontManager(graphics);
        // TODO: check font group defaulting to Symbol
        buFont = dfm.getMappedFont(graphics, buFont);

        AttributedString str = new AttributedString(dfm.mapFontCharset(graphics,buFont,buCharacter));
        str.addAttribute(TextAttribute.FOREGROUND, fgPaint);
        str.addAttribute(TextAttribute.FAMILY, buFont.getTypeface());
        str.addAttribute(TextAttribute.SIZE, fontSize);

        TextLayout layout = new TextLayout(str.getIterator(), graphics.getFontRenderContext());
        DrawFactory fact = DrawFactory.getInstance(graphics);
        return fact.getTextFragment(layout, str);
    }

    protected String getRenderableText(Graphics2D graphics, TextRun tr) {
        if (tr.getFieldType() == FieldType.SLIDE_NUMBER) {
            Slide<?,?> slide = (Slide<?,?>)graphics.getRenderingHint(Drawable.CURRENT_SLIDE);
            return (slide == null) ? "" : Integer.toString(slide.getSlideNumber());
        }
        return getRenderableText(tr);
    }

    @Internal
    public String getRenderableText(final TextRun tr) {
        final String txtSpace = tr.getRawText().replace("\t", tab2space(tr)).replace('\u000b', '\n');
        final Locale loc = LocaleUtil.getUserLocale();

        switch (tr.getTextCap()) {
            case ALL:
                return txtSpace.toUpperCase(loc);
            case SMALL:
                return txtSpace.toLowerCase(loc);
            default:
                return txtSpace;
        }
    }

    /**
     * Replace a tab with the effective number of white spaces.
     */
    private String tab2space(TextRun tr) {
        AttributedString string = new AttributedString(" ");
        String fontFamily = tr.getFontFamily();
        if (fontFamily == null) {
            fontFamily = "Lucida Sans";
        }
        string.addAttribute(TextAttribute.FAMILY, fontFamily);

        Double fs = tr.getFontSize();
        if (fs == null) {
            fs = 12d;
        }
        string.addAttribute(TextAttribute.SIZE, fs.floatValue());

        TextLayout l = new TextLayout(string.getIterator(), new FontRenderContext(null, true, true));
        double wspace = l.getAdvance();

        Double tabSz = paragraph.getDefaultTabSize();
        if (tabSz == null) {
            tabSz = wspace*4;
        }

        int numSpaces = (int)Math.ceil(tabSz / wspace);
        StringBuilder buf = new StringBuilder();
        for(int i = 0; i < numSpaces; i++) {
            buf.append(' ');
        }
        return buf.toString();
    }


    /**
     * Returns wrapping width to break lines in this paragraph
     *
     * @param firstLine whether the first line is breaking
     *
     * @return  wrapping width in points
     */
    protected double getWrappingWidth(boolean firstLine, Graphics2D graphics){
        TextShape<?,?> ts = paragraph.getParentShape();

        // internal margins for the text box
        Insets2D insets = ts.getInsets();
        double leftInset = insets.left;
        double rightInset = insets.right;

        int indentLevel = paragraph.getIndentLevel();
        if (indentLevel == -1) {
            // default to 0, if indentLevel is not set
            indentLevel = 0;
        }
        Double leftMargin = paragraph.getLeftMargin();
        if (leftMargin == null) {
            // if the marL attribute is omitted, then a value of 347663 is implied
            leftMargin = Units.toPoints(347663L*(indentLevel+1));
        }
        Double indent = paragraph.getIndent();
        if (indent == null) {
            indent = Units.toPoints(347663L*indentLevel);
        }
        Double rightMargin = paragraph.getRightMargin();
        if (rightMargin == null) {
            rightMargin = 0d;
        }

        Rectangle2D anchor = DrawShape.getAnchor(graphics, ts);
        TextDirection textDir = ts.getTextDirection();
        double width;
        if (!ts.getWordWrap()) {
            Dimension pageDim = ts.getSheet().getSlideShow().getPageSize();
            // if wordWrap == false then we return the advance to the (right) border of the sheet
            switch (textDir) {
                default:
                    width = pageDim.getWidth() - anchor.getX();
                    break;
                case VERTICAL:
                    width = pageDim.getHeight() - anchor.getX();
                    break;
                case VERTICAL_270:
                    width = anchor.getX();
                    break;
            }
        } else {
            switch (textDir) {
                default:
                    width = anchor.getWidth() - leftInset - rightInset - leftMargin - rightMargin;
                    break;
                case VERTICAL:
                case VERTICAL_270:
                    width = anchor.getHeight() - leftInset - rightInset - leftMargin - rightMargin;
                    break;
            }
            if (firstLine && !isHSLF()) {
                if (bullet != null){
                    if (indent > 0) {
                        width -= indent;
                    }
                } else {
                    if (indent > 0) {
                        width -= indent; // first line indentation
                    } else if (indent < 0) { // hanging indentation: the first line start at the left margin
                        width += leftMargin;
                    }
                }
            }
        }

        return width;
    }

    private static class AttributedStringData {
        Attribute attribute;
        Object value;
        int beginIndex, endIndex;
        AttributedStringData(Attribute attribute, Object value, int beginIndex, int endIndex) {
            this.attribute = attribute;
            this.value = value;
            this.beginIndex = beginIndex;
            this.endIndex = endIndex;
        }
    }

    /**
     * Helper method for paint style relative to bounds, e.g. gradient paint
     */
    @SuppressWarnings("rawtypes")
    private PlaceableShape<?,?> getParagraphShape() {
        return new PlaceableShape(){
            @Override
            public ShapeContainer<?,?> getParent() { return null; }
            @Override
            public Rectangle2D getAnchor() { return paragraph.getParentShape().getAnchor(); }
            @Override
            public void setAnchor(Rectangle2D anchor) {}
            @Override
            public double getRotation() { return 0; }
            @Override
            public void setRotation(double theta) {}
            @Override
            public void setFlipHorizontal(boolean flip) {}
            @Override
            public void setFlipVertical(boolean flip) {}
            @Override
            public boolean getFlipHorizontal() { return false; }
            @Override
            public boolean getFlipVertical() { return false; }
            @Override
            public Sheet<?,?> getSheet() { return paragraph.getParentShape().getSheet(); }
        };
    }

    protected AttributedString getAttributedString(Graphics2D graphics, StringBuilder text){
        List<AttributedStringData> attList = new ArrayList<>();
        if (text == null) {
            text = new StringBuilder();
        }

        PlaceableShape<?,?> ps = getParagraphShape();
        DrawFontManager dfm = DrawFactory.getInstance(graphics).getFontManager(graphics);
        assert(dfm != null);

        for (TextRun run : paragraph){
            String runText = getRenderableText(graphics, run);
            // skip empty runs
            if (runText.isEmpty()) {
                continue;
            }

            // user can pass an custom object to convert fonts

            runText = dfm.mapFontCharset(graphics, run.getFontInfo(null), runText);
            int beginIndex = text.length();
            text.append(runText);
            int endIndex = text.length();

            PaintStyle fgPaintStyle = run.getFontColor();
            Paint fgPaint = new DrawPaint(ps).getPaint(graphics, fgPaintStyle);
            attList.add(new AttributedStringData(TextAttribute.FOREGROUND, fgPaint, beginIndex, endIndex));

            Double fontSz = run.getFontSize();
            if (fontSz == null) {
                fontSz = paragraph.getDefaultFontSize();
            }
            attList.add(new AttributedStringData(TextAttribute.SIZE, fontSz.floatValue(), beginIndex, endIndex));

            if(run.isBold()) {
                attList.add(new AttributedStringData(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD, beginIndex, endIndex));
            }
            if(run.isItalic()) {
                attList.add(new AttributedStringData(TextAttribute.POSTURE, TextAttribute.POSTURE_OBLIQUE, beginIndex, endIndex));
            }
            if(run.isUnderlined()) {
                attList.add(new AttributedStringData(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON, beginIndex, endIndex));
                attList.add(new AttributedStringData(TextAttribute.INPUT_METHOD_UNDERLINE, TextAttribute.UNDERLINE_LOW_TWO_PIXEL, beginIndex, endIndex));
            }
            if(run.isStrikethrough()) {
                attList.add(new AttributedStringData(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON, beginIndex, endIndex));
            }
            if(run.isSubscript()) {
                attList.add(new AttributedStringData(TextAttribute.SUPERSCRIPT, TextAttribute.SUPERSCRIPT_SUB, beginIndex, endIndex));
            }
            if(run.isSuperscript()) {
                attList.add(new AttributedStringData(TextAttribute.SUPERSCRIPT, TextAttribute.SUPERSCRIPT_SUPER, beginIndex, endIndex));
            }

            Hyperlink<?,?> hl = run.getHyperlink();
            if (hl != null) {
                attList.add(new AttributedStringData(HYPERLINK_HREF, hl.getAddress(), beginIndex, endIndex));
                attList.add(new AttributedStringData(HYPERLINK_LABEL, hl.getLabel(), beginIndex, endIndex));
            }

            processGlyphs(graphics, dfm, attList, beginIndex, run, runText);
        }

        // ensure that the paragraph contains at least one character
        // We need this trick to correctly measure text
        if (text.length() == 0) {
            Double fontSz = paragraph.getDefaultFontSize();
            text.append(" ");
            attList.add(new AttributedStringData(TextAttribute.SIZE, fontSz.floatValue(), 0, 1));
        }

        AttributedString string = new AttributedString(text.toString());
        for (AttributedStringData asd : attList) {
            string.addAttribute(asd.attribute, asd.value, asd.beginIndex, asd.endIndex);
        }

        return string;
    }

    /**
     * Processing the glyphs is done in two steps.
     * <li>determine the font group - a text run can have different font groups. Depending on the chars,
     * the correct font group needs to be used
     *
     * @see <a href="https://blogs.msdn.microsoft.com/officeinteroperability/2013/04/22/office-open-xml-themes-schemes-and-fonts/">Office Open XML Themes, Schemes, and Fonts</a>
     */
    private void processGlyphs(Graphics2D graphics, DrawFontManager dfm, List<AttributedStringData> attList, final int beginIndex, TextRun run, String runText) {
        // determine font group ranges of the textrun to focus the fallback handling only on that font group
        List<FontGroupRange> ttrList = FontGroup.getFontGroupRanges(runText);
        int rangeBegin = 0;
        for (FontGroupRange ttr : ttrList) {
            FontInfo fiRun = run.getFontInfo(ttr.getFontGroup());
            if (fiRun == null) {
                // if the font group specific font wasn't defined, fallback to LATIN
                fiRun = run.getFontInfo(FontGroup.LATIN);
            }
            FontInfo fiMapped = dfm.getMappedFont(graphics, fiRun);
            FontInfo fiFallback = dfm.getFallbackFont(graphics, fiRun);
            assert(fiFallback != null);
            if (fiMapped == null) {
                fiMapped = dfm.getMappedFont(graphics, new DrawFontInfo(paragraph.getDefaultFontFamily()));
            }
            if (fiMapped == null) {
                fiMapped = fiFallback;
            }

            Font fontMapped = dfm.createAWTFont(graphics, fiMapped, 10, run.isBold(), run.isItalic());
            Font fontFallback = dfm.createAWTFont(graphics, fiFallback, 10, run.isBold(), run.isItalic());

            // check for unsupported characters and add a fallback font for these
            final int rangeLen = ttr.getLength();
            int partEnd = rangeBegin;
            while (partEnd<rangeBegin+rangeLen) {
                // start with the assumption that the font is able to display the chars
                int partBegin = partEnd;
                partEnd = nextPart(fontMapped, runText, partBegin, rangeBegin+rangeLen, true);

                // Now we have 3 cases:
                // (a) the first part couldn't be displayed,
                // (b) only part of the text run could be displayed
                // (c) or all chars can be displayed (default)

                if (partBegin < partEnd) {
                    // handle (b) and (c)
                    attList.add(new AttributedStringData(TextAttribute.FAMILY, fontMapped.getFontName(Locale.ROOT), beginIndex+partBegin, beginIndex+partEnd));
                    if (LOG.check(POILogger.DEBUG)) {
                        LOG.log(POILogger.DEBUG, "mapped: ",fontMapped.getFontName(Locale.ROOT)," ",(beginIndex+partBegin)," ",(beginIndex+partEnd)," - ",runText.substring(beginIndex+partBegin, beginIndex+partEnd));
                    }
                }

                // fallback for unsupported glyphs
                partBegin = partEnd;
                partEnd = nextPart(fontMapped, runText, partBegin, rangeBegin+rangeLen, false);
                
                if (partBegin < partEnd) {
                    // handle (a) and (b)
                    attList.add(new AttributedStringData(TextAttribute.FAMILY, fontFallback.getFontName(Locale.ROOT), beginIndex+partBegin, beginIndex+partEnd));
                    if (LOG.check(POILogger.DEBUG)) {
                        LOG.log(POILogger.DEBUG, "fallback: ",fontFallback.getFontName(Locale.ROOT)," ",(beginIndex+partBegin)," ",(beginIndex+partEnd)," - ",runText.substring(beginIndex+partBegin, beginIndex+partEnd));
                    }
                }
            }
            
            rangeBegin += rangeLen;
        }
    }
    
    private static int nextPart(Font fontMapped, String runText, int beginPart, int endPart, boolean isDisplayed) {
        int rIdx = beginPart;
        while (rIdx < endPart) {
            int codepoint = runText.codePointAt(rIdx);
            if (fontMapped.canDisplay(codepoint) != isDisplayed) {
                break;
            }
            rIdx += Character.charCount(codepoint);
        }
        return rIdx;
    }

    /**
     * @return {@code true} if the HSLF implementation is used
     */
    protected boolean isHSLF() {
        return DrawShape.isHSLF(paragraph.getParentShape());
    }
}
