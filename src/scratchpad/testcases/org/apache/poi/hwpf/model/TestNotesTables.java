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

import junit.framework.TestCase;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.HWPFTestDataSamples;
import org.apache.poi.hwpf.usermodel.Notes;

/**
 * Test cases for {@link NotesTables} and default implementation of
 * {@link Notes}
 * 
 * @author Sergey Vladimirov (vlsergey {at} gmail {dot} com)
 */
public class TestNotesTables extends TestCase
{
    public void test()
    {
        HWPFDocument doc = HWPFTestDataSamples
                .openSampleFile( "endingnote.doc" );
        Notes notes = doc.getEndnotes();

        assertEquals( 1, notes.getNotesCount() );

        assertEquals( 10, notes.getNoteAnchorPosition( 0 ) );
        assertEquals( 0, notes.getNoteTextStartOffset( 0 ) );
        assertEquals( 19, notes.getNoteTextEndOffset( 0 ) );
    }
}
