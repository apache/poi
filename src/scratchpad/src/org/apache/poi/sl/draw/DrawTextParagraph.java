package org.apache.poi.sl.draw;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.font.*;
import java.awt.geom.Rectangle2D;
import java.text.*;
import java.text.AttributedCharacterIterator.Attribute;
import java.util.*;

import org.apache.poi.sl.usermodel.*;
import org.apache.poi.sl.usermodel.TextParagraph.BulletStyle;
import org.apache.poi.sl.usermodel.TextRun.TextCap;
import org.apache.poi.sl.usermodel.TextParagraph.TextAlign;

public class DrawTextParagraph<T extends TextRun> implements Drawable {
    protected TextParagraph<T> paragraph;
    double x, y;
    protected Insets2D insets = new Insets2D(0,0,0,0);
    protected List<DrawTextFragment> lines = new ArrayList<DrawTextFragment>();
    protected String rawText;
    protected DrawTextFragment bullet;

    /**
     * the highest line in this paragraph. Used for line spacing.
     */
    protected double maxLineHeight;

    public DrawTextParagraph(TextParagraph<T> paragraph) {
        this.paragraph = paragraph;
    }

    public Insets2D getInsets() {
        return insets;
    }

    public void setInsets(Insets2D insets) {
        this.insets.set(insets.top, insets.left, insets.bottom, insets.right);
    }

    public void setPosition(double x, double y) {
        // TODO: replace it, by applyTransform????
        this.x = x;
        this.y = y;
    }

    public double getY() {
        return y;
    }
    
