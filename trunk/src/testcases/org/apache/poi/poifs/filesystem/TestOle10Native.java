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

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.POIDataSamples;
import org.apache.poi.util.IOUtils;
import org.junit.Ignore;
import org.junit.Test;

public class TestOle10Native {
    private static final POIDataSamples dataSamples = POIDataSamples.getPOIFSInstance();

    @Test
    public void testOleNative() throws IOException, Ole10NativeException {
        POIFSFileSystem fs = new POIFSFileSystem(dataSamples.openResourceAsStream("oleObject1.bin"));

        Ole10Native ole = Ole10Native.createFromEmbeddedOleObject(fs);

        assertEquals("File1.svg", ole.getLabel());
        assertEquals("D:\\Documents and Settings\\rsc\\My Documents\\file1.svg", ole.getCommand());
    }

    @Test
    public void testFiles() throws IOException, Ole10NativeException {
        File files[] = {
            // bug 51891
            POIDataSamples.getPOIFSInstance().getFile("multimedia.doc"),
            // tika bug 1072
            POIDataSamples.getPOIFSInstance().getFile("20-Force-on-a-current-S00.doc"),
            // other files containing ole10native records ...
            POIDataSamples.getDocumentInstance().getFile("Bug53380_3.doc"),
            POIDataSamples.getDocumentInstance().getFile("Bug47731.doc")
        };
        
        for (File f : files) {
            NPOIFSFileSystem fs = new NPOIFSFileSystem(f, true);
            List<Entry> entries = new ArrayList<Entry>();
            findOle10(entries, fs.getRoot(), "/", "");
            
            for (Entry e : entries) {
                ByteArrayOutputStream bosExp = new ByteArrayOutputStream();
                InputStream is = ((DirectoryNode)e.getParent()).createDocumentInputStream(e);
                IOUtils.copy(is,bosExp);
                is.close();
                
                Ole10Native ole = Ole10Native.createFromEmbeddedOleObject((DirectoryNode)e.getParent());
                
                ByteArrayOutputStream bosAct = new ByteArrayOutputStream();
                ole.writeOut(bosAct);
                
                assertThat(bosExp.toByteArray(), equalTo(bosAct.toByteArray()));
            }
            
            fs.close();
        }
    }

    /*
    void searchOle10Files() throws Exception {
        File dir = new File("test-data/document");
        for (File file : dir.listFiles(new FileFilter(){
            public boolean accept(File pathname) {
                return pathname.getName().endsWith("doc");
            }
        })) {
            NPOIFSFileSystem fs = new NPOIFSFileSystem(file, true);
            findOle10(null, fs.getRoot(), "/", file.getName());
            fs.close();
        }
    }*/
    
    void findOle10(List<Entry> entries, DirectoryNode dn, String path, String filename) {
        Iterator<Entry> iter = dn.getEntries();
        while (iter.hasNext()) {
            Entry e = iter.next();
            if (Ole10Native.OLE10_NATIVE.equals(e.getName())) {
                if (entries != null) entries.add(e);
                // System.out.println(filename+" : "+path);
            } else if (e.isDirectoryEntry()) {
                findOle10(entries, (DirectoryNode)e, path+e.getName()+"/", filename);
            }
        }
    }

    @Test
    public void testOleNativeOOM() throws IOException, Ole10NativeException {
        POIFSFileSystem fs = new POIFSFileSystem(dataSamples.openResourceAsStream("60256.bin"));
        try {
            Ole10Native.createFromEmbeddedOleObject(fs);
            fail("Should have thrown exception because OLENative lacks a length parameter");
        } catch (Ole10NativeException e) {
            assertTrue(e.getMessage().indexOf("declared data length") > -1);
        }
    }

}
