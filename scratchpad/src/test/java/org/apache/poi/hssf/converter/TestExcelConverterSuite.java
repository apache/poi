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
package org.apache.poi.hssf.converter;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.util.XMLHelper;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class TestExcelConverterSuite
{
    /**
     * YK: a quick hack to exclude failing documents from the suite.
     */
    @SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
    private static final List<String> failingFiles = Arrays.asList(
            /* not failing, but requires more memory */
            "ex45698-22488.xls" );

    public static Stream<Arguments> files() {
        List<Arguments> files = new ArrayList<>();
        File directory = POIDataSamples.getDocumentInstance().getFile(
                "../spreadsheet" );
        for ( final File child : Objects.requireNonNull(directory.listFiles((dir, name) -> name.endsWith(".xls") && !failingFiles.contains(name)))) {
            files.add(Arguments.of(child));
        }

        return files.stream();
    }

    @ParameterizedTest
    @MethodSource("files")
    void testFo(File child) throws Exception
    {
        HSSFWorkbook workbook;
        try {
            workbook = ExcelToHtmlUtils.loadXls( child );
        } catch ( Exception exc ) {
            // unable to parse file -- not ExcelToFoConverter fault
            return;
        }

        ExcelToHtmlConverter excelToHtmlConverter = new ExcelToHtmlConverter(
                XMLHelper.newDocumentBuilder().newDocument() );
        excelToHtmlConverter.processWorkbook( workbook );

        StringWriter stringWriter = new StringWriter();

        Transformer transformer = XMLHelper.newTransformer();
        transformer.transform(
                new DOMSource( excelToHtmlConverter.getDocument() ),
                new StreamResult( stringWriter ) );

        assertNotNull(stringWriter.toString());
    }

    @ParameterizedTest
    @MethodSource("files")
    void testHtml(File child) throws Exception
    {
        HSSFWorkbook workbook;
        try {
            workbook = ExcelToHtmlUtils.loadXls( child );
        } catch ( Exception exc ) {
            // unable to parse file -- not ExcelToFoConverter fault
            return;
        }

        ExcelToHtmlConverter excelToHtmlConverter = new ExcelToHtmlConverter(
                XMLHelper.newDocumentBuilder().newDocument() );
        excelToHtmlConverter.processWorkbook( workbook );

        StringWriter stringWriter = new StringWriter();

        Transformer transformer = XMLHelper.newTransformer();
        transformer.setOutputProperty( OutputKeys.METHOD, "html" );
        transformer.transform(
                new DOMSource( excelToHtmlConverter.getDocument() ),
                new StreamResult( stringWriter ) );

        assertNotNull(stringWriter.toString());
    }
}
