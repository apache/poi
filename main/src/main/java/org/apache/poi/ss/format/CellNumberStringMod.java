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
package org.apache.poi.ss.format;

import org.apache.poi.ss.format.CellNumberFormatter.Special;
import org.apache.poi.util.Internal;

/**
 * Internal helper class for CellNumberFormatter
 *
 * This class represents a single modification to a result string.  The way
 * this works is complicated, but so is numeric formatting.  In general, for
 * most formats, we use a DecimalFormat object that will put the string out
 * in a known format, usually with all possible leading and trailing zeros.
 * We then walk through the result and the original format, and note any
 * modifications that need to be made.  Finally, we go through and apply
 * them all, dealing with overlapping modifications.
 */
@Internal
public class CellNumberStringMod implements Comparable<CellNumberStringMod> {
    public static final int BEFORE = 1;
    public static final int AFTER = 2;
    public static final int REPLACE = 3;

    private final Special special;
    private final int op;
    private CharSequence toAdd;
    private Special end;
    private boolean startInclusive;
    private boolean endInclusive;

    public CellNumberStringMod(Special special, CharSequence toAdd, int op) {
        this.special = special;
        this.toAdd = toAdd;
        this.op = op;
    }

    public CellNumberStringMod(Special start, boolean startInclusive, Special end, boolean endInclusive, char toAdd) {
        this(start, startInclusive, end, endInclusive);
        this.toAdd = toAdd + "";
    }

    public CellNumberStringMod(Special start, boolean startInclusive, Special end, boolean endInclusive) {
        special = start;
        this.startInclusive = startInclusive;
        this.end = end;
        this.endInclusive = endInclusive;
        op = REPLACE;
        toAdd = "";
    }

    @Override
    public int compareTo(CellNumberStringMod that) {
        int diff = special.pos - that.special.pos;
        return (diff != 0) ? diff : (op - that.op);
    }

    @Override
    public boolean equals(Object that) {
        return (that instanceof CellNumberStringMod) && compareTo((CellNumberStringMod) that) == 0;
    }

    @Override
    public int hashCode() {
        return special.hashCode() + op;
    }

    public Special getSpecial() {
        return special;
    }

    public int getOp() {
        return op;
    }

    public CharSequence getToAdd() {
        return toAdd;
    }

    public Special getEnd() {
        return end;
    }

    public boolean isStartInclusive() {
        return startInclusive;
    }

    public boolean isEndInclusive() {
        return endInclusive;
    }
}
