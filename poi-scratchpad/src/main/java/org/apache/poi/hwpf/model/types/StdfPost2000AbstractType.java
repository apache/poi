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

import org.apache.poi.util.BitField;
import org.apache.poi.util.Internal;
import org.apache.poi.util.LittleEndian;

/**
 * The StdfPost2000 structure specifies general information about a style.
 */
@Internal
public abstract class StdfPost2000AbstractType
{

    protected short field_1_info1;
    /**/private static final BitField istdLink = new BitField(0x0FFF);
    /**/private static final BitField fHasOriginalStyle = new BitField(0x1000);
    /**/private static final BitField fSpare = new BitField(0xE000);
    protected long field_2_rsid;
    protected short field_3_info3;
    /**/private static final BitField iftcHtml = new BitField(0x0007);
    /**/private static final BitField unused = new BitField(0x0008);
    /**/private static final BitField iPriority = new BitField(0xFFF0);

    protected StdfPost2000AbstractType()
    {
    }

    protected void fillFields( byte[] data, int offset )
    {
        field_1_info1                  = LittleEndian.getShort(data, 0x0 + offset);
        field_2_rsid                   = LittleEndian.getUInt(data, 0x2 + offset);
        field_3_info3                  = LittleEndian.getShort(data, 0x6 + offset);
    }

    public void serialize( byte[] data, int offset )
    {
        LittleEndian.putShort(data, 0x0 + offset, field_1_info1);
        LittleEndian.putUInt(data, 0x2 + offset, field_2_rsid);
        LittleEndian.putShort(data, 0x6 + offset, field_3_info3);
    }

    /**
     * Size of record
     */
    public static int getSize()
    {
        return 0 + 2 + 4 + 2;
    }

    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("[StdfPost2000]\n");
        builder.append("    .info1                = ");
        builder.append(" (").append(getInfo1()).append(" )\n");
        builder.append("         .istdLink                 = ").append(getIstdLink()).append('\n');
        builder.append("         .fHasOriginalStyle        = ").append(isFHasOriginalStyle()).append('\n');
        builder.append("         .fSpare                   = ").append(getFSpare()).append('\n');
        builder.append("    .rsid                 = ");
        builder.append(" (").append(getRsid()).append(" )\n");
        builder.append("    .info3                = ");
        builder.append(" (").append(getInfo3()).append(" )\n");
        builder.append("         .iftcHtml                 = ").append(getIftcHtml()).append('\n');
        builder.append("         .unused                   = ").append(isUnused()).append('\n');
        builder.append("         .iPriority                = ").append(getIPriority()).append('\n');

        builder.append("[/StdfPost2000]\n");
        return builder.toString();
    }

    /**
     * Get the info1 field for the StdfPost2000 record.
     */
    @Internal
    public short getInfo1()
    {
        return field_1_info1;
    }

    /**
     * Set the info1 field for the StdfPost2000 record.
     */
    @Internal
    public void setInfo1( short field_1_info1 )
    {
        this.field_1_info1 = field_1_info1;
    }

    /**
     *  An unsigned integer that specifies the revision save identifier of the session when this style definition was last modified.
     */
    @Internal
    public long getRsid()
    {
        return field_2_rsid;
    }

    /**
     *  An unsigned integer that specifies the revision save identifier of the session when this style definition was last modified.
     */
    @Internal
    public void setRsid( long field_2_rsid )
    {
        this.field_2_rsid = field_2_rsid;
    }

    /**
     * Get the info3 field for the StdfPost2000 record.
     */
    @Internal
    public short getInfo3()
    {
        return field_3_info3;
    }

    /**
     * Set the info3 field for the StdfPost2000 record.
     */
    @Internal
    public void setInfo3( short field_3_info3 )
    {
        this.field_3_info3 = field_3_info3;
    }

    /**
     * Sets the istdLink field value.
     * An unsigned integer that specifies the istd of the style that is linked to this one, or 0x0000 if this style is not linked to any other style in the document.
     */
    @Internal
    public void setIstdLink( short value )
    {
        field_1_info1 = (short)istdLink.setValue(field_1_info1, value);
    }

    /**
     * An unsigned integer that specifies the istd of the style that is linked to this one, or 0x0000 if this style is not linked to any other style in the document.
     * @return  the istdLink field value.
     */
    @Internal
    public short getIstdLink()
    {
        return ( short )istdLink.getValue(field_1_info1);
    }

    /**
     * Sets the fHasOriginalStyle field value.
     * Specifies whether the style is revision-marked. A revision-marked style stores the pre-revision formatting in addition to the current formatting. If this bit is set to 1, the cupx member of StdfBase MUST include the formatting sets that specify that pre-revision formatting
     */
    @Internal
    public void setFHasOriginalStyle( boolean value )
    {
        field_1_info1 = (short)fHasOriginalStyle.setBoolean(field_1_info1, value);
    }

    /**
     * Specifies whether the style is revision-marked. A revision-marked style stores the pre-revision formatting in addition to the current formatting. If this bit is set to 1, the cupx member of StdfBase MUST include the formatting sets that specify that pre-revision formatting
     * @return  the fHasOriginalStyle field value.
     */
    @Internal
    public boolean isFHasOriginalStyle()
    {
        return fHasOriginalStyle.isSet(field_1_info1);
    }

    /**
     * Sets the fSpare field value.
     * Specifies whether the paragraph height information in the fcPlcfPhe field of FibRgFcLcb97
     */
    @Internal
    public void setFSpare( byte value )
    {
        field_1_info1 = (short)fSpare.setValue(field_1_info1, value);
    }

    /**
     * Specifies whether the paragraph height information in the fcPlcfPhe field of FibRgFcLcb97
     * @return  the fSpare field value.
     */
    @Internal
    public byte getFSpare()
    {
        return ( byte )fSpare.getValue(field_1_info1);
    }

    /**
     * Sets the iftcHtml field value.
     * This field is undefined and MUST be ignored
     */
    @Internal
    public void setIftcHtml( byte value )
    {
        field_3_info3 = (short)iftcHtml.setValue(field_3_info3, value);
    }

    /**
     * This field is undefined and MUST be ignored
     * @return  the iftcHtml field value.
     */
    @Internal
    public byte getIftcHtml()
    {
        return ( byte )iftcHtml.getValue(field_3_info3);
    }

    /**
     * Sets the unused field value.
     * This value MUST be zero and MUST be ignored
     */
    @Internal
    public void setUnused( boolean value )
    {
        field_3_info3 = (short)unused.setBoolean(field_3_info3, value);
    }

    /**
     * This value MUST be zero and MUST be ignored
     * @return  the unused field value.
     */
    @Internal
    public boolean isUnused()
    {
        return unused.isSet(field_3_info3);
    }

    /**
     * Sets the iPriority field value.
     * An unsigned integer that specifies the priority value that is assigned to this style and that is used when ordering the styles by priority in the user interface
     */
    @Internal
    public void setIPriority( short value )
    {
        field_3_info3 = (short)iPriority.setValue(field_3_info3, value);
    }

    /**
     * An unsigned integer that specifies the priority value that is assigned to this style and that is used when ordering the styles by priority in the user interface
     * @return  the iPriority field value.
     */
    @Internal
    public short getIPriority()
    {
        return ( short )iPriority.getValue(field_3_info3);
    }

}  // END OF CLASS
