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

import org.apache.poi.util.LittleEndian;
/**
 * <p>Class to manipulate data in the Clipboard Variant ({@link
 * Variant#VT_CF VT_CF}) format.</p>
 *
 * @author Drew Varner (Drew.Varner inOrAround sc.edu)
 * @see SummaryInformation#getThumbnail()
 * @version $Id$
 * @since 2002-04-29
 */
public class Thumbnail
{

    /**
     * <p>Offset in bytes where the Clipboard Format Tag starts in the
     * <code>byte[]</code> returned by {@link
     * SummaryInformation#getThumbnail()}</p>
     */
    public static int OFFSET_CFTAG = 4;

    /**
     * <p>Offset in bytes where the Clipboard Format starts in the
     * <code>byte[]</code> returned by {@link
     * SummaryInformation#getThumbnail()}</p>
     *
     * <p>This is only valid if the Clipboard Format Tag is {@link
     * #CFTAG_WINDOWS}</p>
     */
    public static int OFFSET_CF = 8;

    /**
     * <p>Offset in bytes where the Windows Metafile (WMF) image data
     * starts in the <code>byte[]</code> returned by {@link
     * SummaryInformation#getThumbnail()}</p>
     *
     * <p>There is only WMF data at this point in the
     * <code>byte[]</code> if the Clipboard Format Tag is {@link
     * #CFTAG_WINDOWS} and the Clipboard Format is {@link
     * #CF_METAFILEPICT}.</p>
     *
     * <p>Note: The <code>byte[]</code> that starts at
     * <code>OFFSET_WMFDATA</code> and ends at
     * <code>getThumbnail().length - 1</code> forms a complete WMF
     * image. It can be saved to disk with a <code>.wmf</code> file
     * type and read using a WMF-capable image viewer.</p>
     */
    public static int OFFSET_WMFDATA = 20;

    /**
     * <p>Clipboard Format Tag - Windows clipboard format</p>
     *
     * <p>A <code>DWORD</code> indicating a built-in Windows clipboard
     * format value</p>
     *
     * <p>See: <a
     * href="http://msdn.microsoft.com/library/en-us/dnolegen/html/msdn_propset.asp"
     * target="_blank">http://msdn.microsoft.com/library/en-us/dnolegen/html/msdn_propset.asp</a>.</p>
     */
    public static int CFTAG_WINDOWS = -1;

    /**
     * <p>Clipboard Format Tag - Macintosh clipboard format</p>
     *
     * <p>A <code>DWORD</code> indicating a Macintosh clipboard format
     * value</p>
     *
     * <p>See: <a
     * href="http://msdn.microsoft.com/library/en-us/dnolegen/html/msdn_propset.asp"
     * target="_blank">http://msdn.microsoft.com/library/en-us/dnolegen/html/msdn_propset.asp</a>.</p>
     */
    public static int CFTAG_MACINTOSH = -2;

    /**
     * <p>Clipboard Format Tag - Format ID</p>
     *
     * <p>A GUID containing a format identifier (FMTID). This is
     * rarely used.</p>
     *
     * <p>See: <a
     * href="http://msdn.microsoft.com/library/en-us/dnolegen/html/msdn_propset.asp"
     * target="_blank">http://msdn.microsoft.com/library/en-us/dnolegen/html/msdn_propset.asp</a>.</p>
     */
    public static int CFTAG_FMTID = -3;

    /**
     * <p>Clipboard Format Tag - No Data</p>
     *
     * <p>A <code>DWORD</code> indicating No data. This is rarely
     * used.</p>
     *
     * <p>See: <a
     * href="http://msdn.microsoft.com/library/en-us/dnolegen/html/msdn_propset.asp"
     * target="_blank">
     * http://msdn.microsoft.com/library/en-us/dnolegen/html/msdn_propset.asp</a>.</p>
     */
    public static int CFTAG_NODATA = 0;

    /**
     * <p>Clipboard Format - Windows metafile format. This is the
     * recommended way to store thumbnails in Property Streams.</p>
     *
     * <p><strong>Note:</strong> This is not the same format used in
     * regular WMF images. The clipboard version of this format has an
     * extra clipboard-specific header.</p>
     */
    public static int CF_METAFILEPICT = 3;

    /**
     * <p>Clipboard Format - Device Independent Bitmap</p>
     */
    public static int CF_DIB = 8;

    /**
     * <p>Clipboard Format - Enhanced Windows metafile format</p>
     */
    public static int CF_ENHMETAFILE = 14;

    /**
     * <p>Clipboard Format - Bitmap</p>
     *
     * <p>Obsolete, see <a
     * href="msdn.microsoft.com/library/en-us/dnw98bk/html/clipboardoperations.asp
     * target="_blank">msdn.microsoft.com/library/en-us/dnw98bk/html/clipboardoperations.asp</a>.</p>
     */
    public static int CF_BITMAP = 2;

    /**
     * <p>A <code>byte[]</code> to hold a thumbnail image in ({@link
     * Variant#VT_CF VT_CF}) format.</p>
     */
    private byte[] thumbnailData = null;



