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

/**
 * High level representation for Border Formatting component
 * of Conditional Formatting settings
 */
public interface BorderFormatting {

    /**
     * @deprecated POI 3.15. Use {@link #getBorderBottomEnum()}.
     * This method will return an BorderStyle enum in the future.
     */
    short getBorderBottom();
    /** @since POI 3.15 */
    BorderStyle getBorderBottomEnum();

    /**
     * @deprecated POI 3.15. Use {@link #getBorderDiagonalEnum()}.
     * This method will return an BorderStyle enum in the future.
     */
    short getBorderDiagonal();
    /** @since POI 3.15 */
    BorderStyle getBorderDiagonalEnum();

    /**
     * @deprecated POI 3.15. Use {@link #getBorderLeftEnum()}.
     * This method will return an BorderStyle enum in the future.
     */
    short getBorderLeft();
    /** @since POI 3.15 */
    BorderStyle getBorderLeftEnum();

    /**
     * @deprecated POI 3.15. Use {@link #getBorderRightEnum()}.
     * This method will return an BorderStyle enum in the future.
     */
    short getBorderRight();
    /** @since POI 3.15 */
    BorderStyle getBorderRightEnum();

    /**
     * @deprecated POI 3.15. Use {@link #getBorderTopEnum()}.
     * This method will return an BorderStyle enum in the future.
     */
    short getBorderTop();
    /** @since POI 3.15 */
    BorderStyle getBorderTopEnum();

    /**
     * Only valid for range borders, such as table styles
     * @since 3.17 beta 1
     * @return border style
     */
    BorderStyle getBorderVerticalEnum();
    /**
     * Only valid for range borders, such as table styles
     * @since 3.17 beta 1
     * @return border style
     */
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
     * @param border  MUST be a BORDER_* constant
     * @deprecated 3.15 beta 2. Use {@link BorderFormatting#setBorderBottom(BorderStyle)}
     */
    void setBorderBottom(short border);

    /**
     * Set bottom border.
     *
     * @param border
     */
    void setBorderBottom(BorderStyle border);
    
    /**
     * Set diagonal border.
     *
     * @param border  MUST be a BORDER_* constant
     * @deprecated 3.15 beta 2. Use {@link BorderFormatting#setBorderDiagonal(BorderStyle)}
     */
    void setBorderDiagonal(short border);
    
    /**
     * Set diagonal border.
     *
     * @param border
     */
    void setBorderDiagonal(BorderStyle border);

    /**
     * Set left border.
     *
     * @param border  MUST be a BORDER_* constant
     * @deprecated 3.15 beta 2. Use {@link BorderFormatting#setBorderLeft(BorderStyle)}
     */
    void setBorderLeft(short border);
    
    /**
     * Set left border.
     *
     * @param border
     */
    void setBorderLeft(BorderStyle border);

    /**
     * Set right border.
     *
     * @param border  MUST be a BORDER_* constant
     * @deprecated 3.15 beta 2. Use {@link BorderFormatting#setBorderRight(BorderStyle)}
     */
    void setBorderRight(short border);
    
    /**
     * Set right border.
     *
     * @param border
     */
    void setBorderRight(BorderStyle border);

    /**
     * Set top border.
     *
     * @param border  MUST be a BORDER_* constant
     * @deprecated 3.15 beta 2. Use {@link BorderFormatting#setBorderTop(BorderStyle)}
     */
    void setBorderTop(short border);
    
    /**
     * Set top border.
     *
     * @param border
     */
    void setBorderTop(BorderStyle border);
    
    /**
     * Set range internal horizontal borders.
     *
     * @since 3.17 beta 1
     * @param border
     */
    void setBorderHorizontal(BorderStyle border);
    
    /**
     * Set range internal vertical borders.
     *
     * @since 3.17 beta 1
     * @param border
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
