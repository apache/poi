package org.apache.poi.hssf.record;

public interface BiffHeaderInput {

	/**
	 * Read an unsigned short from the stream without decrypting
	 */
	int readRecordSID();
	/**
	 * Read an unsigned short from the stream without decrypting
	 */
	int readDataSize();

	int available();

}
