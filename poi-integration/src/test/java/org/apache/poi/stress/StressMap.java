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
package org.apache.poi.stress;

import java.io.File;
import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

public class StressMap {
    private final MultiValuedMap<String, ExcInfo> exMap = new ArrayListValuedHashMap<>();
    private final Map<String,String> handlerMap = new LinkedHashMap<>();
    private final boolean SCRATCH_IGNORE = Boolean.getBoolean("scratchpad.ignore");
    private final Pattern SCRATCH_HANDLER = Pattern.compile("(HSLF|HWPF|HSMF|HMEF)");

    public void load(File mapFile) throws IOException {
        try (Workbook wb = WorkbookFactory.create(mapFile, null, true)) {
            readExMap(wb.getSheet("Exceptions"));
            readHandlerMap(wb.getSheet("Handlers"));
        }
    }

    public List<FileHandlerKnown> getHandler(String file) {
        // ... failures/handlers lookup doesn't work on windows otherwise
        final String uniFile = file.replace('\\', '/');

        String firstHandler = handlerMap.entrySet().stream()
            .filter(me -> uniFile.endsWith(me.getKey()))
            .map(Map.Entry::getValue).findFirst().orElse("NULL");

        return Stream.of(firstHandler, secondHandler(firstHandler))
            .filter(h -> !"NULL".equals(h))
            .map(FileHandlerKnown::valueOf)
            .collect(Collectors.toList());
    }

    public ExcInfo getExcInfo(String file, String testName, FileHandlerKnown handler) {
        // ... failures/handlers lookup doesn't work on windows otherwise
        final String uniFile = file.replace('\\', '/');

        return exMap.get(uniFile).stream()
            .filter(e -> e.isMatch(testName, handler.name()))
            .findFirst().orElse(null);
    }

    public void readHandlerMap(Sheet sh) {
        if (sh == null) {
            return;
        }

        handlerMap.clear();

        boolean isFirst = true;
        for (Row row : sh) {
            if (isFirst) {
                isFirst = false;
                continue;
            }
            Cell cell = row.getCell(2);
            if (SCRATCH_IGNORE || cell == null || cell.getCellType() != CellType.STRING) {
                cell = row.getCell(1);
            }
            handlerMap.put(row.getCell(0).getStringCellValue(), cell.getStringCellValue());
        }
    }


    public void readExMap(Sheet sh) {
        if (sh == null) {
            return;
        }

        exMap.clear();

        Iterator<Row> iter = sh.iterator();
        List<Map.Entry<String, BiConsumer<ExcInfo,String>>> cols = initCols(iter.next());

        int idx = 0, handlerIdx = -1;
        for (Map.Entry<String, BiConsumer<ExcInfo, String>> e : cols) {
            if ("Handler".equals(e.getKey())) {
                handlerIdx = idx;
            }
            idx++;
        }

        while (iter.hasNext()) {
            Row row = iter.next();

            if (SCRATCH_IGNORE && handlerIdx > -1) {
                String handler = row.getCell(handlerIdx) == null ? "" : row.getCell(handlerIdx).getStringCellValue();
                if (SCRATCH_HANDLER.matcher(handler).find()) {
                    // ignore exception of ignored files
                    continue;
                }
            }

            ExcInfo info = new ExcInfo();
            for (Cell cell : row) {
                if (cell.getCellType() == CellType.STRING) {
                    cols.get(cell.getColumnIndex()).getValue().accept(info, cell.getStringCellValue());
                }
            }
            exMap.put(info.getFile(), info);
        }
    }

    private static List<Map.Entry<String, BiConsumer<ExcInfo,String>>> initCols(Row row) {
        Map<String,BiConsumer<ExcInfo,String>> m = new HashMap<>();
        m.put("File", ExcInfo::setFile);
        m.put("Tests", ExcInfo::setTests);
        m.put("Handler", ExcInfo::setHandler);
        m.put("Password", ExcInfo::setPassword);
        m.put("Exception Class", ExcInfo::setExClazz);
        m.put("Exception Message", ExcInfo::setExMessage);

        return StreamSupport
            .stream(row.spliterator(), false)
            .map(Cell::getStringCellValue)
            .map(v -> new SimpleEntry<>(v, m.getOrDefault(v, (e,s) -> {})))
            .collect(Collectors.toList());
    }

    private static String secondHandler(String handlerStr) {
        switch (handlerStr) {
            case "XSSF":
            case "XWPF":
            case "XSLF":
            case "XDGF":
                return "OPC";
            case "HSSF":
            case "HWPF":
            case "HSLF":
            case "HDGF":
            case "HSMF":
            case "HBPF":
                return "HPSF";
            default:
                return "NULL";
        }
    }
}
