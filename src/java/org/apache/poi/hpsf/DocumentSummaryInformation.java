/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2000 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.poi.hpsf;

import java.io.*;
import java.util.*;
import org.apache.poi.hpsf.wellknown.*;

/**
 * <p>Convenience class representing a DocumentSummary Information
 * stream in a Microsoft Office document.</p>
 *
 * @see SummaryInformation
 *
 * @author Rainer Klute (klute@rainer-klute.de)
 * @version $Id$
 * @since 2002-02-09
 */
public class DocumentSummaryInformation extends SpecialPropertySet
{

    /**
     * <p>Creates a {@link DocumentSummaryInformation} from a given
     * {@link PropertySet}.</p>
     *
     * @param ps A property set which should be created from a
     * document summary information stream.
     *
     * @throws UnexpectedPropertySetTypeException if <var>ps</var>
     * does not contain a document summary information stream.
     */
    public DocumentSummaryInformation(final PropertySet ps)
        throws UnexpectedPropertySetTypeException
    {
        super(ps);
        if (!isDocumentSummaryInformation())
            throw new UnexpectedPropertySetTypeException
                ("Not a " + getClass().getName());
    }



    /**
     * <p>Returns the stream's category (or <code>null</code>).</p>
     */
    public String getCategory()
    {
        return (String) getProperty(PropertyIDMap.PID_CATEGORY);
    }



    /**
     * <p>Returns the stream's presentation format (or
     * <code>null</code>).</p>
     */
    public String getPresentationFormat()
    {
        return (String) getProperty(PropertyIDMap.PID_PRESFORMAT);
    }



    /**
     * <p>Returns the stream's byte count or 0 if the {@link
     * DocumentSummaryInformation} does not contain a byte count.</p>
     */
    public int getByteCount()
    {
        return getPropertyIntValue(PropertyIDMap.PID_BYTECOUNT);
    }



    /**
     * <p>Returns the stream's line count or 0 if the {@link
     * DocumentSummaryInformation} does not contain a line count.</p>
     */
    public int getLineCount()
    {
        return getPropertyIntValue(PropertyIDMap.PID_LINECOUNT);
    }



    /**
     * <p>Returns the stream's par count or 0 if the {@link
     * DocumentSummaryInformation} does not contain a par count.</p>
     */
    public int getParCount()
    {
        return getPropertyIntValue(PropertyIDMap.PID_PARCOUNT);
    }



    /**
     * <p>Returns the stream's slide count or 0 if the {@link
     * DocumentSummaryInformation} does not contain a slide count.</p>
     */
    public int getSlideCount()
    {
        return getPropertyIntValue(PropertyIDMap.PID_SLIDECOUNT);
    }



    /**
     * <p>Returns the stream's note count or 0 if the {@link
     * DocumentSummaryInformation} does not contain a note count.</p>
     */
    public int getNoteCount()
    {
        return getPropertyIntValue(PropertyIDMap.PID_NOTECOUNT);
    }



    /**
     * <p>Returns the stream's hidden count or 0 if the {@link
     * DocumentSummaryInformation} does not contain a hidden count.</p>
     */
    public int getHiddenCount()
    {
        return getPropertyIntValue(PropertyIDMap.PID_HIDDENCOUNT);
    }



    /**
     * <p>Returns the stream's mmclip count or 0 if the {@link
     * DocumentSummaryInformation} does not contain a mmclip count.</p>
     */
    public int getMMClipCount()
    {
        return getPropertyIntValue(PropertyIDMap.PID_MMCLIPCOUNT);
    }



    /**
     * <p>Returns the stream's scale (or <code>null</code>)
     * <strong>when this method is implemented. Please note that the
     * return type is likely to change!</strong>
     */
    public byte[] getScale()
    {
        if (true)
            throw new UnsupportedOperationException("FIXME");
        return (byte[]) getProperty(PropertyIDMap.PID_SCALE);
    }



    /**
     * <p>Returns the stream's heading pair (or <code>null</code>)
     * <strong>when this method is implemented. Please note that the
     * return type is likely to change!</strong>
     */
    public byte[] getHeadingPair()
    {
        if (true)
            throw new UnsupportedOperationException("FIXME");
        return (byte[]) getProperty(PropertyIDMap.PID_HEADINGPAIR);
    }



    /**
     * <p>Returns the stream's doc parts (or <code>null</code>)
     * <strong>when this method is implemented. Please note that the
     * return type is likely to change!</strong>
     */
    public byte[] getDocparts()
    {
        if (true)
            throw new UnsupportedOperationException("FIXME");
        return (byte[]) getProperty(PropertyIDMap.PID_DOCPARTS);
    }



    /**
     * <p>Returns the stream's manager (or <code>null</code>).</p>
     */
    public String getManager()
    {
        return (String) getProperty(PropertyIDMap.PID_MANAGER);
    }



    /**
     * <p>Returns the stream's company (or <code>null</code>).</p>
     */
    public String getCompany()
    {
        return (String) getProperty(PropertyIDMap.PID_COMPANY);
    }



    /**
     * <p>Returns the stream's links dirty (or <code>null</code>)
     * <strong>when this method is implemented. Please note that the
     * return type is likely to change!</strong>
     */
    public byte[] getLinksDirty()
    {
        if (true)
            throw new UnsupportedOperationException("FIXME");
        return (byte[]) getProperty(PropertyIDMap.PID_LINKSDIRTY);
    }

}
