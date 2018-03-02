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

import java.util.HashMap;

/**
 * Corresponds to the Saver and XmlOptions.
 * <p>
 * This class is used to set up a map containing characters to be escaped.
 * Characters can be escaped as hex, decimal or as a predefined entity (this
 * latter option applies only to the 5 characters defined as predefined entities
 * in the XML Spec).
 * <p>
 * <ul>
 * For example:
 * <pre>
 *      XmlOptionCharEscapeMap escapes = new XmlOptionCharEscapeMap();
 *      escapes.addMapping('A', XmlOptionCharEscapeMap.HEXADECIMAL);
 *      escapes.addMapping('B', XmlOptionCharEscapeMap.DECIMAL);
 *      escapes.addMapping('>', XmlOptionCharEscapeMap.PREDEF_ENTITY);
 *
 *      XmlOptions opts = new XmlOptions();
 *      opts.setSaveSubstituteCharacters(escapes);
 *      System.out.println(myXml.xmlText(opts));
 *
 *      will result in:
 *      A being printed as &#x41;
 *      B being printed as &#66;
 *      > being printed as &gt;
 *
 * </pre>
 */
public class XmlOptionCharEscapeMap
{
    public static final int PREDEF_ENTITY = 0;
    public static final int DECIMAL       = 1;
    public static final int HEXADECIMAL   = 2;

    // map of Character to String which will represent it in the output document
    private HashMap _charMap;

    // internal HashMap just for predefined entities
    private static final HashMap _predefEntities = new HashMap();
    static {
        _predefEntities.put(new Character('<'), "&lt;");
        _predefEntities.put(new Character('>'), "&gt;");
        _predefEntities.put(new Character('&'), "&amp;");
        _predefEntities.put(new Character('\''), "&apos;");
        _predefEntities.put(new Character('"'), "&quot;");
    }

    /**
     * Construct a new XmlOptionCharEncoder.
     */
    public XmlOptionCharEscapeMap()
    {
        _charMap = new HashMap();
    }

    /**
     *  @return whether a character encoding exists for this character
     */
    public boolean containsChar(char ch)
    {
        return _charMap.containsKey(new Character(ch));
    }

    /**
     * set up this character to be escaped in output documents
     * according to the given mode
     */
    public void addMapping(char ch, int mode) throws XmlException
    {
        Character theChar = new Character(ch);
        switch(mode)
        {
            case PREDEF_ENTITY:
                String replString = (String)_predefEntities.get(theChar);
                if ( replString == null )
                {
                    throw new XmlException("XmlOptionCharEscapeMap.addMapping(): " +
                        "the PREDEF_ENTITY mode can only be used for the following " +
                        "characters: <, >, &, \" and '");
                }
                _charMap.put(theChar, replString);
                break;

            case DECIMAL:
                _charMap.put(theChar, "&#" + (int)ch + ";");
                break;

            case HEXADECIMAL:
                String hexCharPoint = Integer.toHexString((int)ch);
                _charMap.put(theChar, "&#x" + hexCharPoint + ";");
                break;

            default:
                throw new XmlException("XmlOptionCharEscapeMap.addMapping(): " +
                    "mode must be PREDEF_ENTITY, DECIMAL or HEXADECIMAL");
        }
    }

    /**
     * set up this contiguous set of characters to be escaped in
     * output documents according to the given mode
     */
    public void addMappings(char ch1, char ch2, int mode) throws XmlException
    {
        if (ch1 > ch2)
        {
            throw new XmlException("XmlOptionCharEscapeMap.addMappings(): " +
                "ch1 must be <= ch2");
        }

        for (char c = ch1; c <= ch2; c++)
        {
            addMapping(c, mode);
        }
    }

    /**
     * returns the escaped String for the character
     */
    public String getEscapedString(char ch)
    {
        return (String)_charMap.get(new Character(ch));
    }
}
