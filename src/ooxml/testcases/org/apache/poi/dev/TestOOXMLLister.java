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

   2012 - Alfresco Software, Ltd.
   Alfresco Software has modified source of this file
   The details of changes as svn diff can be found in svn at location root/projects/3rd-party/src
==================================================================== */
package org.apache.poi.dev;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.util.NullOutputStream;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.junit.Test;

public class TestOOXMLLister {
    @Test
    public void testMain() throws IOException, InvalidFormatException {
        File file = XSSFTestDataSamples.getSampleFile("Formatting.xlsx");
        OOXMLLister.main(new String[] {file.getAbsolutePath()});
    }

    @Test
    public void testWithPrintStream() throws IOException, InvalidFormatException {
        File file = XSSFTestDataSamples.getSampleFile("Formatting.xlsx");
        PrintStream nullStream = new PrintStream(new NullOutputStream(), true, "UTF-8");
        OPCPackage opc = OPCPackage.open(file.getAbsolutePath(), PackageAccess.READ);
        OOXMLLister lister = new OOXMLLister(opc, nullStream);
        lister.displayParts();
        lister.displayRelations();
        lister.close();
        opc.close();
        nullStream.close();
    }
}
