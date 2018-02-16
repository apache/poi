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

package org.apache.xmlbeans.impl.common;

import org.apache.xmlbeans.XmlCursor;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;

/**
 * First event must be a BEGIN with no name (to communicate any xsi
 * information).  BEGIN and END need to match.
 */

public interface ValidatorListener
{
    public static final int BEGIN    = 1; // Begin element
    public static final int END      = 2; // End element
    public static final int TEXT     = 3; // Text
    public static final int ATTR     = 4; // Attr (non-namespaces, non xsi only)
    public static final int ENDATTRS = 5; // after BEGIN, after attrs

    void nextEvent ( int kind, Event event );

    public interface Event extends PrefixResolver
    {
        public static final int PRESERVE = 1;
        public static final int REPLACE  = 2;
        public static final int COLLAPSE = 3;

        XmlCursor getLocationAsCursor ( );
        Location getLocation();

        String getXsiType  ( ); // BEGIN xsi:type
        String getXsiNil   ( ); // BEGIN xsi:nil
        String getXsiLoc   ( ); // BEGIN xsi:schemaLocation
        String getXsiNoLoc ( ); // BEGIN xsi:noNamespaceSchemaLocation
                
        // On START and ATTR
        QName getName ( );
        
        // On TEXT and ATTR
        String getText ( );
        String getText (int wsr );

        boolean textIsWhitespace ( );
    }
}