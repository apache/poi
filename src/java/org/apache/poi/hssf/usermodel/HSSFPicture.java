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

package org.apache.poi.hssf.usermodel;

import org.apache.poi.ddf.EscherBSERecord;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.poi.ss.usermodel.Picture;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Iterator;

/**
 * Represents a escher picture.  Eg. A GIF, JPEG etc...
 *
 * @author Glen Stampoultzis
 * @author Yegor Kozlov (yegor at apache.org)
 */
public final class HSSFPicture extends HSSFSimpleShape implements Picture {
    public static final int PICTURE_TYPE_EMF = HSSFWorkbook.PICTURE_TYPE_EMF;                // Windows Enhanced Metafile
    public static final int PICTURE_TYPE_WMF = HSSFWorkbook.PICTURE_TYPE_WMF;                // Windows Metafile
    public static final int PICTURE_TYPE_PICT = HSSFWorkbook.PICTURE_TYPE_PICT;              // Macintosh PICT
    public static final int PICTURE_TYPE_JPEG = HSSFWorkbook.PICTURE_TYPE_JPEG;              // JFIF
    public static final int PICTURE_TYPE_PNG = HSSFWorkbook.PICTURE_TYPE_PNG;                // PNG
    public static final int PICTURE_TYPE_DIB = HSSFWorkbook.PICTURE_TYPE_DIB;                // Windows DIB

    /**
     * width of 1px in columns with default width in units of 1/256 of a character width
     */
    private static final float PX_DEFAULT = 32.00f;
    /**
     * width of 1px in columns with overridden width in units of 1/256 of a character width
     */
    private static final float PX_MODIFIED = 36.56f;

    /**
     * Height of 1px of a row
     */
    private static final int PX_ROW = 15;

    private int _pictureIndex;
    HSSFPatriarch _patriarch;  // TODO make private

    private static final POILogger log = POILogFactory.getLogger(HSSFPicture.class);

    /**
     * Constructs a picture object.
     */
    HSSFPicture( HSSFShape parent, HSSFAnchor anchor )
    {
        super( parent, anchor );
        setShapeType(OBJECT_TYPE_PICTURE);
    }

    public int getPictureIndex()
    {
        return _pictureIndex;
    }

    public void setPictureIndex( int pictureIndex )
    {
        this._pictureIndex = pictureIndex;
    }

    /**
     * Resize the image
     *
     * @param scale the amount by which image dimensions are multiplied relative to the original size.
     * <code>resize(1.0)</code> sets the original size, <code>resize(0.5)</code> resize to 50% of the original,
     * <code>resize(2.0)</code> resizes to 200% of the original.
     */
    public void resize(double scale){
        HSSFClientAnchor anchor = (HSSFClientAnchor)getAnchor();
        anchor.setAnchorType(2);

        HSSFClientAnchor pref = getPreferredSize(scale);

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
     * Reset the image to the original size.
     */
    public void resize(){
        resize(1.0);
    }

    /**
     * Calculate the preferred size for this picture.
     *
     * @return HSSFClientAnchor with the preferred size for this image
     * @since POI 3.0.2
     */
    public HSSFClientAnchor getPreferredSize(){
        return getPreferredSize(1.0);
    }

    /**
     * Calculate the preferred size for this picture.
     *
     * @param scale the amount by which image dimensions are multiplied relative to the original size.
     * @return HSSFClientAnchor with the preferred size for this image
     * @since POI 3.0.2
     */
    public HSSFClientAnchor getPreferredSize(double scale){
        HSSFClientAnchor anchor = (HSSFClientAnchor)getAnchor();

        Dimension size = getImageDimension();
        double scaledWidth = size.getWidth() * scale;
        double scaledHeight = size.getHeight() * scale;

        float w = 0;

        //space in the leftmost cell
        w += getColumnWidthInPixels(anchor.col1)*(1 - (float)anchor.dx1/1024);
        short col2 = (short)(anchor.col1 + 1);
        int dx2 = 0;

        while(w < scaledWidth){
            w += getColumnWidthInPixels(col2++);
        }

        if(w > scaledWidth) {
            //calculate dx2, offset in the rightmost cell
            col2--;
            double cw = getColumnWidthInPixels(col2);
            double delta = w - scaledWidth;
            dx2 = (int)((cw-delta)/cw*1024);
        }
        anchor.col2 = col2;
        anchor.dx2 = dx2;

        float h = 0;
        h += (1 - (float)anchor.dy1/256)* getRowHeightInPixels(anchor.row1);
        int row2 = anchor.row1 + 1;
        int dy2 = 0;

        while(h < scaledHeight){
            h += getRowHeightInPixels(row2++);
        }
        if(h > scaledHeight) {
            row2--;
            double ch = getRowHeightInPixels(row2);
            double delta = h - scaledHeight;
            dy2 = (int)((ch-delta)/ch*256);
        }
        anchor.row2 = row2;
        anchor.dy2 = dy2;

        return anchor;
    }

    private float getColumnWidthInPixels(int column){

        int cw = _patriarch._sheet.getColumnWidth(column);
        float px = getPixelWidth(column);

        return cw/px;
    }

    private float getRowHeightInPixels(int i){

        HSSFRow row = _patriarch._sheet.getRow(i);
        float height;
        if(row != null) height = row.getHeight();
        else height = _patriarch._sheet.getDefaultRowHeight();

        return height/PX_ROW;
    }

    private float getPixelWidth(int column){

        int def = _patriarch._sheet.getDefaultColumnWidth()*256;
        int cw = _patriarch._sheet.getColumnWidth(column);

        return cw == def ? PX_DEFAULT : PX_MODIFIED;
    }

    /**
     * The metadata of PNG and JPEG can contain the width of a pixel in millimeters.
     * Return the the "effective" dpi calculated as <code>25.4/HorizontalPixelSize</code>
     * and <code>25.4/VerticalPixelSize</code>.  Where 25.4 is the number of mm in inch.
     *
     * @return array of two elements: <code>{horisontalPdi, verticalDpi}</code>.
     * {96, 96} is the default.
     */
    protected int[] getResolution(ImageReader r) throws IOException {
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
     * Return the dimension of this image
     *
     * @return image dimension
     */
    public Dimension getImageDimension(){
        EscherBSERecord bse = _patriarch._sheet._book.getBSERecord(_pictureIndex);
        byte[] data = bse.getBlipRecord().getPicturedata();
        int type = bse.getBlipTypeWin32();
        Dimension size = new Dimension();

        switch (type){
            //we can calculate the preferred size only for JPEG and PNG
            //other formats like WMF, EMF and PICT are not supported in Java
            case HSSFWorkbook.PICTURE_TYPE_JPEG:
            case HSSFWorkbook.PICTURE_TYPE_PNG:
            case HSSFWorkbook.PICTURE_TYPE_DIB:
                try {
                    //read the image using javax.imageio.*
                    ImageInputStream iis = ImageIO.createImageInputStream( new ByteArrayInputStream(data) );
                    Iterator<ImageReader> i = ImageIO.getImageReaders( iis );
                    ImageReader r = i.next();
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
                    log.log(POILogger.WARN, e);
                }

                break;
        }
        return size;
    }
}
