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

package org.apache.poi.poifs.filesystem;

import static org.apache.poi.POIDataSamples.writeOutAndReadBack;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.poifs.property.DirectoryProperty;
import org.apache.poi.poifs.property.Property;
import org.junit.jupiter.api.Test;

/**
 * Verify the order of entries {@code DirectoryProperty} .
 * <p>
 * In particular it is important to serialize ROOT._VBA_PROJECT_CUR.VBA node.
 * See bug 39234 in bugzilla. Thanks to Bill Seddon for providing the solution.
 * </p>
 */
final class TestPropertySorter {

    //the correct order of entries in the test file
    private static final String[] _entries = {
        "dir", "JML", "UTIL", "Loader", "Sheet1", "Sheet2", "Sheet3",
        "__SRP_0", "__SRP_1", "__SRP_2", "__SRP_3", "__SRP_4", "__SRP_5",
        "ThisWorkbook", "_VBA_PROJECT",
    };

    private static POIFSFileSystem openSampleFS() {
        InputStream is = HSSFTestDataSamples.openSampleFileStream("39234.xls");
        try {
            return new POIFSFileSystem(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Test sorting of properties in {@code DirectoryProperty}
     */
    @Test
    void testSortProperties() throws IOException {
        try (POIFSFileSystem fs = openSampleFS()) {
            Property[] props = getVBAProperties(fs);

            assertEquals(_entries.length, props.length);

            // (1). See that there is a problem with the old case-sensitive property comparator
            Arrays.sort(props, TestPropertySorter::oldCaseSensitivePropertyCompareTo);

            String exp = String.join("", _entries);
            String actOld = Stream.of(props).map(Property::getName).collect(Collectors.joining());

            assertNotEquals(exp, actOld, "expected old case-sensitive property comparator to return properties in wrong order");

            // (2) Verify that the fixed property comparator works right
            Arrays.sort(props, new DirectoryProperty.PropertyComparator());
            String[] actNew = Stream.of(props).map(Property::getName).toArray(String[]::new);

            assertArrayEquals(_entries, actNew);
        }
    }

    private static int oldCaseSensitivePropertyCompareTo(Property o1, Property o2) {
        String name1  = o1.getName();
        String name2  = o2.getName();
        int result = name1.length() - name2.length();
        return (result != 0) ? result : name1.compareTo(name2);
    }

    /**
     * Serialize file system and verify that the order of properties is the same as in the original file.
     */
    @Test
    void testSerialization() throws IOException {
        try (POIFSFileSystem fs = openSampleFS();
             POIFSFileSystem fs2 = writeOutAndReadBack(fs)) {
            Property[] props = getVBAProperties(fs2);
            Arrays.sort(props, new DirectoryProperty.PropertyComparator());

            String[] act = Stream.of(props).map(Property::getName).toArray(String[]::new);
            assertArrayEquals(_entries, act);
        }
    }

    /**
     * @return array of properties read from ROOT._VBA_PROJECT_CUR.VBA node
     */
    private Property[] getVBAProperties(POIFSFileSystem fs) throws IOException {
        String _VBA_PROJECT_CUR = "_VBA_PROJECT_CUR";
        String VBA = "VBA";

        DirectoryEntry root = fs.getRoot();
        DirectoryEntry vba_project = (DirectoryEntry)root.getEntry(_VBA_PROJECT_CUR);

        DirectoryNode vba = (DirectoryNode)vba_project.getEntry(VBA);
        DirectoryProperty  p = (DirectoryProperty)vba.getProperty();

        List<Property> lst = new ArrayList<>();
        for (Property ch : p){
            lst.add(ch);
        }
        return lst.toArray(new Property[0]);
    }
}
