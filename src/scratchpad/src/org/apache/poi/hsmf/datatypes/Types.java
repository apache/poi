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

package org.apache.poi.hsmf.datatypes;

public final class Types {
	public static int BINARY = 0x0102;

	/** A string, until Outlook 3.0 */
	public static int OLD_STRING = 0x001E;
	/** A string, from Outlook 3.0 onwards */
	public static int NEW_STRING = 0x001F;

	public static int LONG = 0x0003;
	public static int TIME = 0x0040;
	public static int BOOLEAN = 0x000B;

	public static String asFileEnding(int type) {
		String str = Integer.toHexString(type).toUpperCase();
		while(str.length() < 4) {
			str = "0" + str;
		}
		return str;
	}
}
