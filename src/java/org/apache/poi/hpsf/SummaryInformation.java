
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

import java.util.Date;
import org.apache.poi.hpsf.wellknown.PropertyIDMap;

/**
 * <p>Convenience class representing a Summary Information stream in a
 * Microsoft Office document.</p>
 *
 * <p>See <a
 * href="http://msdn.microsoft.com/library/default.asp?url=/library/en-us/com/stgu_8910.asp">http://msdn.microsoft.com/library/default.asp?url=/library/en-us/com/stgu_8910.asp</a>
 * for documentation from That Redmond Company.</p>
 *
 * @author Rainer Klute <a
 * href="mailto:klute@rainer-klute.de">&lt;klute@rainer-klute.de&gt;</a>
 * @see DocumentSummaryInformation
 * @version $Id$
 * @since 2002-02-09
 */
public class SummaryInformation extends SpecialPropertySet
{

    /**
     * <p>The document name a summary information stream usually has
     * in a POIFS filesystem.</p>
     */
    public static final String DEFAULT_STREAM_NAME = "\005SummaryInformation";



    /**
     * <p>Creates a {@link SummaryInformation} from a given {@link
     * PropertySet}.</p>
     *
     * @param ps A property set which should be created from a summary
     * information stream.
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
     *
     * @return The title or <code>null</code>
     */
    public String getTitle()
    {
        return (String) getProperty(PropertyIDMap.PID_TITLE);
    }



    /**
     * <p>Returns the stream's subject (or <code>null</code>).</p>
     *
     * @return The subject or <code>null</code>
     */
    public String getSubject()
    {
        return (String) getProperty(PropertyIDMap.PID_SUBJECT);
    }



    /**
     * <p>Returns the stream's author (or <code>null</code>).</p>
     *
     * @return The author or <code>null</code>
     */
    public String getAuthor()
    {
        return (String) getProperty(PropertyIDMap.PID_AUTHOR);
    }



    /**
     * <p>Returns the stream's keywords (or <code>null</code>).</p>
     *
     * @return The keywords or <code>null</code>
     */
    public String getKeywords()
    {
        return (String) getProperty(PropertyIDMap.PID_KEYWORDS);
    }



    /**
     * <p>Returns the stream's comments (or <code>null</code>).</p>
     *
     * @return The comments or <code>null</code>
     */
    public String getComments()
    {
        return (String) getProperty(PropertyIDMap.PID_COMMENTS);
    }



    /**
     * <p>Returns the stream's template (or <code>null</code>).</p>
     *
     * @return The template or <code>null</code>
     */
    public String getTemplate()
    {
        return (String) getProperty(PropertyIDMap.PID_TEMPLATE);
    }



    /**
     * <p>Returns the stream's last author (or <code>null</code>).</p>
     *
     * @return The last author or <code>null</code>
     */
    public String getLastAuthor()
    {
        return (String) getProperty(PropertyIDMap.PID_LASTAUTHOR);
    }



    /**
     * <p>Returns the stream's revision number (or
     * <code>null</code>). </p>
     *
     * @return The revision number or <code>null</code>
     */
    public String getRevNumber()
    {
        return (String) getProperty(PropertyIDMap.PID_REVNUMBER);
    }



    /**
     * <p>Returns the total time spent in editing the document
     * (or <code>0</code>).</p>
     *
     * @return The total time spent in editing the document or 0 if the {@link
     * SummaryInformation} does not contain this information.
     */
    public long getEditTime()
    {
        final Date d = (Date) getProperty(PropertyIDMap.PID_EDITTIME);
        if (d == null)
            return 0;
        else
            return Util.dateToFileTime(d);
    }



    /**
     * <p>Returns the stream's last printed time (or
     * <code>null</code>).</p>
     *
     * @return The last printed time or <code>null</code>
     */
    public Date getLastPrinted()
    {
        return (Date) getProperty(PropertyIDMap.PID_LASTPRINTED);
    }



