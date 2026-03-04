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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.input.BOMInputStream;

public class StressMap {
    private final MultiValuedMap<String, ExcInfo> exMap = new ArrayListValuedHashMap<>();
    private final Map<String,String> handlerMap = new LinkedHashMap<>();
    private final boolean SCRATCH_IGNORE = Boolean.getBoolean("scratchpad.ignore");
    private final Pattern SCRATCH_HANDLER = Pattern.compile("(HSLF|HWPF|HSMF|HMEF)");

    public void loadDataFiles() throws IOException {
        readExMap();
        readHandlerMap();
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

    public void readHandlerMap() throws IOException {
        handlerMap.clear();

        CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader()                 // empty => parse header from first record
                .setSkipHeaderRecord(true)   // skip the header row when iterating records
                .get();
        File inputFile = new File(TestAllFiles.ROOT_DIR, "poi-integration-handlers.csv");
        try (
                FileInputStream in = new FileInputStream(inputFile);
                BOMInputStream bomInputStream = BOMInputStream.builder().setInputStream(in).get()
        ) {
            Iterable<CSVRecord> records = csvFormat.parse(
                    new InputStreamReader(bomInputStream, StandardCharsets.UTF_8));
            records.forEach(record -> {
                final String filePart = record.get(0);
                if (filePart != null && !filePart.isBlank()) {
                    String handlerType = record.get(2);
                    if (SCRATCH_IGNORE || handlerType == null || handlerType.isBlank()) {
                        handlerType = record.get(1);
                    }
                    handlerMap.put(filePart, handlerType);
                }
            });
        }
    }

    public void readExMap() throws IOException {
        exMap.clear();

        CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader()                 // empty => parse header from first record
                .setSkipHeaderRecord(true)   // skip the header row when iterating records
                .get();
        File inputFile = new File(TestAllFiles.ROOT_DIR, "poi-integration-exceptions.csv");
        try (
                FileInputStream in = new FileInputStream(inputFile);
                BOMInputStream bomInputStream = BOMInputStream.builder().setInputStream(in).get()

        ) {
            Iterable<CSVRecord> records = csvFormat.parse(
                    new InputStreamReader(bomInputStream, StandardCharsets.UTF_8));
            records.forEach(record -> {
                final String file = record.get("File");
                if (file != null && !file.isBlank()) {
                    ExcInfo info = new ExcInfo();
                    info.setFile(file);
                    String tests = record.get("Tests");
                    if (tests != null && !tests.isBlank()) {
                        info.setTests(tests);
                    }
                    String handler = record.get("Handler");
                    if (handler != null && !handler.isBlank()) {
                        info.setHandler(handler);
                    }
                    String password = record.get("Password");
                    if (password != null && !password.isBlank()) {
                        info.setPassword(password);
                    }
                    String exClass = record.get("Exception Class");
                    if (exClass != null && !exClass.isBlank()) {
                        info.setExClazz(exClass);
                    }
                    String exMessage = record.get("Exception Message");
                    if (exMessage != null && !exMessage.isBlank()) {
                        info.setExMessage(exMessage);
                    }
                    final boolean ignore =
                            SCRATCH_IGNORE && SCRATCH_HANDLER.matcher(info.getHandler()).find();
                    if (!ignore) {
                        exMap.put(file, info);
                    }
                }
            });
        }
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
