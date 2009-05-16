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

/**
 * Collection of convenence chunks for standard parts of the MSG file attachment.
 */
public class AttachmentChunks {

	public static final String namePrefix = "__attach_version1.0_#";
	
	/* String parts of Outlook Messages Attachments that are currently known */

	public ByteChunk attachData;
	public StringChunk attachExtension;
	public StringChunk attachFileName;
	public StringChunk attachLongFileName;
	public StringChunk attachMimeTag;
	
	private AttachmentChunks(boolean newStringType) {
		attachData = new ByteChunk(0x3701, 0x0102);
		attachExtension = new StringChunk(0x3703, newStringType);
		attachFileName = new StringChunk(0x3704, newStringType);
		attachLongFileName = new StringChunk(0x3707, newStringType);
		attachMimeTag = new StringChunk(0x370E, newStringType);
	}
	
	public static AttachmentChunks getInstance(boolean newStringType) {
		return new AttachmentChunks(newStringType);
	}
}
