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

package org.apache.poi.osgi;

import org.apache.poi.extractor.ExtractorFactory;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.sl.usermodel.Slide;
import org.apache.poi.sl.usermodel.SlideShow;
import org.apache.poi.sl.usermodel.TextBox;
import org.apache.poi.sl.usermodel.TextShape;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.junit.Assert.assertEquals;

/**
 * Test to ensure that all our main formats can create, write
 * and read back in, when running under OSGi
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class OSGiExtractorsIT extends BaseOSGiTestCase {


    byte[] createSlideShow(SlideShow ppt) throws Exception {
        Slide<?, ?> slide = ppt.createSlide();

        TextBox<?, ?> box = slide.createTextBox();
        box.setTextPlaceholder(TextShape.TextPlaceholder.TITLE);
        box.setText("Hello, World!");
        box.setAnchor(new Rectangle(36, 15, 648, 65));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ppt.write(out);
        return out.toByteArray();
    }

    byte[] createWorkbook(Workbook wb) throws Exception {
        Sheet s = wb.createSheet("OSGi");
        s.createRow(0).createCell(0).setCellValue("Hello, World!");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wb.write(out);
        return out.toByteArray();


    }

    /**
     * this should invoke OLE2ScratchpadExtractorFactory
     */
    @Test
    public void testHSLF() throws Exception {
        byte[] bytes = createSlideShow(new HSLFSlideShow());
        ByteArrayInputStream is = new ByteArrayInputStream(bytes);

        String text = ExtractorFactory.createExtractor(is).getText().trim();
        assertEquals("Hello, World!", text);
    }

    /**
     * this should invoke POIXMLExtractorFactory
     */
    @Test
    public void testXSLF() throws Exception {
        byte[] bytes = createSlideShow(new XMLSlideShow());
        ByteArrayInputStream is = new ByteArrayInputStream(bytes);

        String text = ExtractorFactory.createExtractor(is).getText().trim();
        assertEquals("Hello, World!", text);
    }

    @Test
    public void testHSSF() throws Exception {
        byte[] bytes = createWorkbook(new HSSFWorkbook());
        ByteArrayInputStream is = new ByteArrayInputStream(bytes);

        String text = ExtractorFactory.createExtractor(is).getText().trim();
        assertEquals("OSGi\nHello, World!", text);
    }

    /**
     * this should invoke POIXMLExtractorFactory
     */
    @Test
    public void testXSSF() throws Exception {
        byte[] bytes = createWorkbook(new XSSFWorkbook());
        ByteArrayInputStream is = new ByteArrayInputStream(bytes);

        String text = ExtractorFactory.createExtractor(is).getText().trim();
        assertEquals("OSGi\nHello, World!", text);
    }
}
