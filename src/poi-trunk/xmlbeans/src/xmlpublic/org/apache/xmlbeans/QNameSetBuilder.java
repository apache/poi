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
import java.util.HashSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

/**
 * Used to build {@link QNameSet QNameSets}.
 */ 
public class QNameSetBuilder implements QNameSetSpecification, java.io.Serializable
{
    private static final long serialVersionUID = 1L;
    
    private boolean _inverted;
    private Set _includedURIs;
    private Set _excludedQNames;
    private Set _includedQNames;

    /**
     * Constructs an empty QNameSetBuilder.
     */
    public QNameSetBuilder()
    {
        _inverted = false;
        _includedURIs = new HashSet();
        _excludedQNames = new HashSet();
        _includedQNames = new HashSet();
    }

    /**
     * Constructs a QNameSetBuilder whose initial contents are given by
     * another QNameSetSpecification.
     * @param set the QNameSetSpecificaiton to copy
     */
    public QNameSetBuilder(QNameSetSpecification set)
    {
        Set includedURIs = set.includedURIs();
        if (includedURIs != null)
        {
            _inverted = false;
            _includedURIs = new HashSet(includedURIs);
            _excludedQNames = new HashSet(set.excludedQNamesInIncludedURIs());
            _includedQNames = new HashSet(set.includedQNamesInExcludedURIs());
        }
        else
        {
            _inverted = true;
            _includedURIs = new HashSet(set.excludedURIs());
            _excludedQNames = new HashSet(set.includedQNamesInExcludedURIs());
            _includedQNames = new HashSet(set.excludedQNamesInIncludedURIs());
        }
    }

    /**
     * Constructs a QNameSetBuilder whose inital contents are given by
     * the four sets.  Exactly one of either excludedURIs or includedURIs must
     * be non-null.
     * 
     * @param excludedURIs the finite set of namespace URI strings to exclude from the set, or null if this set is infinite
     * @param includedURIs the finite set of namespace URI strings to include in the set, or null if this set is infinite
     * @param excludedQNamesInIncludedURIs the finite set of exceptional QNames to exclude from the included namespaces
     * @param excludedQNamesInIncludedURIs the finite set of exceptional QNames to include that are in the excluded namespaces
     */
    public QNameSetBuilder(Set excludedURIs, Set includedURIs, Set excludedQNamesInIncludedURIs, Set includedQNamesInExcludedURIs)
    {
        if (includedURIs != null && excludedURIs == null)
        {
            _inverted = false;
            _includedURIs = new HashSet(includedURIs);
            _excludedQNames = new HashSet(excludedQNamesInIncludedURIs);
            _includedQNames = new HashSet(includedQNamesInExcludedURIs);
        }
        else if (excludedURIs != null && includedURIs == null)
        {
            _inverted = true;
            _includedURIs = new HashSet(excludedURIs);
            _excludedQNames = new HashSet(includedQNamesInExcludedURIs);
            _includedQNames = new HashSet(excludedQNamesInIncludedURIs);
        }
        else
            throw new IllegalArgumentException("Exactly one of excludedURIs and includedURIs must be null");
    }


    /**
     * Constructs a QNameSetBuilder whose initial contents are given
     * as a list of namespace URIs, using the same format used by wildcards
     * in XSD files.
     * 
     * @param str a wildcard namespace specification string such as "##any",
     *        "##other", "##local", "##targetNamespace", or a space-separated
     *        list of URIs.
     * @param targetURI the current targetNamespace
     */
    public QNameSetBuilder(String str, String targetURI)
    {
        this();

        if (str == null)
            str = "##any";

        String[] uri = splitList(str);
        for (int i = 0; i < uri.length; i++)
        {
            String adduri = uri[i];
            if (adduri.startsWith("##"))
            {
                if (adduri.equals("##other"))
                {
                    if (targetURI == null)
                        throw new IllegalArgumentException();
                    QNameSetBuilder temp = new QNameSetBuilder();
                    temp.addNamespace(targetURI);
                    temp.addNamespace("");
                    temp.invert();
                    addAll(temp);
                    continue;
                }
                else if (adduri.equals("##any"))
                {
                    clear();
                    invert();
                    continue;
                }
                else if (uri[i].equals("##targetNamespace"))
                {
                    if (targetURI == null)
                        throw new IllegalArgumentException();
                    adduri = targetURI;
                }
                else if (uri[i].equals("##local"))
                {
                    adduri = "";
                }
            }
            addNamespace(adduri);
        }
    }
    
