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

import junit.framework.TestCase;

import java.io.IOException;
import java.io.FileInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Test <code>HSSFPicture</code>.
 *
 * @author Yegor Kozlov (yegor at apache.org)
 */
public class TestHSSFPicture extends TestCase{

    public void testResize() throws Exception {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sh1 = wb.createSheet();
        HSSFPatriarch p1 = sh1.createDrawingPatriarch();

        int idx1 = loadPicture( "src/resources/logos/logoKarmokar4.png", wb);
        HSSFPicture picture1 = p1.createPicture(new HSSFClientAnchor(), idx1);
        HSSFClientAnchor anchor1 = picture1.getPrefferedSize();

        //assert against what would BiffViewer print if we insert the image in xls and dump the file
        assertEquals(0, anchor1.getCol1());
        assertEquals(0, anchor1.getRow1());
        assertEquals(1, anchor1.getCol2());
        assertEquals(9, anchor1.getRow2());
        assertEquals(0, anchor1.getDx1());
        assertEquals(0, anchor1.getDy1());
        assertEquals(848, anchor1.getDx2());
        assertEquals(240, anchor1.getDy2());
    }

    /**
     * Copied from org.apache.poi.hssf.usermodel.examples.OfficeDrawing
     */
    private static int loadPicture( String path, HSSFWorkbook wb ) throws IOException
    {
        int pictureIndex;
        FileInputStream fis = null;
        ByteArrayOutputStream bos = null;
        try
        {
            fis = new FileInputStream( path);
            bos = new ByteArrayOutputStream( );
            int c;
            while ( (c = fis.read()) != -1)
                bos.write( c );
            pictureIndex = wb.addPicture( bos.toByteArray(), HSSFWorkbook.PICTURE_TYPE_PNG );
        }
        finally
        {
            if (fis != null)
                fis.close();
            if (bos != null)
                bos.close();
        }
        return pictureIndex;
    }

}
