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

package org.apache.poi.ss.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains specific examples of <tt>double</tt> value pairs and their comparison result according to Excel.
 *
 * @author Josh Micich
 */
final class NumberComparisonExamples {

	private NumberComparisonExamples() {
		// no instances of this class
	}

	/**
	 * represents one comparison test case
	 */
	public static final class ComparisonExample {
		private final long _rawBitsA;
		private final long _rawBitsB;
		private final int _expectedResult;

		public ComparisonExample(long rawBitsA, long rawBitsB, int expectedResult) {
			_rawBitsA = rawBitsA;
			_rawBitsB = rawBitsB;
			_expectedResult = expectedResult;
		}

		public double getA() {
			return Double.longBitsToDouble(_rawBitsA);
		}
		public double getB() {
			return Double.longBitsToDouble(_rawBitsB);
		}
		public double getNegA() {
			return -Double.longBitsToDouble(_rawBitsA);
		}
		public double getNegB() {
			return -Double.longBitsToDouble(_rawBitsB);
		}
		public int getExpectedResult() {
			return _expectedResult;
		}
	}

	private static final ComparisonExample[] examples = initExamples();

	private static ComparisonExample[] initExamples() {

		List<ComparisonExample> temp = new ArrayList<ComparisonExample>();

		addStepTransition(temp, 0x4010000000000005L);
		addStepTransition(temp, 0x4010000000000010L);
		addStepTransition(temp, 0x401000000000001CL);

		addStepTransition(temp, 0x403CE0FFFFFFFFF1L);

		addStepTransition(temp, 0x5010000000000006L);
		addStepTransition(temp, 0x5010000000000010L);
		addStepTransition(temp, 0x501000000000001AL);

		addStepTransition(temp, 0x544CE6345CF32018L);
		addStepTransition(temp, 0x544CE6345CF3205AL);
		addStepTransition(temp, 0x544CE6345CF3209CL);
		addStepTransition(temp, 0x544CE6345CF320DEL);

		addStepTransition(temp, 0x54B250001000101DL);
		addStepTransition(temp, 0x54B2500010001050L);
		addStepTransition(temp, 0x54B2500010001083L);

		addStepTransition(temp, 0x6230100010001000L);
		addStepTransition(temp, 0x6230100010001005L);
		addStepTransition(temp, 0x623010001000100AL);

		addStepTransition(temp, 0x7F50300020001011L);
		addStepTransition(temp, 0x7F5030002000102BL);
		addStepTransition(temp, 0x7F50300020001044L);


		addStepTransition(temp, 0x2B2BFFFF1000102AL);
		addStepTransition(temp, 0x2B2BFFFF10001079L);
		addStepTransition(temp, 0x2B2BFFFF100010C8L);

		addStepTransition(temp, 0x2B2BFF001000102DL);
		addStepTransition(temp, 0x2B2BFF0010001035L);
		addStepTransition(temp, 0x2B2BFF001000103DL);

		addStepTransition(temp, 0x2B61800040002024L);
		addStepTransition(temp, 0x2B61800040002055L);
		addStepTransition(temp, 0x2B61800040002086L);


		addStepTransition(temp, 0x008000000000000BL);
		// just outside 'subnormal' range
		addStepTransition(temp, 0x0010000000000007L);
		addStepTransition(temp, 0x001000000000001BL);
		addStepTransition(temp, 0x001000000000002FL);

		for(ComparisonExample ce : new ComparisonExample[] {
				// negative, and exponents differ by more than 1
				ce(0xBF30000000000000L, 0xBE60000000000000L, -1),

				// negative zero *is* less than positive zero, but not easy to get out of calculations
				ce(0x0000000000000000L, 0x8000000000000000L, +1),
				// subnormal numbers compare without rounding for some reason
				ce(0x0000000000000000L, 0x0000000000000001L, -1),
				ce(0x0008000000000000L, 0x0008000000000001L, -1),
				ce(0x000FFFFFFFFFFFFFL, 0x000FFFFFFFFFFFFEL, +1),
				ce(0x000FFFFFFFFFFFFBL, 0x000FFFFFFFFFFFFCL, -1),
				ce(0x000FFFFFFFFFFFFBL, 0x000FFFFFFFFFFFFEL, -1),

				// across subnormal threshold (some mistakes when close)
				ce(0x000FFFFFFFFFFFFFL, 0x0010000000000000L, +1),
				ce(0x000FFFFFFFFFFFFBL, 0x0010000000000007L, +1),
				ce(0x000FFFFFFFFFFFFAL, 0x0010000000000007L, 0),

				// when a bit further apart - normal results
				ce(0x000FFFFFFFFFFFF9L, 0x0010000000000007L, -1),
				ce(0x000FFFFFFFFFFFFAL, 0x0010000000000008L, -1),
				ce(0x000FFFFFFFFFFFFBL, 0x0010000000000008L, -1),
		}) {
			temp.add(ce);
		}

		ComparisonExample[] result = new ComparisonExample[temp.size()];
		temp.toArray(result);
		return result;
	}

	private static ComparisonExample ce(long rawBitsA, long rawBitsB, int expectedResult) {
		return new ComparisonExample(rawBitsA, rawBitsB, expectedResult);
	}

	private static void addStepTransition(List<ComparisonExample> temp, long rawBits) {
		for(ComparisonExample ce : new ComparisonExample[] {
				ce(rawBits-1, rawBits+0, 0),
				ce(rawBits+0, rawBits+1, -1),
				ce(rawBits+1, rawBits+2, 0),
		}) {
			temp.add(ce);
		}

	}

	public static ComparisonExample[] getComparisonExamples() {
		return examples.clone();
	}

	public static ComparisonExample[] getComparisonExamples2() {
		ComparisonExample[] result = examples.clone();

		for (int i = 0; i < result.length; i++) {
			int ha = ("a"+i).hashCode();
			double a = ha * Math.pow(0.75, ha % 100);
			int hb = ("b"+i).hashCode();
			double b = hb * Math.pow(0.75, hb % 100);

			result[i] = new ComparisonExample(Double.doubleToLongBits(a), Double.doubleToLongBits(b), Double.compare(a, b));
		}

		return result;
	}
}
