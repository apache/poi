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
import java.util.function.Consumer;
import java.util.function.Function;

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

public final class ImageUtils {
    private static final POILogger logger = POILogFactory.getLogger(ImageUtils.class);

    private static final int WIDTH_UNITS = 1024;
    private static final int HEIGHT_UNITS = 256;

    private ImageUtils() {}

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
                                if (dpi[0] == 0) dpi[0] = Units.PIXEL_DPI;
                                if (dpi[1] == 0) dpi[1] = Units.PIXEL_DPI;

                                size.width = img.getWidth()*Units.PIXEL_DPI/dpi[0];
                                size.height = img.getHeight()*Units.PIXEL_DPI/dpi[1];
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
     * @return array of two elements: <code>{horizontalDpi, verticalDpi}</code>.
     * {96, 96} is the default.
     */
    public static int[] getResolution(ImageReader r) throws IOException {
        int hdpi=96, vdpi=96;
        double mm2inch = 25.4;

        NodeList lst;
        Element node = (Element)r.getImageMetadata(0).getAsTree("javax_imageio_1.0");
        lst = node.getElementsByTagName("HorizontalPixelSize");
        if(lst != null && lst.getLength() == 1) {
            hdpi = (int)(mm2inch/Float.parseFloat(((Element)lst.item(0)).getAttribute("value")));
        }

        lst = node.getElementsByTagName("VerticalPixelSize");
        if(lst != null && lst.getLength() == 1) {
            vdpi = (int)(mm2inch/Float.parseFloat(((Element)lst.item(0)).getAttribute("value")));
        }

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
        final Dimension imgSize = (scaleX == Double.MAX_VALUE || scaleY == Double.MAX_VALUE)
            ? getImageDimension(new ByteArrayInputStream(data.getData()), data.getPictureType())
            : new Dimension();

        // in emus
        final Dimension anchorSize = (scaleX != Double.MAX_VALUE || scaleY != Double.MAX_VALUE)
            ? ImageUtils.getDimensionFromAnchor(picture)
            : new Dimension();

        final double scaledWidth = (scaleX == Double.MAX_VALUE)
            ? imgSize.getWidth() : anchorSize.getWidth()/EMU_PER_PIXEL * scaleX;
        final double scaledHeight = (scaleY == Double.MAX_VALUE)
            ? imgSize.getHeight() : anchorSize.getHeight()/EMU_PER_PIXEL * scaleY;

        scaleCell(scaledWidth, anchor.getCol1(), anchor.getDx1(), anchor::setCol2, anchor::setDx2,
             isHSSF ? WIDTH_UNITS : 0, sheet::getColumnWidthInPixels);

        scaleCell(scaledHeight, anchor.getRow1(), anchor.getDy1(), anchor::setRow2, anchor::setDy2,
                  isHSSF ? HEIGHT_UNITS : 0, (row) -> getRowHeightInPixels(sheet, row));

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

        // default to image size (in pixel), if the anchor is only specified for Col1/Row1
        Dimension imgSize = null;
        if (anchor.getCol2() < anchor.getCol1() || anchor.getRow2() < anchor.getRow1()) {
            PictureData data = picture.getPictureData();
            imgSize = getImageDimension(new ByteArrayInputStream(data.getData()), data.getPictureType());
        }

        int w = getDimFromCell(imgSize == null ? 0 : imgSize.getWidth(), anchor.getCol1(), anchor.getDx1(), anchor.getCol2(), anchor.getDx2(),
            isHSSF ? WIDTH_UNITS : 0, sheet::getColumnWidthInPixels);

        int h = getDimFromCell(imgSize == null ? 0 : imgSize.getHeight(), anchor.getRow1(), anchor.getDy1(), anchor.getRow2(), anchor.getDy2(),
                               isHSSF ? HEIGHT_UNITS : 0, (row) -> getRowHeightInPixels(sheet, row));

        return new Dimension(w, h);
    }


    public static double getRowHeightInPixels(Sheet sheet, int rowNum) {
        Row r = sheet.getRow(rowNum);
        double points = (r == null) ? sheet.getDefaultRowHeightInPoints() : r.getHeightInPoints();
        return Units.toEMU(points)/(double)EMU_PER_PIXEL;
    }

    private static void scaleCell(final double targetSize,
                                  final int startCell,
                                  final int startD,
                                  Consumer<Integer> endCell,
                                  Consumer<Integer> endD,
                                  final int hssfUnits,
                                  Function<Integer,Number> nextSize) {
        if (targetSize < 0) {
            throw new IllegalArgumentException("target size < 0");
        }

        int cellIdx = startCell;
        double dim, delta;
        for (double totalDim = 0, remDim;; cellIdx++, totalDim += remDim) {
            dim = nextSize.apply(cellIdx).doubleValue();
            remDim = dim;
            if (cellIdx == startCell) {
                if (hssfUnits > 0) {
                    remDim *= 1 - startD/(double)hssfUnits;
                } else {
                    remDim -= startD/(double)EMU_PER_PIXEL;
                }
            }
            delta = targetSize - totalDim;
            if (delta < remDim) {
                break;
            }
        }

        double endDval;
        if (hssfUnits > 0) {
            endDval = delta/dim * (double)hssfUnits;
        } else {
            endDval = delta * EMU_PER_PIXEL;
        }
        if (cellIdx == startCell) {
            endDval += startD;
        }

        endCell.accept(cellIdx);
        endD.accept((int)Math.rint(endDval));
    }

    private static int getDimFromCell(double imgSize, int startCell, int startD, int endCell, int endD, int hssfUnits,
         Function<Integer,Number> nextSize) {
        double targetSize;
        if (endCell < startCell) {
            targetSize = imgSize * EMU_PER_PIXEL;
        } else {
            targetSize = 0;
            for (int cellIdx = startCell; cellIdx<=endCell; cellIdx++) {
                final double dim = nextSize.apply(cellIdx).doubleValue() * EMU_PER_PIXEL;
                double leadSpace = 0;
                if (cellIdx == startCell) {
                    //space in the leftmost cell
                    leadSpace = (hssfUnits > 0)
                        ? dim * startD/(double)hssfUnits
                        : startD;
                }

                double trailSpace = 0;
                if (cellIdx == endCell) {
                    // space after the rightmost cell
                    trailSpace = (hssfUnits > 0)
                        ? dim * (hssfUnits-endD)/(double)hssfUnits
                        : dim - endD;
                }
                targetSize += dim - leadSpace - trailSpace;
            }
        }

        return (int)Math.rint(targetSize);
    }
}
