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

import java.util.Objects;

import org.apache.poi.hssf.record.cont.ContinuableRecordOutput;
import org.apache.poi.util.Internal;
import org.apache.poi.util.LittleEndianInput;

@Internal
public class PhRun {
    final int phoneticTextFirstCharacterOffset;
    final int realTextFirstCharacterOffset;
    final int realTextLength;

    public PhRun(PhRun other) {
        phoneticTextFirstCharacterOffset = other.phoneticTextFirstCharacterOffset;
        realTextFirstCharacterOffset = other.realTextFirstCharacterOffset;
        realTextLength = other.realTextLength;
    }

    public PhRun(int phoneticTextFirstCharacterOffset,
                 int realTextFirstCharacterOffset, int realTextLength) {
        this.phoneticTextFirstCharacterOffset = phoneticTextFirstCharacterOffset;
        this.realTextFirstCharacterOffset = realTextFirstCharacterOffset;
        this.realTextLength = realTextLength;
    }

    PhRun(LittleEndianInput in) {
        phoneticTextFirstCharacterOffset = in.readUShort();
        realTextFirstCharacterOffset = in.readUShort();
        realTextLength = in.readUShort();
    }

    void serialize(ContinuableRecordOutput out) {
        out.writeContinueIfRequired(6);
        out.writeShort(phoneticTextFirstCharacterOffset);
        out.writeShort(realTextFirstCharacterOffset);
        out.writeShort(realTextLength);
    }

    @Override
    public int hashCode() {
        return Objects.hash(phoneticTextFirstCharacterOffset, realTextFirstCharacterOffset, realTextLength);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PhRun phRun = (PhRun) o;
        return phoneticTextFirstCharacterOffset == phRun.phoneticTextFirstCharacterOffset
            && realTextFirstCharacterOffset == phRun.realTextFirstCharacterOffset
            && realTextLength == phRun.realTextLength;
    }
}
