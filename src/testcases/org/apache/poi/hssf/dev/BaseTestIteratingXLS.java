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
package org.apache.poi.hssf.dev;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.poi.POIDataSamples;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Base class for integration-style tests which iterate over all test-files
 * and execute the same action to find out if any change breaks these applications.
 */
public abstract class BaseTestIteratingXLS {
	protected static final Map<String,Class<? extends Throwable>> EXCLUDED = new HashMap<>();

    private static Stream<Arguments> files() {
        String dataDirName = System.getProperty(POIDataSamples.TEST_PROPERTY);
        if(dataDirName == null) {
            dataDirName = "test-data";
        }

        List<Arguments> files = new ArrayList<>();
        findFile(files, dataDirName + "/spreadsheet");
        findFile(files, dataDirName + "/hpsf");

        return files.stream();
    }

    private static void findFile(List<Arguments> list, String dir) {
        String[] files = new File(dir).list((arg0, arg1) -> arg1.toLowerCase(Locale.ROOT).endsWith(".xls"));
        assertNotNull(files, "Did not find any xls files in directory " + dir);

        for(String file : files) {
            list.add(Arguments.of(new File(dir, file)));
        }
    }

    @ParameterizedTest
    @MethodSource("files")
	void testMain(File file) throws Exception {
	    String fileName = file.getName();

	    Class<? extends Throwable> t = EXCLUDED.get(fileName);

        if (t == null) {
            runOneFile(file);
        } else {
            assertThrows(t, () -> runOneFile(file));
        }

	}

	abstract void runOneFile(File pFile) throws Exception;
}
