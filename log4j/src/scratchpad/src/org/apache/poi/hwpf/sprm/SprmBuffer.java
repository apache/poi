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

package org.apache.poi.hwpf.sprm;

import java.util.Arrays;

import org.apache.poi.common.Duplicatable;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.Internal;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LittleEndianConsts;

@Internal
public final class SprmBuffer implements Duplicatable {

    //arbitrarily selected; may need to increase
    private static final int MAX_RECORD_LENGTH = 100_000;

    byte[] _buf;
    boolean _istd;
    int _offset;

    private final int _sprmsStartOffset;

    public SprmBuffer(SprmBuffer other) {
        _buf = (other._buf == null) ? null : other._buf.clone();
        _istd = other._istd;
        _offset = other._offset;
        _sprmsStartOffset = other._sprmsStartOffset;
    }

    public SprmBuffer(byte[] buf, boolean istd, int sprmsStartOffset) {
        _offset = buf.length;
        _buf = buf;
        _istd = istd;
        _sprmsStartOffset = sprmsStartOffset;
    }

    public SprmBuffer(byte[] buf, int _sprmsStartOffset) {
        this(buf, false, _sprmsStartOffset);
    }

    public SprmBuffer(int sprmsStartOffset) {
        _buf = IOUtils.safelyAllocate(sprmsStartOffset + 4L, MAX_RECORD_LENGTH);
        _offset = sprmsStartOffset;
        _sprmsStartOffset = sprmsStartOffset;
    }

    public void addSprm(short opcode, byte operand) {
        int addition = LittleEndianConsts.SHORT_SIZE + LittleEndianConsts.BYTE_SIZE;
        ensureCapacity(addition);
        LittleEndian.putShort(_buf, _offset, opcode);
        _offset += LittleEndianConsts.SHORT_SIZE;
        _buf[_offset++] = operand;
    }

    public void addSprm(short opcode, byte[] operand) {
        int addition = LittleEndianConsts.SHORT_SIZE + LittleEndianConsts.BYTE_SIZE + operand.length;
        ensureCapacity(addition);
        LittleEndian.putShort(_buf, _offset, opcode);
        _offset += LittleEndianConsts.SHORT_SIZE;
        _buf[_offset++] = (byte) operand.length;
        System.arraycopy(operand, 0, _buf, _offset, operand.length);
    }

    public void addSprm(short opcode, int operand) {
        int addition = LittleEndianConsts.SHORT_SIZE + LittleEndianConsts.INT_SIZE;
        ensureCapacity(addition);
        LittleEndian.putShort(_buf, _offset, opcode);
        _offset += LittleEndianConsts.SHORT_SIZE;
        LittleEndian.putInt(_buf, _offset, operand);
        _offset += LittleEndianConsts.INT_SIZE;
    }

    public void addSprm(short opcode, short operand) {
        int addition = LittleEndianConsts.SHORT_SIZE + LittleEndianConsts.SHORT_SIZE;
        ensureCapacity(addition);
        LittleEndian.putShort(_buf, _offset, opcode);
        _offset += LittleEndianConsts.SHORT_SIZE;
        LittleEndian.putShort(_buf, _offset, operand);
        _offset += LittleEndianConsts.SHORT_SIZE;
    }

    public void append(byte[] grpprl) {
        append(grpprl, 0);
    }

    public void append(byte[] grpprl, int offset) {
        ensureCapacity(grpprl.length - offset);
        System.arraycopy(grpprl, offset, _buf, _offset, grpprl.length - offset);
        _offset += grpprl.length - offset;
    }

    @Override
    public SprmBuffer copy() {
        return new SprmBuffer(this);
    }

    private void ensureCapacity(int addition) {
        if (_offset + addition >= _buf.length) {
            // add 6 more than they need for use the next iteration
            //
            // commented - buffer shall not contain any additional bytes --
            // sergey
            // byte[] newBuf = new byte[_offset + addition + 6];
            IOUtils.safelyAllocateCheck(_offset + (long)addition, MAX_RECORD_LENGTH);
            _buf = Arrays.copyOf(_buf, _offset + addition);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SprmBuffer)) return false;
        SprmBuffer sprmBuf = (SprmBuffer) obj;
        return (Arrays.equals(_buf, sprmBuf._buf));
    }

    @Override
    public int hashCode() {
        assert false : "hashCode not designed";
        return 42; // any arbitrary constant will do
    }

    public SprmOperation findSprm(short opcode) {
        int operation = SprmOperation.getOperationFromOpcode(opcode);
        int type = SprmOperation.getTypeFromOpcode(opcode);

        SprmIterator si = new SprmIterator(_buf, 2);
        while (si.hasNext()) {
            SprmOperation i = si.next();
            if (i.getOperation() == operation && i.getType() == type)
                return i;
        }
        return null;
    }

    private int findSprmOffset(short opcode) {
        SprmOperation sprmOperation = findSprm(opcode);
        if (sprmOperation == null)
            return -1;

        return sprmOperation.getGrpprlOffset();
    }

    public byte[] toByteArray() {
        return _buf;
    }

    public SprmIterator iterator() {
        return new SprmIterator(_buf, _sprmsStartOffset);
    }

    public void updateSprm(short opcode, byte operand) {
        int grpprlOffset = findSprmOffset(opcode);
        if (grpprlOffset != -1) {
            _buf[grpprlOffset] = operand;
            return;
        }
        addSprm(opcode, operand);
    }

    public void updateSprm(short opcode, boolean operand) {
        int grpprlOffset = findSprmOffset(opcode);
        if (grpprlOffset != -1) {
            _buf[grpprlOffset] = (byte) (operand ? 1 : 0);
            return;
        }
        addSprm(opcode, operand ? 1 : 0);
    }

    public void updateSprm(short opcode, int operand) {
        int grpprlOffset = findSprmOffset(opcode);
        if (grpprlOffset != -1) {
            LittleEndian.putInt(_buf, grpprlOffset, operand);
            return;
        }
        addSprm(opcode, operand);
    }

    public void updateSprm(short opcode, short operand) {
        int grpprlOffset = findSprmOffset(opcode);
        if (grpprlOffset != -1) {
            LittleEndian.putShort(_buf, grpprlOffset, operand);
            return;
        }
        addSprm(opcode, operand);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Sprms (");
        stringBuilder.append(_buf.length);
        stringBuilder.append(" byte(s)): ");
        for (SprmIterator iterator = iterator(); iterator.hasNext(); ) {
            try {
                stringBuilder.append(iterator.next());
            } catch (Exception exc) {
                stringBuilder.append("error");
            }
            stringBuilder.append("; ");
        }
        return stringBuilder.toString();
    }


}
