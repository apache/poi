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

package org.apache.xmlbeans.soap;

import javax.xml.namespace.QName;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

import org.apache.xmlbeans.impl.values.XmlValueOutOfRangeException;
import org.apache.xmlbeans.impl.common.XmlWhitespace;
import org.apache.xmlbeans.impl.common.QNameHelper;
import org.apache.xmlbeans.impl.common.PrefixResolver;

public final class SOAPArrayType
{
    // Example foo:bar[,][][,,][7,9]
    // -> _type = QName(foo:bar)
    // -> _ranks = {2,1,3}
    // -> _dimensions = {7,9}
    private QName _type;
    private int[] _ranks; // if ranks is empty, it means there are no nested arrays
    private int[] _dimensions; // Any dimension can be -1 to indicate "any".

    /**
     * True if the ranks for the passed SOAPArrayType
     * are equal to this one.
     *
     * Does NOT compare the _type fields.
     */
    public boolean isSameRankAs(SOAPArrayType otherType)
    {
        if (_ranks.length != otherType._ranks.length)
            return false;
        for (int i = 0; i < _ranks.length; i++)
        {
            if (_ranks[i] != otherType._ranks[i])
                return false;
        }
        if (_dimensions.length != otherType._dimensions.length)
            return false;
        return true;
    }

    /**
     * Given SOAP 1.1-formatted index string, returns an array
     * index.  For example, given "[4,3,5]", returns an int array
     * containing 4, 3, and 5.
     */
    public static int[] parseSoap11Index(String inbraces)
    {
        inbraces = XmlWhitespace.collapse(inbraces, XmlWhitespace.WS_COLLAPSE);
        if (!inbraces.startsWith("[") || !inbraces.endsWith("]"))
            throw new IllegalArgumentException("Misformed SOAP 1.1 index: must be contained in braces []");
        return internalParseCommaIntString(inbraces.substring(1, inbraces.length() - 1));
    }

    private static int[] internalParseCommaIntString(String csl)
    {
        List dimStrings = new ArrayList();
        int i = 0;
        for (;;)
        {
            int j = csl.indexOf(',', i);
            if (j < 0)
            {
                dimStrings.add(csl.substring(i));
                break;
            }
            dimStrings.add(csl.substring(i, j));
            i = j + 1;
        }

        int[] result = new int[dimStrings.size()];
        i = 0;
        for (Iterator it = dimStrings.iterator(); it.hasNext(); i++)
        {
            String dimString = XmlWhitespace.collapse((String)it.next(), XmlWhitespace.WS_COLLAPSE);
            if (dimString.equals("*") || dimString.equals(""))
            {
                result[i] = -1;
            }
            else
            {
                try
                {
                    result[i] = Integer.parseInt(dimString);
                }
                catch (Exception e)
                {
                    throw new XmlValueOutOfRangeException("Malformed integer in SOAP array index");
                }
            }
        }
        return result;
    }

    /**
     * Parses a SOAP 1.1 array type string.
     *
     * Since an array type string contains a QName, a prefix resolver
     * must be passed.
     */
    public SOAPArrayType(String s, PrefixResolver m)
    {
        int firstbrace = s.indexOf('[');
        if (firstbrace < 0)
            throw new XmlValueOutOfRangeException();

        // grab the QName
        String firstpart = XmlWhitespace.collapse(s.substring(0, firstbrace), XmlWhitespace.WS_COLLAPSE);
        int firstcolon = firstpart.indexOf(':');
        String prefix = "";
        if (firstcolon >= 0)
            prefix = firstpart.substring(0, firstcolon);

        String uri = m.getNamespaceForPrefix(prefix);
        if (uri == null)
            throw new XmlValueOutOfRangeException();

        _type = QNameHelper.forLNS(firstpart.substring(firstcolon + 1), uri);

        initDimensions(s, firstbrace);
    }

    private static int[] EMPTY_INT_ARRAY = new int[0];

