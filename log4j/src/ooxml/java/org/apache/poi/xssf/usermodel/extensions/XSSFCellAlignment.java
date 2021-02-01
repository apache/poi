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
package org.apache.poi.xssf.usermodel.extensions;

import java.math.BigInteger;

import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.ReadingOrder;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.util.Internal;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCellAlignment;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STHorizontalAlignment;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STVerticalAlignment;


/**
 * Cell settings available in the Format/Alignment tab
 */
public class XSSFCellAlignment {
    private final CTCellAlignment cellAlignement;

    /**
     * Creates a Cell Alignment from the supplied XML definition
     *
     * @param cellAlignment The low-level XML definition of the cell alignment
     */
    public XSSFCellAlignment(CTCellAlignment cellAlignment) {
        this.cellAlignement = cellAlignment;
    }

    /**
     * Get the type of vertical alignment for the cell
     *
     * @return the type of aligment
     * @see VerticalAlignment
     */
    public VerticalAlignment getVertical() {
        STVerticalAlignment.Enum align = cellAlignement.getVertical();
        if (align == null) align = STVerticalAlignment.BOTTOM;

        return VerticalAlignment.values()[align.intValue() - 1];
    }

    /**
     * Set the type of vertical alignment for the cell
     *
     * @param align - the type of alignment
     * @see VerticalAlignment
     */
    public void setVertical(VerticalAlignment align) {
        cellAlignement.setVertical(STVerticalAlignment.Enum.forInt(align.ordinal() + 1));
    }

    /**
     * Get the type of horizontal alignment for the cell
     *
     * @return the type of aligment
     * @see HorizontalAlignment
     */
    public HorizontalAlignment getHorizontal() {
        STHorizontalAlignment.Enum align = cellAlignement.getHorizontal();
        if (align == null) align = STHorizontalAlignment.GENERAL;

        return HorizontalAlignment.values()[align.intValue() - 1];
    }

    /**
     * Set the type of horizontal alignment for the cell
     *
     * @param align - the type of alignment
     * @see HorizontalAlignment
     */
    public void setHorizontal(HorizontalAlignment align) {
        cellAlignement.setHorizontal(STHorizontalAlignment.Enum.forInt(align.ordinal() + 1));
    }

    /**
     * Set the type of reading order for the cell
     *
     * @param order - the type of reading order
     * @see ReadingOrder
     */
    public void setReadingOrder(ReadingOrder order) {
        cellAlignement.setReadingOrder(order.getCode());
    }

    /**
     * Get the reading order for the cell
     *
     * @return the value of reading order
     * @see ReadingOrder
     */
    public ReadingOrder getReadingOrder() {
        if(cellAlignement != null && cellAlignement.isSetReadingOrder()) {
            return ReadingOrder.forLong(cellAlignement.getReadingOrder());
        }
        return ReadingOrder.CONTEXT;
    }

    /**
     * Get the number of spaces to indent the text in the cell
     *
     * @return indent - number of spaces
     */
    public long getIndent() {
        return cellAlignement.getIndent();
    }

    /**
     * Set the number of spaces to indent the text in the cell
     *
     * @param indent - number of spaces
     */
    public void setIndent(long indent) {
        cellAlignement.setIndent(indent);
    }

    /**
     * Get the degree of rotation for the text in the cell
     * <p>
     * Expressed in degrees. Values range from 0 to 180. The first letter of
     * the text is considered the center-point of the arc.
     * <br>
     * For 0 - 90, the value represents degrees above horizon. For 91-180 the degrees below the
     * horizon is calculated as:
     * <br>
     * <code>[degrees below horizon] = 90 - textRotation.</code>
     * </p>
     *
     * @return rotation degrees (between 0 and 180 degrees)
     */
    public long getTextRotation() {
        return cellAlignement.isSetTextRotation() ? cellAlignement.getTextRotation().longValue() : 0;
    }

    /**
     * Set the degree of rotation for the text in the cell
     * <p>
     * Expressed in degrees. Values range from 0 to 180. The first letter of
     * the text is considered the center-point of the arc.
     * <br>
     * For 0 - 90, the value represents degrees above horizon. For 91-180 the degrees below the
     * horizon is calculated as:
     * <br>
     * <code>[degrees below horizon] = 90 - textRotation.</code>
     * </p>
     *
     * Note: HSSF uses values from -90 to 90 degrees, whereas XSSF
     * uses values from 0 to 180 degrees. The implementations of this method will map between these two value-ranges
     * accordingly, however the corresponding getter is returning values in the range mandated by the current type
     * of Excel file-format that this CellStyle is applied to.
     *
     * @param rotation - the rotation degrees (between 0 and 180 degrees)
     */
    public void setTextRotation(long rotation) {
        if(rotation < 0 && rotation >= -90) {
            rotation = 90 + ((-1)*rotation);
        }
        cellAlignement.setTextRotation(BigInteger.valueOf(rotation));
    }

    /**
     * Whether the text should be wrapped
     *
     * @return a boolean value indicating if the text in a cell should be line-wrapped within the cell.
     */
    public boolean getWrapText() {
        return cellAlignement.getWrapText();
    }

    /**
     * Set whether the text should be wrapped
     *
     * @param wrapped a boolean value indicating if the text in a cell should be line-wrapped within the cell.
     */
    public void setWrapText(boolean wrapped) {
        cellAlignement.setWrapText(wrapped);
    }

    public boolean getShrinkToFit() {
    	return cellAlignement.getShrinkToFit();
    }

    public void setShrinkToFit(boolean shrink) {
    	cellAlignement.setShrinkToFit(shrink);
    }

    /**
     * Access to low-level data
     */
    @Internal
    public CTCellAlignment getCTCellAlignment() {
        return cellAlignement;
    }
}
