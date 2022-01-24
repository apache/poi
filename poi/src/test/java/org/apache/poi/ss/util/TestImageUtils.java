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

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.usermodel.HSSFClientAnchor;
import org.apache.poi.hssf.usermodel.HSSFPatriarch;
import org.apache.poi.hssf.usermodel.HSSFPicture;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests ImageUtils.
 *
 * @see ImageUtils
 */
final class TestImageUtils {

    @Test
    void testSetPreferredSizeNegativeScale() throws IOException {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFSheet sheet = wb.createSheet();
            HSSFPatriarch patriarch = sheet.createDrawingPatriarch();

            byte[] pictureData = HSSFTestDataSamples.getTestDataFileContent("45829.png");
            int idx1 = wb.addPicture(pictureData, HSSFWorkbook.PICTURE_TYPE_PNG);
            HSSFClientAnchor anchor = new HSSFClientAnchor(0, 0, 10, 10, (short)0, 0, (short)10, 10);
            HSSFPicture picture = patriarch.createPicture(anchor, idx1);

            assertThrows(IllegalArgumentException.class, () ->
                    ImageUtils.setPreferredSize(picture, -1, 1)
            );
            assertThrows(IllegalArgumentException.class, () ->
                    ImageUtils.setPreferredSize(picture, 1, -1)
            );
        }
    }

    @Test
    void testSetPreferredSizeInfiniteScale() throws IOException {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFSheet sheet = wb.createSheet();
            HSSFPatriarch patriarch = sheet.createDrawingPatriarch();

            byte[] pictureData = HSSFTestDataSamples.getTestDataFileContent("45829.png");
            int idx1 = wb.addPicture(pictureData, HSSFWorkbook.PICTURE_TYPE_PNG);
            HSSFClientAnchor anchor = new HSSFClientAnchor(0, 0, 10, 10, (short)0, 0, (short)10, 10);
            HSSFPicture picture = patriarch.createPicture(anchor, idx1);

            assertThrows(IllegalArgumentException.class, () ->
                    ImageUtils.setPreferredSize(picture, Double.POSITIVE_INFINITY, 1)
            );
            assertThrows(IllegalArgumentException.class, () ->
                    ImageUtils.setPreferredSize(picture, 1, Double.NEGATIVE_INFINITY)
            );
            assertThrows(IllegalArgumentException.class, () ->
                    ImageUtils.setPreferredSize(picture, 1, Double.POSITIVE_INFINITY)
            );
            assertThrows(IllegalArgumentException.class, () ->
                    ImageUtils.setPreferredSize(picture, Double.NEGATIVE_INFINITY, 1)
            );
            assertThrows(IllegalArgumentException.class, () ->
                    ImageUtils.setPreferredSize(picture, Double.NaN, 1)
            );
            assertThrows(IllegalArgumentException.class, () ->
                    ImageUtils.setPreferredSize(picture, 1, Double.NaN)
            );
        }
    }
}