    /**
     * Parses SOAP 1.1(advanced) array type strings.
     *
     * Since in SOAP 1.1(advanced) the dimension specification is separated from the
     * QName for the underlying type, these are passed in separate
     * arguments.
     */
    public SOAPArrayType(QName name, String dimensions)
    {
        int firstbrace = dimensions.indexOf('[');
        if (firstbrace < 0)
        {
            _type = name;
            _ranks = EMPTY_INT_ARRAY;
            dimensions = XmlWhitespace.collapse(dimensions, XmlWhitespace.WS_COLLAPSE);
            String[] dimStrings = dimensions.split(" ");
            for (int i = 0; i < dimStrings.length; i++)
            {
                String dimString = dimStrings[i];
                if (dimString.equals("*"))
                {
                    _dimensions[i] = -1;
                    // _hasIndeterminateDimensions = true;
                }
                else
                {
                    try
                    {
                        _dimensions[i] = Integer.parseInt(dimStrings[i]);
                    }
                    catch (Exception e)
                    {
                        throw new XmlValueOutOfRangeException();
                    }
                }
            }
        }
        else
        {
            _type = name;
            initDimensions(dimensions, firstbrace);
        }
    }

    /**
     * Given a nested SOAPArrayType and a set of dimensions for the outermost
     * array, comes up with the right SOAPArrayType for the whole thing.
     *
     * E.g.,
     * Nested foo:bar[,][][,,][1,2]
     * Dimensions [6,7,8]
     * Result -> foo:bar[,][][,,][,][6,7,8]
     */
    public SOAPArrayType(SOAPArrayType nested, int[] dimensions)
    {
        _type = nested._type;

        _ranks = new int[nested._ranks.length + 1];
        System.arraycopy(nested._ranks, 0, _ranks, 0, nested._ranks.length);
        _ranks[_ranks.length - 1] = nested._dimensions.length;

        _dimensions = new int[dimensions.length];
        System.arraycopy(dimensions, 0, _dimensions, 0, dimensions.length);
    }

    /**
     * Initialize dimensions based on SOAP11 parsed dimension substring
     */
    private void initDimensions(String s, int firstbrace)
    {
        List braces = new ArrayList();
        int lastbrace = -1;
        for (int i = firstbrace; i >= 0; )
        {
            lastbrace = s.indexOf(']', i);
            if (lastbrace < 0)
                throw new XmlValueOutOfRangeException();
            braces.add(s.substring(i + 1, lastbrace));
            i = s.indexOf('[', lastbrace);
        }

        String trailer = s.substring(lastbrace + 1);
        if (!XmlWhitespace.isAllSpace(trailer))
            throw new XmlValueOutOfRangeException();

        // now fill in rank array
        _ranks = new int[braces.size() - 1];
        for (int i = 0; i < _ranks.length; i++)
        {
            String commas = (String)braces.get(i);
            int commacount = 0;
            for (int j = 0; j < commas.length(); j++)
            {
                char ch = commas.charAt(j);
                if (ch == ',')
                    commacount += 1;
                else if (!XmlWhitespace.isSpace(ch))
                    throw new XmlValueOutOfRangeException();
            }
            _ranks[i] = commacount + 1;
        }

        // finally fill in dimension array
        _dimensions = internalParseCommaIntString((String)braces.get(braces.size() - 1));

        /*
        for (int i = 0; i < _dimensions.length; i++)
        {
            if (_dimensions[i] < 0)
                _hasIndeterminateDimensions = true;
        }
        */
    }

    /**
     * Returns the QName for the referenced type.
     */
    public QName getQName()
    {
        return _type;
    }

    /**
     * Returns the array of ranks for inner nested arrays.
     * In SOAP 1.1-advanced, this is always an array of length zero.
     * In SOAP 1.1, this array reflects the ranks of nested
     * arrays. For example foo:bar[,][,,][][5,6] will produce
     * a ranks result of 2, 3, 1.
     */
    public int[] getRanks()
    {
        int[] result = new int[_ranks.length];
        System.arraycopy(_ranks, 0, result, 0, result.length);
        return result;
    }

    /**
     * Returns the array of dimensions.
     */
    public int[] getDimensions()
    {
        int[] result = new int[_dimensions.length];
        System.arraycopy(_dimensions, 0, result, 0, result.length);
        return result;
    }

    /**
     * True if this array contains nested arrays. Equivalent
     * to (getRanks().length > 0).
     */
    public boolean containsNestedArrays()
    {
        return (_ranks.length > 0);
    }

