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

package org.apache.poi.hpsf;

import org.apache.poi.POIDataSamples;
import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;

public class TestPropertySet {
    @Test
    public void test52372() throws IOException, NoPropertySetStreamException, MarkUnsupportedException, UnexpectedPropertySetTypeException {
        NPOIFSFileSystem poifs = new NPOIFSFileSystem(POIDataSamples.getDocumentInstance().getFile("52372.doc"));

        DocumentInputStream dis = poifs.createDocumentInputStream(SummaryInformation.DEFAULT_STREAM_NAME);
        PropertySet ps = new PropertySet(dis);
        SummaryInformation si = new SummaryInformation(ps);
        dis.close();

        assertNotNull(si);
    }
}