    /**
     * Local xml names are hased using "" as the namespace.
     */
    private static String nsFromName(QName QName)
    {
        String ns = QName.getNamespaceURI();
        return ns == null ? "" : ns;
    }
    
    private static final String[] EMPTY_STRINGARRAY = new String[0];
    
    private static boolean isSpace(char ch)
    {
        switch (ch)
        {
            case ' ':
            case '\t':
            case '\r':
            case '\n':
                return true;
            default:
                return false;
        }
    }

    private static String[] splitList(String s)
    {
        if (s.length() == 0)
            return EMPTY_STRINGARRAY;
                
        List result = new ArrayList();
        int i = 0;
        int start = 0;
        for (;;)
        {
            while (i < s.length() && isSpace(s.charAt(i)))
                i += 1;
            if (i >= s.length())
                return (String[])result.toArray(EMPTY_STRINGARRAY);
            start = i;
            while (i < s.length() && !isSpace(s.charAt(i)))
                i += 1;
            result.add(s.substring(start, i));
        }
    }
    
    /**
     * Remove all xml names from qnameset whose namespace matches the uri.
     */
    private static void removeAllMatchingNs(String uri, Set qnameset)
    {
        for (Iterator i = qnameset.iterator(); i.hasNext(); )
        {
            if (uri.equals(nsFromName((QName)i.next())))
                i.remove();
        }
    }

    /**
     * Remove all xml names from qnameset whose namespace is in the
     * first set of uris but not the second.
     */
    private static void removeAllMatchingFirstOnly(Set setFirst, Set setSecond, Set qnameset)
    {
        for (Iterator i = qnameset.iterator(); i.hasNext(); )
        {
            String ns = nsFromName((QName)i.next());
            if (setFirst.contains(ns) && !setSecond.contains(ns))
                i.remove();
        }
    }

    /**
     * Remove all xml names from qnameset whose namespace is in both
     * sets of uris.
     */
    private static void removeAllMatchingBoth(Set setFirst, Set setSecond, Set qnameset)
    {
        for (Iterator i = qnameset.iterator(); i.hasNext(); )
        {
            String ns = nsFromName((QName)i.next());
            if (setFirst.contains(ns) && setSecond.contains(ns))
                i.remove();
        }
    }

    /**
     * Remove all xml names from qnameset whose namespace is in neither
     * set of uris.
     */
    private static void removeAllMatchingNeither(Set setFirst, Set setSecond, Set qnameset)
    {
        for (Iterator i = qnameset.iterator(); i.hasNext(); )
        {
            String ns = nsFromName((QName)i.next());
            if (!setFirst.contains(ns) && !setSecond.contains(ns))
                i.remove();
        }
    }

    /**
     * True if this ModelTransitionSet contains the given qname.
     */
    public boolean contains(QName name)
    {
        boolean in = _includedURIs.contains(nsFromName(name)) ?
                     !_excludedQNames.contains(name) :
                      _includedQNames.contains(name);
        return _inverted ^ in;
    }

    /**
     * True if this ModelTransitionSet contains all QNames.
     */
    public boolean isAll()
    {
        return _inverted && _includedURIs.size() == 0 && _includedQNames.size() == 0;
    }

    /**
     * True if this ModelTransitionSet contains no QNames.
     */
    public boolean isEmpty()
    {
        return !_inverted && _includedURIs.size() == 0 && _includedQNames.size() == 0;
    }