    /**
     * Returns the dimensions as a string, e.g., [,][2,3,4]
     */
    public String soap11DimensionString()
    {
        return soap11DimensionString(_dimensions);
    }

    /**
     * Given an actual set of dimensions that may differ from
     * the default that is stored, outputs the soap arrayType
     * string.
     */
    public String soap11DimensionString(int[] actualDimensions)
    {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < _ranks.length; i++)
        {
            sb.append('[');
            for (int j = 1; j < _ranks[i]; j++)
                sb.append(',');
            sb.append(']');
        }

        sb.append('[');
        for (int i = 0; i < actualDimensions.length; i++)
        {
            if (i > 0)
                sb.append(',');
            if (actualDimensions[i] >= 0)
                sb.append(actualDimensions[i]);
        }
        sb.append(']');
        return sb.toString();
    }

    private SOAPArrayType()
    {
    }

    /**
     * SOAP 1.2
     * Constructs a SOAPArrayType from soap-enc:itemType and
     * soap-enc:arraySize attributes
     * @param itemType the item type QName
     * @param arraySize a string with dimentions like: * 3 4
     * @return a SOAPArrayType to represent this
     */
    public static SOAPArrayType newSoap12Array(QName itemType, String arraySize)
    {
        int [] ranks = EMPTY_INT_ARRAY;
        arraySize = XmlWhitespace.collapse(arraySize, XmlWhitespace.WS_COLLAPSE);
        String[] dimStrings = arraySize.split(" ");
        int[] dimensions = new int[dimStrings.length];
        for (int i = 0; i < dimStrings.length; i++)
        {
            String dimString = dimStrings[i];
            if (i==0 && dimString.equals("*"))
            {
                dimensions[i] = -1;
                // _hasIndeterminateDimensions = true;
            }
            else
            {
                try
                {
                    dimensions[i] = Integer.parseInt(dimStrings[i]);
                }
                catch (Exception e)
                {
                    throw new XmlValueOutOfRangeException();
                }
            }
        }
        SOAPArrayType sot = new SOAPArrayType();
        sot._ranks = ranks;
        sot._type = itemType;
        sot._dimensions = dimensions;
        return sot;
    }

    /**
     * SOAP 1.2
     * Given an actual set of dimensions that may differ from
     * the default that is stored, outputs the soap arraySize
     * string.
     */
    public String soap12DimensionString(int[] actualDimensions)
    {
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < actualDimensions.length; i++)
        {
            if (i > 0)
                sb.append(' ');
            if (actualDimensions[i] >= 0)
                sb.append(actualDimensions[i]);
        }
        return sb.toString();
    }

    /**
     * Constructs a SOAPArrayType reflecting the dimensions
     * of the next nested array.
     */
    public SOAPArrayType nestedArrayType()
    {
        if (!containsNestedArrays())
            throw new IllegalStateException();

        SOAPArrayType result = new SOAPArrayType();

        result._type = _type;

        result._ranks = new int[_ranks.length - 1];
        System.arraycopy(_ranks, 0, result._ranks, 0, result._ranks.length);

        result._dimensions = new int[_ranks[_ranks.length - 1]];
        for (int i = 0; i < result._dimensions.length; i++)
            result._dimensions[i] = -1;

        // result._hasIndeterminateDimensions = (result._dimensions.length > 0);

        return result;
    }

    public int hashCode()
    {
        return (_type.hashCode() + _dimensions.length + _ranks.length + (_dimensions.length == 0 ? 0 : _dimensions[0]));
    }

    public boolean equals(Object obj)
    {
        if (obj == this)
            return true;

        if (!obj.getClass().equals(getClass()))
            return false;

        SOAPArrayType sat = (SOAPArrayType)obj;

        if (!_type.equals(sat._type))
            return false;

        if (_ranks.length != sat._ranks.length)
            return false;

        if (_dimensions.length != sat._dimensions.length)
            return false;

        for (int i = 0; i < _ranks.length; i++)
            if (_ranks[i] != sat._ranks[i])
                return false;

        for (int i = 0; i < _dimensions.length; i++)
            if (_dimensions[i] != sat._dimensions[i])
                return false;

        return true;
    }
}
