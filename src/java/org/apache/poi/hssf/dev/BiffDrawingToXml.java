/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */

package org.apache.poi.hssf.dev;

import org.apache.poi.ddf.EscherRecord;
import org.apache.poi.hssf.model.InternalWorkbook;
import org.apache.poi.hssf.record.*;
import org.apache.poi.hssf.usermodel.HSSFPatriarch;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import java.io.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility for representing drawings contained in a binary Excel file as a XML tree
 *
 * @author Evgeniy Berlog
 * date: 10.04.12
 */
public class BiffDrawingToXml {

    private static final String SHEET_NAME_PARAM = "-sheet-name";
    private static final String SHEET_INDEXES_PARAM = "-sheet-indexes";
    private static final String EXCLUDE_WORKBOOK_RECORDS = "-exclude-workbook";

    private static int getAttributeIndex(String attribute, String[] params) {
        for (int i = 0; i < params.length; i++) {
            String param = params[i];
            if (attribute.equals(param)) {
                return i;
            }
        }
        return -1;
    }

    private static boolean isExcludeWorkbookRecords(String[] params) {
        return -1 != getAttributeIndex(EXCLUDE_WORKBOOK_RECORDS, params);
    }

    private static List<Integer> getIndexesByName(String[] params, HSSFWorkbook workbook) {
        List<Integer> list = new ArrayList<Integer>();
        int pos = getAttributeIndex(SHEET_NAME_PARAM, params);
        if (-1 != pos) {
            if (pos >= params.length) {
                throw new IllegalArgumentException("sheet name param value was not specified");
            }
            String sheetName = params[pos + 1];
            int sheetPos = workbook.getSheetIndex(sheetName);
            if (-1 == sheetPos){
                throw new IllegalArgumentException("specified sheet name has not been found in xls file");
            }
            list.add(sheetPos);
        }
        return list;
    }

    private static List<Integer> getIndexesByIdArray(String[] params) {
        List<Integer> list = new ArrayList<Integer>();
        int pos = getAttributeIndex(SHEET_INDEXES_PARAM, params);
        if (-1 != pos) {
            if (pos >= params.length) {
                throw new IllegalArgumentException("sheet list value was not specified");
            }
            String sheetParam = params[pos + 1];
            String[] sheets = sheetParam.split(",");
            for (String sheet : sheets) {
                list.add(Integer.parseInt(sheet));
            }
        }
        return list;
    }

    private static List<Integer> getSheetsIndexes(String[] params, HSSFWorkbook workbook) {
        List<Integer> list = new ArrayList<Integer>();
        list.addAll(getIndexesByIdArray(params));
        list.addAll(getIndexesByName(params, workbook));
        if (0 == list.size()) {
            int size = workbook.getNumberOfSheets();
            for (int i = 0; i < size; i++) {
                list.add(i);
            }
        }
        return list;
    }

    private static String getInputFileName(String[] params) {
        return params[params.length - 1];
    }

    private static String getOutputFileName(String input) {
        if (input.contains("xls")) {
            return input.replace(".xls", ".xml");
        }
        return input + ".xml";
    }

    public static void main(String[] params) throws IOException {
        if (0 == params.length) {
            System.out.println("Usage: BiffDrawingToXml [options] inputWorkbook");
            System.out.println("Options:");
            System.out.println("  -exclude-workbook            exclude workbook-level records");
            System.out.println("  -sheet-indexes   <indexes>   output sheets with specified indexes");
            System.out.println("  -sheet-namek  <names>        output sheets with specified name");
            return;
        }
        String input = getInputFileName(params);
        FileInputStream inp = new FileInputStream(input);
        String output = getOutputFileName(input);
        FileOutputStream outputStream = new FileOutputStream(output);
        writeToFile(outputStream, inp, isExcludeWorkbookRecords(params), params);
        inp.close();
        outputStream.close();
    }

    public static void writeToFile(FileOutputStream fos, InputStream xlsWorkbook, boolean excludeWorkbookRecords, String[] params) throws IOException {
        POIFSFileSystem fs = new POIFSFileSystem(xlsWorkbook);
        HSSFWorkbook workbook = new HSSFWorkbook(fs);
        InternalWorkbook internalWorkbook = getInternalWorkbook(workbook);
        DrawingGroupRecord r = (DrawingGroupRecord) internalWorkbook.findFirstRecordBySid(DrawingGroupRecord.sid);
        r.decode();

        StringBuilder builder = new StringBuilder();
        builder.append("<workbook>\n");
        String tab = "\t";
        if (!excludeWorkbookRecords) {
            List<EscherRecord> escherRecords = r.getEscherRecords();
            for (EscherRecord record : escherRecords) {
                builder.append(record.toXml(tab));
            }
        }
        List<Integer> sheets = getSheetsIndexes(params, workbook);
        for (Integer i : sheets) {
            HSSFPatriarch p = workbook.getSheetAt(i).getDrawingPatriarch();
            if(p != null ) {
                builder.append(tab).append("<sheet").append(i).append(">\n");
                builder.append(getHSSFPatriarchBoundAggregate(p).toXml(tab + "\t"));
                builder.append(tab).append("</sheet").append(i).append(">\n");
            }
        }
        builder.append("</workbook>\n");
        fos.write(builder.toString().getBytes());
        fos.close();
    }

    private static EscherAggregate getHSSFPatriarchBoundAggregate(HSSFPatriarch patriarch) {
        Field boundAggregateField = null;
        try {
            boundAggregateField = patriarch.getClass().getDeclaredField("_boundAggregate");
            boundAggregateField.setAccessible(true);
            return (EscherAggregate) boundAggregateField.get(patriarch);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static InternalWorkbook getInternalWorkbook(HSSFWorkbook workbook) {
        Field internalSheetField = null;
        try {
            internalSheetField = workbook.getClass().getDeclaredField("workbook");
            internalSheetField.setAccessible(true);
            return (InternalWorkbook) internalSheetField.get(workbook);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
}
