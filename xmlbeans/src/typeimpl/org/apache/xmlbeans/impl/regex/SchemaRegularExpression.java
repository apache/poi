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

package org.apache.xmlbeans.impl.regex;

import org.apache.xmlbeans.impl.common.XMLChar;

import java.util.HashMap;
import java.util.Map;

public class SchemaRegularExpression extends RegularExpression
{
    private SchemaRegularExpression(String pattern)
    {
        super(pattern, "X");
    }

    public static RegularExpression forPattern(String s)
    {
        SchemaRegularExpression tre = (SchemaRegularExpression)knownPatterns.get(s);
        if (tre != null)
            return tre;
        return new RegularExpression(s, "X");
    }

    static final Map knownPatterns = buildKnownPatternMap();

    private static Map buildKnownPatternMap()
    {
        Map result = new HashMap();
        result.put("\\c+", new SchemaRegularExpression("\\c+")
            { public boolean matches(String s) { return XMLChar.isValidNmtoken(s); } } );
        result.put("\\i\\c*", new SchemaRegularExpression("\\i\\c*")
            { public boolean matches(String s) { return XMLChar.isValidName(s); } } );
        result.put("[\\i-[:]][\\c-[:]]*", new SchemaRegularExpression("[\\i-[:]][\\c-[:]]*")
            { public boolean matches(String s) { return XMLChar.isValidNCName(s); } } );
        return result;
    }
}
