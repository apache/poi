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
import java.io.IOException;

import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.ImageUtils;
import org.apache.poi.util.Internal;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
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
     * This value is the same for default font in Office 2007 (Calibri) and Office 2003 and earlier (Arial)
     */
    // private static float DEFAULT_COLUMN_WIDTH = 9.140625f;

    /**
     * A default instance of CTShape used for creating new shapes.
     */
    private static CTPicture prototype;

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
     * Reset the image to the dimension of the embedded image
     *
     * @see #resize(double, double)
     */
    public void resize(){
        resize(Double.MAX_VALUE);
    }

    /**
     * Resize the image proportionally.
     *
     * @see #resize(double, double)
     */
    public void resize(double scale) {
        resize(scale, scale);
    }
    
    /**
     * Resize the image relatively to its current size.
     * <p>
     * Please note, that this method works correctly only for workbooks
     * with the default font size (Calibri 11pt for .xlsx).
     * If the default font is changed the resized image can be streched vertically or horizontally.
     * </p>
     * <p>
     * <code>resize(1.0,1.0)</code> keeps the original size,<br>
     * <code>resize(0.5,0.5)</code> resize to 50% of the original,<br>
     * <code>resize(2.0,2.0)</code> resizes to 200% of the original.<br>
     * <code>resize({@link Double#MAX_VALUE},{@link Double#MAX_VALUE})</code> resizes to the dimension of the embedded image. 
     * </p>
     *
     * @param scaleX the amount by which the image width is multiplied relative to the original width,
     *  when set to {@link java.lang.Double#MAX_VALUE} the width of the embedded image is used
     * @param scaleY the amount by which the image height is multiplied relative to the original height,
     *  when set to {@link java.lang.Double#MAX_VALUE} the height of the embedded image is used
     */
    public void resize(double scaleX, double scaleY){
        XSSFClientAnchor anchor = getClientAnchor();
        XSSFClientAnchor pref = getPreferredSize(scaleX,scaleY);
        if (anchor == null || pref == null) {
            logger.log(POILogger.WARN, "picture is not anchored via client anchor - ignoring resize call");
            return;
        }

        int row2 = anchor.getRow1() + (pref.getRow2() - pref.getRow1());
        int col2 = anchor.getCol1() + (pref.getCol2() - pref.getCol1());

        anchor.setCol2(col2);
        // anchor.setDx1(0);
        anchor.setDx2(pref.getDx2());

        anchor.setRow2(row2);
        // anchor.setDy1(0);
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
        return getPreferredSize(scale, scale);
    }
    
    /**
     * Calculate the preferred size for this picture.
     *
     * @param scaleX the amount by which image width is multiplied relative to the original width.
     * @param scaleY the amount by which image height is multiplied relative to the original height.
     * @return XSSFClientAnchor with the preferred size for this image
     */
    public XSSFClientAnchor getPreferredSize(double scaleX, double scaleY){
        Dimension dim = ImageUtils.setPreferredSize(this, scaleX, scaleY);
        CTPositiveSize2D size2d =  ctPicture.getSpPr().getXfrm().getExt();
        size2d.setCx((int)dim.getWidth());
        size2d.setCy((int)dim.getHeight());
        return getClientAnchor();
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
     * Return the dimension of the embedded image in pixel
     *
     * @return image dimension in pixels
     */
    public Dimension getImageDimension() {
        XSSFPictureData picData = getPictureData();
        return getImageDimension(picData.getPackagePart(), picData.getPictureType());
    }
    
    /**
     * Return picture data for this shape
     *
     * @return picture data for this shape
     */
    public XSSFPictureData getPictureData() {
        String blipId = ctPicture.getBlipFill().getBlip().getEmbed();
        return  (XSSFPictureData)getDrawing().getRelationById(blipId);
    }

    protected CTShapeProperties getShapeProperties(){
        return ctPicture.getSpPr();
    }

    /**
     * @return the anchor that is used by this shape.
     */
    @Override
    public XSSFClientAnchor getClientAnchor() {
        XSSFAnchor a = getAnchor();
        return (a instanceof XSSFClientAnchor) ? (XSSFClientAnchor)a : null;
    }

    /**
     * @return the sheet which contains the picture shape
     */
    @Override
    public XSSFSheet getSheet() {
        return (XSSFSheet)getDrawing().getParent();
    }

    @Override
    public String getShapeName() {
        return ctPicture.getNvPicPr().getCNvPr().getName();
    }
}
