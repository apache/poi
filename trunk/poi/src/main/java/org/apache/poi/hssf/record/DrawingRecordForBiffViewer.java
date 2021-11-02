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

package org.apache.poi.hssf.record;

import org.apache.commons.io.input.UnsynchronizedByteArrayInputStream;

import java.util.Map;
import java.util.function.Supplier;

/**
 * This is purely for the biff viewer.  During normal operations we don't want
 * to be seeing this.
 */
public final class DrawingRecordForBiffViewer extends AbstractEscherHolderRecord {
    public static final short sid = 0xEC;

    public DrawingRecordForBiffViewer() {}

    public DrawingRecordForBiffViewer(DrawingRecordForBiffViewer other) {
        super(other);
    }

    public DrawingRecordForBiffViewer( RecordInputStream in)
    {
        super(in);
    }

    public DrawingRecordForBiffViewer(DrawingRecord r)
    {
        super(convertToInputStream(r));
        decode();
    }
    private static RecordInputStream convertToInputStream(DrawingRecord r)
    {
        byte[] data = r.serialize();
        RecordInputStream rinp = new RecordInputStream(
                new UnsynchronizedByteArrayInputStream(data)
        );
        rinp.nextRecord();
        return rinp;
    }

    protected String getRecordName()
    {
        return "MSODRAWING";
    }

    public short getSid()
    {
        return sid;
    }

    @Override
    public DrawingRecordForBiffViewer copy() {
        return new DrawingRecordForBiffViewer(this);
    }

    @Override
    public HSSFRecordTypes getGenericRecordType() {
        return HSSFRecordTypes.DRAWING;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return null;
    }
}
