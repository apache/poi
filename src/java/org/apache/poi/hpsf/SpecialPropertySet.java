
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

import java.util.List;

/**
 * <p>Abstract superclass for the convenience classes {@link
 * SummaryInformation} and {@link DocumentSummaryInformation}.</p>
 *
 * <p>The motivation behind this class is quite nasty if you look
 * behind the scenes, but it serves the application programmer well by
 * providing him with the easy-to-use {@link SummaryInformation} and
 * {@link DocumentSummaryInformation} classes. When parsing the data a
 * property set stream consists of (possibly coming from an {@link
 * java.io.InputStream}) we want to read and process each byte only
 * once. Since we don't know in advance which kind of property set we
 * have, we can expect only the most general {@link
 * PropertySet}. Creating a special subclass should be as easy as
 * calling the special subclass' constructor and pass the general
 * {@link PropertySet} in. To make things easy internally, the special
 * class just holds a reference to the general {@link PropertySet} and
 * delegates all method calls to it.</p>
 *
 * <p>A cleaner implementation would have been like this: The {@link
 * PropertySetFactory} parses the stream data into some internal
 * object first.  Then it finds out whether the stream is a {@link
 * SummaryInformation}, a {@link DocumentSummaryInformation} or a
 * general {@link PropertySet}.  However, the current implementation
 * went the other way round historically: the convenience classes came
 * only late to my mind.</p>
 *
 * @author Rainer Klute <a
 * href="mailto:klute@rainer-klute.de">&lt;klute@rainer-klute.de&gt;</a>
 * @version $Id$
 * @since 2002-02-09
 */
public abstract class SpecialPropertySet extends PropertySet
{

    /**
     * <p>The "real" property set <code>SpecialPropertySet</code>
     * delegates to.</p>
     */
    private PropertySet delegate;



    /**
     * <p>Creates a <code>SpecialPropertySet</code>.
     *
     * @param ps The property set encapsulated by the
     * <code>SpecialPropertySet</code>
     */
    public SpecialPropertySet(final PropertySet ps)
    {
        delegate = ps;
    }



    /**
     * @see PropertySet#getByteOrder
     */
    public int getByteOrder()
    {
        return delegate.getByteOrder();
    }



    /**
     * @see PropertySet#getFormat
     */
    public int getFormat()
    {
        return delegate.getFormat();
    }



    /**
     * @see PropertySet#getOSVersion
     */
    public int getOSVersion()
    {
        return delegate.getOSVersion();
    }



    /**
     * @see PropertySet#getClassID
     */
    public ClassID getClassID()
    {
        return delegate.getClassID();
    }



    /**
     * @see PropertySet#getSectionCount
     */
    public int getSectionCount()
    {
        return delegate.getSectionCount();
    }



    /**
     * @see PropertySet#getSections
     */
    public List getSections()
    {
        return delegate.getSections();
    }



    /**
     * @see PropertySet#isSummaryInformation
     */
    public boolean isSummaryInformation()
    {
        return delegate.isSummaryInformation();
    }



    /**
     * @see PropertySet#isDocumentSummaryInformation
     */
    public boolean isDocumentSummaryInformation()
    {
        return delegate.isDocumentSummaryInformation();
    }



    /**
     * @see PropertySet#getSingleSection
     */
    public Section getSingleSection()
    {
        return delegate.getSingleSection();
    }

}
