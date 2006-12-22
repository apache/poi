
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

import junit.framework.TestCase;
import junit.framework.ComparisonFailure;

import java.io.*;
import java.util.*;

import org.apache.poi.poifs.property.DirectoryProperty;
import org.apache.poi.poifs.property.Property;

/**
 * Verify the order of entries <code>DirectoryProperty</code> .
 * <p>
 * In particular it is important to serialize ROOT._VBA_PROJECT_CUR.VBA node.
 * See bug 39234 in bugzilla. Thanks to Bill Seddon for providing the solution.
 * </p>
 *
 * @author Yegor Kozlov
 */
public class TestPropertySorter extends TestCase {

    //the correct order of entries in the test file
    protected static final String[] _entries = {
        "dir", "JML", "UTIL", "Loader", "Sheet1", "Sheet2", "Sheet3",
        "__SRP_0", "__SRP_1", "__SRP_2", "__SRP_3", "__SRP_4", "__SRP_5",
        "ThisWorkbook", "_VBA_PROJECT",
    };

    protected File testFile;

    public void setUp(){
        String home = System.getProperty("HSSF.testdata.path");
        testFile = new File(home + "/39234.xls");
    }

    /**
     * Test sorting of properties in <code>DirectoryProperty</code>
     */
    public void testSortProperties() throws IOException {
        InputStream is = new FileInputStream(testFile);
        POIFSFileSystem fs = new POIFSFileSystem(is);
        is.close();
        Property[] props = getVBAProperties(fs);

        assertEquals(_entries.length, props.length);

        // (1). See that there is a problem with the old case-sensitive property comparartor
        Arrays.sort(props, new CaseSensitivePropertyComparator());
        try {
            for (int i = 0; i < props.length; i++) {
                assertEquals(_entries[i], props[i].getName());
            }
            fail("case-sensitive property comparator returns properties in wrong order");
        } catch (ComparisonFailure e){
            ; // as expected
        }

        // (2) Verify that the fixed proeprty comparator works right
        Arrays.sort(props, new DirectoryProperty.PropertyComparator());
        for (int i = 0; i < props.length; i++) {
            assertEquals(_entries[i], props[i].getName());
        }
    }

    /**
     * Serialize file system and verify that the order of properties is the same as in the original file.
     */
    public void testSerialization() throws IOException {
        InputStream is = new FileInputStream(testFile);
        POIFSFileSystem fs = new POIFSFileSystem(is);
        is.close();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        fs.writeFilesystem(out);
        out.close();
        is = new ByteArrayInputStream(out.toByteArray());
        fs = new POIFSFileSystem(is);
        is.close();
        Property[] props = getVBAProperties(fs);
        Arrays.sort(props, new DirectoryProperty.PropertyComparator());

        assertEquals(_entries.length, props.length);
        for (int i = 0; i < props.length; i++) {
            assertEquals(_entries[i], props[i].getName());
        }
    }

    /**
     * @return array of properties read from ROOT._VBA_PROJECT_CUR.VBA node
     */
    protected Property[] getVBAProperties(POIFSFileSystem fs) throws IOException {
        String _VBA_PROJECT_CUR = "_VBA_PROJECT_CUR";
        String VBA = "VBA";

        DirectoryEntry root = fs.getRoot();
        DirectoryEntry vba_project = (DirectoryEntry)root.getEntry(_VBA_PROJECT_CUR);

        DirectoryNode vba = (DirectoryNode)vba_project.getEntry(VBA);
        DirectoryProperty  p = (DirectoryProperty)vba.getProperty();

        ArrayList lst = new ArrayList();
        for (Iterator it = p.getChildren(); it.hasNext();){
            Property ch = (Property)it.next();
            lst.add(ch);
        }
        return (Property [])lst.toArray(new Property[ 0 ]);
    }

    /**
     * Old version of case-sensitive PropertyComparator to demonstrate the problem
     */
    private class CaseSensitivePropertyComparator  implements Comparator
    {

        public boolean equals(Object o)
        {
            return this == o;
        }

        public int compare(Object o1, Object o2)
        {
            String name1  = (( Property ) o1).getName();
            String name2  = (( Property ) o2).getName();
            int    result = name1.length() - name2.length();

            if (result == 0)
            {
                result = name1.compareTo(name2);
            }
            return result;
        }
    }

}
