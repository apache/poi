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

import java.util.ArrayList;
import java.util.TreeMap;

import org.apache.poi.openxml4j.exceptions.InvalidOperationException;

/**
 * A package part collection.
 *
 * @author Julien Chable
 * @version 0.1
 */
public final class PackagePartCollection extends
		TreeMap<PackagePartName, PackagePart> {

	private static final long serialVersionUID = 2515031135957635515L;

	/**
	 * Arraylist use to store this collection part names as string for rule
	 * M1.11 optimized checking.
	 */
	private ArrayList<String> registerPartNameStr = new ArrayList<String>();

	@Override
	public Object clone() {
		return super.clone();
	}

	/**
	 * Check rule [M1.11]: a package implementer shall neither create nor
	 * recognize a part with a part name derived from another part name by
	 * appending segments to it.
	 *
	 * @exception InvalidOperationException
	 *                Throws if you try to add a part with a name derived from
	 *                another part name.
	 */
	@Override
	public PackagePart put(PackagePartName partName, PackagePart part) {
		String[] segments = partName.getURI().toASCIIString().split(
				PackagingURIHelper.FORWARD_SLASH_STRING);
		StringBuffer concatSeg = new StringBuffer();
		for (String seg : segments) {
			if (!seg.equals(""))
				concatSeg.append(PackagingURIHelper.FORWARD_SLASH_CHAR);
			concatSeg.append(seg);
			if (this.registerPartNameStr.contains(concatSeg.toString())) {
				throw new InvalidOperationException(
						"You can't add a part with a part name derived from another part ! [M1.11]");
			}
		}
		this.registerPartNameStr.add(partName.getName());
		return super.put(partName, part);
	}

	@Override
	public PackagePart remove(Object key) {
		if (key instanceof PackagePartName) {
			this.registerPartNameStr.remove(((PackagePartName) key).getName());
		}
		return super.remove(key);
	}
}
