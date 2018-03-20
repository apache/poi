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
/**
 * Author: Cezar Andrei ( cezar.andrei at bea.com )
 * Date: Apr 25, 2004
 */
package org.apache.xmlbeans.impl.config;

import java.util.Set;
import java.util.Collections;
import java.util.HashSet;

/**
 * Reprezents a non finite set of names.
 * @see NameSetBuilder
 */
public class NameSet
{
    /**
     * An empty NameSet, it doesn't contain any name
     */
    public static NameSet EMPTY = new NameSet(true, Collections.EMPTY_SET);
    /**
     * The NameSet that contains any name
     */
    public static NameSet EVERYTHING = new NameSet(false, Collections.EMPTY_SET);

    /*
    There are two big cases:
    1) - it reprezents "*", ie all except a finite set of names: _isFinite==false
    2) - if reprezents only a finite set of names: _isFinite==true
    */
    private boolean _isFinite;
    private Set _finiteSet;

    private NameSet(boolean isFinite, Set finiteSet)
    {
        _isFinite = isFinite;
        _finiteSet = finiteSet;
    }

    static NameSet newInstance(boolean isFinite, Set finiteSet)
    {
        if ( finiteSet.size()==0 )
            if ( isFinite )
                return NameSet.EMPTY;
            else
                return NameSet.EVERYTHING;
        else
        {
            Set fs = new HashSet();
            fs.addAll(finiteSet);
            return new NameSet(isFinite, fs);
        }
    }

    private static Set intersectFiniteSets(Set a, Set b)
    {
        Set intersection = new HashSet();
        //compute the intersection of _finiteSet with withSet
        while (a.iterator().hasNext())
        {
            String name = (String) a.iterator().next();
            if (b.contains(name))
                intersection.add(name);
        }
        return intersection;
    }

    /**
     * Returns the union of this NameSet with the 'with' NameSet.
     */
    public NameSet union(NameSet with)
    {
        if (_isFinite)
        {
            if (with._isFinite)
            {
                Set union = new HashSet();
                union.addAll(_finiteSet);
                union.addAll(with._finiteSet);
                return newInstance(true, union);
            }
            else
            {
                Set subst = new HashSet();
                subst.addAll(with._finiteSet);
                subst.removeAll(_finiteSet);
                return newInstance(false, subst);
            }
        }
        else
        {
            if (with._isFinite)
            {
                Set subst = new HashSet();
                subst.addAll(_finiteSet);
                subst.removeAll(with._finiteSet);
                return newInstance(false, subst);
            }
            else
            {
                return newInstance(false, intersectFiniteSets(_finiteSet, with._finiteSet));
            }
        }
    }

    /**
     * Returns the intersection of this NameSet with the 'with' NameSet
     */
    public NameSet intersect(NameSet with)
    {
        if (_isFinite)
        {
            if (with._isFinite)
            {
                return newInstance(true, intersectFiniteSets(_finiteSet, with._finiteSet));
            }
            else
            {
                Set subst = new HashSet();
                subst.addAll(_finiteSet);
                subst.removeAll(with._finiteSet);
                return newInstance(false, subst);
            }
        }
        else
        {
            if (with._isFinite)
            {
                Set subst = new HashSet();
                subst.addAll(with._finiteSet);
                subst.removeAll(_finiteSet);
                return newInstance(true, subst);
            }
            else
            {
                Set union = new HashSet();
                union.addAll(_finiteSet);
                union.addAll(with._finiteSet);
                return newInstance(false, union);
            }
        }
    }

    /**
     * Returns the result of substracting this NameSet from 'from' NameSet
     * @see NameSet#substract
     */
    public NameSet substractFrom(NameSet from)
    {
        return from.substract(this);
    }

    /**
     * Returns the result of substracting 'what' NameSet from this NameSet
     * @see NameSet#substractFrom
     */
    public NameSet substract(NameSet what)
    {
        if (_isFinite)
        {
            if ( what._isFinite )
            {
                // it's the subst of _finiteSet with what._finiteSet
                Set subst = new HashSet();
                subst.addAll(_finiteSet);
                subst.removeAll(what._finiteSet);
                return newInstance(true, subst);
            }
            else
            {
                return newInstance(true, intersectFiniteSets(_finiteSet, what._finiteSet));
            }
        }
        else
        {
            if ( what._isFinite )
            {
                // it's the union of _finiteSet with what._finiteSet
                Set union = new HashSet();
                union.addAll(_finiteSet);
                union.addAll(what._finiteSet);
                return newInstance(false, union);
            }
            else
            {
                // what's in thisSet and it's not in whatSet
                Set subst = new HashSet();
                subst.addAll(what._finiteSet);
                subst.removeAll(_finiteSet);
                return newInstance(true, subst);
            }
        }
    }

    /**
     * Returns an inversion of this NameSet
     */
    public NameSet invert()
    {
        return newInstance(!_isFinite, _finiteSet);
    }

    public boolean contains(String name)
    {
        if (_isFinite)
            return _finiteSet.contains(name);
        else
            return !_finiteSet.contains(name);
    }
}
