/*
 *  ====================================================================
 *  The Apache Software License, Version 1.1
 *
 *  Copyright (c) 2003 The Apache Software Foundation.  All rights
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LittleEndianConsts;



/**
 * <p>Adds writing support to the {@link PropertySet} class.</p>
 *
 * <p>Please be aware that this class' functionality will be merged into the
 * {@link PropertySet} class at a later time, so the API will change.</p>
 *
 * @author Rainer Klute <a
 * href="mailto:klute@rainer-klute.de">&lt;klute@rainer-klute.de&gt;</a>
 * @version $Id$
 * @since 2003-02-19
 */
public class MutablePropertySet extends PropertySet
{

    /**
     * <p>Constructs a <code>MutablePropertySet</code> instance. Its
     * primary task is to initialize the immutable field with their proper
     * values. It also sets fields that might change to reasonable defaults.</p>
     */
    public MutablePropertySet()
    {
        /* Initialize the "byteOrder" field. */
        byteOrder = LittleEndian.getUShort(BYTE_ORDER_ASSERTION);

        /* Initialize the "format" field. */
        format = LittleEndian.getUShort(FORMAT_ASSERTION);

        /* Initialize "osVersion" field as if the property has been created on
         * a Win32 platform, whether this is the case or not. */
        osVersion = (OS_WIN32 << 16) | 0x0A04;

        /* Initailize the "classID" field. */
        classID = new ClassID();

        /* Initialize the sections. Since property set must have at least
         * one section it is added right here. */
        sections = new LinkedList();
        sections.add(new MutableSection());
    }



    /**
     * <p>Constructs a <code>MutablePropertySet</code> by doing a deep copy of
     * an existing <code>PropertySet</code>. All nested elements, i.e. 
     * <code>Section</code>s and <code>Property</code> instances, will be their
     * mutable counterparts in the new <code>MutablePropertySet</code>.</p>
     * 
     * @param ps The property set to copy
     */
    public MutablePropertySet(final PropertySet ps)
    {
        byteOrder = ps.getByteOrder();
        format = ps.getFormat();
        osVersion = ps.getOSVersion();
        classID = new ClassID(ps.getClassID().getBytes(), 0);
        clearSections();
        for (final Iterator i = ps.getSections().iterator(); i.hasNext();)
        {
            final MutableSection s = new MutableSection((Section) (i.next()));
            addSection(s);
        }
    }



    /**
     * <p>The length of the property set stream header.</p>
     */
    private final int OFFSET_HEADER =
        BYTE_ORDER_ASSERTION.length + /* Byte order    */
        FORMAT_ASSERTION.length +     /* Format        */
        LittleEndianConsts.INT_SIZE + /* OS version    */
        ClassID.LENGTH +              /* Class ID      */
        LittleEndianConsts.INT_SIZE;  /* Section count */



    /**
     * <p>Sets the "byteOrder" property.</p>
     *
     * @param byteOrder the byteOrder value to set
     */
    public void setByteOrder(final int byteOrder)
    {
        this.byteOrder = byteOrder;
    }



    /**
     * <p>Sets the "format" property.</p>
     *
     * @param format the format value to set
     */
    public void setFormat(final int format)
    {
        this.format = format;
    }



    /**
     * <p>Sets the "osVersion" property.</p>
     *
     * @param osVersion the osVersion value to set
     */
    public void setOSVersion(final int osVersion)
    {
        this.osVersion = osVersion;
    }



    /**
     * <p>Sets the property set stream's low-level "class ID"
     * field.</p>
     *
     * @param classID The property set stream's low-level "class ID" field.
     *
     * @see #getClassID
     */
    public void setClassID(final ClassID classID)
    {
        this.classID = classID;
    }



    /**
     * <p>Removes all sections from this property set.</p>
     */
    public void clearSections()
    {
        sections = null;
        sectionCount = 0;
    }



    /**
     * <p>Adds a section to this property set.</p>
     *
     * @param section The {@link Section} to add. It will be appended
     * after any sections that are already present in the property set
     * and thus become the last section.
     */
    public void addSection(final Section section)
    {
        if (sections == null)
            sections = new LinkedList();
        sections.add(section);
        sectionCount = sections.size();
    }



    /**
     * <p>Writes the property set to an output stream.</p>
     * 
     * @param out the output stream to write the section to
     * @exception IOException if an error when writing to the output stream
     * occurs
     * @exception WritingNotSupportedException if HPSF does not yet support
     * writing a property's variant type.
     */
    public void write(final OutputStream out)
        throws WritingNotSupportedException, IOException
    {
        /* Write the number of sections in this property set stream. */
        final int nrSections = sections.size();
        int length = 0;

        /* Write the property set's header. */
        length += TypeWriter.writeToStream(out, (short) getByteOrder());
        length += TypeWriter.writeToStream(out, (short) getFormat());
        length += TypeWriter.writeToStream(out, (int) getOSVersion());
        length += TypeWriter.writeToStream(out, getClassID());
        length += TypeWriter.writeToStream(out, (int) nrSections);
        int offset = OFFSET_HEADER;

        /* Write the section list, i.e. the references to the sections. Each
         * entry in the section list consist of the section's class ID and the
         * section's offset relative to the beginning of the stream. */
        offset += nrSections * (ClassID.LENGTH + LittleEndian.INT_SIZE);
        final int sectionsBegin = offset;
        for (final ListIterator i = sections.listIterator(); i.hasNext();)
        {
            final MutableSection s = (MutableSection) i.next();
            final ClassID formatID = s.getFormatID();
            if (formatID == null)
                throw new NoFormatIDException();
            length += TypeWriter.writeToStream(out, s.getFormatID());
            length += TypeWriter.writeUIntToStream(out, offset);
            offset += s.getSize();
        }

        /* Write the sections themselves. */
        offset = sectionsBegin;
        for (final ListIterator i = sections.listIterator(); i.hasNext();)
        {
            final MutableSection s = (MutableSection) i.next();
            offset += s.write(out);
        }
    }



    /**
     * <p>Returns the contents of this property set stream as an input stream.
     * The latter can be used for example to write the property set into a POIFS
     * document. The input stream represents a snapshot of the property set.
     * If the latter is modified while the input stream is still being
     * read, the modifications will not be reflected in the input stream but in
     * the {@link MutablePropertySet} only.</p>
     *
     * @return the contents of this property set stream
     * 
     * @throws WritingNotSupportedException if HPSF does not yet support writing
     * of a property's variant type.
     * @throws IOException if an I/O exception occurs.
     */
    public InputStream toInputStream()
        throws IOException, WritingNotSupportedException
    {
        final ByteArrayOutputStream psStream = new ByteArrayOutputStream();
        write(psStream);
        psStream.close();
        final byte[] streamData = psStream.toByteArray();
        return new ByteArrayInputStream(streamData);
    }

}