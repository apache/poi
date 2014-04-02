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

package org.apache.poi.hwpf.model;

import java.io.*;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.HWPFTestDataSamples;

/**
 * Unit test for {@link SavedByTable} and {@link SavedByEntry}.
 *
 * @author Daniel Noll
 */
public final class TestSavedByTable
  extends TestCase
{

  /** The expected entries in the test document. */
  private List expected = Arrays.asList(new Object[] {
    new SavedByEntry("cic22", "C:\\DOCUME~1\\phamill\\LOCALS~1\\Temp\\AutoRecovery save of Iraq - security.asd"),
    new SavedByEntry("cic22", "C:\\DOCUME~1\\phamill\\LOCALS~1\\Temp\\AutoRecovery save of Iraq - security.asd"),
    new SavedByEntry("cic22", "C:\\DOCUME~1\\phamill\\LOCALS~1\\Temp\\AutoRecovery save of Iraq - security.asd"),
    new SavedByEntry("JPratt", "C:\\TEMP\\Iraq - security.doc"),
    new SavedByEntry("JPratt", "A:\\Iraq - security.doc"),
    new SavedByEntry("ablackshaw", "C:\\ABlackshaw\\Iraq - security.doc"),
    new SavedByEntry("ablackshaw", "C:\\ABlackshaw\\A;Iraq - security.doc"),
    new SavedByEntry("ablackshaw", "A:\\Iraq - security.doc"),
    new SavedByEntry("MKhan", "C:\\TEMP\\Iraq - security.doc"),
    new SavedByEntry("MKhan", "C:\\WINNT\\Profiles\\mkhan\\Desktop\\Iraq.doc")
  });

  /**
   * Tests reading in the entries, comparing them against the expected entries.
   * Then tests writing the document out and reading the entries yet again.
   *
   * @throws Exception if an unexpected error occurs.
   */
  public void testReadWrite()
    throws Exception
  {
    // This document is widely available on the internet as "blair.doc".
    // I tried stripping the content and saving the document but my version
    // of Word (from Office XP) strips this table out.
    HWPFDocument doc = HWPFTestDataSamples.openSampleFile("saved-by-table.doc");

    // Check what we just read.
    assertEquals("List of saved-by entries was not as expected",
                 expected, doc.getSavedByTable().getEntries());

    // Now write the entire document out, and read it back in...
    ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
    doc.write(byteStream);
    InputStream copyStream = new ByteArrayInputStream(byteStream.toByteArray());
    HWPFDocument copy = new HWPFDocument(copyStream);

    // And check again.
    assertEquals("List of saved-by entries was incorrect after writing",
                 expected, copy.getSavedByTable().getEntries());
  }
}
