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

import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.POILogger;
import org.apache.poi.util.POILogFactory;

import javax.imageio.ImageReader;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Iterator;

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
    public static Dimension getImageDimension(InputStream is, int type){
        Dimension size = new Dimension();

        switch (type){
            //we can calculate the preferred size only for JPEG, PNG and BMP
            //other formats like WMF, EMF and PICT are not supported in Java
            case Workbook.PICTURE_TYPE_JPEG:
            case Workbook.PICTURE_TYPE_PNG:
            case Workbook.PICTURE_TYPE_DIB:
                try {
                    //read the image using javax.imageio.*
                    ImageInputStream iis = ImageIO.createImageInputStream( is );
                    Iterator i = ImageIO.getImageReaders( iis );
                    ImageReader r = (ImageReader) i.next();
                    r.setInput( iis );
                    BufferedImage img = r.read(0);

                    int[] dpi = getResolution(r);

                    //if DPI is zero then assume standard 96 DPI
                    //since cannot divide by zero
                    if (dpi[0] == 0) dpi[0] = PIXEL_DPI;
                    if (dpi[1] == 0) dpi[1] = PIXEL_DPI;

                    size.width = img.getWidth()*PIXEL_DPI/dpi[0];
                    size.height = img.getHeight()*PIXEL_DPI/dpi[1];

                    r.dispose();
                    iis.close();

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

}
