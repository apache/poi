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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.ToIntFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.zaxxer.sparsebits.SparseBitSet;
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

    /**
     * The lengths of the names in {@link #registerPartNameStr}, with the number
     * of names of each length.
     */
    private final Map<Integer, Integer> registerPartNameLengths = new HashMap<>();

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
     * @throws InvalidOperationException
     *                Throws if you try to add a part with a name derived from
     *                another part name.
     */
    public PackagePart put(final PackagePartName partName, final PackagePart part) {
        final String ppName = partName.getName();
        // check the name up to each slash (but the leading one) and the whole name -
        // only build the prefixes which are as long as a registered name, as building
        // every prefix takes quadratic time for deeply nested names
        int end = ppName.indexOf(PackagingURIHelper.FORWARD_SLASH_CHAR, 1);
        while (true) {
            final int len = (end < 0) ? ppName.length() : end;
            if (registerPartNameLengths.containsKey(len)
                    && registerPartNameStr.contains(ppName.substring(0, len))) {
                throw new InvalidOperationException(
                    "You can't add a part with a part name derived from another part ! [M1.11]");
            }
            if (end < 0) {
                break;
            }
            end = ppName.indexOf(PackagingURIHelper.FORWARD_SLASH_CHAR, end + 1);
        }
        registerPartNameStr.add(ppName);
        registerPartNameLengths.merge(ppName.length(), 1, Integer::sum);
        return packagePartLookup.put(ppName, part);
    }

    public PackagePart remove(PackagePartName key) {
        if (key == null) {
            return null;
        }
        final String ppName = key.getName();
        PackagePart pp = packagePartLookup.remove(ppName);
        if (pp != null && this.registerPartNameStr.remove(ppName)) {
            registerPartNameLengths.computeIfPresent(ppName.length(), (len, count) -> count == 1 ? null : count - 1);
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
            .collect(SparseBitSet::new, SparseBitSet::set, (s1,s2) -> s1.or(s2)).nextClearBit(1);
    }

    // used to ensure resources are closed when they are no longer needed
    void closeParts() {
        for (PackagePart part : packagePartLookup.values()) {
            part.close();
        }
    }
}
