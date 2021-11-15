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
import java.util.Map;
import java.util.function.Supplier;

import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;
import org.apache.poi.ddf.EscherTextboxRecord;
import org.apache.poi.util.GenericRecordUtil;

/**
 * A wrapper around a DDF (Escher) EscherTextbox Record. Causes the DDF
 *  Record to be accessible as if it were a HSLF record.
 * Note: when asked to write out, will simply put any child records correctly
 *  into the Escher layer. A call to the escher layer to write out (by the
 *  parent PPDrawing) will do the actual write out
 */
public final class EscherTextboxWrapper extends RecordContainer {
    private final EscherTextboxRecord _escherRecord;
    private long _type;
    private int shapeId;
    private StyleTextPropAtom styleTextPropAtom;
    private StyleTextProp9Atom styleTextProp9Atom;

    /**
     * Returns the underlying DDF Escher Record
     */
    public EscherTextboxRecord getEscherRecord() { return _escherRecord; }

    /**
     * Creates the wrapper for the given DDF Escher Record and children
     */
    public EscherTextboxWrapper(EscherTextboxRecord textbox) {
        _escherRecord = textbox;
        _type = _escherRecord.getRecordId();

        // Find the child records in the escher data
        byte[] data = _escherRecord.getData();
        _children = Record.findChildRecords(data,0,data.length);
        for (org.apache.poi.hslf.record.Record r : this._children) {
            if (r instanceof StyleTextPropAtom) { this.styleTextPropAtom = (StyleTextPropAtom) r; }
        }
    }

    /**
     * Creates a new, empty wrapper for DDF Escher Records and their children
     */
    public EscherTextboxWrapper() {
        _escherRecord = new EscherTextboxRecord();
        _escherRecord.setRecordId(EscherTextboxRecord.RECORD_ID);
        _escherRecord.setOptions((short)15);

        _children = new org.apache.poi.hslf.record.Record[0];
    }


    /**
     * Return the type of the escher record (normally in the 0xFnnn range)
     */
    @Override
    public long getRecordType() { return _type; }

    /**
     * Stores the data for the child records back into the Escher layer.
     * Doesn't actually do the writing out, that's left to the Escher
     *  layer to do. Must be called before writeOut/serialize is called
     *  on the underlying Escher object!
     */
    @Override
    public void writeOut(OutputStream out) throws IOException {
        // Write out our children, and stuff them into the Escher layer

        // Grab the children's data
        try (UnsynchronizedByteArrayOutputStream baos = new UnsynchronizedByteArrayOutputStream()) {
            for (org.apache.poi.hslf.record.Record r : _children) {
                r.writeOut(baos);
            }
            // Save in the escher layer
            _escherRecord.setData(baos.toByteArray());
        }
    }

    /**
     * @return  Shape ID
     */
    public int getShapeId(){
        return shapeId;
    }

    /**
     *  @param id Shape ID
     */
    public void setShapeId(int id){
        shapeId = id;
    }

    public StyleTextPropAtom getStyleTextPropAtom() {
        return styleTextPropAtom;
    }

    public void setStyleTextProp9Atom(final StyleTextProp9Atom nineAtom) {
        this.styleTextProp9Atom = nineAtom;
    }
    public StyleTextProp9Atom getStyleTextProp9Atom() {
        return this.styleTextProp9Atom;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "shapeId", this::getShapeId,
            "escherRecord", this::getEscherRecord
        );
    }
}
