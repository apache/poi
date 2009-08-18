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

package org.apache.poi.hwpf.usermodel;

import junit.framework.TestCase;

/**
 * Tests for Range which aren't around deletion, insertion,
 *  text replacement or textual contents
 */
public final class TestRange extends TestCase {
	public void testFieldStripping() {
		String exp = "This is some text.";

		String single = "This is some \u0013Blah!\u0015text.";
		String with14 = "This is \u0013Blah!\u0014some\u0015 text.";
		String withNested =
			"This is \u0013Blah!\u0013Blah!\u0015\u0015some text.";
		String withNested14 =
			"This is \u0013Blah!\u0013Blah!\u0014don't see me\u0015 blah!\u0015some text.";
		String withNestedIn14 =
			"This is \u0013Blah!\u0014some\u0013Blah!\u0015 \u0015text.";

		// Check all comes out right
		assertEquals(exp, Range.stripFields(exp));
		assertEquals(exp, Range.stripFields(single));
		assertEquals(exp, Range.stripFields(with14));
		assertEquals(exp, Range.stripFields(withNested));
		assertEquals(exp, Range.stripFields(withNested14));
		assertEquals(exp, Range.stripFields(withNestedIn14));

		// Ones that are odd and we won't change
		String odd1 = "This\u0015 is \u0013 odd";
		String odd2 = "This\u0015 is \u0014 also \u0013 odd";

		assertEquals(odd1, Range.stripFields(odd1));
		assertEquals(odd2, Range.stripFields(odd2));
	}
}
