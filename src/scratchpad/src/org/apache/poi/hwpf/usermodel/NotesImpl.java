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
package org.apache.poi.hwpf.usermodel;

import org.apache.poi.hwpf.model.NotesTables;

/**
 * Default implementation of {@link Notes} interface
 * 
 * @author Sergey Vladimirov (vlsergey {at} gmail {doc} com)
 */
public class NotesImpl implements Notes
{
    private final NotesTables notesTables;

    public NotesImpl( NotesTables notesTables )
    {
        this.notesTables = notesTables;
    }

    public int getNoteAnchorPosition( int index )
    {
        return notesTables.getDescriptor( index ).getStart();
    }

    public int getNotesCount()
    {
        return notesTables.getDescriptorsCount();
    }

    public int getNoteTextEndOffset( int index )
    {
        return notesTables.getTextPosition( index ).getEnd();
    }

    public int getNoteTextStartOffset( int index )
    {
        return notesTables.getTextPosition( index ).getStart();
    }
}
