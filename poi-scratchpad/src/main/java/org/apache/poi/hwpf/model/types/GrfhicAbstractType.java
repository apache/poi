/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.apache.poi.hwpf.model.types;


import java.util.Objects;

import org.apache.poi.util.BitField;
import org.apache.poi.util.Internal;

/**
 * The grfhic structure is a set of HTML incompatibility flags that specify the HTML
 * incompatibilities of a list structure. The values specify possible incompatibilities between
 * an LVL or LVLF and HTML lists. The values do not define list properties.
 */
@Internal
public abstract class GrfhicAbstractType
{

    protected byte field_1_grfhic;
    /**/private static final BitField fHtmlChecked = new BitField(0x01);
    /**/private static final BitField fHtmlUnsupported = new BitField(0x02);
    /**/private static final BitField fHtmlListTextNotSharpDot = new BitField(0x04);
    /**/private static final BitField fHtmlNotPeriod = new BitField(0x08);
    /**/private static final BitField fHtmlFirstLineMismatch = new BitField(0x10);
    /**/private static final BitField fHtmlTabLeftIndentMismatch = new BitField(0x20);
    /**/private static final BitField fHtmlHangingIndentBeneathNumber = new BitField(0x40);
    /**/private static final BitField fHtmlBuiltInBullet = new BitField(0x80);

    protected GrfhicAbstractType()
    {
    }

    protected void fillFields( byte[] data, int offset )
    {
        field_1_grfhic                 = data[ 0x0 + offset ];
    }

    public void serialize( byte[] data, int offset )
    {
        data[ 0x0 + offset ] = field_1_grfhic;
    }

    public byte[] serialize()
    {
        final byte[] result = new byte[ getSize() ];
        serialize( result, 0 );
        return result;
    }

    /**
     * Size of record
     */
    public static int getSize()
    {
        return 1;
    }

    @Override
    public boolean equals( Object obj )
    {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        GrfhicAbstractType other = (GrfhicAbstractType) obj;
        return field_1_grfhic == other.field_1_grfhic;
    }

    @Override
    public int hashCode() {
        return Objects.hash(field_1_grfhic);
    }

    public String toString()
    {
        return "[Grfhic]\n" +
            "    .grfhic               = " +
            " ( " + field_1_grfhic + " )\n" +
            "         .fHtmlChecked             = " + isFHtmlChecked() + '\n' +
            "         .fHtmlUnsupported         = " + isFHtmlUnsupported() + '\n' +
            "         .fHtmlListTextNotSharpDot     = " + isFHtmlListTextNotSharpDot() + '\n' +
            "         .fHtmlNotPeriod           = " + isFHtmlNotPeriod() + '\n' +
            "         .fHtmlFirstLineMismatch     = " + isFHtmlFirstLineMismatch() + '\n' +
            "         .fHtmlTabLeftIndentMismatch     = " + isFHtmlTabLeftIndentMismatch() + '\n' +
            "         .fHtmlHangingIndentBeneathNumber     = " + isFHtmlHangingIndentBeneathNumber() + '\n' +
            "         .fHtmlBuiltInBullet       = " + isFHtmlBuiltInBullet() + '\n' +
            "[/Grfhic]";
    }

    /**
     * HTML compatibility flags.
     */
    @Internal
    public byte getGrfhic()
    {
        return field_1_grfhic;
    }

    /**
     * HTML compatibility flags.
     */
    @Internal
    public void setGrfhic( byte field_1_grfhic )
    {
        this.field_1_grfhic = field_1_grfhic;
    }

    /**
     * Sets the fHtmlChecked field value.
     * Checked
     */
    @Internal
    public void setFHtmlChecked( boolean value )
    {
        field_1_grfhic = (byte)fHtmlChecked.setBoolean(field_1_grfhic, value);
    }

    /**
     * Checked
     * @return  the fHtmlChecked field value.
     */
    @Internal
    public boolean isFHtmlChecked()
    {
        return fHtmlChecked.isSet(field_1_grfhic);
    }

    /**
     * Sets the fHtmlUnsupported field value.
     * The numbering sequence or format is unsupported (includes tab &amp; size)
     */
    @Internal
    public void setFHtmlUnsupported( boolean value )
    {
        field_1_grfhic = (byte)fHtmlUnsupported.setBoolean(field_1_grfhic, value);
    }

