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

/**
 * A structure that specifies additional paragraph-level formatting
 * such as Bullet Auto Number Scheme.
 */
package org.apache.poi.hslf.model.textproperties;

import org.apache.poi.sl.usermodel.AutoNumberingScheme;
import org.apache.poi.util.LittleEndian;

/**
 * This structure store text autonumber scheme and start number.
 * If a paragraph has an autonumber(fBulletHasAutoNumber = 0x0001) but start number and scheme are empty,
 * this means the default values will be used: statNumber=1 and sheme=ANM_ArabicPeriod
 * @see <a href="http://social.msdn.microsoft.com/Forums/mr-IN/os_binaryfile/thread/650888db-fabd-4b95-88dc-f0455f6e2d28">
 *     http://social.msdn.microsoft.com/Forums/mr-IN/os_binaryfile/thread/650888db-fabd-4b95-88dc-f0455f6e2d28</a>
 *
 * @author Alex Nikiforov [mailto:anikif@gmail.com]
 *
 */
public class TextPFException9 {
	//private final byte mask1;
	//private final byte mask2;
	private final byte mask3;
	private final byte mask4;
	private final Short bulletBlipRef;
	private final Short fBulletHasAutoNumber;
	private final AutoNumberingScheme autoNumberScheme;
	private final static AutoNumberingScheme DEFAULT_AUTONUMBER_SHEME = AutoNumberingScheme.arabicPeriod;
	private final Short autoNumberStartNumber;
	private final static Short DEFAULT_START_NUMBER = 1;
	private final int recordLength;
	public TextPFException9(final byte[] source, final int startIndex) { // NOSONAR
		//this.mask1 = source[startIndex];
		//this.mask2 = source[startIndex + 1];
		this.mask3 = source[startIndex + 2];
		this.mask4 = source[startIndex + 3];
		int length = 4;
		int index = startIndex + 4;
		if (0 == (mask3 & (byte)0x80 )) {
			this.bulletBlipRef = null;
		} else {
			this.bulletBlipRef = LittleEndian.getShort(source, index);
			index +=2;
			length = 6;
		}
		if (0 == (mask4 & 2)) {
			this.fBulletHasAutoNumber = null;
		} else {
			this.fBulletHasAutoNumber = LittleEndian.getShort(source, index);
			index +=2;
			length +=2;
		}
		if (0 == (mask4 & 1)) {
			this.autoNumberScheme = null;
			this.autoNumberStartNumber = null;
		} else {
			this.autoNumberScheme = AutoNumberingScheme.forNativeID(LittleEndian.getShort(source, index));
			index +=2;
			this.autoNumberStartNumber = LittleEndian.getShort(source, index);
			index +=2;
			length +=4;
		}
		this.recordLength = length;
	}
	public Short getBulletBlipRef() {
		return bulletBlipRef;
	}
	public Short getfBulletHasAutoNumber() {
		return fBulletHasAutoNumber;
	}
	public AutoNumberingScheme getAutoNumberScheme() {
		if (null != this.autoNumberScheme) {
			return this.autoNumberScheme;
		}
		if (null != this.fBulletHasAutoNumber && 1 == this.fBulletHasAutoNumber.shortValue()) {
			return DEFAULT_AUTONUMBER_SHEME;
		}
		return null;
	}
	public Short getAutoNumberStartNumber() {
		if (null != this.autoNumberStartNumber) {
			return this.autoNumberStartNumber;
		}
		if (null != this.fBulletHasAutoNumber && 1 == this.fBulletHasAutoNumber.shortValue()) {
			return DEFAULT_START_NUMBER;
		}
		return null;
	}
	public int getRecordLength() {
		return recordLength;
	}
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("Record length: ").append(this.recordLength).append(" bytes\n");
		sb.append("bulletBlipRef: ").append(this.bulletBlipRef).append("\n");
		sb.append("fBulletHasAutoNumber: ").append(this.fBulletHasAutoNumber).append("\n");
		sb.append("autoNumberScheme: ").append(this.autoNumberScheme).append("\n");
		sb.append("autoNumberStartNumber: ").append(this.autoNumberStartNumber).append("\n");
		return sb.toString();
	}
}
