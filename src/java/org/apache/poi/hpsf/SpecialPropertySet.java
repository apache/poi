/*
 *  ====================================================================
 *  The Apache Software License, Version 1.1
 *
 *  Copyright (c) 2000 The Apache Software Foundation.  All rights
 *  reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions
 *  are met:
 *
 *  1. Redistributions of source code must retain the above copyright
 *  notice, this list of conditions and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright
 *  notice, this list of conditions and the following disclaimer in
 *  the documentation and/or other materials provided with the
 *  distribution.
 *
 *  3. The end-user documentation included with the redistribution,
 *  if any, must include the following acknowledgment:
 *  "This product includes software developed by the
 *  Apache Software Foundation (http://www.apache.org/)."
 *  Alternately, this acknowledgment may appear in the software itself,
 *  if and wherever such third-party acknowledgments normally appear.
 *
 *  4. The names "Apache" and "Apache Software Foundation" must
 *  not be used to endorse or promote products derived from this
 *  software without prior written permission. For written
 *  permission, please contact apache@apache.org.
 *
 *  5. Products derived from this software may not be called "Apache",
 *  nor may "Apache" appear in their name, without prior written
 *  permission of the Apache Software Foundation.
 *
 *  THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *  OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 *  ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 *  USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 *  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 *  OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 *  SUCH DAMAGE.
 *  ====================================================================
 *
 *  This software consists of voluntary contributions made by many
 *  individuals on behalf of the Apache Software Foundation.  For more
 *  information on the Apache Software Foundation, please see
 *  <http://www.apache.org/>.
 */
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
 * @author Rainer Klute (klute@rainer-klute.de)
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
