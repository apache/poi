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

import java.util.List;

public interface TextShape<
    S extends Shape<S,P>,
    P extends TextParagraph<S,P,?>
> extends SimpleShape<S,P>, Iterable<P>  {
    /**
     * Vertical Text Types
     */
    public enum TextDirection {
        /**
         * Horizontal text. This should be default.
         */
        HORIZONTAL,
        /**
         * Vertical orientation.
         * (each line is 90 degrees rotated clockwise, so it goes
         * from top to bottom; each next line is to the left from
         * the previous one).
         */
        VERTICAL,
        /**
         * Vertical orientation.
         * (each line is 270 degrees rotated clockwise, so it goes
         * from bottom to top; each next line is to the right from
         * the previous one).
         */
        VERTICAL_270,
        /**
         * Determines if all of the text is vertical
         * ("one letter on top of another").
         */
        STACKED;
    }

    /**
     * Specifies alist of auto-fit types.
     * <p>
     * Autofit specofies that a shape should be auto-fit to fully contain the text described within it.
     * Auto-fitting is when text within a shape is scaled in order to contain all the text inside
     * </p>
     */
    public enum TextAutofit {
        /**
         * Specifies that text within the text body should not be auto-fit to the bounding box.
         * Auto-fitting is when text within a text box is scaled in order to remain inside
         * the text box.
         */
        NONE,
        /**
         * Specifies that text within the text body should be normally auto-fit to the bounding box.
         * Autofitting is when text within a text box is scaled in order to remain inside the text box.
         *
         * <p>
         * <em>Example:</em> Consider the situation where a user is building a diagram and needs
         * to have the text for each shape that they are using stay within the bounds of the shape.
         * An easy way this might be done is by using NORMAL autofit
         * </p>
         */
        NORMAL,
        /**
         * Specifies that a shape should be auto-fit to fully contain the text described within it.
         * Auto-fitting is when text within a shape is scaled in order to contain all the text inside.
         *
         * <p>
         * <em>Example:</em> Consider the situation where a user is building a diagram and needs to have
         * the text for each shape that they are using stay within the bounds of the shape.
         * An easy way this might be done is by using SHAPE autofit
         * </p>
         */
        SHAPE
    }
    
    /**
     * @return the TextParagraphs for this text box
     */
    List<? extends TextParagraph<S,P,?>> getTextParagraphs();

    /**
     * @return text shape margin
     */
    Insets2D getInsets();

    /**
     * Compute the cumulative height occupied by the text
     */
    double getTextHeight();

    /**
     * Returns the type of vertical alignment for the text.
     *
     * @return the type of vertical alignment
     */
    VerticalAlignment getVerticalAlignment();

    /**
     * Returns if the text is centered.
     * If true and if the individual paragraph settings allow it,
     * the whole text block will be displayed centered, i.e. its left and right
     * margin will be maximized while still keeping the alignment of the paragraphs
     *
     * @return true, if the text anchor is horizontal centered
     */
    boolean isHorizontalCentered();

    /**
     * @return whether to wrap words within the bounding rectangle
     */
    boolean getWordWrap();

    /**
     * @return vertical orientation of the text
     */
    TextDirection getTextDirection();
}
