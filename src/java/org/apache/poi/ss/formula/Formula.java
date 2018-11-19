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

package org.apache.poi.ss.formula;

import java.util.Arrays;

import org.apache.poi.ss.formula.ptg.ExpPtg;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.formula.ptg.TblPtg;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LittleEndianByteArrayInputStream;
import org.apache.poi.util.LittleEndianInput;
import org.apache.poi.util.LittleEndianOutput;

/**
 * Encapsulates an encoded formula token array.
 *
 * @author Josh Micich
 */
public class Formula {

	//Arbitrarily set.  May need to increase.
	private static final int MAX_ENCODED_LEN = 100000;

	private static final Formula EMPTY = new Formula(new byte[0], 0);

	/** immutable */
	private final byte[] _byteEncoding;
	private final int _encodedTokenLen;

	private Formula(byte[] byteEncoding, int encodedTokenLen) {
		_byteEncoding = byteEncoding.clone();
		_encodedTokenLen = encodedTokenLen;

		// TODO - this seems to occur when IntersectionPtg is present
		// This example file "IntersectionPtg.xls"
		// used by test: TestIntersectionPtg.testReading()
		// has 10 bytes unused at the end of the formula
		// 10 extra bytes are just 0x01 and 0x00
		// LittleEndianByteArrayInputStream in = new LittleEndianByteArrayInputStream(byteEncoding);
		// Ptg.readTokens(encodedTokenLen, in);
		// int nUnusedBytes = _byteEncoding.length - in.getReadIndex();
	}
	/**
	 * Convenience method for {@link #read(int, LittleEndianInput, int)}
	 */
	public static Formula read(int encodedTokenLen, LittleEndianInput in) {
		return read(encodedTokenLen, in, encodedTokenLen);
	}
	/**
	 * When there are no array constants present, <tt>encodedTokenLen</tt>==<tt>totalEncodedLen</tt>
	 * @param encodedTokenLen number of bytes in the stream taken by the plain formula tokens
	 * @param totalEncodedLen the total number of bytes in the formula (includes trailing encoding
	 * for array constants, but does not include 2 bytes for initial <tt>ushort encodedTokenLen</tt> field.
	 * @return A new formula object as read from the stream.  Possibly empty, never <code>null</code>.
	 */
	public static Formula read(int encodedTokenLen, LittleEndianInput in, int totalEncodedLen) {
		byte[] byteEncoding = IOUtils.safelyAllocate(totalEncodedLen, MAX_ENCODED_LEN);
		in.readFully(byteEncoding);
		return new Formula(byteEncoding, encodedTokenLen);
	}

	public Ptg[] getTokens() {
		LittleEndianInput in = new LittleEndianByteArrayInputStream(_byteEncoding);
		return Ptg.readTokens(_encodedTokenLen, in);
	}
	/**
	 * Writes  The formula encoding is includes:
	 * <ul>
	 * <li>ushort tokenDataLen</li>
	 * <li>tokenData</li>
	 * <li>arrayConstantData (if present)</li>
	 * </ul>
	 */
	public void serialize(LittleEndianOutput out) {
		out.writeShort(_encodedTokenLen);
		out.write(_byteEncoding);
	}

	public void serializeTokens(LittleEndianOutput out) {
		out.write(_byteEncoding, 0, _encodedTokenLen);
	}
	public void serializeArrayConstantData(LittleEndianOutput out) {
		int len = _byteEncoding.length-_encodedTokenLen;
		out.write(_byteEncoding, _encodedTokenLen, len);
	}


	/**
	 * @return total formula encoding length.  The formula encoding includes:
	 * <ul>
	 * <li>ushort tokenDataLen</li>
	 * <li>tokenData</li>
	 * <li>arrayConstantData (optional)</li>
	 * </ul>
	 * Note - this value is different to <tt>tokenDataLength</tt>
	 */
	public int getEncodedSize() {
		return 2 + _byteEncoding.length;
	}
	/**
	 * This method is often used when the formula length does not appear immediately before
	 * the encoded token data.
	 *
	 * @return the encoded length of the plain formula tokens.  This does <em>not</em> include
	 * the leading ushort field, nor any trailing array constant data.
	 */
	public int getEncodedTokenSize() {
		return _encodedTokenLen;
	}

	/**
	 * Creates a {@link Formula} object from a supplied {@link Ptg} array.
	 * Handles <code>null</code>s OK.
	 * @param ptgs may be <code>null</code>
	 * @return Never <code>null</code> (Possibly empty if the supplied <tt>ptgs</tt> is <code>null</code>)
	 */
	public static Formula create(Ptg[] ptgs) {
		if (ptgs == null || ptgs.length < 1) {
			return EMPTY;
		}
		int totalSize = Ptg.getEncodedSize(ptgs);
		byte[] encodedData = new byte[totalSize];
		Ptg.serializePtgs(ptgs, encodedData, 0);
		int encodedTokenLen = Ptg.getEncodedSizeWithoutArrayData(ptgs);
		return new Formula(encodedData, encodedTokenLen);
	}
	/**
	 * Gets the {@link Ptg} array from the supplied {@link Formula}.
	 * Handles <code>null</code>s OK.
	 *
	 * @param formula may be <code>null</code>
	 * @return possibly <code>null</code> (if the supplied <tt>formula</tt> is <code>null</code>)
	 */
	public static Ptg[] getTokens(Formula formula) {
		if (formula == null) {
			return null;
		}
		return formula.getTokens();
	}

	public Formula copy() {
		// OK to return this because immutable
		return this;
	}

	/**
	 * Gets the locator for the corresponding {@link org.apache.poi.hssf.record.SharedFormulaRecord},
     * {@link org.apache.poi.hssf.record.ArrayRecord} or {@link org.apache.poi.hssf.record.TableRecord}
     * if this formula belongs to such a grouping.  The {@link CellReference}
	 * returned by this method will  match the top left corner of the range of that grouping.
	 * The return value is usually not the same as the location of the cell containing this formula.
	 *
	 * @return the firstRow &amp; firstColumn of an array formula or shared formula that this formula
	 * belongs to.  <code>null</code> if this formula is not part of an array or shared formula.
	 */
	public CellReference getExpReference() {
		byte[] data = _byteEncoding;
		if (data.length != 5) {
			// tExp and tTbl are always 5 bytes long, and the only ptg in the formula
			return null;
		}
		switch (data[0]) {
			case ExpPtg.sid:
				break;
			case TblPtg.sid:
				break;
			default:
				return null;
		}
		int firstRow = LittleEndian.getUShort(data, 1);
		int firstColumn = LittleEndian.getUShort(data, 3);
		return new CellReference(firstRow, firstColumn);
	}
	public boolean isSame(Formula other) {
		return Arrays.equals(_byteEncoding, other._byteEncoding);
	}
}
