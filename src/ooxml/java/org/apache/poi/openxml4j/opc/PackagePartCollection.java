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

import java.io.Serializable;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.ToIntFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.InvalidOperationException;

/**
 * A package part collection.
 */
public final class PackagePartCollection implements Serializable {

	private static final long serialVersionUID = 2515031135957635517L;

    /**
     * HashSet use to store this collection part names as string for rule
     * M1.11 optimized checking.
     */
    private final Set<String> registerPartNameStr = new HashSet<>();

	private final TreeMap<String, PackagePart> packagePartLookup =
        new TreeMap<>(PackagePartName::compare);


	/**
	 * Check rule [M1.11]: a package implementer shall neither create nor
	 * recognize a part with a part name derived from another part name by
	 * appending segments to it.
	 *
	 * @param partName name of part
	 * @param part part to put
     * @return the previous value associated with {@code partName}, or
     *         {@code null} if there was no mapping for {@code partName}.
	 * @exception InvalidOperationException
	 *                Throws if you try to add a part with a name derived from
	 *                another part name.
	 */
	public PackagePart put(final PackagePartName partName, final PackagePart part) {
	    final String ppName = partName.getName();
        final StringBuilder concatSeg = new StringBuilder();
        // split at slash, but keep leading slash
        final String delim = "(?=["+PackagingURIHelper.FORWARD_SLASH_STRING+".])";
		for (String seg : ppName.split(delim)) {
			concatSeg.append(seg);
			if (registerPartNameStr.contains(concatSeg.toString())) {
				throw new InvalidOperationException(
					"You can't add a part with a part name derived from another part ! [M1.11]");
			}
		}
		registerPartNameStr.add(ppName);
		return packagePartLookup.put(ppName, part);
	}

	public PackagePart remove(PackagePartName key) {
	    if (key == null) {
	        return null;
	    }
        final String ppName = key.getName();
	    PackagePart pp = packagePartLookup.remove(ppName);
	    if (pp != null) {
	        this.registerPartNameStr.remove(ppName);
	    }
		return pp;
	}


	/**
	 * The values themselves should be returned in sorted order. Doing it here
	 * avoids paying the high cost of Natural Ordering per insertion.
     * @return unmodifiable collection of parts
	 */
	public Collection<PackagePart> sortedValues() {
	    return Collections.unmodifiableCollection(packagePartLookup.values());

	}

	public boolean containsKey(PackagePartName partName) {
		return partName != null && packagePartLookup.containsKey(partName.getName());
	}

	public PackagePart get(PackagePartName partName) {
		return partName == null ? null : packagePartLookup.get(partName.getName());
	}

	public int size() {
		return packagePartLookup.size();
	}



    /**
     * Get an unused part index based on the namePattern, which doesn't exist yet
     * and has the lowest positive index
     *
     * @param nameTemplate
     *      The template for new part names containing a {@code '#'} for the index,
     *      e.g. "/ppt/slides/slide#.xml"
     * @return the next available part name index
     * @throws InvalidFormatException if the nameTemplate is null or doesn't contain
     *      the index char (#) or results in an invalid part name 
     */
    public int getUnusedPartIndex(final String nameTemplate) throws InvalidFormatException {
        if (nameTemplate == null || !nameTemplate.contains("#")) {
            throw new InvalidFormatException("name template must not be null and contain an index char (#)");
        }

        final Pattern pattern = Pattern.compile(nameTemplate.replace("#", "([0-9]+)"));
        
        final ToIntFunction<String> indexFromName = name -> {
            Matcher m = pattern.matcher(name);
            return m.matches() ? Integer.parseInt(m.group(1)) : 0;
        };
        
        return packagePartLookup.keySet().stream()
            .mapToInt(indexFromName)
            .collect(BitSet::new, BitSet::set, BitSet::or).nextClearBit(1);
    }
}