    /**
     * Returns a new QNameSet that is the intersection of this one and another.
     */
    public QNameSet intersect(QNameSetSpecification set)
    {
        QNameSetBuilder result = new QNameSetBuilder(this);
        result.restrict(set);
        return result.toQNameSet();
    }

    /**
     * Returns a new QNameSet that is the union of this one and another.
     */
    public QNameSet union(QNameSetSpecification set)
    {
        QNameSetBuilder result = new QNameSetBuilder(this);
        result.addAll(set);
        return result.toQNameSet();
    }

    /**
     * Returns a new QNameSet that is the inverse of this one.
     */
    public QNameSet inverse()
    {
        return QNameSet.forSets(includedURIs(), excludedURIs(), includedQNamesInExcludedURIs(), excludedQNamesInIncludedURIs());
    }

    /**
     * True if the parameter is a subset of this set.
     */
    public boolean containsAll(QNameSetSpecification set)
    {
        if (!_inverted && set.excludedURIs() != null)
            return false;
        
        return inverse().isDisjoint(set);
    }

    /**
     * True if the given set is disjoint from this one.
     */
    public boolean isDisjoint(QNameSetSpecification set)
    {
        if (_inverted && set.excludedURIs() != null)
            return false;

        if (_inverted)
            return isDisjointImpl(set, this);
        else
            return isDisjointImpl(this, set);
    }

    private boolean isDisjointImpl(QNameSetSpecification set1, QNameSetSpecification set2)
    {
        Set includeURIs = set1.includedURIs();
        Set otherIncludeURIs = set2.includedURIs();
        if (otherIncludeURIs != null)
        {
            for (Iterator i = includeURIs.iterator(); i.hasNext(); )
            {
                if (otherIncludeURIs.contains(i.next()))
                    return false;
            }
        }
        else
        {
            Set otherExcludeURIs = set2.excludedURIs();
            for (Iterator i = includeURIs.iterator(); i.hasNext(); )
            {
                if (!otherExcludeURIs.contains(i.next()))
                    return false;
            }
        }

        for (Iterator i = set1.includedQNamesInExcludedURIs().iterator(); i.hasNext(); )
        {
            if (set2.contains((QName)i.next()))
                return false;
        }

        if (includeURIs.size() > 0)
            for (Iterator i = set2.includedQNamesInExcludedURIs().iterator(); i.hasNext(); )
        {
            if (set1.contains((QName)i.next()))
                return false;
        }

        return true;
    }


    /**
     * Clears this QNameSetBuilder
     */
    public void clear()
    {
        _inverted = false;
        _includedURIs.clear();
        _excludedQNames.clear();
        _includedQNames.clear();
    }

    /**
     * Inverts this QNameSetBuilder.
     */
    public void invert()
    {
        _inverted = !_inverted;
    }

    /**
     * Adds a single QName to this QNameSetBuilder.
     */
    public void add(QName qname)
    {
        if (!_inverted)
            addImpl(qname);
        else
            removeImpl(qname);
    }

    /**
     * Adds an entire namespace URI of QNames to this QNameSetBuilder.
     * The empty string is used to signifiy the (local) no-namespace.
     */
    public void addNamespace(String uri)
    {
        if (!_inverted)
            addNamespaceImpl(uri);
        else
            removeNamespaceImpl(uri);
    }

    /**
     * Adds the contents of another QNameSet to this QNameSetBuilder.
     */
    public void addAll(QNameSetSpecification set)
    {
        if (_inverted)
            removeAllImpl(set.includedURIs(), set.excludedURIs(), set.includedQNamesInExcludedURIs(), set.excludedQNamesInIncludedURIs());
        else
            addAllImpl(set.includedURIs(), set.excludedURIs(), set.includedQNamesInExcludedURIs(), set.excludedQNamesInIncludedURIs());
    }

    /**
     * Removes the given qname from this QNameSetBuilder.
     */
    public void remove(QName qname)
    {
        if (_inverted)
            addImpl(qname);
        else
            removeImpl(qname);
    }

