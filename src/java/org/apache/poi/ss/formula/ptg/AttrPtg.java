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

package org.apache.poi.ss.formula.ptg;

import static org.apache.poi.util.GenericRecordUtil.getBitsAsString;
import static org.apache.poi.util.GenericRecordUtil.getEnumBitsAsString;

import java.util.Map;
import java.util.function.Supplier;

import org.apache.poi.util.BitField;
import org.apache.poi.util.BitFieldFactory;
import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.LittleEndianConsts;
import org.apache.poi.util.LittleEndianInput;
import org.apache.poi.util.LittleEndianOutput;

/**
 * "Special Attributes"<p>
 * This seems to be a Misc Stuff and Junk record.  One function it serves is
 * in SUM functions (i.e. SUM(A1:A3) causes an area PTG then an ATTR with the SUM option set)
 */
public final class AttrPtg extends ControlPtg {
    public final static byte sid  = 0x19;
    private final static int  SIZE = 4;

    // flags 'volatile' and 'space', can be combined.
    // OOO spec says other combinations are theoretically possible but not likely to occur.
    private static final BitField semiVolatile = BitFieldFactory.getInstance(0x01);
    private static final BitField optiIf       = BitFieldFactory.getInstance(0x02);
    private static final BitField optiChoose   = BitFieldFactory.getInstance(0x04);
    private static final BitField optiSkip     = BitFieldFactory.getInstance(0x08);
    private static final BitField optiSum      = BitFieldFactory.getInstance(0x10);
    private static final BitField baxcel       = BitFieldFactory.getInstance(0x20); // 'assignment-style formula in a macro sheet'
    private static final BitField space        = BitFieldFactory.getInstance(0x40);

    public static final AttrPtg SUM = new AttrPtg(0x0010, 0, null, -1);

    public static final class SpaceType {
        private SpaceType() {
            // no instances of this class
        }

        /** 00H = Spaces before the next token (not allowed before tParen token) */
        public static final int SPACE_BEFORE = 0x00;
        /** 01H = Carriage returns before the next token (not allowed before tParen token) */
        public static final int CR_BEFORE = 0x01;
        /** 02H = Spaces before opening parenthesis (only allowed before tParen token) */
        public static final int SPACE_BEFORE_OPEN_PAREN = 0x02;
        /** 03H = Carriage returns before opening parenthesis (only allowed before tParen token) */
        public static final int CR_BEFORE_OPEN_PAREN = 0x03;
        /** 04H = Spaces before closing parenthesis (only allowed before tParen, tFunc, and tFuncVar tokens) */
        public static final int SPACE_BEFORE_CLOSE_PAREN = 0x04;
        /** 05H = Carriage returns before closing parenthesis (only allowed before tParen, tFunc, and tFuncVar tokens) */
        public static final int CR_BEFORE_CLOSE_PAREN = 0x05;
        /** 06H = Spaces following the equality sign (only in macro sheets) */
        public static final int SPACE_AFTER_EQUALITY = 0x06;
    }

    private final byte _options;
    private final short _data;

    /** only used for tAttrChoose: table of offsets to starts of args */
    private final int[] _jumpTable;
    /** only used for tAttrChoose: offset to the tFuncVar for CHOOSE() */
    private final int   _chooseFuncOffset;

    public AttrPtg(LittleEndianInput in) {
        _options = in.readByte();
        _data    = in.readShort();
        if (isOptimizedChoose()) {
            int[] jumpTable = new int[(int) _data];
            for (int i = 0; i < jumpTable.length; i++) {
                jumpTable[i] = in.readUShort();
            }
            _jumpTable = jumpTable;
            _chooseFuncOffset = in.readUShort();
        } else {
            _jumpTable = null;
            _chooseFuncOffset = -1;
        }

    }
    private AttrPtg(int options, int data, int[] jt, int chooseFuncOffset) {
        _options = (byte) options;
        _data = (short) data;
        _jumpTable = jt;
        _chooseFuncOffset = chooseFuncOffset;
    }

    /**
     * @param type a constant from <tt>SpaceType</tt>
     * @param count the number of space characters
     */
    public static AttrPtg createSpace(int type, int count) {
        int data = type & 0x00FF | (count << 8) & 0x00FFFF;
        return new AttrPtg(space.set(0), data, null, -1);
    }

    /**
     * @param dist distance (in bytes) to start of either <ul><li>false parameter</li>
     * <li>tFuncVar(IF) token (when false parameter is not present)</li></ul>
     */
    public static AttrPtg createIf(int dist) {
        return new AttrPtg(optiIf.set(0), dist, null, -1);
    }

