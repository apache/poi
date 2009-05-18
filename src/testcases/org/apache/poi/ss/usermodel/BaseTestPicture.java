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

import junit.framework.TestCase;
import org.apache.poi.ss.ITestDataProvider;

/**
 * @author Yegor Kozlov
 */
public abstract class BaseTestPicture extends TestCase {

    protected abstract ITestDataProvider getTestDataProvider();

    public void baseTestResize(ClientAnchor referenceAnchor) {
        Workbook wb = getTestDataProvider().createWorkbook();
        Sheet sh1 = wb.createSheet();
        Drawing  p1 = sh1.createDrawingPatriarch();
        CreationHelper factory = wb.getCreationHelper();

        byte[] pictureData = getTestDataProvider().getTestDataFileContent("logoKarmokar4.png");
        int idx1 = wb.addPicture( pictureData, Workbook.PICTURE_TYPE_PNG );
        Picture picture = p1.createPicture(factory.createClientAnchor(), idx1);
        picture.resize();
        ClientAnchor anchor1 = picture.getPreferredSize();

        //assert against what would BiffViewer print if we insert the image in xls and dump the file
        assertEquals(referenceAnchor.getCol1(), anchor1.getCol1());
        assertEquals(referenceAnchor.getRow1(), anchor1.getRow1());
        assertEquals(referenceAnchor.getCol2(), anchor1.getCol2());
        assertEquals(referenceAnchor.getRow2(), anchor1.getRow2());
        assertEquals(referenceAnchor.getDx1(), anchor1.getDx1());
        assertEquals(referenceAnchor.getDy1(), anchor1.getDy1());
        assertEquals(referenceAnchor.getDx2(), anchor1.getDx2());
        assertEquals(referenceAnchor.getDy2(), anchor1.getDy2());
    }
}
