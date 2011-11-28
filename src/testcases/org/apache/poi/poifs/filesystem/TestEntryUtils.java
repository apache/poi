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
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

public class TestEntryUtils extends TestCase {
    private byte[] dataSmallA = new byte[] { 12, 42, 11, -12, -121 };
    private byte[] dataSmallB = new byte[] { 11, 73, 21, -92, -103 };

    public void testCopyRecursively() throws Exception {
       POIFSFileSystem fsD = new POIFSFileSystem();
       POIFSFileSystem fs = new POIFSFileSystem();
       DirectoryEntry dirA = fs.createDirectory("DirA");
       DirectoryEntry dirB = fs.createDirectory("DirB");
       
       DocumentEntry entryR = fs.createDocument(new ByteArrayInputStream(dataSmallA), "EntryRoot");
       DocumentEntry entryA1 = dirA.createDocument("EntryA1", new ByteArrayInputStream(dataSmallA));
       DocumentEntry entryA2 = dirA.createDocument("EntryA2", new ByteArrayInputStream(dataSmallB));
       
       // Copy docs
       assertEquals(0, fsD.getRoot().getEntryCount());
       EntryUtils.copyNodeRecursively(entryR, fsD.getRoot());
       
       assertEquals(1, fsD.getRoot().getEntryCount());
       assertNotNull(fsD.getRoot().getEntry("EntryRoot"));
       
       EntryUtils.copyNodeRecursively(entryA1, fsD.getRoot());
       assertEquals(2, fsD.getRoot().getEntryCount());
       assertNotNull(fsD.getRoot().getEntry("EntryRoot"));
       assertNotNull(fsD.getRoot().getEntry("EntryA1"));
       
       EntryUtils.copyNodeRecursively(entryA2, fsD.getRoot());
       assertEquals(3, fsD.getRoot().getEntryCount());
       assertNotNull(fsD.getRoot().getEntry("EntryRoot"));
       assertNotNull(fsD.getRoot().getEntry("EntryA1"));
       assertNotNull(fsD.getRoot().getEntry("EntryA2"));
       
       // Copy directories
       fsD = new POIFSFileSystem();
       assertEquals(0, fsD.getRoot().getEntryCount());
       
       EntryUtils.copyNodeRecursively(dirB, fsD.getRoot());
       assertEquals(1, fsD.getRoot().getEntryCount());
       assertNotNull(fsD.getRoot().getEntry("DirB"));
       assertEquals(0, ((DirectoryEntry)fsD.getRoot().getEntry("DirB")).getEntryCount());
       
       EntryUtils.copyNodeRecursively(dirA, fsD.getRoot());
       assertEquals(2, fsD.getRoot().getEntryCount());
       assertNotNull(fsD.getRoot().getEntry("DirB"));
       assertEquals(0, ((DirectoryEntry)fsD.getRoot().getEntry("DirB")).getEntryCount());
       assertNotNull(fsD.getRoot().getEntry("DirA"));
       assertEquals(2, ((DirectoryEntry)fsD.getRoot().getEntry("DirA")).getEntryCount());
       
       // Copy the whole lot
       fsD = new POIFSFileSystem();
       assertEquals(0, fsD.getRoot().getEntryCount());
       
       EntryUtils.copyNodes(fs, fsD, new ArrayList<String>());
       assertEquals(3, fsD.getRoot().getEntryCount());
       assertNotNull(fsD.getRoot().getEntry(dirA.getName()));
       assertNotNull(fsD.getRoot().getEntry(dirB.getName()));
       assertNotNull(fsD.getRoot().getEntry(entryR.getName()));
       assertEquals(0, ((DirectoryEntry)fsD.getRoot().getEntry("DirB")).getEntryCount());
       assertEquals(2, ((DirectoryEntry)fsD.getRoot().getEntry("DirA")).getEntryCount());
    }