    /**
     * @param dist distance (in bytes) to position behind tFuncVar(IF) token (minus 1)
     */
    public static AttrPtg createSkip(int dist) {
        return new AttrPtg(optiSkip.set(0), dist, null, -1);
    }

    public static AttrPtg getSumSingle() {
        return new AttrPtg(optiSum.set(0), 0, null, -1);
    }


    public boolean isSemiVolatile() {
        return semiVolatile.isSet(_options);
    }

    public boolean isOptimizedIf() {
        return optiIf.isSet(_options);
    }

    public boolean isOptimizedChoose() {
        return optiChoose.isSet(_options);
    }

    public boolean isSum() {
        return optiSum.isSet(_options);
    }
    public boolean isSkip() {
        return optiSkip.isSet(_options);
    }

    // lets hope no one uses this anymore
    private boolean isBaxcel() {
        return baxcel.isSet(_options);
    }

    public boolean isSpace() {
        return space.isSet(_options);
    }

    public short getData() {
        return _data;
    }
    public int[] getJumpTable() {
        return _jumpTable.clone();
    }
    public int getChooseFuncOffset() {
        if (_jumpTable == null) {
            throw new IllegalStateException("Not tAttrChoose");
        }
        return _chooseFuncOffset;
    }

    public void write(LittleEndianOutput out) {
        out.writeByte(sid + getPtgClass());
        out.writeByte(_options);
        out.writeShort(_data);
        int[] jt = _jumpTable;
        if (jt != null) {
            for (int value : jt) {
                out.writeShort(value);
            }
            out.writeShort(_chooseFuncOffset);
        }
    }

    @Override
    public byte getSid() {
        return sid;
    }

    public int getSize() {
        if (_jumpTable != null) {
            return SIZE + (_jumpTable.length + 1) * LittleEndianConsts.SHORT_SIZE;
        }
        return SIZE;
    }

    public String toFormulaString(String[] operands) {
        if(space.isSet(_options)) {
            return operands[ 0 ];
        } else if (optiIf.isSet(_options)) {
            return toFormulaString() + "(" + operands[0] + ")";
        } else if (optiSkip.isSet(_options)) {
            return toFormulaString() + operands[0];   //goto isn't a real formula element should not show up
        } else {
            return toFormulaString() + "(" + operands[0] + ")";
        }
    }


    public int getNumberOfOperands() {
        return 1;
    }

    public int getType() {
        return -1;
    }

    public String toFormulaString() {
        if (semiVolatile.isSet(_options)) {
            return "ATTR(semiVolatile)";
        }
        if (optiIf.isSet(_options)) {
            return "IF";
        }
        if (optiChoose.isSet(_options)) {
            return "CHOOSE";
        }
        if (optiSkip.isSet(_options)) {
            return "";
        }
        if (optiSum.isSet(_options)) {
            return "SUM";
        }
        if (baxcel.isSet(_options)) {
            return "ATTR(baxcel)";
        }
        if (space.isSet(_options)) {
            return "";
        }
        return "UNKNOWN ATTRIBUTE";
    }

    @Override
    public AttrPtg copy() {
        // immutable
        return this;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "volatile", this::isSemiVolatile,
            "options", getBitsAsString(() -> _options,
                new BitField[]{semiVolatile, optiIf, optiChoose, optiSkip, optiSum, baxcel, space},
                new String[]{"SEMI_VOLATILE", "OPTI_IF", "OPTI_CHOOSE", "OPTI_SKIP", "OPTI_SUM", "BAXCEL", "SPACE"}),
            "space_count", () -> (_data >> 8) & 0xFF,
            "space_type",  getEnumBitsAsString(() -> _data & 0xFF,
                new int[]{SpaceType.SPACE_BEFORE,SpaceType.CR_BEFORE,SpaceType.SPACE_BEFORE_OPEN_PAREN,SpaceType.CR_BEFORE_OPEN_PAREN,SpaceType.SPACE_BEFORE_CLOSE_PAREN,SpaceType.CR_BEFORE_CLOSE_PAREN,SpaceType.SPACE_AFTER_EQUALITY},
                new String[]{"SPACE_BEFORE","CR_BEFORE","SPACE_BEFORE_OPEN_PAREN","CR_BEFORE_OPEN_PAREN","SPACE_BEFORE_CLOSE_PAREN","CR_BEFORE_CLOSE_PAREN","SPACE_AFTER_EQUALITY"})
        );
    }
}
