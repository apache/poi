/* ====================================================================
   Copyright 2002-2004   Apache Software Foundation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */


package org.apache.poi.hssf.usermodel;

import java.io.IOException;
import java.util.Iterator;

import org.apache.poi.hssf.record.EmbeddedObjectRefSubRecord;
import org.apache.poi.hssf.record.ObjRecord;
import org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.apache.poi.poifs.filesystem.Entry;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.HexDump;

/**
 * Represents binary object (i.e. OLE) data stored in the file.  Eg. A GIF, JPEG etc...
 *
 * @author Daniel Noll
 */
public class HSSFObjectData
{
    /**
     * Underlying object record ultimately containing a reference to the object.
     */
    private ObjRecord record;

    /**
     * Reference to the filesystem, required for retrieving the object data.
     */
    private POIFSFileSystem poifs;

    /**
     * Constructs object data by wrapping a lower level object record.
     *
     * @param record the low-level object record.
     * @param poifs the filesystem, required for retrieving the object data.
     */
    public HSSFObjectData(ObjRecord record, POIFSFileSystem poifs)
    {
        this.record = record;
        this.poifs = poifs;
    }

    /**
     * Gets the object data.
     *
     * @return the object data as an OLE2 directory.
     * @throws IOException if there was an error reading the data.
     */
    public DirectoryEntry getDirectory() throws IOException
    {
        Iterator subRecordIter = record.getSubRecords().iterator();
        while (subRecordIter.hasNext())
        {
            Object subRecord = subRecordIter.next();
            if (subRecord instanceof EmbeddedObjectRefSubRecord)
            {
                int streamId = ((EmbeddedObjectRefSubRecord) subRecord).getStreamId();
                String streamName = "MBD" + HexDump.toHex(streamId);

                Entry entry = poifs.getRoot().getEntry(streamName);
                if (entry instanceof DirectoryEntry)
                {
                    return (DirectoryEntry) entry;
                }
                else
                {
                    throw new IOException("Stream " + streamName + " was not an OLE2 directory");
                }
            }
        }

        throw new IllegalStateException("Object data does not contain a reference to an embedded object OLE2 directory");
    }
}
