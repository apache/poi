
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

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

/**
 * Class to test FilteringDirectoryNode functionality
 */
public final class TestFilteringDirectoryNode extends TestCase {
    private POIFSFileSystem fs;
    private DirectoryEntry dirA;
    private DirectoryEntry dirAA;
    private DirectoryEntry dirB;
    private DocumentEntry eRoot;
    private DocumentEntry eA;
    private DocumentEntry eAA;
    
    @Override
    protected void setUp() throws Exception {
       fs = new POIFSFileSystem();
       dirA = fs.createDirectory("DirA");
       dirB = fs.createDirectory("DirB");
       dirAA = dirA.createDirectory("DirAA");
       eRoot = fs.getRoot().createDocument("Root", new ByteArrayInputStream(new byte[] {}));
       eA  = dirA.createDocument("NA", new ByteArrayInputStream(new byte[] {}));
       eAA = dirAA.createDocument("NAA", new ByteArrayInputStream(new byte[] {}));
    }
    
    public void testNoFiltering() throws Exception {
       FilteringDirectoryNode d = new FilteringDirectoryNode(fs.getRoot(), new HashSet<>());
       assertEquals(3, d.getEntryCount());
       assertEquals(dirA.getName(), d.getEntry(dirA.getName()).getName());
       
       assertEquals(true, d.getEntry(dirA.getName()).isDirectoryEntry());
       assertEquals(false, d.getEntry(dirA.getName()).isDocumentEntry());
       
       assertEquals(true, d.getEntry(dirB.getName()).isDirectoryEntry());
       assertEquals(false, d.getEntry(dirB.getName()).isDocumentEntry());
       
       assertEquals(false, d.getEntry(eRoot.getName()).isDirectoryEntry());
       assertEquals(true, d.getEntry(eRoot.getName()).isDocumentEntry());
       
       Iterator<Entry> i = d.getEntries();
       assertEquals(dirA, i.next());
       assertEquals(dirB, i.next());
       assertEquals(eRoot, i.next());
       assertEquals(null, i.next());
    }
    
    public void testChildFiltering() throws Exception {
       List<String> excl = Arrays.asList(new String[] {"NotThere","AlsoNotThere", eRoot.getName()});
       FilteringDirectoryNode d = new FilteringDirectoryNode(fs.getRoot(), excl);
       
       assertEquals(2, d.getEntryCount());
       assertEquals(true, d.hasEntry(dirA.getName()));
       assertEquals(true, d.hasEntry(dirB.getName()));
       assertEquals(false, d.hasEntry(eRoot.getName()));
       
       assertEquals(dirA, d.getEntry(dirA.getName()));
       assertEquals(dirB, d.getEntry(dirB.getName()));
       try {
          d.getEntry(eRoot.getName());
          fail("Should be filtered");
       } catch(FileNotFoundException e) {}
       
       Iterator<Entry> i = d.getEntries();
       assertEquals(dirA, i.next());
       assertEquals(dirB, i.next());
       assertEquals(null, i.next());
       
       
       // Filter more
       excl = Arrays.asList(new String[] {"NotThere","AlsoNotThere", eRoot.getName(), dirA.getName()});
       d = new FilteringDirectoryNode(fs.getRoot(), excl);
       
       assertEquals(1, d.getEntryCount());
       assertEquals(false, d.hasEntry(dirA.getName()));
       assertEquals(true, d.hasEntry(dirB.getName()));
       assertEquals(false, d.hasEntry(eRoot.getName()));
       
       try {
          d.getEntry(dirA.getName());
          fail("Should be filtered");
       } catch(FileNotFoundException e) {}
       assertEquals(dirB, d.getEntry(dirB.getName()));
       try {
          d.getEntry(eRoot.getName());
          fail("Should be filtered");
       } catch(FileNotFoundException e) {}
       
       i = d.getEntries();
       assertEquals(dirB, i.next());
       assertEquals(null, i.next());
       
       
       // Filter everything
       excl = Arrays.asList(new String[] {"NotThere", eRoot.getName(), dirA.getName(), dirB.getName()});
       d = new FilteringDirectoryNode(fs.getRoot(), excl);
       
       assertEquals(0, d.getEntryCount());
       assertEquals(false, d.hasEntry(dirA.getName()));
       assertEquals(false, d.hasEntry(dirB.getName()));
       assertEquals(false, d.hasEntry(eRoot.getName()));
       
       try {
          d.getEntry(dirA.getName());
          fail("Should be filtered");
       } catch(FileNotFoundException e) {}
       try {
          d.getEntry(dirB.getName());
          fail("Should be filtered");
       } catch(FileNotFoundException e) {}
       try {
          d.getEntry(eRoot.getName());
          fail("Should be filtered");
       } catch(FileNotFoundException e) {}
       
       i = d.getEntries();
       assertEquals(null, i.next());
    }
    
    public void testNestedFiltering() throws Exception {
       List<String> excl = Arrays.asList(new String[] {
             dirA.getName()+"/"+"MadeUp",
             dirA.getName()+"/"+eA.getName(),
             dirA.getName()+"/"+dirAA.getName()+"/Test",
             eRoot.getName()
       });
       FilteringDirectoryNode d = new FilteringDirectoryNode(fs.getRoot(), excl);
       
       // Check main
       assertEquals(2, d.getEntryCount());
       assertEquals(true, d.hasEntry(dirA.getName()));
       assertEquals(true, d.hasEntry(dirB.getName()));
       assertEquals(false, d.hasEntry(eRoot.getName()));
       
       // Check filtering down
       assertEquals(true, d.getEntry(dirA.getName()) instanceof FilteringDirectoryNode);
       assertEquals(false, d.getEntry(dirB.getName()) instanceof FilteringDirectoryNode);
       
       DirectoryEntry fdA = (DirectoryEntry)d.getEntry(dirA.getName()); 
       assertEquals(false, fdA.hasEntry(eA.getName()));
       assertEquals(true, fdA.hasEntry(dirAA.getName()));
       
       DirectoryEntry fdAA = (DirectoryEntry)fdA.getEntry(dirAA.getName()); 
       assertEquals(true, fdAA.hasEntry(eAA.getName()));
    }
}
