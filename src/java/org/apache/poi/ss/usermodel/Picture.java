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
package org.apache.poi.ss.usermodel;

import java.awt.Dimension;


/**
 * Repersents a picture in a SpreadsheetML document
 */
public interface Picture extends Shape {

    /**
     * Reset the image to the dimension of the embedded image
     *
     * @see #resize(double, double)
     */
    void resize();

    /**
     * Resize the image proportionally.
     *
     * @see #resize(double, double)
     */
    void resize(double scale);
    
    /**
     * Resize the image.
     * <p>
     * Please note, that this method works correctly only for workbooks
     * with the default font size (Arial 10pt for .xls and Calibri 11pt for .xlsx).
     * If the default font is changed the resized image can be streched vertically or horizontally.
     * </p>
     * <p>
     * <code>resize(1.0,1.0)</code> keeps the original size,<br>
     * <code>resize(0.5,0.5)</code> resize to 50% of the original,<br>
     * <code>resize(2.0,2.0)</code> resizes to 200% of the original.<br>
     * <code>resize({@link Double#MAX_VALUE},{@link Double#MAX_VALUE})</code> resizes to the dimension of the embedded image. 
     * </p>
     *
     * @param scaleX the amount by which the image width is multiplied relative to the original width.
     * @param scaleY the amount by which the image height is multiplied relative to the original height.
     */
    void resize(double scaleX, double scaleY);

    /**
     * Calculate the preferred size for this picture.
     *
     * @return XSSFClientAnchor with the preferred size for this image
     */
    ClientAnchor getPreferredSize();
    
    /**
     * Calculate the preferred size for this picture.
     *
     * @param scaleX the amount by which image width is multiplied relative to the original width.
     * @param scaleY the amount by which image height is multiplied relative to the original height.
     * @return ClientAnchor with the preferred size for this image
     */
    ClientAnchor getPreferredSize(double scaleX, double scaleY);

    /**
     * Return the dimension of the embedded image in pixel
     *
     * @return image dimension in pixels
     */
    Dimension getImageDimension();
    
    /**
     * Return picture data for this picture
     *
     * @return picture data for this picture
     */
    PictureData getPictureData();

    /**
     * @return  the anchor that is used by this picture
     */
    ClientAnchor getClientAnchor();


    /**
     * @return the sheet which contains the picture
     */
    Sheet getSheet();
}
