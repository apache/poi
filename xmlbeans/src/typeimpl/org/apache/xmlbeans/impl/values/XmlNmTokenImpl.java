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

package org.apache.xmlbeans.impl.values;

import org.apache.xmlbeans.XmlNMTOKEN;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlErrorCodes;
import org.apache.xmlbeans.impl.common.ValidationContext;
import org.apache.xmlbeans.impl.common.XMLChar;


public class XmlNmTokenImpl extends JavaStringHolderEx implements XmlNMTOKEN
{
    public XmlNmTokenImpl()
        { super(XmlNMTOKEN.type, false); }
    public XmlNmTokenImpl(SchemaType type, boolean complex)
        { super(type, complex); }

    public static void validateLexical(String v, ValidationContext context)
    {
        if ( !XMLChar.isValidNmtoken(v) )
        {
            context.invalid(XmlErrorCodes.NMTOKEN, new Object[] { v });
            return;
        }
    }
}
