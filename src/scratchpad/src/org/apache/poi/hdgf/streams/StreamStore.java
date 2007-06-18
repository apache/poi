package org.apache.poi.hdgf.streams;

/**
 * Holds the representation of the stream on-disk, and
 *  handles de-compressing it as required.
 * In future, may also handle writing it back out again
 */
class StreamStore {
	private byte[] contents;
	
	/**
	 * Creates a new, non compressed Stream Store
	 */
	protected StreamStore(byte[] data, int offset, int length) {
		contents = new byte[length];
		System.arraycopy(data, offset, contents, 0, length);
	}
	
	protected byte[] getContents() { return contents; }
}