    /**
     * Removes an entire namespace URI from this QNameSetBuilder.
     */
    public void removeNamespace(String uri)
    {
        if (_inverted)
            addNamespaceImpl(uri);
        else
            removeNamespaceImpl(uri);
    }

    /**
     * Removes all contents of a given QNameSet from this QNameSetBuilder.
     */
    public void removeAll(QNameSetSpecification set)
    {
        if (_inverted)
            addAllImpl(set.includedURIs(), set.excludedURIs(), set.includedQNamesInExcludedURIs(), set.excludedQNamesInIncludedURIs());
        else
            removeAllImpl(set.includedURIs(), set.excludedURIs(), set.includedQNamesInExcludedURIs(), set.excludedQNamesInIncludedURIs());
    }

    /**
     * Restricts the contents of this QNameSetBuilder to be a subset of the
     * given QNameSet. In other words, computes an intersection.
     */
    public void restrict(QNameSetSpecification set)
    {
        if (_inverted)
            addAllImpl(set.excludedURIs(), set.includedURIs(), set.excludedQNamesInIncludedURIs(), set.includedQNamesInExcludedURIs());
        else
            removeAllImpl(set.excludedURIs(), set.includedURIs(), set.excludedQNamesInIncludedURIs(), set.includedQNamesInExcludedURIs());
    }

    /**
     * Implementation of add(qname) that ignores inversion.
     */
    private void addImpl(QName qname)
    {
        if (_includedURIs.contains(nsFromName(qname)))
            _excludedQNames.remove(qname);
        else
            _includedQNames.add(qname);
    }

    /**
     * Implementation of add(ns) that ignores inversion.
     */
    private void addNamespaceImpl(String uri)
    {
        if (_includedURIs.contains(uri))
        {
            removeAllMatchingNs(uri, _excludedQNames);
        }
        else
        {
            removeAllMatchingNs(uri, _includedQNames);
            _includedURIs.add(uri);
        }
    }

    /**
     * Implementation of add(set) that ignores inversion.
     */
    private void addAllImpl(Set includedURIs, Set excludedURIs, Set includedQNames, Set excludedQNames)
    {
        boolean exclude = (excludedURIs != null);
        Set specialURIs = exclude ? excludedURIs : includedURIs;

        for (Iterator i = _excludedQNames.iterator(); i.hasNext(); )
        {
            QName name = (QName)i.next();
            String uri = nsFromName(name);
            if ((exclude ^ specialURIs.contains(uri)) && !excludedQNames.contains(name))
                i.remove();
        }

        for (Iterator i = excludedQNames.iterator(); i.hasNext(); )
        {
            QName name = (QName)i.next();
            String uri = nsFromName(name);
            if (!_includedURIs.contains(uri) && !_includedQNames.contains(name))
                _excludedQNames.add(name);
        }

        for (Iterator i = includedQNames.iterator(); i.hasNext(); )
        {
            QName name = (QName)i.next();
            String uri = nsFromName(name);
            if (!_includedURIs.contains(uri))
                _includedQNames.add(name);
            else
                _excludedQNames.remove(name);
        }

        if (!exclude)
        {
            removeAllMatchingFirstOnly(includedURIs, _includedURIs, _includedQNames);
            _includedURIs.addAll(includedURIs);
        }
        else
        {
            removeAllMatchingNeither(excludedURIs, _includedURIs, _includedQNames);
            for (Iterator i = _includedURIs.iterator(); i.hasNext(); )
            {
                String uri = (String)i.next();
                if (!excludedURIs.contains(uri))
                    i.remove();
            }

            for (Iterator i = excludedURIs.iterator(); i.hasNext(); )
            {
                String uri = (String)i.next();
                if (!_includedURIs.contains(uri))
                    _includedURIs.add(uri);
                else
                    _includedURIs.remove(uri);
            }
            Set temp = _excludedQNames;
            _excludedQNames = _includedQNames;
            _includedQNames = temp;
            _inverted = !_inverted;
        }
    }

