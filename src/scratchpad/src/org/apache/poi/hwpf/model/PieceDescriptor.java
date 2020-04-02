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

package org.apache.poi.hwpf.model;

import java.nio.charset.Charset;
import java.util.Objects;

import org.apache.poi.common.Duplicatable;
import org.apache.poi.util.Internal;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LittleEndianConsts;
import org.apache.poi.util.StringUtil;

@Internal
public final class PieceDescriptor implements Duplicatable {
    private final short descriptor;
    int fc;     // used from the outside?!?
    private final PropertyModifier prm;
    private final boolean unicode;
    private final Charset charset;

    public PieceDescriptor(PieceDescriptor other) {
        descriptor = other.descriptor;
        fc = other.fc;
        prm = (other.prm == null) ? null : other.prm.copy();
        unicode = other.unicode;
        charset = other.charset;
    }

    public PieceDescriptor(byte[] buf, int offset) {
        this(buf, offset, null);
    }

    /**
     * This initializer should only be used for HWPFOldDocuments.
     *
     * @param buf The buffer to read data from
     * @param offset The offset into the buffer to start reading from
     * @param charset which charset to use if this is not unicode
     */
    public PieceDescriptor(byte[] buf, int offset, Charset charset) {
        descriptor = LittleEndian.getShort(buf, offset);
        offset += LittleEndianConsts.SHORT_SIZE;
        fc = LittleEndian.getInt(buf, offset);
        offset += LittleEndianConsts.INT_SIZE;
        prm = new PropertyModifier(LittleEndian.getShort(buf, offset));
        if (charset == null) {
            // see if this piece uses unicode.
            //From the documentation: If the second most significant bit
            //is clear, then this indicates the actual file offset of the Unicode character (two bytes). If the
            //second most significant bit is set, then the actual address of the codepage-1252
            //compressed version of the Unicode character (one byte), is actually at the offset indicated
            //by clearing this bit and dividing by two.
            if ((fc & 0x40000000) == 0) {
                unicode = true;
                this.charset = null;
            } else {
                unicode = false;
                fc &= ~(0x40000000);//gives me FC in doc stream
                fc /= 2;
                this.charset = StringUtil.WIN_1252;
            }
        } else {
            if (charset == StringUtil.UTF16LE) {
                unicode = true;
            } else {
                unicode = false;
            }
            this.charset = charset;
        }

    }

    public int getFilePosition() {
        return fc;
    }

    public void setFilePosition(int pos) {
        fc = pos;
    }

    public boolean isUnicode() {
        return unicode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(descriptor,prm,unicode);
    }

    /**
     * @return charset to use if this is not a Unicode PieceDescriptor
     * this can be <code>null</code>
     */
    public Charset getCharset() {
        return charset;
    }

    public PropertyModifier getPrm() {
        return prm;
    }

    protected byte[] toByteArray() {
        // set up the fc
        int tempFc = fc;
        if (!unicode) {
            tempFc *= 2;
            tempFc |= (0x40000000);
        }

        int offset = 0;
        byte[] buf = new byte[8];
        LittleEndian.putShort(buf, offset, descriptor);
        offset += LittleEndianConsts.SHORT_SIZE;
        LittleEndian.putInt(buf, offset, tempFc);
        offset += LittleEndianConsts.INT_SIZE;
        LittleEndian.putShort(buf, offset, prm.getValue());

        return buf;
    }

    public static int getSizeInBytes() {
        return 8;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PieceDescriptor other = (PieceDescriptor) obj;
        if (descriptor != other.descriptor)
            return false;
        if (prm == null) {
            if (other.prm != null)
                return false;
        } else if (!prm.equals(other.prm))
            return false;
        if (unicode != other.unicode)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "PieceDescriptor (pos: " + getFilePosition() + "; "
                + (isUnicode() ? "unicode" : "non-unicode") + "; prm: "
                + getPrm() + ")";
    }

    @Override
    public PieceDescriptor copy() {
        return new PieceDescriptor(this);
    }
}
