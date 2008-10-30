package org.apache.poi.ss.formula;

import org.apache.poi.hssf.record.formula.Ptg;
import org.apache.poi.util.LittleEndianByteArrayInputStream;
import org.apache.poi.util.LittleEndianInput;
import org.apache.poi.util.LittleEndianOutput;

public class Formula {

	private static final byte[] EMPTY_BYTE_ARRAY = { };
	private final byte[] _byteEncoding;
	private final int _encodedTokenLen;
	
	private Formula(byte[] byteEncoding, int encodedTokenLen) {
		_byteEncoding = byteEncoding;
		_encodedTokenLen = encodedTokenLen;
		if (false) { // set to true to eagerly check Ptg decoding 
    		LittleEndianByteArrayInputStream in = new LittleEndianByteArrayInputStream(byteEncoding);
    		Ptg.readTokens(encodedTokenLen, in);
    		int nUnusedBytes = _byteEncoding.length - in.getReadIndex();
    		if (nUnusedBytes > 0) {
    			// TODO - this seems to occur when IntersectionPtg is present
        		// This example file "IntersectionPtg.xls"
        		// used by test: TestIntersectionPtg.testReading()
        		// has 10 bytes unused at the end of the formula
    			// 10 extra bytes are just 0x01 and 0x00
    			System.out.println(nUnusedBytes + " unused bytes at end of formula");
    		}
		}
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
		byte[] byteEncoding = new byte[totalEncodedLen];
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
		if (ptgs == null) {
			return new Formula(EMPTY_BYTE_ARRAY, 0);
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
		// OK to return this for the moment because currently immutable
		return this;
	}
}
