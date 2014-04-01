/*
 *  ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ====================================================================
 */

package org.apache.poi.hwpf.model;

import java.text.MessageFormat;

import org.apache.poi.util.Internal;

/**
 * Structure describing the Plex for fields (contained plclfd* in the spec).
 * 
 * @author Cedric Bosdonnat <cbosdonnat@novell.com>
 */
@Internal
public class PlexOfField
{

    private final GenericPropertyNode propertyNode;
    private final FieldDescriptor fld;

    @Deprecated
    public PlexOfField( int fcStart, int fcEnd, byte[] data )
    {
        propertyNode = new GenericPropertyNode( fcStart, fcEnd, data );
        fld = new FieldDescriptor( data );
    }

    public PlexOfField( GenericPropertyNode propertyNode )
    {
        this.propertyNode = propertyNode;
        fld = new FieldDescriptor( propertyNode.getBytes() );
    }

    public int getFcStart()
    {
        return propertyNode.getStart();
    }

    public int getFcEnd()
    {
        return propertyNode.getEnd();
    }

    public FieldDescriptor getFld()
    {
        return fld;
    }

    public String toString()
    {
        return MessageFormat.format( "[{0}, {1}) - FLD - 0x{2}; 0x{3}",
                getFcStart(), getFcEnd(),
                Integer.toHexString( 0xff & fld.getBoundaryType() ),
                Integer.toHexString( 0xff & fld.getFlt() ) );
    }
}