    /**
     * <p>Default Constructor. If you use it then one you'll have to add
     * the thumbnail <code>byte[]</code> from {@link
     * SummaryInformation#getThumbnail()} to do any useful
     * manipulations, otherwise you'll get a
     * <code>NullPointerException</code>.</p>
     */
    public Thumbnail()
    {
        super();
    }



    /**
     * <p>Creates a <code>Thumbnail</code> instance and initializes
     * with the specified image bytes.</p>
     *
     * @param thumbnailData The thumbnail data
     */
    public Thumbnail(byte[] thumbnailData)
    {
        this.thumbnailData = thumbnailData;
    }



    /**
     * <p>Returns the thumbnail as a <code>byte[]</code> in {@link
     * Variant#VT_CF VT_CF} format.</p>
     *
     * @return The thumbnail value
     * @see SummaryInformation#getThumbnail()
     */
    public byte[] getThumbnail()
    {
        return thumbnailData;
    }



    /**
     * <p>Sets the Thumbnail's underlying <code>byte[]</code> in
     * {@link Variant#VT_CF VT_CF} format.</p>
     *
     * @param thumbnail The new thumbnail value
     * @see SummaryInformation#getThumbnail()
     */
    public void setThumbnail(byte[] thumbnail)
    {
        this.thumbnailData = thumbnail;
    }



    /**
     * <p>Returns an <code>int</code> representing the Clipboard
     * Format Tag</p>
     *
     * <p>Possible return values are:</p>
     * <ul>
     *  <li>{@link #CFTAG_WINDOWS CFTAG_WINDOWS}</li>
     *  <li>{@link #CFTAG_MACINTOSH CFTAG_MACINTOSH}</li>
     *  <li>{@link #CFTAG_FMTID CFTAG_FMTID}</li>
     *  <li>{@link #CFTAG_NODATA CFTAG_NODATA}</li>
     * </ul>
     *
     * @return A flag indicating the Clipboard Format Tag
     */
    public long getClipboardFormatTag()
    {
        long clipboardFormatTag = LittleEndian.getUInt(getThumbnail(),
						       OFFSET_CFTAG);
        return clipboardFormatTag;
    }



    /**
     * <p>Returns an <code>int</code> representing the Clipboard
     * Format</p>
     *
     * <p>Will throw an exception if the Thumbnail's Clipboard Format
     * Tag is not {@link Thumbnail#CFTAG_WINDOWS CFTAG_WINDOWS}.</p>
     *
     * <p>Possible return values are:</p>
     *
     * <ul>
     *  <li>{@link #CF_METAFILEPICT CF_METAFILEPICT}</li>
     *  <li>{@link #CF_DIB CF_DIB}</li>
     *  <li>{@link #CF_ENHMETAFILE CF_ENHMETAFILE}</li>
     *  <li>{@link #CF_BITMAP CF_BITMAP}</li>
     * </ul>
     *
     * @return a flag indicating the Clipboard Format
     * @throws HPSFException if the Thumbnail isn't CFTAG_WINDOWS
     */
    public long getClipboardFormat() throws HPSFException
    {
        if (!(getClipboardFormatTag() == CFTAG_WINDOWS))
            throw new HPSFException("Clipboard Format Tag of Thumbnail must " +
				    "be CFTAG_WINDOWS.");

        return LittleEndian.getUInt(getThumbnail(), OFFSET_CF);
    }



    /**
     * <p>Returns the Thumbnail as a <code>byte[]</code> of WMF data
     * if the Thumbnail's Clipboard Format Tag is {@link
     * #CFTAG_WINDOWS CFTAG_WINDOWS} and its Clipboard Format is
     * {@link #CF_METAFILEPICT CF_METAFILEPICT}</p> <p>This
     * <code>byte[]</code> is in the traditional WMF file, not the
     * clipboard-specific version with special headers.</p>
     *
     * <p>See <a href="http://www.wvware.com/caolan/ora-wmf.html"
     * target="_blank">http://www.wvware.com/caolan/ora-wmf.html</a>
     * for more information on the WMF image format.</p>
     *
     * @return A WMF image of the Thumbnail
     * @throws HPSFException if the Thumbnail isn't CFTAG_WINDOWS and
     * CF_METAFILEPICT
     */
    public byte[] getThumbnailAsWMF() throws HPSFException
    {
        if (!(getClipboardFormatTag() == CFTAG_WINDOWS))
            throw new HPSFException("Clipboard Format Tag of Thumbnail must " +
				    "be CFTAG_WINDOWS.");
        if (!(getClipboardFormat() == CF_METAFILEPICT))
            throw new HPSFException("Clipboard Format of Thumbnail must " +
				    "be CF_METAFILEPICT.");
        else
	{
            byte[] thumbnail = getThumbnail();
            int wmfImageLength = thumbnail.length - OFFSET_WMFDATA;
            byte[] wmfImage = new byte[wmfImageLength];
            System.arraycopy(thumbnail,
			     OFFSET_WMFDATA,
			     wmfImage,
			     0,
			     wmfImageLength);
            return wmfImage;
        }
    }

}
