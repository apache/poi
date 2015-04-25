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
import java.net.URISyntaxException;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * Test to ensure that all our main formats can create, write
 *  and read back in, when running under OSGi
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerMethod.class)
public class TestOSGiBundle {

    private final File TARGET = new File("target");

    @Inject
    private BundleContext bc;

    @Configuration
    public Option[] configuration() throws IOException, URISyntaxException {
        File base = new File(TARGET, "test-bundles");
        return options(
                junitBundles(),
                bundle(new File(base, "poi-bundle.jar").toURI().toURL().toString()));
    }

    @Test
    public void testHSSF() throws Exception {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet s = wb.createSheet("OSGi");
        s.createRow(0).createCell(0).setCellValue("With OSGi");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        wb.write(baos);
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

        wb = new HSSFWorkbook(bais);
        assertEquals(1, wb.getNumberOfSheets());

        s = wb.getSheet("OSGi");
        assertEquals("With OSGi", s.getRow(0).getCell(0).toString());
    }
}
