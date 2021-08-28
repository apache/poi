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
import java.util.Arrays;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;
import org.apache.poi.ddf.DefaultEscherRecordFactory;
import org.apache.poi.ddf.EscherContainerRecord;
import org.apache.poi.ddf.EscherDggRecord;
import org.apache.poi.ddf.EscherRecord;
import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LittleEndian;

/**
 * Container records which always exists inside Document.
 * It always acts as a holder for escher DGG container
 *  which may contain which Escher BStore container information
 *  about pictures containes in the presentation (if any).
 */
public final class PPDrawingGroup extends RecordAtom {

    //arbitrarily selected; may need to increase
    private static final int MAX_RECORD_LENGTH = 10_485_760;


    private final byte[] _header;
    private final EscherContainerRecord dggContainer;
    //cached dgg
    private EscherDggRecord dgg;

    PPDrawingGroup(byte[] source, int start, int len) {
        // Get the header
        _header = Arrays.copyOfRange(source, start, start+8);

        // Get the contents for now
        byte[] contents = IOUtils.safelyClone(source, start, len, MAX_RECORD_LENGTH);

        DefaultEscherRecordFactory erf = new HSLFEscherRecordFactory();
        EscherRecord child = erf.createRecord(contents, 0);
        child.fillFields( contents, 0, erf );
        dggContainer = (EscherContainerRecord)child.getChild(0);
    }

    /**
     * We are type 1035
     */
    @Override
    public long getRecordType() {
        return RecordTypes.PPDrawingGroup.typeID;
    }

    /**
     * We're pretending to be an atom, so return null
     */
    @Override
    public org.apache.poi.hslf.record.Record[] getChildRecords() {
        return null;
    }

    @Override
    public void writeOut(OutputStream out) throws IOException {
        byte[] bstorehead = new byte[8];
        byte[] recordBytes = new byte[36 + 8];
        try (UnsynchronizedByteArrayOutputStream bout = new UnsynchronizedByteArrayOutputStream();
             UnsynchronizedByteArrayOutputStream recordBuf = new UnsynchronizedByteArrayOutputStream()) {
            for (EscherRecord r : dggContainer) {
                if (r.getRecordId() == EscherContainerRecord.BSTORE_CONTAINER) {
                    EscherContainerRecord bstore = (EscherContainerRecord) r;
                    recordBuf.reset();
                    for (EscherRecord br : bstore) {
                        br.serialize(0, recordBytes);
                        recordBuf.write(recordBytes);
                    }
                    LittleEndian.putShort(bstorehead, 0, bstore.getOptions());
                    LittleEndian.putShort(bstorehead, 2, bstore.getRecordId());
                    LittleEndian.putInt(bstorehead, 4, recordBuf.size());
                    bout.write(bstorehead);
                    recordBuf.writeTo(bout);
                } else {
                    bout.write(r.serialize());
                }
            }
            int size = bout.size();

            // Update the size (header bytes 5-8)
            LittleEndian.putInt(_header, 4, size + 8);

            // Write out our header
            out.write(_header);

            byte[] dgghead = new byte[8];
            LittleEndian.putShort(dgghead, 0, dggContainer.getOptions());
            LittleEndian.putShort(dgghead, 2, dggContainer.getRecordId());
            LittleEndian.putInt(dgghead, 4, size);
            out.write(dgghead);

            // Finally, write out the children
            bout.writeTo(out);
        }
    }

    public EscherContainerRecord getDggContainer(){
        return dggContainer;
    }

    public EscherDggRecord getEscherDggRecord(){
        if(dgg == null){
            for(EscherRecord r : dggContainer){
                if(r instanceof EscherDggRecord){
                    dgg = (EscherDggRecord)r;
                    break;
                }
            }
        }
        return dgg;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
           "dggContainer", this::getDggContainer
        );
    }
}
