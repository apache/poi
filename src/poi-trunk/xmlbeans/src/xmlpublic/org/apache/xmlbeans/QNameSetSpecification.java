/*   Copyright 2004 The Apache Software Foundation
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.xmlbeans;

import javax.xml.namespace.QName;

import java.util.Set;

/**
 * Represents a lattice of finite and infinite sets of QNames.
 * 
 * @see QNameSet
 */
public interface QNameSetSpecification
{
    /**
     * True if the set contains the given QName.
     *
     * Roughly equivalent to:
     *    (includedURIs() == null ?
     *           excludedURIs().contains(namespace) :
     *           includedURIs().contains(namespace)
     *    ) ?
     *        !excludedQNamesInIncludedURIs().contains(name) :
     *         includedQNamesInExcludedURIs().contains(name)
     */
    boolean contains(QName name);

    /**
     * True if the set is the set of all QNames.
     */
    boolean isAll();

    /**
     * True if the set is empty.
     */
    boolean isEmpty();

    /**
     * True if the parameter is a subset of this set.
     */ 
    boolean containsAll(QNameSetSpecification set);
    
    /**
     * True if is disjoint from the specified set.
     */
    boolean isDisjoint(QNameSetSpecification set);

    /**
     * Returns the intersection with another QNameSet.
     */
    QNameSet intersect(QNameSetSpecification set);

    /**
     * Returns the union with another QNameSet.
     */
    QNameSet union(QNameSetSpecification set);

    /**
     * Return the inverse of this QNameSet. That is the QNameSet which
     * contains all the QNames not contained in this set. In other words
     * for which set.contains(name) != set.inverse().contains(name) for
     * all names.
     */
    QNameSet inverse();

    /**
     * The finite set of namespace URIs that are almost completely excluded from
     * the set (that is, each namespace URI that included in the set with with
     * a finite number of QName exceptions). Null if the set of namespaceURIs
     * that are almost completely included is infinite.
     * <p>
     * Null (meaning almost all URIs excluded) if includedURIs() is non-null;
     * non-null otherwise.
     * <p>
     * The same set as inverse().includedURIs().
     */
    Set excludedURIs();

    /**
     * The finite set of namespace URIs that are almost completely included in
     * the set (that is, each namespace URI that included in the set with with
     * a finite number of QName exceptions). Null if the set of namespaceURIs
     * that are almost completely included is infinite.
     * <p>
     * Null (meaning almost all URIs included) if excludedURIs() is non-null;
     * non-null otherwise.
     * <p>
     * The same as inverse.excludedURIs().
     */
    Set includedURIs();

    /**
     * The finite set of QNames that are excluded from the set within namespaces
     * that are otherwise included. Should only contain QNames within namespace
     * that are within the set includedURIs() (or any URI, if includedURIs()
     * is null, which means that all URIs are almost completely included).
     * <p>
     * Never null.
     * <p>
     * The same set as inverse().includedQNames().
     */
    Set excludedQNamesInIncludedURIs();

    /**
     * The finite set of QNames that are included in the set within namespaces
     * that are otherwise excluded. Should only contain QNames within namespace
     * that are within the set excludedURIs() (or any URI, if excludedURIs()
     * is null, which means that all URIs are almost completely excluded).
     * <p>
     * Never null.
     * <p>
     * The same as inverse().excludedQNames().
     */
    Set includedQNamesInExcludedURIs();
}