    /**
     * The numbering sequence or format is unsupported (includes tab &amp; size)
     * @return  the fHtmlUnsupported field value.
     */
    @Internal
    public boolean isFHtmlUnsupported()
    {
        return fHtmlUnsupported.isSet(field_1_grfhic);
    }

    /**
     * Sets the fHtmlListTextNotSharpDot field value.
     * The list text is not "#."
     */
    @Internal
    public void setFHtmlListTextNotSharpDot( boolean value )
    {
        field_1_grfhic = (byte)fHtmlListTextNotSharpDot.setBoolean(field_1_grfhic, value);
    }

    /**
     * The list text is not "#."
     * @return  the fHtmlListTextNotSharpDot field value.
     */
    @Internal
    public boolean isFHtmlListTextNotSharpDot()
    {
        return fHtmlListTextNotSharpDot.isSet(field_1_grfhic);
    }

    /**
     * Sets the fHtmlNotPeriod field value.
     * Something other than a period is used
     */
    @Internal
    public void setFHtmlNotPeriod( boolean value )
    {
        field_1_grfhic = (byte)fHtmlNotPeriod.setBoolean(field_1_grfhic, value);
    }

    /**
     * Something other than a period is used
     * @return  the fHtmlNotPeriod field value.
     */
    @Internal
    public boolean isFHtmlNotPeriod()
    {
        return fHtmlNotPeriod.isSet(field_1_grfhic);
    }

    /**
     * Sets the fHtmlFirstLineMismatch field value.
     * First line indent mismatch
     */
    @Internal
    public void setFHtmlFirstLineMismatch( boolean value )
    {
        field_1_grfhic = (byte)fHtmlFirstLineMismatch.setBoolean(field_1_grfhic, value);
    }

    /**
     * First line indent mismatch
     * @return  the fHtmlFirstLineMismatch field value.
     */
    @Internal
    public boolean isFHtmlFirstLineMismatch()
    {
        return fHtmlFirstLineMismatch.isSet(field_1_grfhic);
    }

    /**
     * Sets the fHtmlTabLeftIndentMismatch field value.
     * The list tab and the dxaLeft don't match (need table?)
     */
    @Internal
    public void setFHtmlTabLeftIndentMismatch( boolean value )
    {
        field_1_grfhic = (byte)fHtmlTabLeftIndentMismatch.setBoolean(field_1_grfhic, value);
    }

    /**
     * The list tab and the dxaLeft don't match (need table?)
     * @return  the fHtmlTabLeftIndentMismatch field value.
     */
    @Internal
    public boolean isFHtmlTabLeftIndentMismatch()
    {
        return fHtmlTabLeftIndentMismatch.isSet(field_1_grfhic);
    }

    /**
     * Sets the fHtmlHangingIndentBeneathNumber field value.
     * The hanging indent falls beneath the number (need plain text)
     */
    @Internal
    public void setFHtmlHangingIndentBeneathNumber( boolean value )
    {
        field_1_grfhic = (byte)fHtmlHangingIndentBeneathNumber.setBoolean(field_1_grfhic, value);
    }

    /**
     * The hanging indent falls beneath the number (need plain text)
     * @return  the fHtmlHangingIndentBeneathNumber field value.
     */
    @Internal
    public boolean isFHtmlHangingIndentBeneathNumber()
    {
        return fHtmlHangingIndentBeneathNumber.isSet(field_1_grfhic);
    }

    /**
     * Sets the fHtmlBuiltInBullet field value.
     * A built-in HTML bullet
     */
    @Internal
    public void setFHtmlBuiltInBullet( boolean value )
    {
        field_1_grfhic = (byte)fHtmlBuiltInBullet.setBoolean(field_1_grfhic, value);
    }

    /**
     * A built-in HTML bullet
     * @return  the fHtmlBuiltInBullet field value.
     */
    @Internal
    public boolean isFHtmlBuiltInBullet()
    {
        return fHtmlBuiltInBullet.isSet(field_1_grfhic);
    }

}  // END OF CLASS
