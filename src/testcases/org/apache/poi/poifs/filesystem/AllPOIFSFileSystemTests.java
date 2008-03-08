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

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for org.apache.poi.poifs.filesystem<br/>
 * 
 * @author Josh Micich
 */
public final class AllPOIFSFileSystemTests {

    public static Test suite() {
        TestSuite result = new TestSuite("Tests for org.apache.poi.poifs.filesystem");
        result.addTestSuite(TestDirectoryNode.class);
        result.addTestSuite(TestDocument.class);
        result.addTestSuite(TestDocumentDescriptor.class);
        result.addTestSuite(TestDocumentInputStream.class);
        result.addTestSuite(TestDocumentNode.class);
        result.addTestSuite(TestDocumentOutputStream.class);
        result.addTestSuite(TestEmptyDocument.class);
        result.addTestSuite(TestOffice2007XMLException.class);
        result.addTestSuite(TestPOIFSDocumentPath.class);
        result.addTestSuite(TestPropertySorter.class);
        return result;
    }
}
