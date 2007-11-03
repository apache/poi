/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.apache.poi.hssf.usermodel;

import org.apache.poi.ddf.EscherBSERecord;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Iterator;

/**
 * Represents a escher picture.  Eg. A GIF, JPEG etc...
 *
 * @author Glen Stampoultzis
 * @author Yegor Kozlov (yegor at apache.org)
 */
public class HSSFPicture
        extends HSSFSimpleShape
{
    public static final int PICTURE_TYPE_EMF = HSSFWorkbook.PICTURE_TYPE_EMF;                // Windows Enhanced Metafile
    public static final int PICTURE_TYPE_WMF = HSSFWorkbook.PICTURE_TYPE_WMF;                // Windows Metafile
    public static final int PICTURE_TYPE_PICT = HSSFWorkbook.PICTURE_TYPE_PICT;              // Macintosh PICT
    public static final int PICTURE_TYPE_JPEG = HSSFWorkbook.PICTURE_TYPE_JPEG;              // JFIF
    public static final int PICTURE_TYPE_PNG = HSSFWorkbook.PICTURE_TYPE_PNG;                // PNG
    public static final int PICTURE_TYPE_DIB = HSSFWorkbook.PICTURE_TYPE_DIB;                // Windows DIB

    int pictureIndex;
    HSSFPatriarch patriarch;

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
        return pictureIndex;
    }

    public void setPictureIndex( int pictureIndex )
    {
        this.pictureIndex = pictureIndex;
    }

    /**
     * Reset the image to the original size.
     *
     * @since POI 3.0.2
     */
    public void resize(){
        HSSFClientAnchor anchor = (HSSFClientAnchor)getAnchor();
        anchor.setAnchorType(2);

        HSSFClientAnchor pref = getPreferredSize();

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
     * @return HSSFClientAnchor with the preferred size for this image
     * @since POI 3.0.2
     */
    public HSSFClientAnchor getPreferredSize(){
        HSSFClientAnchor anchor = new HSSFClientAnchor();

        EscherBSERecord bse = (EscherBSERecord)patriarch.sheet.book.getBSERecord(pictureIndex);
        byte[] data = bse.getBlipRecord().getPicturedata();
        int type = bse.getBlipTypeWin32();
        switch (type){
            //we can calculate the preferred size only for JPEG and PNG
            //other formats like WMF, EMF and PICT are not supported in Java
            case HSSFWorkbook.PICTURE_TYPE_JPEG:
            case HSSFWorkbook.PICTURE_TYPE_PNG:
                BufferedImage img = null;
                ImageReader r = null;
                try {
                    //read the image using javax.imageio.*
                    ImageInputStream iis = ImageIO.createImageInputStream( new ByteArrayInputStream(data) );
                    Iterator i = ImageIO.getImageReaders( iis );
                    r = (ImageReader) i.next();
                    r.setInput( iis );
                    img = r.read(0);

                    int[] dpi = getResolution(r);
                    int imgWidth = img.getWidth()*96/dpi[0];
                    int imgHeight = img.getHeight()*96/dpi[1];

                    //Excel measures cells in units of 1/256th of a character width.
                    //The cell width calculated based on this info is always "off".
                    //A better approach seems to be to use empirically obtained cell width and row height
                    int cellwidth = 64;
                    int rowheight = 17;

                    int col2 = imgWidth/cellwidth;
                    int row2 = imgHeight/rowheight;

                    int dx2 = (int)((float)(imgWidth % cellwidth)/cellwidth * 1024);
                    int dy2 = (int)((float)(imgHeight % rowheight)/rowheight * 256);

                    anchor.setCol2((short)col2);
                    anchor.setDx2(dx2);

                    anchor.setRow2(row2);
                    anchor.setDy2(dy2);

                } catch (IOException e){
                    //silently return if ImageIO failed to read the image
                    log.log(POILogger.WARN, e);
                    img = null;
                }

                break;
        }
        return anchor;
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

}
