/**
 * 
 */
package org.apache.poi.hdgf.streams;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.apache.poi.hdgf.LZW4HDGF;

class CompressedStreamStore extends StreamStore {
	/** The raw, compressed contents */
	private byte[] compressedContents;
	/** 
	 * We're not sure what this is, but it comes before the
	 *  real contents in the de-compressed data
	 */
	private byte[] blockHeader = new byte[4];
	
	protected byte[] _getCompressedContents() { return compressedContents; }
	protected byte[] _getBlockHeader() { return blockHeader; }
	
	/**
	 * Creates a new compressed StreamStore, which will handle
	 *  the decompression.
	 */
	protected CompressedStreamStore(byte[] data, int offset, int length) throws IOException {
		this(decompress(data,offset,length));
		
		compressedContents = new byte[length];
		System.arraycopy(data, offset, compressedContents, 0, length);
	}
	/**
	 * Handles passing the de-compressed data onto our superclass.
	 */
	private CompressedStreamStore(byte[][] decompressedData) {
		super(decompressedData[1], 0, decompressedData[1].length);
		blockHeader = decompressedData[0];
	}

	/**
	 * Decompresses the given data, returning it as header + contents
	 */
	public static byte[][] decompress(byte[] data, int offset, int length) throws IOException {
		ByteArrayInputStream bais = new ByteArrayInputStream(data, offset, length);
		
		// Decompress
		LZW4HDGF lzw = new LZW4HDGF();
		byte[] decompressed = lzw.decode(bais);
		
		// Split into header and contents
		byte[][] ret = new byte[2][];
		ret[0] = new byte[4];
		ret[1] = new byte[decompressed.length - 4];
		
		System.arraycopy(decompressed, 0, ret[0], 0, 4);
		System.arraycopy(decompressed, 4, ret[1], 0, ret[1].length);
		
		// All done
		return ret;
	}
}