    public void draw(Graphics2D graphics){
        if (lines.isEmpty()) return;
        
        double leftInset = insets.left;
        double rightInset = insets.right;
        double penY = y;

        double leftMargin = paragraph.getLeftMargin();
        boolean firstLine = true;
        double indent = paragraph.getIndent();

        //The vertical line spacing
        double spacing = paragraph.getLineSpacing();
        for(DrawTextFragment line : lines){
            double penX = x + leftMargin;

            if(firstLine) {
                if (!isEmptyParagraph()) {
                    bullet = getBullet(graphics, line.getAttributedString().getIterator());
                }
                
                if(bullet != null){
                    if (indent < 0) {
                        // a negative value means "Hanging" indentation and
                        // indicates the position of the actual bullet character.
                        // (the bullet is shifted to right relative to the text)
                        bullet.setPosition(penX + indent, penY);
                    } else if(indent > 0){
                        // a positive value means the "First Line" indentation:
                        // the first line is indented and other lines start at the bullet ofset
                        bullet.setPosition(penX, penY);
                        penX += indent;
                    } else {
                        // a zero indent means that the bullet and text have the same offset
                        bullet.setPosition(penX, penY);

                        // don't let text overlay the bullet and advance by the bullet width
                        penX += bullet.getLayout().getAdvance() + 1;
                    }

                    bullet.draw(graphics);
                } else {
                    penX += indent;
                }
            }

            Rectangle2D anchor = DrawShape.getAnchor(graphics, paragraph.getParentShape());

            switch (paragraph.getTextAlign()) {
                case CENTER:
                    penX += (anchor.getWidth() - leftMargin - line.getWidth() - leftInset - rightInset) / 2;
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
                // positive value means absolute spacing in points
                penY += -spacing;
            }

            firstLine = false;
        }

        y = penY - y;
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
    
    public void applyTransform(Graphics2D graphics) {
    }

    public void drawContent(Graphics2D graphics) {
    }

    /**
     * break text into lines, each representing a line of text that fits in the wrapping width
     *
     * @param graphics
     */
    protected void breakText(Graphics2D graphics){
        lines.clear();

        DrawFactory fact = DrawFactory.getInstance(graphics);
        StringBuilder text = new StringBuilder();
        AttributedString at = getAttributedString(graphics, text);
        boolean emptyParagraph = ("".equals(text.toString().trim()));

        AttributedCharacterIterator it = at.getIterator();
        LineBreakMeasurer measurer = new LineBreakMeasurer(it, graphics.getFontRenderContext());
        for (;;) {
            int startIndex = measurer.getPosition();

            double wrappingWidth = getWrappingWidth(lines.size() == 0, graphics) + 1; // add a pixel to compensate rounding errors
            // shape width can be smaller that the sum of insets (this was proved by a test file)
            if(wrappingWidth < 0) wrappingWidth = 1;

            int nextBreak = text.indexOf("\n", startIndex + 1);
            if(nextBreak == -1) nextBreak = it.getEndIndex();

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

            if(endIndex == it.getEndIndex()) break;
        }

        rawText = text.toString();
    }

    protected DrawTextFragment getBullet(Graphics2D graphics, AttributedCharacterIterator firstLineAttr) {
        BulletStyle bulletStyle = paragraph.getBulletStyle();
        if (bulletStyle == null) return null;

        String buCharacter = bulletStyle.getBulletCharacter();
        if (buCharacter == null) return null;

        String buFont = bulletStyle.getBulletFont();
        if (buFont == null) buFont = paragraph.getDefaultFontFamily();
        assert(buFont != null);

        Color buColor = bulletStyle.getBulletFontColor();
        if (buColor == null) buColor = (Color)firstLineAttr.getAttribute(TextAttribute.FOREGROUND);

        float fontSize = (Float)firstLineAttr.getAttribute(TextAttribute.SIZE);
        float buSz = (float)bulletStyle.getBulletFontSize();
        if(buSz > 0) fontSize *= buSz* 0.01;
        else fontSize = -buSz;

        
        AttributedString str = new AttributedString(buCharacter);
        str.addAttribute(TextAttribute.FOREGROUND, buColor);
        str.addAttribute(TextAttribute.FAMILY, buFont);
        str.addAttribute(TextAttribute.SIZE, fontSize);

        TextLayout layout = new TextLayout(str.getIterator(), graphics.getFontRenderContext());
        DrawFactory fact = DrawFactory.getInstance(graphics);
        return fact.getTextFragment(layout, str);
    }

    protected String getRenderableText(TextRun tr) {
        StringBuilder buf = new StringBuilder();
        TextCap cap = tr.getTextCap();
        for (char c : tr.getRawText().toCharArray()) {
            if(c == '\t') {
                // TODO: finish support for tabs
                buf.append("  ");
                continue;
            }

            switch (cap) {
                case ALL: c = Character.toUpperCase(c); break;
                case SMALL: c = Character.toLowerCase(c); break;
                case NONE: break;
            }

            buf.append(c);
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
        // internal margins for the text box

        double leftInset = insets.left;
        double rightInset = insets.right;

        Rectangle2D anchor = DrawShape.getAnchor(graphics, paragraph.getParentShape());

        double leftMargin = paragraph.getLeftMargin();
        double indent = paragraph.getIndent();

        double width;
        TextShape<? extends TextParagraph<T>> ts = paragraph.getParentShape();
        if (!ts.getWordWrap()) {
            // if wordWrap == false then we return the advance to the right border of the sheet
            width = ts.getSheet().getSlideShow().getPageSize().getWidth() - anchor.getX();
        } else {
            width = anchor.getWidth() -  leftInset - rightInset - leftMargin;
            if (firstLine) {
                if (bullet != null){
                    if (indent > 0) width -= indent;
                } else {
                    if (indent > 0) width -= indent; // first line indentation
                    else if (indent < 0) { // hanging indentation: the first line start at the left margin
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

    protected AttributedString getAttributedString(Graphics2D graphics, StringBuilder text){
        List<AttributedStringData> attList = new ArrayList<AttributedStringData>();
        if (text == null) text = new StringBuilder();

        DrawFontManager fontHandler = (DrawFontManager)graphics.getRenderingHint(Drawable.FONT_HANDLER);

        for (TextRun run : paragraph){
            String runText = getRenderableText(run);
            // skip empty runs
            if (runText.isEmpty()) continue;

            int beginIndex = text.length();
            text.append(runText);
            int endIndex = text.length();

            attList.add(new AttributedStringData(TextAttribute.FOREGROUND, run.getFontColor(), beginIndex, endIndex));

            // user can pass an custom object to convert fonts
            String fontFamily = run.getFontFamily();
            @SuppressWarnings("unchecked")
            Map<String,String> fontMap = (Map<String,String>)graphics.getRenderingHint(Drawable.FONT_MAP);
            if (fontMap != null && fontMap.containsKey(fontFamily)) {
                fontFamily = fontMap.get(fontFamily);
            }
            if(fontHandler != null) {
                fontFamily = fontHandler.getRendererableFont(fontFamily, run.getPitchAndFamily());
            }
            attList.add(new AttributedStringData(TextAttribute.FAMILY, fontFamily, beginIndex, endIndex));

            float fontSz = (float)run.getFontSize();
            attList.add(new AttributedStringData(TextAttribute.SIZE, fontSz, beginIndex, endIndex));

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
        }

        // ensure that the paragraph contains at least one character
        // We need this trick to correctly measure text
        if (text.length() == 0) {
            float fontSz = (float)paragraph.getDefaultFontSize();
            text.append(" ");
            attList.add(new AttributedStringData(TextAttribute.SIZE, fontSz, 0, 1));
        }

        AttributedString string = new AttributedString(text.toString());
        for (AttributedStringData asd : attList) {
            string.addAttribute(asd.attribute, asd.value, asd.beginIndex, asd.endIndex);
        }

        return string;
    }


}
