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

package org.apache.poi.openxml4j.opc;

import java.util.zip.Deflater;

/**
 * Specifies the compression level for content that is stored in a PackagePart.
 *
 * @author Julien Chable
 * @version 1.0
 */
public enum CompressionOption {
	/** Compression is optimized for performance. */
	FAST(Deflater.BEST_SPEED),
	/** Compression is optimized for size. */
	MAXIMUM(Deflater.BEST_COMPRESSION),
	/** Compression is optimized for a balance between size and performance. */
	NORMAL(Deflater.DEFAULT_COMPRESSION),
	/** Compression is turned off. */
	NOT_COMPRESSED(Deflater.NO_COMPRESSION);

	private final int value;

	CompressionOption(int value) {
		this.value = value;
	}

	public int value() {
		return this.value;
	}
}