    /**
     * Implementation of remove(qname) that ignores inversion.
     */
    private void removeImpl(QName qname)
    {
        if (_includedURIs.contains(nsFromName(qname)))
            _excludedQNames.add(qname);
        else
            _includedQNames.remove(qname);
    }

    /**
     * Implementation of remove(ns) that ignores inversion.
     */
    private void removeNamespaceImpl(String uri)
    {
        if (_includedURIs.contains(uri))
        {
            removeAllMatchingNs(uri, _excludedQNames);
            _includedURIs.remove(uri);
        }
        else
        {
            removeAllMatchingNs(uri, _includedQNames);
        }
    }

    /**
     * Implementation of remove(set) that ignores inversion.
     */
    private void removeAllImpl(Set includedURIs, Set excludedURIs, Set includedQNames, Set excludedQNames)
    {
        boolean exclude = (excludedURIs != null);
        Set specialURIs = exclude ? excludedURIs : includedURIs;

        for (Iterator i = _includedQNames.iterator(); i.hasNext(); )
        {
            QName name = (QName)i.next();
            String uri = nsFromName(name);
            if (exclude ^ specialURIs.contains(uri))
            {
                if (!excludedQNames.contains(name))
                    i.remove();
            }
            else
            {
                if (includedQNames.contains(name))
                    i.remove();
            }
        }

        for (Iterator i = includedQNames.iterator(); i.hasNext(); )
        {
            QName name = (QName)i.next();
            String uri = nsFromName(name);
            if (_includedURIs.contains(uri))
                _excludedQNames.add(name);
        }

        for (Iterator i = excludedQNames.iterator(); i.hasNext(); )
        {
            QName name = (QName)i.next();
            String uri = nsFromName(name);
            if (_includedURIs.contains(uri) && !_excludedQNames.contains(name))
                _includedQNames.add(name);
        }

        if (exclude)
        {
            removeAllMatchingFirstOnly(_includedURIs, excludedURIs, _excludedQNames);
        }
        else
        {
            removeAllMatchingBoth(_includedURIs, includedURIs, _excludedQNames);
        }

        for (Iterator i = _includedURIs.iterator(); i.hasNext(); )
        {
            if (exclude ^ specialURIs.contains(i.next()))
                i.remove();
        }
    }

    public Set excludedURIs()
    {
        if (_inverted) return Collections.unmodifiableSet(_includedURIs);
        return null;
    }

    public Set includedURIs()
    {
        if (!_inverted) return _includedURIs;
        return null;
    }

    public Set excludedQNamesInIncludedURIs()
    {
        return Collections.unmodifiableSet(_inverted ? _includedQNames : _excludedQNames);
    }

    public Set includedQNamesInExcludedURIs()
    {
        return Collections.unmodifiableSet(_inverted ? _excludedQNames : _includedQNames);
    }

    private String prettyQName(QName name)
    {
        if (name.getNamespaceURI() == null)
            return name.getLocalPart();
        return name.getLocalPart() + "@" + name.getNamespaceURI();
    }

    /**
     * Returns a string representation useful for debugging, subject to change.
     */ 
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("QNameSetBuilder");
        sb.append(_inverted ? "-(" : "+(");
        for (Iterator i = _includedURIs.iterator(); i.hasNext(); )
        {
            sb.append("+*@");
            sb.append(i.next());
            sb.append(", ");
        }
        for (Iterator i = _excludedQNames.iterator(); i.hasNext(); )
        {
            sb.append("-");
            sb.append(prettyQName((QName)i.next()));
            sb.append(", ");
        }
        for (Iterator i = _includedQNames.iterator(); i.hasNext(); )
        {
            sb.append("+");
            sb.append(prettyQName((QName)i.next()));
            sb.append(", ");
        }
        int index = sb.lastIndexOf(", ");
        if (index > 0)
            sb.setLength(index);
        sb.append(')');
        return sb.toString();
    }
    
    /**
     * Returns a {@link QNameSet} equivalent to the current state of this
     * QNameSetBuilder.
     */ 
    public QNameSet toQNameSet()
    {
        return QNameSet.forSpecification(this);
    }
}
