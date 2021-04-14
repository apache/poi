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

/**
 * User-friendly interface to access document notes information
 */
public interface Notes
{
    /**
     * Returns the location of note anchor in main textspace
     */
    int getNoteAnchorPosition( int index );

    /**
     * Returns count of notes in document
     */
    int getNotesCount();

    /**
     * Returns index of note (if exists, otherwise -1) with specified anchor
     * position
     */
    int getNoteIndexByAnchorPosition( int anchorPosition );

    /**
     * Returns the end offset of the text corresponding to the reference within
     * the footnote text address space
     */
    int getNoteTextEndOffset( int index );

    /**
     * Returns the start offset of the text corresponding to the reference
     * within the footnote text address space
     */
    int getNoteTextStartOffset( int index );
}
