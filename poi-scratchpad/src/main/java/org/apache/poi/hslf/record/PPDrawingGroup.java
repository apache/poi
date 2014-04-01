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

import org.apache.poi.ddf.*;
import org.apache.poi.util.LittleEndian;

import java.io.OutputStream;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.util.Iterator;

/**
 * Container records which always exists inside Document.
 * It always acts as a holder for escher DGG container
 *  which may contain which Escher BStore container information
 *  about pictures containes in the presentation (if any).
 *
 * @author Yegor Kozlov
 */
public final class PPDrawingGroup extends RecordAtom {

    private byte[] _header;
    private EscherContainerRecord dggContainer;
    //cached dgg
    private EscherDggRecord dgg;

    protected PPDrawingGroup(byte[] source, int start, int len) {
        // Get the header
        _header = new byte[8];
        System.arraycopy(source,start,_header,0,8);

        // Get the contents for now
        byte[] contents = new byte[len];
        System.arraycopy(source,start,contents,0,len);

        DefaultEscherRecordFactory erf = new DefaultEscherRecordFactory();
        EscherRecord child = erf.createRecord(contents, 0);
        child.fillFields( contents, 0, erf );
        dggContainer = (EscherContainerRecord)child.getChild(0);
    }

    /**
     * We are type 1035
     */
    public long getRecordType() {
        return RecordTypes.PPDrawingGroup.typeID;
    }

    /**
     * We're pretending to be an atom, so return null
     */
    public Record[] getChildRecords() {
        return null;
    }

    public void writeOut(OutputStream out) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        Iterator<EscherRecord> iter = dggContainer.getChildIterator();
        while (iter.hasNext()) {
        	EscherRecord r = iter.next();
            if (r.getRecordId() == EscherContainerRecord.BSTORE_CONTAINER){
                EscherContainerRecord bstore = (EscherContainerRecord)r;

                ByteArrayOutputStream b2 = new ByteArrayOutputStream();
                for (Iterator<EscherRecord> it= bstore.getChildIterator(); it.hasNext();) {
                    EscherBSERecord bse = (EscherBSERecord)it.next();
                    byte[] b = new byte[36+8];
                    bse.serialize(0, b);
                    b2.write(b);
                }
                byte[] bstorehead = new byte[8];
                LittleEndian.putShort(bstorehead, 0, bstore.getOptions());
                LittleEndian.putShort(bstorehead, 2, bstore.getRecordId());
                LittleEndian.putInt(bstorehead, 4, b2.size());
                bout.write(bstorehead);
                bout.write(b2.toByteArray());

            } else {
                bout.write(r.serialize());
            }
        }
        int size = bout.size();

        // Update the size (header bytes 5-8)
        LittleEndian.putInt(_header,4,size+8);

        // Write out our header
        out.write(_header);

        byte[] dgghead = new byte[8];
        LittleEndian.putShort(dgghead, 0, dggContainer.getOptions());
        LittleEndian.putShort(dgghead, 2, dggContainer.getRecordId());
        LittleEndian.putInt(dgghead, 4, size);
        out.write(dgghead);

        // Finally, write out the children
        out.write(bout.toByteArray());

    }

    public EscherContainerRecord getDggContainer(){
        return dggContainer;
    }

    public EscherDggRecord getEscherDggRecord(){
        if(dgg == null){
            for(Iterator<EscherRecord> it = dggContainer.getChildIterator(); it.hasNext();){
                EscherRecord r = it.next();
                if(r instanceof EscherDggRecord){
                    dgg = (EscherDggRecord)r;
                    break;
                }
            }
        }
        return dgg;
    }
}
