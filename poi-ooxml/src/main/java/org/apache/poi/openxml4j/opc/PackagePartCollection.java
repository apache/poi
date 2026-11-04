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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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

    private final TreeMap<String, PackagePart> packagePartLookup =
        new TreeMap<>(PackagePartName::compare);

    /**
     * The registered part names as a tree of their segments, for checking rule
     * M1.11. Not serialized, as older versions didn't have it: it is rebuilt from
     * {@link #packagePartLookup} on first use.
     */
    private transient PartNameTree registeredNames;

    private PartNameTree registeredNames() {
        if (registeredNames == null) {
            registeredNames = new PartNameTree();
            for (String name : packagePartLookup.keySet()) {
                registeredNames.add(name);
            }
        }
        return registeredNames;
    }


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
        final PartNameTree names = registeredNames();
        if (names.containsPrefixOf(ppName)) {
            throw new InvalidOperationException(
                "You can't add a part with a part name derived from another part ! [M1.11]");
        }
        names.add(ppName);
        return packagePartLookup.put(ppName, part);
    }

    public PackagePart remove(PackagePartName key) {
        if (key == null) {
            return null;
        }
        final String ppName = key.getName();
        PackagePart pp = packagePartLookup.remove(ppName);
        if (pp != null) {
            registeredNames().remove(ppName);
        }
        return pp;
    }

    /**
     * @return the number of characters compared so far while checking rule M1.11,
     *      which must stay linear in the length of the checked names (for tests)
     */
    long comparedChars() {
        return registeredNames().comparedChars;
    }

    /**
     * The registered part names, split at their slashes into a tree of segments,
     * so that checking whether a registered name is a prefix of a part name (at a
     * segment boundary) takes time linear in the length of that name, whatever
     * the registered names are. Segments without a branch or a name end in
     * between are kept in one node, so the tree takes memory linear in the total
     * length of the registered names. Names are compared ignoring case, like
     * {@link PackagePartName}.
     */
    private static final class PartNameTree {
        private static final class Node {
            /** the segments of this node (after those of its parents), separated by slashes */
            private String label;
            /** whether a registered name ends with this node */
            private boolean isName;
            /** the children by their first segment */
            private final Map<String, Node> children = new HashMap<>();

            Node(String label, boolean isName) {
                this.label = label;
                this.isName = isName;
            }
        }

        private final Node root = new Node("", false);

        /** the characters compared by {@link #containsPrefixOf}, for tests */
        private long comparedChars;

        /**
         * @return whether a registered name equals the given name or is a prefix
         *      of it ending at a slash
         */
        boolean containsPrefixOf(String name) {
            final String n = normalize(name);
            Node node = root;
            int pos = 0;
            while (true) {
                final String segment = segmentAt(n, pos);
                comparedChars += segment.length();
                final Node child = node.children.get(segment);
                if (child == null) {
                    return false;
                }
                final String label = child.label;
                final int end = pos + label.length();
                if (end > n.length()) {
                    return false;
                }
                comparedChars += label.length();
                if (!n.startsWith(label, pos) || !isSegmentEnd(n, end)) {
                    return false;
                }
                if (child.isName) {
                    return true;
                }
                if (end == n.length()) {
                    return false;
                }
                node = child;
                pos = end + 1;
            }
        }

        void add(String name) {
            final String n = normalize(name);
            Node node = root;
            int pos = 0;
            while (true) {
                final String segment = segmentAt(n, pos);
                Node child = node.children.get(segment);
                if (child == null) {
                    node.children.put(segment, new Node(n.substring(pos), true));
                    return;
                }
                final int common = commonSegments(n, pos, child.label);
                if (common < child.label.length()) {
                    // the name leaves the segments of the child half-way: split it there
                    final Node split = new Node(child.label.substring(0, common), false);
                    child.label = child.label.substring(common + 1);
                    split.children.put(segmentAt(child.label, 0), child);
                    node.children.put(segment, split);
                    child = split;
                }
                pos += common;
                if (pos == n.length()) {
                    child.isName = true;
                    return;
                }
                node = child;
                pos++;
            }
        }

        void remove(String name) {
            final String n = normalize(name);
            final List<Node> path = new ArrayList<>();
            final List<String> keys = new ArrayList<>();
            Node node = root;
            int pos = 0;
            while (true) {
                final String segment = segmentAt(n, pos);
                final Node child = node.children.get(segment);
                if (child == null) {
                    return;
                }
                final int end = pos + child.label.length();
                if (!n.startsWith(child.label, pos) || !isSegmentEnd(n, end)) {
                    return;
                }
                path.add(node);
                keys.add(segment);
                node = child;
                if (end == n.length()) {
                    break;
                }
                pos = end + 1;
            }
            node.isName = false;
            // drop the nodes which neither end a name nor lead to one anymore
            for (int i = path.size() - 1; i >= 0 && !node.isName && node.children.isEmpty(); i--) {
                path.get(i).children.remove(keys.get(i));
                node = path.get(i);
            }
        }

        /** @return the name in lower case and without its leading slash */
        private static String normalize(String name) {
            final String n = name.toLowerCase(Locale.ROOT);
            return n.startsWith(PackagingURIHelper.FORWARD_SLASH_STRING) ? n.substring(1) : n;
        }

        private static String segmentAt(String n, int pos) {
            final int end = n.indexOf(PackagingURIHelper.FORWARD_SLASH_CHAR, pos);
            return n.substring(pos, end < 0 ? n.length() : end);
        }

        private static boolean isSegmentEnd(String n, int pos) {
            return pos == n.length() || (pos < n.length() && n.charAt(pos) == PackagingURIHelper.FORWARD_SLASH_CHAR);
        }

        /**
         * @return the length of the longest run of whole segments at the start of
         *      the label which equals the name from the given position on
         */
        private static int commonSegments(String n, int pos, String label) {
            int common = 0;
            int start = 0;
            while (true) {
                int end = label.indexOf(PackagingURIHelper.FORWARD_SLASH_CHAR, start);
                if (end < 0) {
                    end = label.length();
                }
                if (!n.regionMatches(pos + start, label, start, end - start) || !isSegmentEnd(n, pos + end)) {
                    return common;
                }
                common = end;
                if (end == label.length()) {
                    return common;
                }
                start = end + 1;
            }
        }
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
