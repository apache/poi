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

import org.xml.sax.ContentHandler;
import org.xml.sax.ext.LexicalHandler;

/**
 * A holder for a SAX {@link ContentHandler} and {@link LexicalHandler} that are
 * capable of loading an {@link XmlObject} instance.  Once all the SAX events are pushed
 * to the handlers, call {@link #getObject()} to get the loaded XmlObject.
 * 
 * @see XmlObject.Factory#newXmlSaxHandler
 * @see SchemaTypeLoader#newXmlSaxHandler
 */
public interface XmlSaxHandler
{
    /**
     * The ContentHandler expecting SAX content events.
     * @see ContentHandler
     */ 
    ContentHandler getContentHandler ( );
    
    /**
     * The LexicalHandler expecting SAX lexical events.
     * @see LexicalHandler
     */ 
    LexicalHandler getLexicalHandler ( );
    
    /**
     * Insert a bookmark before the token associated with the last SAX event.
     */ 
    void bookmarkLastEvent ( XmlCursor.XmlBookmark mark );
    
    /**
     * Insert a bookmark before the attr token associated with the last SAX element event.
     */ 
    void bookmarkLastAttr ( QName attrName, XmlCursor.XmlBookmark mark );
    
    /** Returns the loaded XmlObject after all the SAX events have been finished */
    XmlObject getObject ( ) throws XmlException;
}