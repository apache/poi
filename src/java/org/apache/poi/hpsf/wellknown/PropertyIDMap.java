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
package org.apache.poi.hpsf.wellknown;

import java.util.*;

/**
 * <p>This is a dictionary which maps property ID values to property
 * ID strings.</p>
 *
 * <p>The methods {@link #getSummaryInformationProperties} and {@link
 * #getDocumentSummaryInformationProperties} return singleton {@link
 * PropertyIDMap}s. An application that wants to extend these maps
 * should treat them as unmodifiable, copy them and modifiy the
 * copies.</p>
 *
 * <p><strong>FIXME:</strong> Make the singletons
 * unmodifiable. However, since this requires to use a {@link HashMap}
 * delegate instead of extending {@link HashMap} and thus requires a
 * lot of stupid typing. I won't do that for the time being.</p>
 *
 * @author Rainer Klute (klute@rainer-klute.de)
 * @version $Id$
 * @since 2002-02-09
 */
public class PropertyIDMap extends HashMap
{

    /*
     * The following definitions are for property IDs in the first
     * (and only) section of the Summary Information property set.
     */
    public final static int PID_TITLE = 2;
    public final static int PID_SUBJECT = 3;
    public final static int PID_AUTHOR = 4;
    public final static int PID_KEYWORDS = 5;
    public final static int PID_COMMENTS = 6;
    public final static int PID_TEMPLATE = 7;
    public final static int PID_LASTAUTHOR = 8;
    public final static int PID_REVNUMBER = 9;
    public final static int PID_EDITTIME = 10;
    public final static int PID_LASTPRINTED = 11;
    public final static int PID_CREATE_DTM = 12;
    public final static int PID_LASTSAVE_DTM = 13;
    public final static int PID_PAGECOUNT = 14;
    public final static int PID_WORDCOUNT = 15;
    public final static int PID_CHARCOUNT = 16;
    public final static int PID_THUMBNAIL = 17;
    public final static int PID_APPNAME = 18;
    public final static int PID_SECURITY = 19;



    /*
     * The following definitions are for property IDs in the first
     * section of the Document Summary Information property set.
     */

    /** 
     * <p>The entry is a dictionary.</p>
     */
    public final static int PID_DICTIONARY = 0;

    /**
     * <p>The entry denotes a code page.</p>
     */
    public final static int PID_CODEPAGE = 1;

    /** 
     * <p>The entry is a string denoting the category the file belongs
     * to, e.g. review, memo, etc. This is useful to find documents of
     * same type.</p>
     */
    public final static int PID_CATEGORY = 2;

    /** 
     * <p>Target format for power point presentation, e.g. 35mm,
     * printer, video etc.</p>
     */
    public final static int PID_PRESFORMAT = 3;

    /** 
     * <p>Number of bytes.</p>
     */
    public final static int PID_BYTECOUNT = 4;

    /** 
     * <p>Number of lines.</p>
     */
    public final static int PID_LINECOUNT = 5;

    /** 
     * <p>Number of paragraphs.</p>
     */
    public final static int PID_PARCOUNT = 6;

    /** 
     * <p>Number of slides in a power point presentation.</p>
     */
    public final static int PID_SLIDECOUNT = 7;

    /** 
     * <p>Number of slides with notes.</p>
     */
    public final static int PID_NOTECOUNT = 8;

    /** 
     * <p>Number of hidden slides.</p>
     */
    public final static int PID_HIDDENCOUNT = 9;

    /** 
     * <p>Number of multimedia clips, e.g. sound or video.</p>
     */
    public final static int PID_MMCLIPCOUNT = 10;

    /** 
     * <p>This entry is set to -1 when scaling of the thumbnail is
     * desired. Otherwise the thumbnail should be cropped.</p>
     */
    public final static int PID_SCALE = 11;

    /** 
     * <p>This entry denotes an internally used property. It is a
     * vector of variants consisting of pairs of a string (VT_LPSTR)
     * and a number (VT_I4). The string is a heading name, and the
     * number tells how many document parts are under that
     * heading.</p>
     */
    public final static int PID_HEADINGPAIR = 12;

    /** 
     * <p>This entry contains the names of document parts (word: names
     * of the documents in the master document, excel: sheet names,
     * power point: slide titles, binder: document names).</p>
     */
    public final static int PID_DOCPARTS = 13;

    /** 
     * <p>This entry contains the name of the project manager.</p>
     */
    public final static int PID_MANAGER = 14;

    /** 
     * <p>This entry contains the company name.</p>
     */
    public final static int PID_COMPANY = 15;

    /** 
     * <p>If this entry is -1 the links are dirty and should be
     * re-evaluated.</p>
     */
    public final static int PID_LINKSDIRTY = 16;



    /**
     * <p>Contains the summary information property ID values and
     * associated strings. See the overall HPSF documentation for
     * details!</p>
     */
    private static PropertyIDMap summaryInformationProperties;

