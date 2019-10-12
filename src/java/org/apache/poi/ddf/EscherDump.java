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

package org.apache.poi.ddf;

import java.io.PrintStream;

/**
 * Used to dump the contents of escher records to a PrintStream.
 */
public final class EscherDump {

    /**
     * Decodes the escher stream from a byte array and dumps the results to
     * a print stream.
     *
     * @param data      The data array containing the escher records.
     * @param offset    The starting offset within the data array.
     * @param size      The number of bytes to read.
     * @param out       The output stream to write the results to.
     *
     */
    public void dump(byte[] data, int offset, int size, PrintStream out) {
        EscherRecordFactory recordFactory = new DefaultEscherRecordFactory();
        int pos = offset;
        while ( pos < offset + size )
        {
            EscherRecord r = recordFactory.createRecord(data, pos);
            int bytesRead = r.fillFields(data, pos, recordFactory );
            out.println(r);
            pos += bytesRead;
        }
    }


    public void dump( int recordSize, byte[] data, PrintStream out ) {
        dump( data, 0, recordSize, out );
    }
}
