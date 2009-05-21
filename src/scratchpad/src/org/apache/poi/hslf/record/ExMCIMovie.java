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

package org.apache.poi.hslf.record;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.POILogger;

/**
 * A container record that specifies information about a movie stored externally.
 *
 * @author Yegor Kozlov
 */
public class ExMCIMovie extends RecordContainer { // TODO - instantiable superclass
    private byte[] _header;

    //An ExVideoContainer record that specifies information about the MCI movie
    private ExVideoContainer exVideo;

    /**
     * Set things up, and find our more interesting children
     */
    protected ExMCIMovie(byte[] source, int start, int len) {
        // Grab the header
        _header = new byte[8];
        System.arraycopy(source, start, _header, 0, 8);

        // Find our children
        _children = Record.findChildRecords(source, start + 8, len - 8);
        findInterestingChildren();
    }

    /**
     * Create a new ExMCIMovie, with blank fields
     */
    public ExMCIMovie() {
        _header = new byte[8];
        // Setup our header block
        _header[0] = 0x0f; // We are a container record
        LittleEndian.putShort(_header, 2, (short) getRecordType());

        exVideo = new ExVideoContainer();
        _children = new Record[]{exVideo};

    }

    /**
     * Go through our child records, picking out the ones that are
     * interesting, and saving those for use by the easy helper
     * methods.
     */
    private void findInterestingChildren() {

        // First child should be the ExVideoContainer
        if (_children[0] instanceof ExVideoContainer) {
            exVideo = (ExVideoContainer) _children[0];
        } else {
            logger.log(POILogger.ERROR, "First child record wasn't a ExVideoContainer, was of type " + _children[0].getRecordType());
        }
    }

    /**
     * We are of type 4103
     */
    public long getRecordType() {
        return RecordTypes.ExMCIMovie.typeID;
    }

    /**
     * Write the contents of the record back, so it can be written
     * to disk
     */
    public void writeOut(OutputStream out) throws IOException {
        writeOut(_header[0], _header[1], getRecordType(), _children, out);
    }

    /**
     * Returns the ExVideoContainer that specifies information about the MCI movie
     */
    public ExVideoContainer getExVideo() {
        return exVideo; }


}
