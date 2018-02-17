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

import org.apache.xmlbeans.xml.stream.XMLEvent;
import org.apache.xmlbeans.xml.stream.events.ElementTypeNames;

/**
 * Abstract base class which implements the type part XMLEvent.
 */

public abstract class XmlEventBase implements XMLEvent
{
    public XmlEventBase ( )
    {
    }
    
    public XmlEventBase ( int type )
    {
        _type = type;
    }

    public void setType ( int type )
    {
        _type = type;
    }
    
    public int getType ( )
    {
        return _type;
    }

    public String getTypeAsString ( )
    {
        return ElementTypeNames.getName( _type );
    }
            
    public boolean isStartElement ( )
    {
        return _type == XMLEvent.START_ELEMENT;
    }
    
    public boolean isEndElement ( )
    {
        return _type == XMLEvent.END_ELEMENT;
    }
    
    public boolean isEntityReference ( )
    {
        return _type == XMLEvent.ENTITY_REFERENCE;
    }
    
    public boolean isStartPrefixMapping ( )
    {
        return _type == XMLEvent.START_PREFIX_MAPPING;
    }
    
    public boolean isEndPrefixMapping ( )
    {
        return _type == XMLEvent.END_PREFIX_MAPPING;
    }
    
    public boolean isChangePrefixMapping ( )
    {
        return _type == XMLEvent.CHANGE_PREFIX_MAPPING;
    }
    
    public boolean isProcessingInstruction ( )
    {
        return _type == XMLEvent.PROCESSING_INSTRUCTION;
    }
    
    public boolean isCharacterData ( )
    {
        return _type == XMLEvent.CHARACTER_DATA;
    }
    
    public boolean isSpace ( )
    {
        return _type == XMLEvent.SPACE;
    }
    
    public boolean isNull ( )
    {
        return _type == XMLEvent.NULL_ELEMENT;
    }
    
    public boolean isStartDocument ( )
    {
        return _type == XMLEvent.START_DOCUMENT;
    }
    
    public boolean isEndDocument ( )
    {
        return _type == XMLEvent.END_DOCUMENT;
    }

    private int _type;
}
