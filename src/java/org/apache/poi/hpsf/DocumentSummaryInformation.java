
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.poi.hpsf.wellknown.PropertyIDMap;

/**
 * <p>Convenience class representing a DocumentSummary Information stream in a
 * Microsoft Office document.</p>
 *
 * @author Rainer Klute <a
 * href="mailto:klute@rainer-klute.de">&lt;klute@rainer-klute.de&gt;</a>
 * @author Drew Varner (Drew.Varner closeTo sc.edu)
 * @author robert_flaherty@hyperion.com
 * @see SummaryInformation
 * @version $Id$
 * @since 2002-02-09
 */
public class DocumentSummaryInformation extends SpecialPropertySet
{

    /**
     * <p>The document name a document summary information stream
     * usually has in a POIFS filesystem.</p>
     */
    public static final String DEFAULT_STREAM_NAME =
        "\005DocumentSummaryInformation";



    /**
     * <p>Creates a {@link DocumentSummaryInformation} from a given
     * {@link PropertySet}.</p>
     *
     * @param ps A property set which should be created from a
     * document summary information stream.
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
     *
     * @return The category value
     */
    public String getCategory()
    {
        return (String) getProperty(PropertyIDMap.PID_CATEGORY);
    }



    /**
     * <p>Returns the stream's presentation format (or
     * <code>null</code>).</p>
     *
     * @return The presentationFormat value
     */
    public String getPresentationFormat()
    {
        return (String) getProperty(PropertyIDMap.PID_PRESFORMAT);
    }



    /**
     * <p>Returns the stream's byte count or 0 if the {@link
     * DocumentSummaryInformation} does not contain a byte count.</p>
     *
     * @return The byteCount value
     */
    public int getByteCount()
    {
        return getPropertyIntValue(PropertyIDMap.PID_BYTECOUNT);
    }



    /**
     * <p>Returns the stream's line count or 0 if the {@link
     * DocumentSummaryInformation} does not contain a line count.</p>
     *
     * @return The lineCount value
     */
    public int getLineCount()
    {
        return getPropertyIntValue(PropertyIDMap.PID_LINECOUNT);
    }



    /**
     * <p>Returns the stream's par count or 0 if the {@link
     * DocumentSummaryInformation} does not contain a par count.</p>
     *
     * @return The parCount value
     */
    public int getParCount()
    {
        return getPropertyIntValue(PropertyIDMap.PID_PARCOUNT);
    }



    /**
     * <p>Returns the stream's slide count or 0 if the {@link
     * DocumentSummaryInformation} does not contain a slide count.</p>
     *
     * @return The slideCount value
     */
    public int getSlideCount()
    {
        return getPropertyIntValue(PropertyIDMap.PID_SLIDECOUNT);
    }



    /**
     * <p>Returns the stream's note count or 0 if the {@link
     * DocumentSummaryInformation} does not contain a note count.</p>
     *
     * @return The noteCount value
     */
    public int getNoteCount()
    {
        return getPropertyIntValue(PropertyIDMap.PID_NOTECOUNT);
    }



    /**
     * <p>Returns the stream's hidden count or 0 if the {@link
     * DocumentSummaryInformation} does not contain a hidden
     * count.</p>
     *
     * @return The hiddenCount value
     */
    public int getHiddenCount()
    {
        return getPropertyIntValue(PropertyIDMap.PID_HIDDENCOUNT);
    }



    /**
     * <p>Returns the stream's mmclip count or 0 if the {@link
     * DocumentSummaryInformation} does not contain a mmclip
     * count.</p>
     *
     * @return The mMClipCount value
     */
    public int getMMClipCount()
    {
        return getPropertyIntValue(PropertyIDMap.PID_MMCLIPCOUNT);
    }



    /**
     * <p>Returns <code>true</code> when scaling of the thumbnail is
     * desired, <code>false</code> if cropping is desired.</p>
     *
     * @return The scale value
     */
    public boolean getScale()
    {
        return getPropertyBooleanValue(PropertyIDMap.PID_SCALE);
    }



    /**
     * <p>Returns the stream's heading pair (or <code>null</code>)
     * <strong>when this method is implemented. Please note that the
     * return type is likely to change!</strong>
     *
     * @return The headingPair value
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
     *
     * @return The docparts value
     */
    public byte[] getDocparts()
    {
        if (true)
            throw new UnsupportedOperationException("FIXME");
        return (byte[]) getProperty(PropertyIDMap.PID_DOCPARTS);
    }



    /**
     * <p>Returns the stream's manager (or <code>null</code>).</p>
     *
     * @return The manager value
     */
    public String getManager()
    {
        return (String) getProperty(PropertyIDMap.PID_MANAGER);
    }



    /**
     * <p>Returns the stream's company (or <code>null</code>).</p>
     *
     * @return The company value
     */
    public String getCompany()
    {
        return (String) getProperty(PropertyIDMap.PID_COMPANY);
    }



    /**
     * <p>Returns <code>true</code> if the custom links are hampered
     * by excessive noise, for all applications.</p> <p>
     *
     * <strong>FIXME (3):</strong> Explain this some more! I (Rainer)
     * don't understand it.</p>
     *
     * @return The linksDirty value
     */
    public boolean getLinksDirty()
    {
        return getPropertyBooleanValue(PropertyIDMap.PID_LINKSDIRTY);
    }



    /**
     * <p>Gets the custom properties as a map from the property name to
     * value.</p>
     * 
     * @return The custom properties if any exist, <code>null</code> otherwise.
     * @since 2003-10-22
     */
    public Map getCustomProperties()
    {
        Map nameToValue = null;
        if (getSectionCount() >= 2)
        {
            final Section section = (Section) getSections().get(1);
            final Map pidToName = 
                      (Map) section.getProperty(PropertyIDMap.PID_DICTIONARY);
            if (pidToName != null)
            {
                nameToValue = new HashMap(pidToName.size());
                for (Iterator i = pidToName.entrySet().iterator(); i.hasNext();)
                {
                    final Map.Entry e = (Map.Entry) i.next();
                    final long pid = ((Number) e.getKey()).longValue();
                    nameToValue.put(e.getValue(), section.getProperty(pid));
                }
            }
        }
        return nameToValue;
    }

}
