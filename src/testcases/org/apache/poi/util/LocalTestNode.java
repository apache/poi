
/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache POI" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache POI", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.poi.util;

class LocalTestNode
    implements Comparable
{
    private Comparable _key;
    private Comparable _value;

    /**
     * construct a LocalTestNode
     *
     * @param key value used to create the key and value
     */

    LocalTestNode(final int key)
    {
        _key   = new Integer(key);
        _value = String.valueOf(key);
    }

    /**
     * @param key the unique key associated with the current node.
     */

    void setKey(Comparable key)
    {
        _key = key;
    }

    /**
     * @return the unique key associated with the current node
     */

    Comparable getKey()
    {
        return _key;
    }

    /**
     * @param value the unique value associated with the current node.
     */

    void setValue(Comparable value)
    {
        _value = value;
    }

    /**
     * @return the unique value associated with the current node
     */

    Comparable getValue()
    {
        return _value;
    }

    /**
     * Method compareTo
     *
     * @param o
     *
     * @return
     */

    public int compareTo(Object o)
    {
        LocalTestNode other = ( LocalTestNode ) o;
        int           rval  = getKey().compareTo(other.getKey());

        if (rval == 0)
        {
            rval = getValue().compareTo(other.getValue());
        }
        return rval;
    }

    /**
     * Method equals
     *
     * @param o
     *
     * @return true if equal
     */

    public boolean equals(Object o)
    {
        if (o == null)
        {
            return false;
        }
        if (!(o.getClass().equals(this.getClass())))
        {
            return false;
        }
        LocalTestNode node = ( LocalTestNode ) o;

        return (getKey().equals(node.getKey())
                && getValue().equals(node.getValue()));
    }

    /**
     * @return hash code
     */

    public int hashCode()
    {
        return getKey().hashCode() ^ getValue().hashCode();
    }
}
