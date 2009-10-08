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

package org.apache.poi.hssf.usermodel;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to help test code verify that generated files do not differ from proof copies in
 * any significant detail.  Normally this task would be simple except for the presence of artifacts
 * in the file that change every time it is generated.  Usually these volatile artifacts are
 * time-stamps, user names, or other machine dependent parameters.
 *
 * @author Josh Micich
 */
public final class StreamUtility {

	/**
	 * Compares two streams with expected differences in specified regions.  The streams are
	 * expected to be of equal length and comparison is always byte for byte.  That is -
	 * differences can only involve exchanging each individual byte for another single byte.<br>
	 * Both input streams are closed.
	 *
	 * @param allowableDifferenceRegions array of integer pairs: (offset, length).
	 * Any differences encountered in these regions of the streams will be ignored
	 * @return <code>null</code> if streams are identical, else the
	 * byte indexes of differing data.  If streams were different lengths,
	 * the returned indexes will be -1 and the length of the shorter stream
	 */
	public static int[] diffStreams(InputStream isA, InputStream isB, int[] allowableDifferenceRegions) {

		if((allowableDifferenceRegions.length % 2) != 0) {
			throw new RuntimeException("allowableDifferenceRegions length is odd");
		}
		boolean success = false;
		int[] result;
		try {
			result = diffInternal(isA, isB, allowableDifferenceRegions);
			success = true;
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			close(isA, success);
			close(isB, success);
		}
		return result;
	}

	/**
	 * @param success <code>false</code> if the outer method is throwing an exception.
	 */
	private static void close(InputStream is, boolean success) {
		try {
			is.close();
		} catch (IOException e) {
			if(success) {
				// this is a new error. ok to throw
				throw new RuntimeException(e);
			}
			// else don't subvert original exception. just print stack trace for this one
			e.printStackTrace();
		}
	}

	private static int[] diffInternal(InputStream isA, InputStream isB, int[] allowableDifferenceRegions)
			throws IOException {
		int offset = 0;
		List temp = new ArrayList();
		while (true) {
			int b = isA.read();
			int b2 = isB.read();
			if (b == -1) {
				// EOF
				if (b2 == -1) {
					return toPrimitiveIntArray(temp);
				}
				return new int[] { -1, offset, };
			}
			if (b2 == -1) {
				return new int[] { -1, offset, };
			}
			if (b != b2 && !isIgnoredRegion(allowableDifferenceRegions, offset)) {
				temp.add(Integer.valueOf(offset));
			}
			offset++;
		}
	}

	private static boolean isIgnoredRegion(int[] allowableDifferenceRegions, int offset) {
		for (int i = 0; i < allowableDifferenceRegions.length; i+=2) {
			int start = allowableDifferenceRegions[i];
			int end = start + allowableDifferenceRegions[i+1];
			if(start <= offset && offset < end) {
				return true;
			}
		}
		return false;
	}

	private static int[] toPrimitiveIntArray(List temp) {
		int nItems = temp.size();
		if(nItems < 1) {
			return null;
		}
		Integer[] boxInts = new Integer[nItems];
		temp.toArray(boxInts);

		int[] result = new int[nItems];
		for (int i = 0; i < result.length; i++) {
			result[i] = boxInts[i].intValue();
		}
		return result;
	}
}
