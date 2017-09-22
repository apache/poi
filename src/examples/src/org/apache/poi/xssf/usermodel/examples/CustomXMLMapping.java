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
package org.apache.poi.xssf.usermodel.examples;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.extractor.XSSFExportToXml;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFMap;

import java.io.ByteArrayOutputStream;

/**
 * Print all custom XML mappings registered in the given workbook
 */
public class CustomXMLMapping {

    public static void main(String[] args) throws Exception {
        try (OPCPackage pkg = OPCPackage.open(args[0]);
             XSSFWorkbook wb = new XSSFWorkbook(pkg)) {
            for (XSSFMap map : wb.getCustomXMLMappings()) {
                XSSFExportToXml exporter = new XSSFExportToXml(map);

                ByteArrayOutputStream os = new ByteArrayOutputStream();
                exporter.exportToXML(os, true);
                String xml = os.toString("UTF-8");
                System.out.println(xml);
            }
        }
    }
}
