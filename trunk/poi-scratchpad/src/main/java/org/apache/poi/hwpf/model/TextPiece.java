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

import org.apache.poi.hwpf.util.DoubleByteUtil;
import org.apache.poi.util.Internal;
import org.apache.poi.util.StringUtil;

/**
 * Lightweight representation of a text piece.
 * Works in the character domain, not the byte domain, so you
 * need to have turned byte references into character
 * references before getting here.
 */
@Internal
public class TextPiece extends PropertyNode<TextPiece> {
    private final boolean _usesUnicode;

    private final PieceDescriptor _pd;

    public TextPiece(TextPiece other) {
        super(other);
        _usesUnicode = other._usesUnicode;
        _pd = (other._pd == null) ? null : other._pd.copy();
    }

    /**
     * @param start Beginning offset in main document stream, in characters.
     * @param end   Ending offset in main document stream, in characters.
     * @param text  The raw bytes of our text
     * instead
     */
    public TextPiece(int start, int end, byte[] text, PieceDescriptor pd,
                     int cpStart) {
        this(start, end, text, pd);
    }

    /**
     * @param start Beginning offset in main document stream, in characters.
     * @param end   Ending offset in main document stream, in characters.
     * @param text  The raw bytes of our text
     */
    public TextPiece(int start, int end, byte[] text, PieceDescriptor pd) {
        super(start, end, buildInitSB(text, pd));
        _usesUnicode = pd.isUnicode();
        _pd = pd;

        // Validate
        int textLength = ((CharSequence) _buf).length();
        if (end - start != textLength) {
            throw new IllegalStateException("Told we're for characters " + start + " -> " + end + ", but actually covers " + textLength + " characters!");
        }
        if (end < start) {
            throw new IllegalStateException("Told we're of negative size! start=" + start + " end=" + end);
        }
    }

    /**
     * Create the StringBuilder from the text and unicode flag
     */
    private static StringBuilder buildInitSB(byte[] text, PieceDescriptor pd) {
        if (DoubleByteUtil.BIG5.equals(pd.getCharset())) {
            return new StringBuilder(DoubleByteUtil.cp950ToString(text, 0, text.length));
        }

        String str = new String(text, 0, text.length, (pd.isUnicode()) ? StringUtil.UTF16LE : pd.getCharset());
        return new StringBuilder(str);
    }

    /**
     * @return If this text piece is unicode
     */
    public boolean isUnicode() {
        return _usesUnicode;
    }

    public PieceDescriptor getPieceDescriptor() {
        return _pd;
    }

    @Deprecated
    public StringBuffer getStringBuffer() {
        return new StringBuffer(getStringBuilder());
    }

    public StringBuilder getStringBuilder() {
        return (StringBuilder) _buf;
    }

    public byte[] getRawBytes() {
        return _buf.toString().getBytes(
                Charset.forName(_usesUnicode ? "UTF-16LE" : "Cp1252")
        );
    }

    /**
     * Returns part of the string.
     * Works only in characters, not in bytes!
     *
     * @param start Local start position, in characters
     * @param end   Local end position, in characters
     */
    @Deprecated
    public String substring(int start, int end) {
        StringBuilder buf = (StringBuilder) _buf;

        // Validate
        if (start < 0) {
            throw new StringIndexOutOfBoundsException("Can't request a substring before 0 - asked for " + start);
        }
        if (end > buf.length()) {
            throw new StringIndexOutOfBoundsException("Index " + end + " out of range 0 -> " + buf.length());
        }
        if (end < start) {
            throw new StringIndexOutOfBoundsException("Asked for text from " + start + " to " + end + ", which has an end before the start!");
        }
        return buf.substring(start, end);
    }

    /**
     * Adjusts the internal string for deletinging
     * some characters within this.
     *
     * @param start  The start position for the delete, in characters
     * @param length The number of characters to delete
     */
    @Override
    @Deprecated
    public void adjustForDelete(int start, int length) {
        int myStart = getStart();
        int myEnd = getEnd();
        int end = start + length;

       /* do we have to delete from this text piece? */
        if (start <= myEnd && end >= myStart) {

           /* find where the deleted area overlaps with this text piece */
            int overlapStart = Math.max(myStart, start);
            int overlapEnd = Math.min(myEnd, end);

            int bufStart = overlapStart - myStart;
            int bufEnd = overlapEnd - myStart;
            ((StringBuilder) _buf).delete(bufStart, bufEnd);
        }

        // We need to invoke this even if text from this piece is not being
        // deleted because the adjustment must propagate to all subsequent
        // text pieces i.e., if text from tp[n] is being deleted, then
        // tp[n + 1], tp[n + 2], etc. will need to be adjusted.
        // The superclass is expected to use a separate sentry for this.
        super.adjustForDelete(start, length);
    }

    /**
     * Returns the length, in characters
     */
    @Deprecated
    public int characterLength() {
        return (getEnd() - getStart());
    }

    /**
     * Returns the length, in bytes
     */
    public int bytesLength() {
        return (getEnd() - getStart()) * (_usesUnicode ? 2 : 1);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof TextPiece)) return false;
        TextPiece tp = (TextPiece) o;
        assert (_buf != null && tp._buf != null && _pd != null && tp._pd != null);

        return (
                limitsAreEqual(o)
                        && tp._usesUnicode == this._usesUnicode
                        && tp._buf.toString().equals(this._buf.toString())
                        && tp._pd.equals(this._pd)
        );
    }

    @Override
    public int hashCode() {
        assert false : "hashCode not designed";
        return 42; // any arbitrary constant will do
    }


    /**
     * Returns the character position we start at.
     */
    public int getCP() {
        return getStart();
    }

    public String toString() {
        return "TextPiece from " + getStart() + " to " + getEnd() + " ("
                + getPieceDescriptor() + ")";
    }

    @Override
    public TextPiece copy() {
        return new TextPiece(this);
    }
}
