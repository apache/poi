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

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.apache.poi.POIXMLDocumentPart;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.ImageUtils;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.poi.util.Internal;
import org.openxmlformats.schemas.drawingml.x2006.main.CTBlipFillProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTNonVisualDrawingProps;
import org.openxmlformats.schemas.drawingml.x2006.main.CTNonVisualPictureProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPoint2D;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPositiveSize2D;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPresetGeometry2D;
import org.openxmlformats.schemas.drawingml.x2006.main.CTShapeProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTransform2D;
import org.openxmlformats.schemas.drawingml.x2006.main.STShapeType;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTPicture;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTPictureNonVisual;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCol;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Represents a picture shape in a SpreadsheetML drawing.
 *
 * @author Yegor Kozlov
 */
public final class XSSFPicture extends XSSFShape implements Picture {
    private static final POILogger logger = POILogFactory.getLogger(XSSFPicture.class);

    /**
     * Column width measured as the number of characters of the maximum digit width of the
     * numbers 0, 1, 2, ..., 9 as rendered in the normal style's font. There are 4 pixels of margin
     * padding (two on each side), plus 1 pixel padding for the gridlines.
     *
     * This value is the same for default font in Office 2007 (Calibry) and Office 2003 and earlier (Arial)
     */
    private static float DEFAULT_COLUMN_WIDTH = 9.140625f;

    /**
     * A default instance of CTShape used for creating new shapes.
     */
    private static CTPicture prototype = null;

    /**
     * This object specifies a picture object and all its properties
     */
    private CTPicture ctPicture;

    /**
     * Construct a new XSSFPicture object. This constructor is called from
     *  {@link XSSFDrawing#createPicture(XSSFClientAnchor, int)}
     *
     * @param drawing the XSSFDrawing that owns this picture
     */
    protected XSSFPicture(XSSFDrawing drawing, CTPicture ctPicture){
        this.drawing = drawing;
        this.ctPicture = ctPicture;
    }

    /**
     * Returns a prototype that is used to construct new shapes
     *
     * @return a prototype that is used to construct new shapes
     */
    protected static CTPicture prototype(){
        if(prototype == null) {
            CTPicture pic = CTPicture.Factory.newInstance();
            CTPictureNonVisual nvpr = pic.addNewNvPicPr();
            CTNonVisualDrawingProps nvProps = nvpr.addNewCNvPr();
            nvProps.setId(1);
            nvProps.setName("Picture 1");
            nvProps.setDescr("Picture");
            CTNonVisualPictureProperties nvPicProps = nvpr.addNewCNvPicPr();
            nvPicProps.addNewPicLocks().setNoChangeAspect(true);

            CTBlipFillProperties blip = pic.addNewBlipFill();
            blip.addNewBlip().setEmbed("");
            blip.addNewStretch().addNewFillRect();

            CTShapeProperties sppr = pic.addNewSpPr();
            CTTransform2D t2d = sppr.addNewXfrm();
            CTPositiveSize2D ext = t2d.addNewExt();
            //should be original picture width and height expressed in EMUs
            ext.setCx(0);
            ext.setCy(0);

            CTPoint2D off = t2d.addNewOff();
            off.setX(0);
            off.setY(0);

            CTPresetGeometry2D prstGeom = sppr.addNewPrstGeom();
            prstGeom.setPrst(STShapeType.RECT);
            prstGeom.addNewAvLst();

            prototype = pic;
        }
        return prototype;
    }

    /**
     * Link this shape with the picture data
     *
     * @param rel relationship referring the picture data
     */
    protected void setPictureReference(PackageRelationship rel){
        ctPicture.getBlipFill().getBlip().setEmbed(rel.getId());
    }

    /**
     * Return the underlying CTPicture bean that holds all properties for this picture
     *
     * @return the underlying CTPicture bean
     */
    @Internal
    public CTPicture getCTPicture(){
        return ctPicture;
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
    public void resize(double scale){
        XSSFClientAnchor anchor = (XSSFClientAnchor)getAnchor();

        XSSFClientAnchor pref = getPreferredSize(scale);

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
    public XSSFClientAnchor getPreferredSize(){
        return getPreferredSize(1);
    }

    /**
     * Calculate the preferred size for this picture.
     *
     * @param scale the amount by which image dimensions are multiplied relative to the original size.
     * @return XSSFClientAnchor with the preferred size for this image
     */
    public XSSFClientAnchor getPreferredSize(double scale){
        XSSFClientAnchor anchor = (XSSFClientAnchor)getAnchor();

        XSSFPictureData data = getPictureData();
        Dimension size = getImageDimension(data.getPackagePart(), data.getPictureType());
        double scaledWidth = size.getWidth() * scale;
        double scaledHeight = size.getHeight() * scale;

        float w = 0;
        int col2 = anchor.getCol1();
        int dx2 = 0;
        if(anchor.getDx1() > 0){
            w += getColumnWidthInPixels(col2) - anchor.getDx1();
            col2++;
        }

        for (;;) {
            w += getColumnWidthInPixels(col2);
            if(w > scaledWidth) break;
            col2++;
        }

        if(w > scaledWidth) {
            double cw = getColumnWidthInPixels(col2 + 1);
            double delta = w - scaledWidth;
            dx2 = (int)(EMU_PER_PIXEL*(cw-delta));
        }
        anchor.setCol2(col2);
        anchor.setDx2(dx2);

        double h = 0;
        int row2 = anchor.getRow1();
        int dy2 = 0;

        if(anchor.getDy1() > 0){
            h += getRowHeightInPixels(row2) - anchor.getDy1();
            row2++;
        }

        for (;;) {
            h += getRowHeightInPixels(row2);
            if(h > scaledHeight) break;
            row2++;
        }

        if(h > scaledHeight) {
            double ch = getRowHeightInPixels(row2 + 1);
            double delta = h - scaledHeight;
            dy2 = (int)(EMU_PER_PIXEL*(ch-delta));
        }
        anchor.setRow2(row2);
        anchor.setDy2(dy2);

        CTPositiveSize2D size2d =  ctPicture.getSpPr().getXfrm().getExt();
        size2d.setCx((long)(scaledWidth*EMU_PER_PIXEL));
        size2d.setCy((long)(scaledHeight*EMU_PER_PIXEL));

        return anchor;
    }

    private float getColumnWidthInPixels(int columnIndex){
        XSSFSheet sheet = (XSSFSheet)getDrawing().getParent();

        CTCol col = sheet.getColumnHelper().getColumn(columnIndex, false);
        double numChars = col == null || !col.isSetWidth() ? DEFAULT_COLUMN_WIDTH : col.getWidth();

        return (float)numChars*XSSFWorkbook.DEFAULT_CHARACTER_WIDTH;
    }

    private float getRowHeightInPixels(int rowIndex){
        XSSFSheet sheet = (XSSFSheet)getDrawing().getParent();

        XSSFRow row = sheet.getRow(rowIndex);
        float height = row != null ?  row.getHeightInPoints() : sheet.getDefaultRowHeightInPoints();
        return height*PIXEL_DPI/POINT_DPI;
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
    public XSSFPictureData getPictureData() {
        String blipId = ctPicture.getBlipFill().getBlip().getEmbed();
        for (POIXMLDocumentPart part : getDrawing().getRelations()) {
            if(part.getPackageRelationship().getId().equals(blipId)){
                return (XSSFPictureData)part;
            }
        }
        logger.log(POILogger.WARN, "Picture data was not found for blipId=" + blipId);
        return null;
    }

    protected CTShapeProperties getShapeProperties(){
        return ctPicture.getSpPr();
    }

}