    /**
     * <p>Returns the stream's creation time (or
     * <code>null</code>).</p>
     *
     * @return The creation time or <code>null</code>
     */
    public Date getCreateDateTime()
    {
        return (Date) getProperty(PropertyIDMap.PID_CREATE_DTM);
    }



    /**
     * <p>Returns the stream's last save time (or
     * <code>null</code>).</p>
     *
     * @return The last save time or <code>null</code>
     */
    public Date getLastSaveDateTime()
    {
        return (Date) getProperty(PropertyIDMap.PID_LASTSAVE_DTM);
    }



    /**
     * <p>Returns the stream's page count or 0 if the {@link
     * SummaryInformation} does not contain a page count.</p>
     *
     * @return The page count or 0 if the {@link SummaryInformation} does not
     * contain a page count.
     */
    public int getPageCount()
    {
        return getPropertyIntValue(PropertyIDMap.PID_PAGECOUNT);
    }



    /**
     * <p>Returns the stream's word count or 0 if the {@link
     * SummaryInformation} does not contain a word count.</p>
     *
     * @return The word count or <code>null</code>
     */
    public int getWordCount()
    {
        return getPropertyIntValue(PropertyIDMap.PID_WORDCOUNT);
    }



    /**
     * <p>Returns the stream's character count or 0 if the {@link
     * SummaryInformation} does not contain a char count.</p>
     *
     * @return The character count or <code>null</code>
     */
    public int getCharCount()
    {
        return getPropertyIntValue(PropertyIDMap.PID_CHARCOUNT);
    }



    /**
     * <p>Returns the stream's thumbnail (or <code>null</code>)
     * <strong>when this method is implemented. Please note that the
     * return type is likely to change!</strong></p>
     *
     * <p><strong>FIXME (3) / Hint to developers:</strong> Drew Varner
     * &lt;Drew.Varner -at- sc.edu&gt; said that this is an image in
     * WMF or Clipboard (BMP?) format. He also provided two links that
     * might be helpful: <a
     * href="http://www.csn.ul.ie/~caolan/publink/file/OLE2SummaryAgainst_file-3.27.patch"
     * target="_blank">http://www.csn.ul.ie/~caolan/publink/file/OLE2SummaryAgainst_file-3.27.patch</a>
     * and <a
     * href="http://msdn.microsoft.com/library/en-us/dno97ta/html/msdn_docprop.asp"
     * target="_blank">http://msdn.microsoft.com/library/en-us/dno97ta/html/msdn_docprop.asp</a>.
     * However, we won't do any conversion into any image type but instead just
     * return a byte array.</p>
     *
     * @return The thumbnail or <code>null</code>
     */
    public byte[] getThumbnail()
    {
        return (byte[]) getProperty(PropertyIDMap.PID_THUMBNAIL);
    }



    /**
     * <p>Returns the stream's application name (or
     * <code>null</code>).</p>
     *
     * @return The application name or <code>null</code>
     */
    public String getApplicationName()
    {
        return (String) getProperty(PropertyIDMap.PID_APPNAME);
    }



    /**
     * <p>Returns a security code which is one of the following
     * values:</p>
     *
     * <ul>
     *  <li>
     *   <p>0 if the {@link SummaryInformation} does not contain a
     *   security field or if there is no security on the
     *   document. Use {@link #wasNull} to distinguish between the
     *   two cases!</p>
     *  </li>
     *
     *  <li>
     *   <p>1 if the document is password protected</p>
     *  </li>
     *
     *   <li>
     *    <p>2 if the document is read-only recommended</p>
     *   </li>
     *
     *   <li>
     *    <p>4 if the document is read-only enforced</p>
     *   </li>
     *
     *   <li>
     *    <p>8 if the document is locked for annotations</p>
     *   </li>
     *
     * </ul>
     *
     * @return The security code or <code>null</code>
     */
    public int getSecurity()
    {
        return getPropertyIntValue(PropertyIDMap.PID_SECURITY);
    }

}
