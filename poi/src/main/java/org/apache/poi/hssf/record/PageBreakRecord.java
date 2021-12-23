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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Spliterator;
import java.util.function.Supplier;

import org.apache.poi.common.usermodel.GenericRecord;
import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.LittleEndianOutput;

/**
 * Record that contains the functionality page breaks (horizontal and vertical)<p>
 *
 * The other two classes just specifically set the SIDS for record creation.<p>
 *
 * REFERENCE:  Microsoft Excel SDK page 322 and 420
 *
 * @see HorizontalPageBreakRecord
 * @see VerticalPageBreakRecord
 */
public abstract class PageBreakRecord extends StandardRecord {
    private static final int[] EMPTY_INT_ARRAY = { };

    private final ArrayList<Break> _breaks = new ArrayList<>();
    private final Map<Integer, Break> _breakMap = new HashMap<>();

    /**
     * Since both records store 2byte integers (short), no point in
     * differentiating it in the records.
     * <p>
     * The subs (rows or columns, don't seem to be able to set but excel sets
     * them automatically)
     */
    public static final class Break implements GenericRecord {

        public static final int ENCODED_SIZE = 6;
        private int main;
        private int subFrom;
        private int subTo;

        public Break(Break other) {
            main = other.main;
            subFrom = other.subFrom;
            subTo = other.subTo;
        }

        public Break(int main, int subFrom, int subTo) {
            this.main = main;
            this.subFrom = subFrom;
            this.subTo = subTo;
        }

        public Break(RecordInputStream in) {
            main = in.readUShort() - 1;
            subFrom = in.readUShort();
            subTo = in.readUShort();
        }

        public int getMain() {
            return main;
        }

        public int getSubFrom() {
            return subFrom;
        }

        public int getSubTo() {
            return subTo;
        }

        public void serialize(LittleEndianOutput out) {
            out.writeShort(main + 1);
            out.writeShort(subFrom);
            out.writeShort(subTo);
        }

        @Override
        public Map<String, Supplier<?>> getGenericProperties() {
            return GenericRecordUtil.getGenericProperties(
                "main", () -> main,
                "subFrom", () -> subFrom,
                "subTo", () -> subTo
            );
        }
    }

    protected PageBreakRecord() {}

    protected PageBreakRecord(PageBreakRecord other) {
        _breaks.addAll(other._breaks);
        initMap();
    }

    protected PageBreakRecord(RecordInputStream in) {
        final int nBreaks = in.readShort();
        _breaks.ensureCapacity(nBreaks + 2);
        for(int k = 0; k < nBreaks; k++) {
            _breaks.add(new Break(in));
        }
        initMap();
    }

    private void initMap() {
        _breaks.forEach(br -> _breakMap.put(br.main, br));
    }

    public boolean isEmpty() {
        return _breaks.isEmpty();
    }
    protected int getDataSize() {
        return 2 + _breaks.size() * Break.ENCODED_SIZE;
    }

    public final void serialize(LittleEndianOutput out) {
        int nBreaks = _breaks.size();
        out.writeShort(nBreaks);
        for (Break aBreak : _breaks) {
            aBreak.serialize(out);
        }
    }

    public int getNumBreaks() {
        return _breaks.size();
    }

    public final Iterator<Break> getBreaksIterator() {
        return _breaks.iterator();
    }

    /**
     * @since POI 5.2.0
     */
    public final Spliterator<Break> getBreaksSpliterator() {
        return _breaks.spliterator();
    }

   /**
    * Adds the page break at the specified parameters
    * @param main Depending on sid, will determine row or column to put page break (zero-based)
    * @param subFrom No user-interface to set (defaults to minimum, 0)
    * @param subTo No user-interface to set
    */
    public void addBreak(int main, int subFrom, int subTo) {

        Integer key = main;
        Break region = _breakMap.get(key);
        if(region == null) {
            region = new Break(main, subFrom, subTo);
            _breakMap.put(key, region);
            _breaks.add(region);
        } else {
            region.main = main;
            region.subFrom = subFrom;
            region.subTo = subTo;
        }
    }

    /**
     * Removes the break indicated by the parameter
     * @param main (zero-based)
     */
    public final void removeBreak(int main) {
        Integer rowKey = main;
        Break region = _breakMap.get(rowKey);
        _breaks.remove(region);
        _breakMap.remove(rowKey);
    }

    /**
     * Retrieves the region at the row/column indicated
     * @param main FIXME: Document this!
     * @return The Break or null if no break exists at the row/col specified.
     */
    public final Break getBreak(int main) {
        Integer rowKey = main;
        return _breakMap.get(rowKey);
    }

    public final int[] getBreaks() {
        int count = getNumBreaks();
        if (count < 1) {
            return EMPTY_INT_ARRAY;
        }
        int[] result = new int[count];
        for (int i=0; i<count; i++) {
            Break breakItem = _breaks.get(i);
            result[i] = breakItem.main;
        }
        return result;
    }

    @Override
    public abstract PageBreakRecord copy();

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "numBreaks", this::getNumBreaks,
            "breaks", () -> _breaks
        );
    }
}
