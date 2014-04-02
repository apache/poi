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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

import junit.framework.TestCase;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.HWPFTestDataSamples;

/**
 * Test the shapes handling
 */
public final class TestShapes extends TestCase {

	/**
	 * two shapes, second is a group
	 */
	public void testShapes() throws Exception {
		HWPFDocument doc = HWPFTestDataSamples.openSampleFile("WithArtShapes.doc");

		List shapes = doc.getShapesTable().getAllShapes();
		List vshapes = doc.getShapesTable().getVisibleShapes();

		assertEquals(2, shapes.size());
		assertEquals(2, vshapes.size());

		Shape s1 = (Shape)shapes.get(0);
		Shape s2 = (Shape)shapes.get(1);

		assertEquals(3616, s1.getWidth());
		assertEquals(1738, s1.getHeight());
		assertEquals(true, s1.isWithinDocument());

		assertEquals(4817, s2.getWidth());
		assertEquals(2164, s2.getHeight());
		assertEquals(true, s2.isWithinDocument());


		// Re-serialisze, check still there
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		doc.write(baos);
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		doc = new HWPFDocument(bais);

		shapes = doc.getShapesTable().getAllShapes();
		vshapes = doc.getShapesTable().getVisibleShapes();

		assertEquals(2, shapes.size());
		assertEquals(2, vshapes.size());

		s1 = (Shape)shapes.get(0);
		s2 = (Shape)shapes.get(1);

		assertEquals(3616, s1.getWidth());
		assertEquals(1738, s1.getHeight());
		assertEquals(true, s1.isWithinDocument());

		assertEquals(4817, s2.getWidth());
		assertEquals(2164, s2.getHeight());
		assertEquals(true, s2.isWithinDocument());
	}
}
