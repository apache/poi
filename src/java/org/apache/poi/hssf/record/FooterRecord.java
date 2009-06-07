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

package org.apache.poi.hssf.record;

/**
 * Title:        Footer Record (0x0015) <p/>
 * Description:  Specifies the footer for a sheet<P>
 * REFERENCE:  PG 317 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)<p/>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Shawn Laubach (slaubach at apache dot org) Modified 3/14/02
 * @author Jason Height (jheight at chariot dot net dot au)
 *
 */
public final class FooterRecord extends HeaderFooterBase {
	public final static short sid = 0x0015;

	public FooterRecord(String text) {
		super(text);
	}

	public FooterRecord(RecordInputStream in) {
		super(in);
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();

		buffer.append("[FOOTER]\n");
		buffer.append("    .footer = ").append(getText()).append("\n");
		buffer.append("[/FOOTER]\n");
		return buffer.toString();
	}

	public short getSid() {
		return sid;
	}

	public Object clone() {
		return new FooterRecord(getText());
	}
}