    public void testAreDocumentsIdentical() throws Exception {
       POIFSFileSystem fs = new POIFSFileSystem();
       DirectoryEntry dirA = fs.createDirectory("DirA");
       DirectoryEntry dirB = fs.createDirectory("DirB");
       
       DocumentEntry entryA1 = dirA.createDocument("Entry1", new ByteArrayInputStream(dataSmallA));
       DocumentEntry entryA1b = dirA.createDocument("Entry1b", new ByteArrayInputStream(dataSmallA));
       DocumentEntry entryA2 = dirA.createDocument("Entry2", new ByteArrayInputStream(dataSmallB));
       DocumentEntry entryB1 = dirB.createDocument("Entry1", new ByteArrayInputStream(dataSmallA));
       
       
       // Names must match
       assertEquals(false, entryA1.getName().equals(entryA1b.getName()));
       assertEquals(false, EntryUtils.areDocumentsIdentical(entryA1, entryA1b));
       
       // Contents must match
       assertEquals(false, EntryUtils.areDocumentsIdentical(entryA1, entryA2));
       
       // Parents don't matter if contents + names are the same
       assertEquals(false, entryA1.getParent().equals(entryB1.getParent()));
       assertEquals(true, EntryUtils.areDocumentsIdentical(entryA1, entryB1));
       
       
       // Can work with NPOIFS + POIFS
       ByteArrayOutputStream tmpO = new ByteArrayOutputStream();
       fs.writeFilesystem(tmpO);
       ByteArrayInputStream tmpI = new ByteArrayInputStream(tmpO.toByteArray());
       NPOIFSFileSystem nfs = new NPOIFSFileSystem(tmpI);
       
       DirectoryEntry dN1 = (DirectoryEntry)nfs.getRoot().getEntry("DirA");
       DirectoryEntry dN2 = (DirectoryEntry)nfs.getRoot().getEntry("DirB");
       DocumentEntry eNA1 = (DocumentEntry)dN1.getEntry(entryA1.getName());
       DocumentEntry eNA2 = (DocumentEntry)dN1.getEntry(entryA2.getName());
       DocumentEntry eNB1 = (DocumentEntry)dN2.getEntry(entryB1.getName());
       
       assertEquals(false, EntryUtils.areDocumentsIdentical(eNA1, eNA2));
       assertEquals(true, EntryUtils.areDocumentsIdentical(eNA1, eNB1));
       
       assertEquals(false, EntryUtils.areDocumentsIdentical(eNA1, entryA1b));
       assertEquals(false, EntryUtils.areDocumentsIdentical(eNA1, entryA2));
       
       assertEquals(true, EntryUtils.areDocumentsIdentical(eNA1, entryA1));
       assertEquals(true, EntryUtils.areDocumentsIdentical(eNA1, entryB1));
    }

    public void testAreDirectoriesIdentical() throws Exception {
       POIFSFileSystem fs = new POIFSFileSystem();
       DirectoryEntry dirA = fs.createDirectory("DirA");
       DirectoryEntry dirB = fs.createDirectory("DirB");
       
       // Names must match
       assertEquals(false, EntryUtils.areDirectoriesIdentical(dirA, dirB));
       
       // Empty dirs are fine
       DirectoryEntry dirA1 = dirA.createDirectory("TheDir"); 
       DirectoryEntry dirB1 = dirB.createDirectory("TheDir");
       assertEquals(0, dirA1.getEntryCount());
       assertEquals(0, dirB1.getEntryCount());
       assertEquals(true, EntryUtils.areDirectoriesIdentical(dirA1, dirB1));
       
       // Otherwise children must match
       dirA1.createDocument("Entry1", new ByteArrayInputStream(dataSmallA));
       assertEquals(false, EntryUtils.areDirectoriesIdentical(dirA1, dirB1));
       
       dirB1.createDocument("Entry1", new ByteArrayInputStream(dataSmallA));
       assertEquals(true, EntryUtils.areDirectoriesIdentical(dirA1, dirB1));
       
       dirA1.createDirectory("DD");
       assertEquals(false, EntryUtils.areDirectoriesIdentical(dirA1, dirB1));
       dirB1.createDirectory("DD");
       assertEquals(true, EntryUtils.areDirectoriesIdentical(dirA1, dirB1));
       
       
       // Excludes support
       List<String> excl = Arrays.asList(new String[] {"Ignore1", "IgnDir/Ign2"});
       FilteringDirectoryNode fdA = new FilteringDirectoryNode(dirA1, excl);
       FilteringDirectoryNode fdB = new FilteringDirectoryNode(dirB1, excl);
       
       assertEquals(true, EntryUtils.areDirectoriesIdentical(fdA, fdB));
       
       // Add an ignored doc, no notice is taken
       fdA.createDocument("Ignore1", new ByteArrayInputStream(dataSmallA));
       assertEquals(true, EntryUtils.areDirectoriesIdentical(fdA, fdB));
       
       // Add a directory with filtered contents, not the same
       DirectoryEntry dirAI = dirA1.createDirectory("IgnDir");
       assertEquals(false, EntryUtils.areDirectoriesIdentical(fdA, fdB));
       
       DirectoryEntry dirBI = dirB1.createDirectory("IgnDir");
       assertEquals(true, EntryUtils.areDirectoriesIdentical(fdA, fdB));
       
       // Add something to the filtered subdir that gets ignored
       dirAI.createDocument("Ign2", new ByteArrayInputStream(dataSmallA));
       assertEquals(true, EntryUtils.areDirectoriesIdentical(fdA, fdB));
       
       // And something that doesn't
       dirAI.createDocument("IgnZZ", new ByteArrayInputStream(dataSmallA));
       assertEquals(false, EntryUtils.areDirectoriesIdentical(fdA, fdB));
       
       dirBI.createDocument("IgnZZ", new ByteArrayInputStream(dataSmallA));
       assertEquals(true, EntryUtils.areDirectoriesIdentical(fdA, fdB));
    }
}
