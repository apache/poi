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

package org.apache.poi.xssf.streaming;

import java.awt.Dimension;
import java.io.IOException;

import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Shape;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.ImageUtils;
import org.apache.poi.util.Internal;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.poi.util.Units;
import org.apache.poi.xssf.usermodel.XSSFAnchor;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFPicture;
import org.apache.poi.xssf.usermodel.XSSFPictureData;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPositiveSize2D;
import org.openxmlformats.schemas.drawingml.x2006.main.CTShapeProperties;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTPicture;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCol;

/**
 * Streaming version of Picture.
 * Most of the code is a copy of the non-streaming XSSFPicture code.
 * This is necessary as a private method getRowHeightInPixels of that class needs to be changed, which is called by a method call chain nested several levels.
 *
 * The main change is to access the rows in the SXSSF sheet, not the always empty rows in the XSSF sheet when checking the row heights.
 */
public final class SXSSFPicture implements Picture {
    private static final POILogger logger = POILogFactory.getLogger(SXSSFPicture.class);
    /**
     * Column width measured as the number of characters of the maximum digit width of the
     * numbers 0, 1, 2, ..., 9 as rendered in the normal style's font. There are 4 pixels of margin
     * padding (two on each side), plus 1 pixel padding for the gridlines.
     *
     * This value is the same for default font in Office 2007 (Calibri) and Office 2003 and earlier (Arial)
     */
    private static float DEFAULT_COLUMN_WIDTH = 9.140625f;

    private final SXSSFWorkbook _wb;
    private final XSSFPicture _picture;

    SXSSFPicture(SXSSFWorkbook _wb, XSSFPicture _picture) {
        this._wb = _wb;
        this._picture = _picture;
    }

    /**
     * Return the underlying CTPicture bean that holds all properties for this picture
     *
     * @return the underlying CTPicture bean
     */
    @Internal
    public CTPicture getCTPicture(){
        return _picture.getCTPicture();
    }

    /**
     * Reset the image to the original size.
     *
     * <p>
     * Please note, that this method works correctly only for workbooks
     * with the default font size (Calibri 11pt for .xlsx).
     * If the default font is changed the resized image can be streched vertically or horizontally.
     * </p>
     */
    @Override
    public void resize(){
        resize(1.0);
    }

    /**
     * Reset the image to the original size.
     * <p>
     * Please note, that this method works correctly only for workbooks
     * with the default font size (Calibri 11pt for .xlsx).
     * If the default font is changed the resized image can be streched vertically or horizontally.
     * </p>
     *
     * @param scale the amount by which image dimensions are multiplied relative to the original size.
     * <code>resize(1.0)</code> sets the original size, <code>resize(0.5)</code> resize to 50% of the original,
     * <code>resize(2.0)</code> resizes to 200% of the original.
     */
    @Override
    public void resize(double scale){
        XSSFClientAnchor anchor = getClientAnchor();
        XSSFClientAnchor pref = getPreferredSize(scale);
        if (anchor == null || pref == null) {
            logger.log(POILogger.WARN, "picture is not anchored via client anchor - ignoring resize call");
            return;
        }

        int row2 = anchor.getRow1() + (pref.getRow2() - pref.getRow1());
        int col2 = anchor.getCol1() + (pref.getCol2() - pref.getCol1());

        anchor.setCol2(col2);
        anchor.setDx1(0);
        anchor.setDx2(pref.getDx2());

        anchor.setRow2(row2);
        anchor.setDy1(0);
        anchor.setDy2(pref.getDy2());
    }

    /**
     * Calculate the preferred size for this picture.
     *
     * @return XSSFClientAnchor with the preferred size for this image
     */
    @Override
    public XSSFClientAnchor getPreferredSize(){
        return getPreferredSize(1.0);
    }

