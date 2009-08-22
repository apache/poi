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

package org.apache.poi.hwpf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import junit.framework.TestCase;

public abstract class HWPFTestCase extends TestCase {
	protected HWPFDocFixture _hWPFDocFixture;

	protected HWPFTestCase() {
	}

	protected void setUp() throws Exception {
		super.setUp();
		/** @todo verify the constructors */
		_hWPFDocFixture = new HWPFDocFixture(this);

		_hWPFDocFixture.setUp();
	}

	protected void tearDown() throws Exception {
		if (_hWPFDocFixture != null) {
			_hWPFDocFixture.tearDown();
		}

		_hWPFDocFixture = null;
		super.tearDown();
	}

	public HWPFDocument writeOutAndRead(HWPFDocument doc) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		HWPFDocument newDoc;
		try {
			doc.write(baos);
			ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
			newDoc = new HWPFDocument(bais);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return newDoc;
	}
}
