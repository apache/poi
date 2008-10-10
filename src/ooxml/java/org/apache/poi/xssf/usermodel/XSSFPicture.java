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

import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.*;
import org.openxmlformats.schemas.drawingml.x2006.main.*;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.POILogger;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.POIXMLDocumentPart;
import org.openxml4j.opc.PackageRelationship;
import org.openxml4j.opc.PackagePart;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Iterator;

/**
 * Represents a picture shape in a SpreadsheetML drawing.
 *
 * @author Yegor Kozlov
 */
public class XSSFPicture extends XSSFShape {
    private static final POILogger logger = POILogFactory.getLogger(XSSFPicture.class);

    /**
     * width of 1px in columns with default width
     */
    private static final float PX_DEFAULT = 0.125f;
    /**
     * width of 1px in columns with overridden width
     */
    private static final float PX_MODIFIED = 0.143f;

    /**
     * Height of 1px of a row
     */
    private static final int PX_ROW = 15;

    /**
     * This object specifies a picture object and all its properties
     */
    private CTPicture ctPicture;

    /**
     * Construct a new XSSFPicture object. This constructor is called from
     *  {@link XSSFDrawing#createPicture(XSSFClientAnchor, int)}
     *
     * @param parent the XSSFDrawing that owns this picture
     * @param rel    the relationship to the picture data
     * @param anchor the two cell anchor placeholder for this picture,
     *   this object encloses the CTPicture bean that holds all the picture properties
     */
    protected XSSFPicture(XSSFDrawing parent, PackageRelationship rel, CTTwoCellAnchor anchor){
        super(parent, anchor);
        //Create a new picture and attach it to the specified two-cell anchor
        ctPicture = newPicture(rel);
        anchor.setPic(ctPicture);
    }

    /**
     * Create a new CTPicture bean and initialize its required attributes
     *
     * @param rel the relationship to the picture data
     * @return a new CTPicture bean
     */
    private static CTPicture newPicture(PackageRelationship rel){
        CTPicture pic = CTPicture.Factory.newInstance();

        CTPictureNonVisual nvpr = pic.addNewNvPicPr();
        CTNonVisualDrawingProps nvProps = nvpr.addNewCNvPr();
        //YK: TODO shape IDs must be unique across workbook
        int shapeId = 1;
        nvProps.setId(shapeId);
        nvProps.setName("Picture " + shapeId);
        nvProps.setDescr(rel.getTargetURI().toString());
        CTNonVisualPictureProperties nvPicProps = nvpr.addNewCNvPicPr();
        nvPicProps.addNewPicLocks().setNoChangeAspect(true);

        CTBlipFillProperties blip = pic.addNewBlipFill();
        blip.addNewBlip().setEmbed(rel.getId());
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
        return pic;
    }

    /**
     * Return the underlying CTPicture bean that holds all properties for this picture
     *
     * @return the underlying CTPicture bean
     */
    public CTPicture getCTPicture(){
        return ctPicture;
    }

    /**
     * Reset the image to the original size.
     */
    public void resize(){
        XSSFClientAnchor anchor = getAnchor();

        XSSFClientAnchor pref = getPreferredSize();

        int row2 = anchor.getRow1() + (pref.getRow2() - pref.getRow1());
        int col2 = anchor.getCol1() + (pref.getCol2() - pref.getCol1());

        anchor.setCol2((short)col2);
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
        XSSFClientAnchor anchor = getAnchor();

        XSSFPictureData data = getPictureData();
        Dimension size = getImageDimension(data.getPackagePart(), data.getPictureType());

        float w = 0;

        //space in the leftmost cell
        w += anchor.getDx1()/EMU_PER_POINT;
        short col2 = (short)(anchor.getCol1() + 1);
        int dx2 = 0;

        while(w < size.width){
            w += getColumnWidthInPixels(col2++);
        }

        if(w > size.width) {
            //calculate dx2, offset in the rightmost cell
            col2--;
            float cw = getColumnWidthInPixels(col2);
            float delta = w - size.width;
            dx2 = (int)(EMU_PER_POINT*(cw-delta));
        }
        anchor.setCol2(col2);
        anchor.setDx2(dx2);

        float h = 0;
        h += (1 - anchor.getDy1()/256)* getRowHeightInPixels(anchor.getRow1());
        int row2 = anchor.getRow1() + 1;
        int dy2 = 0;

        while(h < size.height){
            h += getRowHeightInPixels(row2++);
        }
        if(h > size.height) {
            row2--;
            float ch = getRowHeightInPixels(row2);
            float delta = h - size.height;
            dy2 = (int)((ch-delta)/ch*256);
        }
        anchor.setRow2(row2);
        anchor.setDy2(dy2);

        return anchor;
    }

