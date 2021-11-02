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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.poi.util.Internal;

/**
 * String table containing the history of the last few revisions ("saves") of
 * the document. Read-only for the time being.
 */
@Internal
public final class SavedByTable
{
    /**
     * Array of entries.
     */
    private SavedByEntry[] entries;

    /**
     * Constructor to read the table from the table stream.
     *
     * @param tableStream
     *            the table stream.
     * @param offset
     *            the offset into the byte array.
     * @param size
     *            the size of the table in the byte array.
     */
    public SavedByTable( byte[] tableStream, int offset, int size )
    {
        String[] strings = SttbUtils.readSttbSavedBy( tableStream, offset );
        int numEntries = strings.length / 2;
        entries = new SavedByEntry[numEntries];
        for ( int i = 0; i < numEntries; i++ )
        {
            entries[i] = new SavedByEntry( strings[i * 2], strings[i * 2 + 1] );
        }
    }

    /**
     * Gets the entries. The returned list cannot be modified.
     *
     * @return the list of entries.
     */
    public List<SavedByEntry> getEntries()
    {
        return Collections.unmodifiableList( Arrays.asList( entries ) );
    }

    /**
     * Writes this table to the table stream.
     *
     * @param tableStream
     *            the table stream to write to.
     * @throws IOException
     *             if an error occurs while writing.
     */
    public void writeTo( ByteArrayOutputStream tableStream ) throws IOException
    {
        String[] toSave = new String[entries.length * 2];
        int counter = 0;
        for ( SavedByEntry entry : entries )
        {
            toSave[counter++] = entry.getUserName();
            toSave[counter++] = entry.getSaveLocation();
        }
        SttbUtils.writeSttbSavedBy( toSave, tableStream );
    }

}
