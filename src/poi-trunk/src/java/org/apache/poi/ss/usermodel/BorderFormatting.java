/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */

package org.apache.poi.ss.usermodel;

import org.apache.poi.util.Removal;

/**
 * High level representation for Border Formatting component
 * of Conditional Formatting settings
 */
public interface BorderFormatting {

    /** @since POI 4.0.0 */
    BorderStyle getBorderBottom();

    /** @since POI 4.0.0 */
    BorderStyle getBorderDiagonal();

    /** @since POI 4.0.0 */
    BorderStyle getBorderLeft();

    /** @since POI 4.0.0 */
    BorderStyle getBorderRight();

    /** @since POI 4.0.0 */
    BorderStyle getBorderTop();

    /**
     * Only valid for range borders, such as table styles
     * @since 4.0.0
     * @return border style
     */
    BorderStyle getBorderVertical();
    /**
     * Only valid for range borders, such as table styles
     * @since 4.0.0
     * @return border style
     */
    BorderStyle getBorderHorizontal();

    /**
     * @since POI 3.15
     * @deprecated use <code>getBorderBottom</code> instead
     */
    @Removal(version = "4.2")
    @Deprecated
    BorderStyle getBorderBottomEnum();

    /**
     * @since POI 3.15
     * @deprecated use <code>getBorderDiagonal</code> instead
     */
    @Removal(version = "4.2")
    @Deprecated
    BorderStyle getBorderDiagonalEnum();

    /**
     * @since POI 3.15
     * @deprecated use <code>getBorderLeft</code> instead
     */
    @Removal(version = "4.2")
    @Deprecated
    BorderStyle getBorderLeftEnum();

    /**
     * @since POI 3.15
     * @deprecated use <code>getBorderRight</code> instead
     */
    @Removal(version = "4.2")
    @Deprecated
    BorderStyle getBorderRightEnum();

    /**
     * @since POI 3.15
     * @deprecated use <code>getBorderTop</code> instead
     */
    @Removal(version = "4.2")
    @Deprecated
    BorderStyle getBorderTopEnum();

    /**
     * Only valid for range borders, such as table styles
     * @since 3.17 beta 1
     * @return border style
     * @deprecated use <code>getBorderVertical</code> instead
     */
    @Removal(version = "4.2")
    @Deprecated
    BorderStyle getBorderVerticalEnum();

    /**
     * Only valid for range borders, such as table styles
     * @since 3.17 beta 1
     * @return border style
     * @deprecated use <code>getBorderHorizontal</code> instead
     */
    @Removal(version = "4.2")
    @Deprecated
    BorderStyle getBorderHorizontalEnum();
    
    short getBottomBorderColor();
    Color getBottomBorderColorColor();

    short getDiagonalBorderColor();
    Color getDiagonalBorderColorColor();

    short getLeftBorderColor();
    Color getLeftBorderColorColor();

    short getRightBorderColor();
    Color getRightBorderColorColor();

    short getTopBorderColor();
    Color getTopBorderColorColor();

    /**
     * Range internal borders. Only relevant for range styles, such as table formatting
     * @since  3.17 beta 1
     * @return color index
     */
    short getVerticalBorderColor();
    /**
     * Range internal borders. Only relevant for range styles, such as table formatting
     * @since  3.17 beta 1
     * @return color
     */
    Color getVerticalBorderColorColor();
    
    /**
     * Range internal borders. Only relevant for range styles, such as table formatting
     * @since  3.17 beta 1
     * @return color index
     */
    short getHorizontalBorderColor();
    /**
     * Range internal borders. Only relevant for range styles, such as table formatting
     * @since  3.17 beta 1
     * @return color
     */
    Color getHorizontalBorderColorColor();
    
    /**
     * Set bottom border.
     *
     * @param border The style of border to set.
     */
    void setBorderBottom(BorderStyle border);
    
    /**
     * Set diagonal border.
     *
     * @param border The style of border to set.
     */
    void setBorderDiagonal(BorderStyle border);

    /**
     * Set left border.
     *
     * @param border The style of border to set.
     */
    void setBorderLeft(BorderStyle border);

    /**
     * Set right border.
     *
     * @param border The style of border to set.
     */
    void setBorderRight(BorderStyle border);

    /**
     * Set top border.
     *
     * @param border The style of border to set.
     */
    void setBorderTop(BorderStyle border);
    
    /**
     * Set range internal horizontal borders.
     *
     * @since 3.17 beta 1
     * @param border The style of border to set.
     */
    void setBorderHorizontal(BorderStyle border);
    
    /**
     * Set range internal vertical borders.
     *
     * @since 3.17 beta 1
     * @param border The style of border to set.
     */
    void setBorderVertical(BorderStyle border);

    void setBottomBorderColor(short color);
    void setBottomBorderColor(Color color);

    void setDiagonalBorderColor(short color);
    void setDiagonalBorderColor(Color color);

    void setLeftBorderColor(short color);
    void setLeftBorderColor(Color color);

    void setRightBorderColor(short color);
    void setRightBorderColor(Color color);

    void setTopBorderColor(short color);
    void setTopBorderColor(Color color);
    
    /**
     * Range internal border color, such as table styles
     * @since 3.17 beta 1
     * @param color index
     */
    void setHorizontalBorderColor(short color);
    /**
     * Range internal border color, such as table styles
     * @since 3.17 beta 1
     * @param color index
     */
    void setHorizontalBorderColor(Color color);
    
    /**
     * Range internal border color, such as table styles
     * @since 3.17 beta 1
     * @param color index
     */
    void setVerticalBorderColor(short color);
    /**
     * Range internal border color, such as table styles
     * @since 3.17 beta 1
     * @param color index
     */
    void setVerticalBorderColor(Color color);
}