    private float getColumnWidthInPixels(int column){
        XSSFSheet sheet = (XSSFSheet)getDrawing().getParent();
        int cw = sheet.getColumnWidth(column);
        float px = getPixelWidth(column);

        return cw/px;
    }

    private float getRowHeightInPixels(int i){
        XSSFSheet sheet = (XSSFSheet)getDrawing().getParent();

        XSSFRow row = sheet.getRow(i);
        float height;
        if(row != null) height = row.getHeight();
        else height = sheet.getDefaultRowHeight();

        return height/PX_ROW;
    }

    private float getPixelWidth(int column){
        XSSFSheet sheet = (XSSFSheet)getDrawing().getParent();

        int def = sheet.getDefaultColumnWidth();
        int cw = sheet.getColumnWidth(column);

        return cw == def ? PX_DEFAULT : PX_MODIFIED;
    }

    /**
     * Return the dimension of this image
     *
     * @param part the package part holding raw picture data
     * @param type type of the picture: {@link Workbook#PICTURE_TYPE_JPEG, Workbook#PICTURE_TYPE_PNG or Workbook#PICTURE_TYPE_DIB)
     *
     * @return image dimension in pixels
     */
    protected static Dimension getImageDimension(PackagePart part, int type){
        Dimension size = new Dimension();

        switch (type){
            //we can calculate the preferred size only for JPEG and PNG
            //other formats like WMF, EMF and PICT are not supported in Java
            case Workbook.PICTURE_TYPE_JPEG:
            case Workbook.PICTURE_TYPE_PNG:
            case Workbook.PICTURE_TYPE_DIB:
                try {
                    //read the image using javax.imageio.*
                    ImageInputStream iis = ImageIO.createImageInputStream( part.getInputStream() );
                    Iterator i = ImageIO.getImageReaders( iis );
                    ImageReader r = (ImageReader) i.next();
                    r.setInput( iis );
                    BufferedImage img = r.read(0);

                    int[] dpi = getResolution(r);

                    //if DPI is zero then assume standard 96 DPI
                    //since cannot divide by zero
                    if (dpi[0] == 0) dpi[0] = 96;
                    if (dpi[1] == 0) dpi[1] = 96;

                    size.width = img.getWidth()*96/dpi[0];
                    size.height = img.getHeight()*96/dpi[1];

                } catch (IOException e){
                    //silently return if ImageIO failed to read the image
                    logger.log(POILogger.WARN, e);
                }

                break;
            default:
                logger.log(POILogger.WARN, "Only JPEG, PNG and DIB pictures can be automatically sized");
        }
        return size;
    }

    /**
     * The metadata of PNG and JPEG can contain the width of a pixel in millimeters.
     * Return the the "effective" dpi calculated as <code>25.4/HorizontalPixelSize</code>
     * and <code>25.4/VerticalPixelSize</code>.  Where 25.4 is the number of mm in inch.
     *
     * @return array of two elements: <code>{horisontalPdi, verticalDpi}</code>.
     * {96, 96} is the default.
     */
    protected static int[] getResolution(ImageReader r) throws IOException {
        int hdpi=96, vdpi=96;
        double mm2inch = 25.4;

        NodeList lst;
        Element node = (Element)r.getImageMetadata(0).getAsTree("javax_imageio_1.0");
        lst = node.getElementsByTagName("HorizontalPixelSize");
        if(lst != null && lst.getLength() == 1) hdpi = (int)(mm2inch/Float.parseFloat(((Element)lst.item(0)).getAttribute("value")));

        lst = node.getElementsByTagName("VerticalPixelSize");
        if(lst != null && lst.getLength() == 1) vdpi = (int)(mm2inch/Float.parseFloat(((Element)lst.item(0)).getAttribute("value")));

        return new int[]{hdpi, vdpi};
    }

    /**
     * return the anchor that is used by this shape.
     *
     * @return  the anchor that is used by this shape.
     */
    public XSSFClientAnchor getAnchor(){
        CTTwoCellAnchor ctAnchor = (CTTwoCellAnchor)getShapeContainer();
        return new XSSFClientAnchor(ctAnchor.getFrom(), ctAnchor.getTo());
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

}
