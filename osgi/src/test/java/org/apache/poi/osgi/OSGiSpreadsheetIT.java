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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.ops4j.pax.exam.CoreOptions.bundle;
import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.options;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test to ensure that all our main formats can create, write
 *  and read back in, when running under OSGi
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerMethod.class)
public class OSGiSpreadsheetIT {
    private final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static final String POI_BUNDLE_SYMBOLIC_NAME = "org.apache.poi.bundle";

    @Inject
    private BundleContext bc;

    @Configuration
    public Option[] configuration() throws IOException {
        String bundlePath = System.getProperty("bundle.filename");
        return options(
                junitBundles(),
                bundle(new File(bundlePath).toURI().toURL().toString()));
    }

    @Test
    public void testBundleLoaded()  {
        boolean hasBundle = Arrays.stream(bc.getBundles()).anyMatch(b ->
                POI_BUNDLE_SYMBOLIC_NAME.equals(b.getSymbolicName()));
        assertTrue(POI_BUNDLE_SYMBOLIC_NAME + " not found", hasBundle);
    }

    // create a workbook, validate and write back
    void testWorkbook(Workbook wb) throws Exception {
        logger.info("testing " + wb.getClass().getSimpleName());

        Sheet s = wb.createSheet("OSGi");
        s.createRow(0).createCell(0).setCellValue("With OSGi");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        wb.write(baos);
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

        if(wb instanceof HSSFWorkbook)
            wb = new HSSFWorkbook(bais);
        else
            wb = new XSSFWorkbook(bais);
        assertEquals(1, wb.getNumberOfSheets());

        s = wb.getSheet("OSGi");
        assertEquals("With OSGi", s.getRow(0).getCell(0).toString());


    }

    @Test
    public void testHSSF() throws Exception {
        testWorkbook(new HSSFWorkbook());
    }

    @Test
    public void testXSSF() throws Exception {
        testWorkbook(new XSSFWorkbook());
    }

    @Test
    public void testSXSSF() throws Exception {
        testWorkbook(new SXSSFWorkbook());
    }
}
