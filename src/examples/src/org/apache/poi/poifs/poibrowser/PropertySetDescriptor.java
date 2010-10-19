/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.apache.poi.poifs.poibrowser;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.poi.hpsf.MarkUnsupportedException;
import org.apache.poi.hpsf.NoPropertySetStreamException;
import org.apache.poi.hpsf.PropertySet;
import org.apache.poi.hpsf.PropertySetFactory;
import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.poifs.filesystem.POIFSDocumentPath;

/**
 * <p>Describes the most important (whatever that is) features of a
 * stream containing a {@link PropertySet}.</p>
 *
 * @author Rainer Klute (klute@rainer-klute.de)
 */
public class PropertySetDescriptor extends DocumentDescriptor
{

    protected PropertySet propertySet;

    /**
     * <p>Returns this {@link PropertySetDescriptor}'s {@link
     * PropertySet}.</p>
     */
    public PropertySet getPropertySet()
    {
        return propertySet;
    }



    /**
     * <p>Creates a {@link PropertySetDescriptor} by reading a {@link
     * PropertySet} from a {@link DocumentInputStream}.</p>
     *
     * @param name The stream's name.
     *
     * @param path The stream's path in the POI filesystem hierarchy.
     *
     * @param stream The stream.
     *
     * @param nrOfBytesToDump The maximum number of bytes to display in a
     * dump starting at the beginning of the stream.
     */
    public PropertySetDescriptor(final String name,
                                 final POIFSDocumentPath path,
                                 final DocumentInputStream stream,
                                 final int nrOfBytesToDump)
        throws NoPropertySetStreamException,
               MarkUnsupportedException, UnsupportedEncodingException,
               IOException
    {
        super(name, path, stream, nrOfBytesToDump);
        propertySet = PropertySetFactory.create(stream);
    }

}
