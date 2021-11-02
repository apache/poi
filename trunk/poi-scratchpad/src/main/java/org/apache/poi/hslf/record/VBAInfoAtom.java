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

import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.LittleEndian;

/**
 * An atom record that specifies a reference to the VBA project storage.
 */
public final class VBAInfoAtom extends RecordAtom {
    private static final long _type = RecordTypes.VBAInfoAtom.typeID;

    /**
     * Record header.
     */
    private byte[] _header;

    /**
     * Record data.
     */
    private long persistIdRef;
    private boolean hasMacros;
    private long version;

    /**
     * Constructs an empty atom - not yet supported
     */
    private VBAInfoAtom() {
        _header = new byte[8];
        // TODO: fix me
        LittleEndian.putUInt(_header, 0, _type);
        persistIdRef = 0;
        hasMacros = true;
        version = 2;
    }

    /**
     * Constructs the vba atom record from its source data.
     *
     * @param source the source data as a byte array.
     * @param start the start offset into the byte array.
     * @param len the length of the slice in the byte array.
     */
    public VBAInfoAtom(byte[] source, int start, int len) {
        // Get the header.
        _header = Arrays.copyOfRange(source, start, start+8);

        // Get the record data.
        persistIdRef = LittleEndian.getUInt(source, start+8);
        hasMacros = (LittleEndian.getUInt(source, start+12) == 1);
        version = LittleEndian.getUInt(source, start+16);
    }
    /**
     * Gets the record type.
     * @return the record type.
     */
    public long getRecordType() { return _type; }

    /**
     * Write the contents of the record back, so it can be written
     * to disk
     *
     * @param out the output stream to write to.
     * @throws java.io.IOException if an error occurs.
     */
    public void writeOut(OutputStream out) throws IOException {
        out.write(_header);
        LittleEndian.putUInt(persistIdRef, out);
        LittleEndian.putUInt(hasMacros ? 1 : 0, out);
        LittleEndian.putUInt(version, out);
    }

    public long getPersistIdRef() {
        return persistIdRef;
    }

    public void setPersistIdRef(long persistIdRef) {
        this.persistIdRef = persistIdRef;
    }

    public boolean isHasMacros() {
        return hasMacros;
    }

    public void setHasMacros(boolean hasMacros) {
        this.hasMacros = hasMacros;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "persistIdRef", this::getPersistIdRef,
            "hasMacros", this::isHasMacros,
            "version", this::getVersion
        );
    }
}