    /**
     * <p>Contains the summary information property ID values and
     * associated strings. See the overall HPSF documentation for
     * details!</p>
     */
    private static PropertyIDMap documentSummaryInformationProperties;



    /**
     * <p>Creates a {@link PropertyIDMap}.</p>
     */
    public PropertyIDMap(int initialCapacity, float loadFactor)
    {
        super(initialCapacity, loadFactor);
    }



    /**
     * <p>Puts a ID string for an ID into the {@link
     * PropertyIDMap}.</p>
     *
     * @param id The ID.
     * @param idString The ID string.
     * @return As specified by the {@link Map} interface, this method
     * returns the previous value associated with the specified
     * <var>id</var>, or <code>null</code> if there was no mapping for
     * key.
     */
    public Object put(int id, String idString)
    {
        return put(new Integer(id), idString);
    }



    /**
     * <p>Gets the ID string for an ID from the {@link
     * PropertyIDMap}.</p>
     *
     * @param id The ID.
     * @return The ID string associated with <var>id</var>.
     */
    public Object get(int id)
    {
        return get(new Integer(id));
    }



    /**
     * <p>Returns the Summary Information properties singleton.</p>
     */
    public static PropertyIDMap getSummaryInformationProperties()
    {
        if (summaryInformationProperties == null)
	{
            PropertyIDMap m = new PropertyIDMap(18, (float) 1.0);
            m.put(PID_TITLE, "PID_TITLE");
            m.put(PID_SUBJECT, "PID_SUBJECT");
            m.put(PID_AUTHOR, "PID_AUTHOR");
            m.put(PID_KEYWORDS, "PID_KEYWORDS");
            m.put(PID_COMMENTS, "PID_COMMENTS");
            m.put(PID_TEMPLATE, "PID_TEMPLATE");
            m.put(PID_LASTAUTHOR, "PID_LASTAUTHOR");
            m.put(PID_REVNUMBER, "PID_REVNUMBER");
            m.put(PID_EDITTIME, "PID_EDITTIME");
            m.put(PID_LASTPRINTED, "PID_LASTPRINTED");
            m.put(PID_CREATE_DTM, "PID_CREATE_DTM");
            m.put(PID_LASTSAVE_DTM, "PID_LASTSAVE_DTM");
            m.put(PID_PAGECOUNT, "PID_PAGECOUNT");
            m.put(PID_WORDCOUNT, "PID_WORDCOUNT");
            m.put(PID_CHARCOUNT, "PID_CHARCOUNT");
            m.put(PID_THUMBNAIL, "PID_THUMBNAIL");
            m.put(PID_APPNAME, "PID_APPNAME");
            m.put(PID_SECURITY, "PID_SECURITY");
            summaryInformationProperties = m;
        }
        return summaryInformationProperties;
    }



    /**
     * <p>Returns the Document Summary Information properties
     * singleton.</p>
     *
     * @return The Document Summary Information properties singleton.
     */
    public static PropertyIDMap getDocumentSummaryInformationProperties()
    {
        if (documentSummaryInformationProperties == null)
	{
            PropertyIDMap m = new PropertyIDMap(17, (float) 1.0);
            m.put(PID_DICTIONARY, "PID_DICTIONARY");
            m.put(PID_CODEPAGE, "PID_CODEPAGE");
            m.put(PID_CATEGORY, "PID_CATEGORY");
            m.put(PID_PRESFORMAT, "PID_PRESFORMAT");
            m.put(PID_BYTECOUNT, "PID_BYTECOUNT");
            m.put(PID_LINECOUNT, "PID_LINECOUNT");
            m.put(PID_PARCOUNT, "PID_PARCOUNT");
            m.put(PID_SLIDECOUNT, "PID_SLIDECOUNT");
            m.put(PID_NOTECOUNT, "PID_NOTECOUNT");
            m.put(PID_HIDDENCOUNT, "PID_HIDDENCOUNT");
            m.put(PID_MMCLIPCOUNT, "PID_MMCLIPCOUNT");
            m.put(PID_SCALE, "PID_SCALE");
            m.put(PID_HEADINGPAIR, "PID_HEADINGPAIR");
            m.put(PID_DOCPARTS, "PID_DOCPARTS");
            m.put(PID_MANAGER, "PID_MANAGER");
            m.put(PID_COMPANY, "PID_COMPANY");
            m.put(PID_LINKSDIRTY, "PID_LINKSDIRTY");
            documentSummaryInformationProperties = m;
        }
        return documentSummaryInformationProperties;
    }



    /**
     * <p>For the most basic testing.</p>
     */
    public static void main(String args[])
    {
        PropertyIDMap s1 = getSummaryInformationProperties();
        PropertyIDMap s2 = getDocumentSummaryInformationProperties();
        System.out.println("s1: " + s1);
        System.out.println("s2: " + s2);
    }

}
