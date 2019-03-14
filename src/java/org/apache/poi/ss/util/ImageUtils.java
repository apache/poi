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
package org.apache.poi.ss.util;

import static org.apache.poi.util.Units.EMU_PER_PIXEL;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.apache.poi.hssf.usermodel.HSSFClientAnchor;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.usermodel.PictureData;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.poi.util.Units;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * @author Yegor Kozlov
 */
public class ImageUtils {
    private static final POILogger logger = POILogFactory.getLogger(ImageUtils.class);

    public static final int PIXEL_DPI = 96;

    /**
     * Return the dimension of this image
     *
     * @param is the stream containing the image data
     * @param type type of the picture: {@link org.apache.poi.ss.usermodel.Workbook#PICTURE_TYPE_JPEG},
     * {@link org.apache.poi.ss.usermodel.Workbook#PICTURE_TYPE_PNG} or {@link org.apache.poi.ss.usermodel.Workbook#PICTURE_TYPE_DIB}
     *
     * @return image dimension in pixels
     */
    public static Dimension getImageDimension(InputStream is, int type) {
        Dimension size = new Dimension();

        switch (type){
            //we can calculate the preferred size only for JPEG, PNG and BMP
            //other formats like WMF, EMF and PICT are not supported in Java
            case Workbook.PICTURE_TYPE_JPEG:
            case Workbook.PICTURE_TYPE_PNG:
            case Workbook.PICTURE_TYPE_DIB:
                try {
                    //read the image using javax.imageio.*
                    try (ImageInputStream iis = ImageIO.createImageInputStream(is)) {
                        Iterator<ImageReader> i = ImageIO.getImageReaders( iis );
                        if (i.hasNext()) {
                            ImageReader r = i.next();
                            try {
                                r.setInput( iis );
                                BufferedImage img = r.read(0);

                                int[] dpi = getResolution(r);

                                //if DPI is zero then assume standard 96 DPI
                                //since cannot divide by zero
                                if (dpi[0] == 0) dpi[0] = PIXEL_DPI;
                                if (dpi[1] == 0) dpi[1] = PIXEL_DPI;

                                size.width = img.getWidth()*PIXEL_DPI/dpi[0];
                                size.height = img.getHeight()*PIXEL_DPI/dpi[1];
                            } finally {
                                r.dispose();
                            }
                        } else {
                            logger.log(POILogger.WARN, "ImageIO found no images");
                        }
                    }

                } catch (IOException e) {
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
    public static int[] getResolution(ImageReader r) throws IOException {
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
     * Calculate and set the preferred size (anchor) for this picture.
     *
     * @param scaleX the amount by which image width is multiplied relative to the original width.
     * @param scaleY the amount by which image height is multiplied relative to the original height.
     * @return the new Dimensions of the scaled picture in EMUs
     */
    public static Dimension setPreferredSize(Picture picture, double scaleX, double scaleY){
        ClientAnchor anchor = picture.getClientAnchor();
        boolean isHSSF = (anchor instanceof HSSFClientAnchor);
        PictureData data = picture.getPictureData();
        Sheet sheet = picture.getSheet();
        
        // in pixel
        Dimension imgSize = getImageDimension(new ByteArrayInputStream(data.getData()), data.getPictureType());
        // in emus
        Dimension anchorSize = ImageUtils.getDimensionFromAnchor(picture);
        final double scaledWidth = (scaleX == Double.MAX_VALUE)
            ? imgSize.getWidth() : anchorSize.getWidth()/EMU_PER_PIXEL * scaleX;
        final double scaledHeight = (scaleY == Double.MAX_VALUE)
            ? imgSize.getHeight() : anchorSize.getHeight()/EMU_PER_PIXEL * scaleY;

        double w = 0;
        int col2 = anchor.getCol1();
        int dx2 = 0;

        //space in the leftmost cell
        w = sheet.getColumnWidthInPixels(col2++);
        if (isHSSF) {
            w *= 1d - anchor.getDx1()/1024d;
        } else {
            w -= anchor.getDx1()/(double)EMU_PER_PIXEL;
        }
        
        while(w < scaledWidth){
            w += sheet.getColumnWidthInPixels(col2++);
        }
        
        if(w > scaledWidth) {
            //calculate dx2, offset in the rightmost cell
            double cw = sheet.getColumnWidthInPixels(--col2);
            double delta = w - scaledWidth;
            if (isHSSF) {
                dx2 = (int)((cw-delta)/cw*1024);
            } else {
                dx2 = (int)((cw-delta)*EMU_PER_PIXEL);
            }
            if (dx2 < 0) dx2 = 0;
        }
        anchor.setCol2(col2);
        anchor.setDx2(dx2);

        double h = 0;
        int row2 = anchor.getRow1();
        int dy2 = 0;
        
        h = getRowHeightInPixels(sheet,row2++);
        if (isHSSF) {
            h *= 1 - anchor.getDy1()/256d;
        } else {
            h -= anchor.getDy1()/(double)EMU_PER_PIXEL;
        }

        while(h < scaledHeight){
            h += getRowHeightInPixels(sheet,row2++);
        }
        
        if(h > scaledHeight) {
            double ch = getRowHeightInPixels(sheet,--row2);
            double delta = h - scaledHeight;
            if (isHSSF) {
                dy2 = (int)((ch-delta)/ch*256);
            } else {
                dy2 = (int)((ch-delta)*EMU_PER_PIXEL);
            }
            if (dy2 < 0) dy2 = 0;
        }

        anchor.setRow2(row2);
        anchor.setDy2(dy2);

        return new Dimension(
            (int)Math.round(scaledWidth*EMU_PER_PIXEL),
            (int)Math.round(scaledHeight*EMU_PER_PIXEL)
        );
    }

    /**
     * Calculates the dimensions in EMUs for the anchor of the given picture
     *
     * @param picture the picture containing the anchor
     * @return the dimensions in EMUs
     */
    public static Dimension getDimensionFromAnchor(Picture picture) {
        ClientAnchor anchor = picture.getClientAnchor();
        boolean isHSSF = (anchor instanceof HSSFClientAnchor);
        Sheet sheet = picture.getSheet();

        double w = 0;
        int col2 = anchor.getCol1();

        //space in the leftmost cell
        w = sheet.getColumnWidthInPixels(col2++);
        if (isHSSF) {
            w *= 1 - anchor.getDx1()/1024d;
        } else {
            w -= anchor.getDx1()/(double)EMU_PER_PIXEL;
        }
        
        while(col2 < anchor.getCol2()){
            w += sheet.getColumnWidthInPixels(col2++);
        }
        
        if (isHSSF) {
            w += sheet.getColumnWidthInPixels(col2) * anchor.getDx2()/1024d;
        } else {
            w += anchor.getDx2()/(double)EMU_PER_PIXEL;
        }

        double h = 0;
        int row2 = anchor.getRow1();
        
        h = getRowHeightInPixels(sheet,row2++);
        if (isHSSF) {
            h *= 1 - anchor.getDy1()/256d;
        } else {
            h -= anchor.getDy1()/(double)EMU_PER_PIXEL;
        }

        while(row2 < anchor.getRow2()){
            h += getRowHeightInPixels(sheet,row2++);
        }
        
        if (isHSSF) {
            h += getRowHeightInPixels(sheet,row2) * anchor.getDy2()/256;
        } else {
            h += anchor.getDy2()/(double)EMU_PER_PIXEL;
        }

        w *= EMU_PER_PIXEL;
        h *= EMU_PER_PIXEL;
        
        return new Dimension((int)Math.rint(w), (int)Math.rint(h));
    }
    
    
    public static double getRowHeightInPixels(Sheet sheet, int rowNum) {
        Row r = sheet.getRow(rowNum);
        double points = (r == null) ? sheet.getDefaultRowHeightInPoints() : r.getHeightInPoints();
        return Units.toEMU(points)/(double)EMU_PER_PIXEL;
    }
}