    /**
     * Calculate the preferred size for this picture.
     *
     * @param scale the amount by which image dimensions are multiplied relative to the original size.
     * @return XSSFClientAnchor with the preferred size for this image
     */
    public XSSFClientAnchor getPreferredSize(double scale){
        XSSFClientAnchor anchor = getClientAnchor();
        if (anchor == null) {
            logger.log(POILogger.WARN, "picture is not anchored via client anchor - ignoring resize call");
            return null;
        }

        XSSFPictureData data = getPictureData();
        Dimension size = getImageDimension(data.getPackagePart(), data.getPictureType());
        double scaledWidth = size.getWidth() * scale;
        double scaledHeight = size.getHeight() * scale;

        float w = 0;
        int col2 = anchor.getCol1()-1;

        while (w <= scaledWidth) {
            w += getColumnWidthInPixels(++col2);
        }

        assert (w > scaledWidth);
        double cw = getColumnWidthInPixels(col2);
        double deltaW = w - scaledWidth;
        int dx2 = (int)(Units.EMU_PER_PIXEL * (cw - deltaW));

        anchor.setCol2(col2);
        anchor.setDx2(dx2);

        double h = 0;
        int row2 = anchor.getRow1()-1;

        while (h <= scaledHeight) {
            h += getRowHeightInPixels(++row2);
        }

        assert (h > scaledHeight);
        double ch = getRowHeightInPixels(row2);
        double deltaH = h - scaledHeight;
        int dy2 = (int)(Units.EMU_PER_PIXEL * (ch - deltaH));
        anchor.setRow2(row2);
        anchor.setDy2(dy2);

        CTPositiveSize2D size2d =  getCTPicture().getSpPr().getXfrm().getExt();
        size2d.setCx((long)(scaledWidth * Units.EMU_PER_PIXEL));
        size2d.setCy((long)(scaledHeight * Units.EMU_PER_PIXEL));

        return anchor;
    }

    private float getColumnWidthInPixels(int columnIndex){
        XSSFSheet sheet = getSheet();

        CTCol col = sheet.getColumnHelper().getColumn(columnIndex, false);
        double numChars = col == null || !col.isSetWidth() ? DEFAULT_COLUMN_WIDTH : col.getWidth();

        return (float)numChars*Units.DEFAULT_CHARACTER_WIDTH;
    }

    private float getRowHeightInPixels(int rowIndex) {
        // THE FOLLOWING THREE LINES ARE THE MAIN CHANGE compared to the non-streaming version: use the SXSSF sheet,
		// not the XSSF sheet (which never contais rows when using SXSSF)
        XSSFSheet xssfSheet = getSheet();
        SXSSFSheet sheet = _wb.getSXSSFSheet(xssfSheet);
        Row row = sheet.getRow(rowIndex);
        float height = row != null ?  row.getHeightInPoints() : sheet.getDefaultRowHeightInPoints();
        return height * Units.PIXEL_DPI / Units.POINT_DPI;
    }
    /**
     * Return the dimension of this image
     *
     * @param part the package part holding raw picture data
     * @param type type of the picture: {@link Workbook#PICTURE_TYPE_JPEG},
     * {@link Workbook#PICTURE_TYPE_PNG} or {@link Workbook#PICTURE_TYPE_DIB}
     *
     * @return image dimension in pixels
     */
    protected static Dimension getImageDimension(PackagePart part, int type){
        try {
            return ImageUtils.getImageDimension(part.getInputStream(), type);
        } catch (IOException e){
            //return a "singulariry" if ImageIO failed to read the image
            logger.log(POILogger.WARN, e);
            return new Dimension();
        }
    }

    /**
     * Return picture data for this shape
     *
     * @return picture data for this shape
     */
    @Override
    public XSSFPictureData getPictureData() {
        return _picture.getPictureData();
    }

    protected CTShapeProperties getShapeProperties(){
        return getCTPicture().getSpPr();
    }

    @Override
    public XSSFAnchor getAnchor() {
        return _picture.getAnchor();
    }

    @Override
    public void resize(double scaleX, double scaleY) {
        _picture.resize(scaleX, scaleY);
    }

    @Override
    public XSSFClientAnchor getPreferredSize(double scaleX, double scaleY) {
        return _picture.getPreferredSize(scaleX, scaleY);
    }

    @Override
    public Dimension getImageDimension() {
        return _picture.getImageDimension();
    }

    @Override
    public XSSFClientAnchor getClientAnchor() {
        XSSFAnchor a = getAnchor();
        return (a instanceof XSSFClientAnchor) ? (XSSFClientAnchor)a : null;
    }
    
    public XSSFDrawing getDrawing() {
        return _picture.getDrawing();
    }

    @Override
    public XSSFSheet getSheet() {
        return _picture.getSheet();
    }

    @Override
    public String getShapeName() {
        return _picture.getShapeName();
    }

    @Override
    public Shape getParent() {
        return _picture.getParent();
    }

    @Override
    public boolean isNoFill() {
        return _picture.isNoFill();
    }

    @Override
    public void setNoFill(boolean noFill) {
        _picture.setNoFill(noFill);
    }

    @Override
    public void setFillColor(int red, int green, int blue) {
        _picture.setFillColor(red, green, blue);
    }
    
    @Override
    public void setLineStyleColor( int red, int green, int blue ) {
        _picture.setLineStyleColor(red, green, blue);
    }
}
