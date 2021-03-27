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

package org.apache.poi.hssf.record.common;

import java.util.Map;
import java.util.function.Supplier;

import org.apache.poi.common.usermodel.GenericRecord;
import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.Internal;
import org.apache.poi.util.LittleEndianInput;
import org.apache.poi.util.LittleEndianOutput;

@Internal
public class FormatRun implements Comparable<FormatRun>, GenericRecord {
    final short _character;
    short _fontIndex;

    public FormatRun(short character, short fontIndex) {
        this._character = character;
        this._fontIndex = fontIndex;
    }

    public FormatRun(FormatRun other) {
        _character = other._character;
        _fontIndex = other._fontIndex;
    }

    public FormatRun(LittleEndianInput in) {
        this(in.readShort(), in.readShort());
    }

    public short getCharacterPos() {
        return _character;
    }

    public short getFontIndex() {
        return _fontIndex;
    }

    public boolean equals(Object o) {
        if (!(o instanceof FormatRun)) {
            return false;
        }
        FormatRun other = (FormatRun) o;

        return _character == other._character && _fontIndex == other._fontIndex;
    }

    public int compareTo(FormatRun r) {
        if (_character == r._character && _fontIndex == r._fontIndex) {
            return 0;
        }
        if (_character == r._character) {
            return _fontIndex - r._fontIndex;
        }
        return _character - r._character;
    }

    @Override
    public int hashCode() {
        assert false : "hashCode not designed";
        return 42; // any arbitrary constant will do
    }

    public String toString() {
        return "character=" + _character + ",fontIndex=" + _fontIndex;
    }

    public void serialize(LittleEndianOutput out) {
        out.writeShort(_character);
        out.writeShort(_fontIndex);
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "characterPos", this::getCharacterPos,
            "fontIndex", this::getFontIndex
        );
    }
}
