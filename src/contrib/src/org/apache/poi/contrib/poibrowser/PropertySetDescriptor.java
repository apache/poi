
/* ====================================================================
   Copyright 2002-2004   Apache Software Foundation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */
        

package org.apache.poi.contrib.poibrowser;

import java.io.*;
import org.apache.poi.hpsf.*;
import org.apache.poi.poifs.filesystem.*;

/**
 * <p>Describes the most important (whatever that is) features of a
 * stream containing a {@link PropertySet}.</p>
 *
 * @author Rainer Klute (klute@rainer-klute.de)
 * @version $Id$
 * @since 2002-02-05
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
     * @param nrOfBytes The maximum number of bytes to display in a
     * dump starting at the beginning of the stream.
     */
    public PropertySetDescriptor(final String name,
                                 final POIFSDocumentPath path,
                                 final DocumentInputStream stream,
                                 final int nrOfBytesToDump)
        throws NoPropertySetStreamException, MarkUnsupportedException,
        UnexpectedPropertySetTypeException, IOException
    {
        super(name, path, stream, nrOfBytesToDump);
        propertySet = PropertySetFactory.create(stream);
    }

}
