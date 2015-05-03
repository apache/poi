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

package org.apache.poi.sl.usermodel;

import java.awt.Color;


public interface TextParagraph<T extends TextRun> extends Iterable<T> {

    /**
     * Specifies a list of text alignment types
     */
    public enum TextAlign {
        /**
         * For horizontal text, left aligned.
         * For vertical text, top aligned.
         */
        LEFT,

        /**
         * For horizontal text, centered.
         * For vertical text, middle aligned.
         */
        CENTER,

        /**
         * For horizontal text, right aligned.
         * For vertical text, bottom aligned.
         */
        RIGHT,

        /**
         * Align text so that it is justified across the whole line. It
         * is smart in the sense that it will not justify sentences
         * which are short
         * 
         * For horizontal text, flush left and right.
         * For vertical text, flush top and bottom.
         */
        JUSTIFY,
        
        /**
         * Kashida justify low.
         */    
        JUSTIFY_LOW,

        /**
         * Distribute space between characters.
         */
        DIST,
        
        /**
         * Thai distribution justification.
         */
        THAI_DIST
    }

    /**
     * 
     */
    public enum FontAlign {
        AUTO,
        
        /**
         * Characters hang from top of line height.
         * Also known as "Hanging"
         */
        TOP,
        
        /**
         * Characters centered within line height.
         */
        CENTER,
        
        /**
         * Place characters on font baseline.
         * Also known as "Roman" 
         */
        BASELINE,
        
        /**
         * Characters are anchored to the very bottom of a single line.
         * This is different than BASELINE because of letters such as "g", "q", and "y".
         * Also known as "UpholdFixed"
         */
        BOTTOM; 
    }
    
    public interface BulletStyle {
        String getBulletCharacter();
        String getBulletFont();
        double getBulletFontSize();
        Color getBulletFontColor();
    }
    
    /**
     * The amount of vertical white space before the paragraph
     * This may be specified in two different ways, percentage spacing and font point spacing:
     * <p>
     * If spaceBefore >= 0, then space is a percentage of normal line height.
     * If spaceBefore < 0, the absolute value in points
     * </p>
     *
     * @return the vertical white space before the paragraph
     */
    double getSpaceBefore();
    
    /**
     * The amount of vertical white space after the paragraph
     * This may be specified in two different ways, percentage spacing and font point spacing:
     * <p>
     * If spaceBefore >= 0, then space is a percentage of normal line height.
     * If spaceBefore < 0, the absolute value of linespacing is the spacing in points
     * </p>
     *
     * @return the vertical white space after the paragraph
     */
    double getSpaceAfter();    

    /**
     * @return the left margin (in points) of the paragraph
     */
    double getLeftMargin();

    /**
     * @param leftMargin the left margin (in points) 
     */
    void setLeftMargin(double leftMargin);
    
    
    /**
     * @return the right margin (in points) of the paragraph
     */
    double getRightMargin();

    /**
     * @param rightMargin the right margin (in points) of the paragraph
     */
    void setRightMargin(double rightMargin);
    
    /**
     * @return the indent (in points) applied to the first line of text in the paragraph.
     */
    double getIndent();

    /**
     * @param indent the indent (in points) applied to the first line of text in the paragraph
     */
    void setIndent(double indent);
    
    /**
     * Returns the vertical line spacing that is to be used within a paragraph.
     * This may be specified in two different ways, percentage spacing and font point spacing:
     * <p>
     * If linespacing >= 0, then linespacing is a percentage of normal line height.
     * If linespacing < 0, the absolute value of linespacing is the spacing in points
     * </p>
     *
     * @return the vertical line spacing.
     */
    double getLineSpacing();
    
    /**
     * This element specifies the vertical line spacing that is to be used within a paragraph.
     * This may be specified in two different ways, percentage spacing and font point spacing:
     * <p>
     * If linespacing >= 0, then linespacing is a percentage of normal line height
     * If linespacing < 0, the absolute value of linespacing is the spacing in points
     * </p>
     * Examples:
     * <pre><code>
     *      // spacing will be 120% of the size of the largest text on each line
     *      paragraph.setLineSpacing(120);
     *
     *      // spacing will be 200% of the size of the largest text on each line
     *      paragraph.setLineSpacing(200);
     *
     *      // spacing will be 48 points
     *      paragraph.setLineSpacing(-48.0);
     * </code></pre>
     * 
     * @param linespacing the vertical line spacing
     */
    void setLineSpacing(double lineSpacing);

    String getDefaultFontFamily();
    
    /**
     * @return the default font size, in case its not set in the textrun
     */
    double getDefaultFontSize();
    
    /**
     * Returns the alignment that is applied to the paragraph.
     *
     * If this attribute is omitted, then a value of left is implied.
     * @return ??? alignment that is applied to the paragraph
     */
    TextAlign getTextAlign();
    
    
    /**
     * Returns the font alignment that is applied to the paragraph.
     *
     * If this attribute is omitted, then a value of auto (~ left) is implied.
     * @return ??? alignment that is applied to the paragraph
     */
    FontAlign getFontAlign();
    
    /**
     * @return the bullet style of the paragraph, if {@code null} then no bullets are used 
     */
    BulletStyle getBulletStyle();
    
    TextShape<? extends TextParagraph<T>> getParentShape();
}
