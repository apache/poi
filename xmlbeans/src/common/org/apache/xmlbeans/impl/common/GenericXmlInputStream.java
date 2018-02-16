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

import org.apache.xmlbeans.xml.stream.ReferenceResolver;
import org.apache.xmlbeans.xml.stream.XMLEvent;
import org.apache.xmlbeans.xml.stream.XMLInputStream;
import org.apache.xmlbeans.xml.stream.XMLName;
import org.apache.xmlbeans.xml.stream.XMLStreamException;

/**
 * @deprecated XMLInputStream was deprecated by XMLStreamReader from STaX - jsr173 API.
 */
public class GenericXmlInputStream implements XMLInputStream
{
    public GenericXmlInputStream ( )
    {
        _master = this;
        _elementCount = 1; // Go all the way
    }

    private GenericXmlInputStream ( GenericXmlInputStream master )
    {
        (_master = master).ensureInit();
        _nextEvent = master._nextEvent;
    }

    //
    // The source for all events
    //

    protected XMLEvent nextEvent ( ) throws XMLStreamException
    {
        throw new RuntimeException( "nextEvent not overridden" );
    }

    //
    //
    //

    private class EventItem
    {
        EventItem ( XMLEvent e )
        {
            _event = e;
        }

        int     getType ( ) { return _event.getType(); }
        boolean hasName ( ) { return _event.hasName(); }
        XMLName getName ( ) { return _event.getName(); }

        final XMLEvent _event;

        EventItem _next;
    }

    private void ensureInit ( )
    {
        if (!_master._initialized)
        {
            try
            {
                _master._nextEvent = getNextEvent();
            }
            catch ( XMLStreamException e )
            {
                throw new RuntimeException( e );
            }

            _master._initialized = true;
        }
    }

    private EventItem getNextEvent ( ) throws XMLStreamException
    {
        XMLEvent e = nextEvent();

        return e == null ? null : new EventItem( e );
    }

    public XMLEvent next ( ) throws XMLStreamException
    {
        ensureInit();

        EventItem currentEvent = _nextEvent;

        if (_nextEvent != null)
        {
            if (_nextEvent._next == null)
                _nextEvent._next = _master.getNextEvent();

            _nextEvent = _nextEvent._next;
        }

        if (currentEvent == null)
            return null;

        if (currentEvent.getType() == XMLEvent.END_ELEMENT)
        {
            if (--_elementCount <= 0)
                _nextEvent = null;
        }
        else if (currentEvent.getType() == XMLEvent.START_ELEMENT)
            _elementCount++;

        return currentEvent._event;
    }

    public boolean hasNext ( ) throws XMLStreamException
    {
        ensureInit();

        return _nextEvent != null;
    }

    public void skip ( ) throws XMLStreamException
    {
        next();
    }

    public void skipElement ( ) throws XMLStreamException
    {
        ensureInit();

        for ( ; _nextEvent != null ; next() )
        {
            if (_nextEvent.getType() == XMLEvent.START_ELEMENT)
                break;
        }

        int count = 0;

        for ( ; _nextEvent != null ; next() )
        {
            int type = next().getType();

            if (type == XMLEvent.START_ELEMENT)
                count++;
            else if (type == XMLEvent.END_ELEMENT && --count == 0)
                break;
        }
    }

    public XMLEvent peek ( ) throws XMLStreamException
    {
        ensureInit();

        return _nextEvent._event;
    }

    public boolean skip ( int eventType ) throws XMLStreamException
    {
        ensureInit();

        for ( ; _nextEvent != null ; next() )
        {
            if (_nextEvent.getType() == eventType)
                return true;
        }

        return false;
    }

    public boolean skip ( XMLName name ) throws XMLStreamException
    {
        ensureInit();

        for ( ; _nextEvent != null ; next() )
        {
            if (_nextEvent.hasName() && _nextEvent.getName().equals( name ))
                return true;
        }

        return false;
    }

    public boolean skip ( XMLName name, int eventType ) throws XMLStreamException
    {
        ensureInit();

        for ( ; _nextEvent != null ; next() )
        {
            if (_nextEvent.getType() == eventType &&
                  _nextEvent.hasName() &&
                    _nextEvent.getName().equals( name ))
            {
                return true;
            }
        }

        return false;
    }

    public XMLInputStream getSubStream ( ) throws XMLStreamException
    {
        ensureInit();

        GenericXmlInputStream subStream = new GenericXmlInputStream( this );

        subStream.skip( XMLEvent.START_ELEMENT );

        return subStream;
    }

    public void close ( ) throws XMLStreamException
    {
        // BUGBUG - can I do anything here, really?
        // SHould I count the number of open sub streams?
        // I have no destructor, how can I close properly?
    }

    public ReferenceResolver getReferenceResolver ( )
    {
        ensureInit();

        throw new RuntimeException( "Not impl" );
    }

    public void setReferenceResolver ( ReferenceResolver resolver )
    {
        ensureInit();

        throw new RuntimeException( "Not impl" );
    }

    private boolean               _initialized;
    private EventItem             _nextEvent;
    private int                   _elementCount;
    private GenericXmlInputStream _master;
}
