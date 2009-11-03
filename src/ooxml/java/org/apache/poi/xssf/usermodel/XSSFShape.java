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

package org.apache.poi.xssf.usermodel;

import org.openxmlformats.schemas.drawingml.x2006.main.CTLineProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTNoFillProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPresetLineDashProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTSRgbColor;
import org.openxmlformats.schemas.drawingml.x2006.main.CTShapeProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTSolidColorFillProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.STPresetLineDashVal;

/**
 * Represents a shape in a SpreadsheetML drawing.
 *
 * @author Yegor Kozlov
 */
public abstract class XSSFShape {
    public static final int EMU_PER_PIXEL = 9525;
    public static final int EMU_PER_POINT = 12700;

    public static final int POINT_DPI = 72;
    public static final int PIXEL_DPI = 96;

    /**
     * Parent drawing
     */
    protected XSSFDrawing drawing;

    /**
     * The parent shape, always not-null for shapes in groups
     */
    protected XSSFShapeGroup parent;

    /**
     * anchor that is used by this shape
     */
    protected XSSFAnchor anchor;

    /**
     * Return the drawing that owns this shape
     *
     * @return the parent drawing that owns this shape
     */
    public XSSFDrawing getDrawing(){
        return drawing;
    }

    /**
     * Gets the parent shape.
     */
    public XSSFShapeGroup getParent()
    {
        return parent;
    }

    /**
     * @return  the anchor that is used by this shape.
     */
    public XSSFAnchor getAnchor()
    {
        return anchor;
    }

    /**
     * Returns xml bean with shape properties.
     *
     * @return xml bean with shape properties.
     */
    protected abstract CTShapeProperties getShapeProperties();

    /**
     * Whether this shape is not filled with a color
     *
     * @return true if this shape is not filled with a color.
     */
    public boolean isNoFill() {
        return getShapeProperties().isSetNoFill();
    }

    /**
     * Sets whether this shape is filled or transparent.
     *
     * @param noFill if true then no fill will be applied to the shape element.
     */
    public void setNoFill(boolean noFill) {
        CTShapeProperties props = getShapeProperties();
        //unset solid and pattern fills if they are set
        if (props.isSetPattFill()) props.unsetPattFill();
        if (props.isSetSolidFill()) props.unsetSolidFill();

        props.setNoFill(CTNoFillProperties.Factory.newInstance());
    }

    /**
     * Sets the color used to fill this shape using the solid fill pattern.
     */
    public void setFillColor(int red, int green, int blue) {
        CTShapeProperties props = getShapeProperties();
        CTSolidColorFillProperties fill = props.isSetSolidFill() ? props.getSolidFill() : props.addNewSolidFill();
        CTSRgbColor rgb = CTSRgbColor.Factory.newInstance();
        rgb.setVal(new byte[]{(byte)red, (byte)green, (byte)blue});
        fill.setSrgbClr(rgb);
    }

    /**
     * The color applied to the lines of this shape.
     */
    public void setLineStyleColor( int red, int green, int blue ) {
        CTShapeProperties props = getShapeProperties();
        CTLineProperties ln = props.isSetLn() ? props.getLn() : props.addNewLn();
        CTSolidColorFillProperties fill = ln.isSetSolidFill() ? ln.getSolidFill() : ln.addNewSolidFill();
        CTSRgbColor rgb = CTSRgbColor.Factory.newInstance();
        rgb.setVal(new byte[]{(byte)red, (byte)green, (byte)blue});
        fill.setSrgbClr(rgb);
    }

    /**
     * Specifies the width to be used for the underline stroke.
     *
     * @param lineWidth width in points
     */
    public void setLineWidth( double lineWidth ) {
        CTShapeProperties props = getShapeProperties();
        CTLineProperties ln = props.isSetLn() ? props.getLn() : props.addNewLn();
        ln.setW((int)(lineWidth*EMU_PER_POINT));
    }

    /**
     * Sets the line style.
     *
     * @param lineStyle
     */
    public void setLineStyle( int lineStyle ) {
        CTShapeProperties props = getShapeProperties();
        CTLineProperties ln = props.isSetLn() ? props.getLn() : props.addNewLn();
        CTPresetLineDashProperties dashStyle = CTPresetLineDashProperties.Factory.newInstance();
        dashStyle.setVal(STPresetLineDashVal.Enum.forInt(lineStyle+1));
        ln.setPrstDash(dashStyle);
    }

}
