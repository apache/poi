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
 *
 * Portions of this software are based upon public domain software
 * originally written at the National Center for Supercomputing Applications,
 * University of Illinois, Urbana-Champaign.
 */

package org.apache.poi.hpsf;

import java.io.*;
import java.util.*;
import org.apache.poi.hpsf.wellknown.*;

/**
 * <p>Convenience class representing a Summary Information stream in a
 * Microsoft Office document.</p>
 *
 * <p>See <a
 * href="http://msdn.microsoft.com/library/default.asp?url=/library/en-us/com/stgu_8910.asp">http://msdn.microsoft.com/library/default.asp?url=/library/en-us/com/stgu_8910.asp</a>
 * for documentation from That Redmond Company.
 *
 * @see DocumentSummaryInformation
 *
 * @author Rainer Klute (klute@rainer-klute.de)
 * @version $Id$
 * @since 2002-02-09
 */
public class SummaryInformation extends SpecialPropertySet
{

    /**
     * <p>Creates a {@link SummaryInformation} from a given {@link
     * PropertySet}.</p>
     *
     * @param ps A property set which should be created from a summary
     * information stream.
     *
     * @throws UnexpectedPropertySetTypeException if <var>ps</var>
     * does not contain a summary information stream.
     */
    public SummaryInformation(final PropertySet ps)
        throws UnexpectedPropertySetTypeException
    {
        super(ps);
        if (!isSummaryInformation())
            throw new UnexpectedPropertySetTypeException
                ("Not a " + getClass().getName());
    }



    /**
     * <p>Returns the stream's title (or <code>null</code>).</p>
     */
    public String getTitle()
    {
        return (String) getProperty(PropertyIDMap.PID_TITLE);
    }



    /**
     * <p>Returns the stream's subject (or <code>null</code>).</p>
     */
    public String getSubject()
    {
        return (String) getProperty(PropertyIDMap.PID_SUBJECT);
    }



    /**
     * <p>Returns the stream's author (or <code>null</code>).</p>
     */
    public String getAuthor()
    {
        return (String) getProperty(PropertyIDMap.PID_AUTHOR);
    }



    /**
     * <p>Returns the stream's keywords (or <code>null</code>).</p>
     */
    public String getKeywords()
    {
        return (String) getProperty(PropertyIDMap.PID_KEYWORDS);
    }



    /**
     * <p>Returns the stream's comments (or <code>null</code>).</p>
     */
    public String getComments()
    {
        return (String) getProperty(PropertyIDMap.PID_COMMENTS);
    }



    /**
     * <p>Returns the stream's template (or <code>null</code>).</p>
     */
    public String getTemplate()
    {
        return (String) getProperty(PropertyIDMap.PID_TEMPLATE);
    }



    /**
     * <p>Returns the stream's last author (or <code>null</code>).</p>
     */
    public String getLastAuthor()
    {
        return (String) getProperty(PropertyIDMap.PID_LASTAUTHOR);
    }



    /**
     * <p>Returns the stream's revision number (or
     * <code>null</code>).
     </p> */
    public String getRevNumber()
    {
        return (String) getProperty(PropertyIDMap.PID_REVNUMBER);
    }



    /**
     * <p>Returns the stream's edit time (or <code>null</code>).</p>
     */
    public Date getEditTime()
    {
        return (Date) getProperty(PropertyIDMap.PID_EDITTIME);
    }



    /**
     * <p>Returns the stream's last printed time (or
     * <code>null</code>).</p>
     */
    public Date getLastPrinted()
    {
        return (Date) getProperty(PropertyIDMap.PID_LASTPRINTED);
    }



    /**
     * <p>Returns the stream's creation time (or
     * <code>null</code>).</p>
     */
    public Date getCreateDateTime()
    {
        return (Date) getProperty(PropertyIDMap.PID_CREATE_DTM);
    }



    /**
     * <p>Returns the stream's last save time (or
     * <code>null</code>).</p>
     */
    public Date getLastSaveDateTime()
    {
        return (Date) getProperty(PropertyIDMap.PID_LASTSAVE_DTM);
    }



    /**
     * <p>Returns the stream's page count or 0 if the {@link
     * SummaryInformation} does not contain a page count.</p>
     */
    public int getPageCount()
    {
        return getPropertyIntValue(PropertyIDMap.PID_PAGECOUNT);
    }



    /**
     * <p>Returns the stream's word count or 0 if the {@link
     * SummaryInformation} does not contain a word count.</p>
     */
    public int getWordCount()
    {
        return getPropertyIntValue(PropertyIDMap.PID_WORDCOUNT);
    }



    /**
     * <p>Returns the stream's char count or 0 if the {@link
     * SummaryInformation} does not contain a char count.</p>
     */
    public int getCharCount()
    {
        return getPropertyIntValue(PropertyIDMap.PID_CHARCOUNT);
    }



    /**
     * <p>Returns the stream's thumbnail (or <code>null</code>)
     * <strong>when this method is implemented. Please note that the
     * return type is likely to change!</strong>
     *
     * <p><strong>FIXME / Hint to developers:</strong> Drew Varner
     * &lt;Drew.Varner -at- sc.edu&gt; said that this is an image in WMF
     * or Clipboard (BMP?) format. He also provided two links that
     * might be helpful: <a
     * href="http://www.csn.ul.ie/~caolan/publink/file/OLE2SummaryAgainst_file-3.27.patch"
     * target="_blank">http://www.csn.ul.ie/~caolan/publink/file/OLE2SummaryAgainst_file-3.27.patch</a>
     * and <a
     * href="http://msdn.microsoft.com/library/en-us/dno97ta/html/msdn_docprop.asp"
     * target="_blank">http://msdn.microsoft.com/library/en-us/dno97ta/html/msdn_docprop.asp</a>.
     * However, we won't do any conversion into any image type but
     * instead just return a byte array.</p>
     */
    public byte[] getThumbnail()
    {
        return (byte[]) getProperty(PropertyIDMap.PID_THUMBNAIL);
    }



    /**
     * <p>Returns the stream's application name (or
     * <code>null</code>).</p>
     */
    public String getApplicationName()
    {
        return (String) getProperty(PropertyIDMap.PID_APPNAME);
    }



    /**
     * <p>Returns one of the following values:</p>
     *
     * <ul>
     *
     * <li><p>0 if the {@link SummaryInformation} does not contain a
     * security field or if there is no security on the document. Use
     * {@link #wasNull} to distinguish between the two cases!</p></li>
     *
     * <li><p>1 if the document is password protected</p></li>
     *
     * <li><p>2 if the document is read-only recommended</p></li>
     *
     * <li><p>4 if the document is read-only enforced</p></li>
     *
     * <li><p>8 if the document is locked for annotations</p></li>
     *
     * </ul>
     */
    public int getSecurity()
    {
        return getPropertyIntValue(PropertyIDMap.PID_SECURITY);
    }

}
