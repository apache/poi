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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Set;
import java.util.TreeMap;

import junit.framework.AssertionFailedError;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.poi.util.IOUtils;
import org.junit.Assert;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Comparison;
import org.xmlunit.diff.ComparisonResult;
import org.xmlunit.diff.DefaultNodeMatcher;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.DifferenceEvaluator;
import org.xmlunit.diff.ElementSelectors;

/**
 * Compare the contents of 2 zip files.
 */
public final class ZipFileAssert {
	private ZipFileAssert() {
	}

	private static void equals(
			TreeMap<String, ByteArrayOutputStream> file1,
			TreeMap<String, ByteArrayOutputStream> file2) {
		Set<String> listFile1 = file1.keySet();
		Assert.assertEquals("not the same number of files in zip:", listFile1.size(), file2.keySet().size());
		
		for (String fileName : listFile1) {
			// extract the contents for both
			ByteArrayOutputStream contain1 = file1.get(fileName);
			ByteArrayOutputStream contain2 = file2.get(fileName);

			assertNotNull(fileName + " not found in 2nd zip", contain2);
			// no need to check for contain1. The key come from it

			if (fileName.matches(".*\\.(xml|rels)$")) {
				// we have a xml file
				final Diff diff = DiffBuilder.
						compare(Input.fromByteArray(contain1.toByteArray())).
						withTest(Input.fromByteArray(contain2.toByteArray())).
						ignoreWhitespace().
						checkForSimilar().
						withDifferenceEvaluator(new IgnoreXMLDeclEvaluator()).
						withNodeMatcher(new DefaultNodeMatcher(ElementSelectors.byNameAndAllAttributes, ElementSelectors.byNameAndText)).
						build();
				assertFalse(fileName+": "+diff.toString(), diff.hasDifferences());
            } else {
				// not xml, may be an image or other binary format
                Assert.assertEquals(fileName + " does not have the same size in both zip:", contain1.size(), contain2.size());
				assertArrayEquals("contents differ", contain1.toByteArray(), contain2.toByteArray());
			}
		}
	}

	private static TreeMap<String, ByteArrayOutputStream> decompress(
			File filename) throws IOException {
		// store the zip content in memory
		// let s assume it is not Go ;-)
		TreeMap<String, ByteArrayOutputStream> zipContent = new TreeMap<>();

		/* Open file to decompress */
		FileInputStream file_decompress = new FileInputStream(filename);

		/* Create a buffer for the decompressed files */
		BufferedInputStream buffi = new BufferedInputStream(file_decompress);

		/* Open the file with the buffer */
		ZipArchiveInputStream zis = new ZipArchiveInputStream(buffi);

		/* Processing entries of the zip file */
		ArchiveEntry entree;
		while ((entree = zis.getNextEntry()) != null) {

			/* Create a array for the current entry */
			ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
			IOUtils.copy(zis, byteArray);
			zipContent.put(entree.getName(), byteArray);
		}

		zis.close();

		return zipContent;
	}

	/**
	 * Asserts that two files are equal. Throws an <tt>AssertionFailedError</tt>
	 * if they are not.
	 * <p>
	 * 
	 */
	public static void assertEquals(File expected, File actual) {
		assertNotNull(expected);
		assertNotNull(actual);

		assertTrue("File does not exist [" + expected.getAbsolutePath()
				+ "]", expected.exists());
		assertTrue("File does not exist [" + actual.getAbsolutePath()
				+ "]", actual.exists());

		assertTrue("Expected file not readable", expected.canRead());
		assertTrue("Actual file not readable", actual.canRead());

		try {
			TreeMap<String, ByteArrayOutputStream> file1 = decompress(expected);
			TreeMap<String, ByteArrayOutputStream> file2 = decompress(actual);
			equals(file1, file2);
		} catch (IOException e) {
			throw new AssertionFailedError(e.toString());
		}
	}

	private static class IgnoreXMLDeclEvaluator implements DifferenceEvaluator {
		public ComparisonResult evaluate(final Comparison comparison, final ComparisonResult outcome) {
			if (outcome != ComparisonResult.EQUAL) {
				// only evaluate differences
				switch (comparison.getType()) {
					case CHILD_NODELIST_SEQUENCE:
					case XML_STANDALONE:
					case NAMESPACE_PREFIX:
						return ComparisonResult.SIMILAR;
					case TEXT_VALUE:
						switch (comparison.getControlDetails().getTarget().getParentNode().getNodeName()) {
						case "dcterms:created":
						case "dc:creator":
							return ComparisonResult.SIMILAR;
						}
						break;
					default:
						break;
				}
			}

			return outcome;
		}
	}
}
