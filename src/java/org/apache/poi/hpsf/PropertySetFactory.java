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

package org.apache.poi.hpsf;

import java.io.*;

/**
 * <p>Factory class to create instances of {@link SummaryInformation},
 * {@link DocumentSummaryInformation} and {@link PropertySet}.</p>
 *
 * @author Rainer Klute (klute@rainer-klute.de)
 * @version $Id$
 * @since 2002-02-09
 */
public class PropertySetFactory
{

    /**
     * <p>Creates the most specific {@link PropertySet} from an {@link
     * InputStream}. This is preferrably a {@link
     * DocumentSummaryInformation} or a {@link SummaryInformation}. If
     * the specified {@link InputStream} does not contain a property
     * set stream, an exception is thrown and the {@link InputStream}
     * is repositioned at its beginning.</p>
     *
     * @param stream Contains the property set stream's data.
     * @return The created {@link PropertySet}.
     * @throws NoPropertySetStreamException if the stream does not
     * contain a property set.
     * @throws MarkUnsupportedException if the stream does not support
     * the <code>mark</code> operation.
     * @throws UnexpectedPropertySetTypeException if the property
     * set's type is unexpected.
     * @throws IOException if some I/O problem occurs.
     */
    public static PropertySet create(final InputStream stream)
	throws NoPropertySetStreamException, MarkUnsupportedException,
	       UnexpectedPropertySetTypeException, IOException
    {
        final PropertySet ps = new PropertySet(stream);
        if (ps.isSummaryInformation())
            return new SummaryInformation(ps);
        else if (ps.isDocumentSummaryInformation())
            return new DocumentSummaryInformation(ps);
        else
            return ps;
    }

}
