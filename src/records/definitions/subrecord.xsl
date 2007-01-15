<?xml version="1.0"?>
<!--
   ====================================================================
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
   ====================================================================
-->
<xsl:stylesheet version="1.0"
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
   xmlns:recutil="org.apache.poi.generator.RecordUtil"
   xmlns:field="org.apache.poi.generator.FieldIterator"
   xmlns:java="java" >

<xsl:template match="record">

<xsl:if test="@package">
package <xsl:value-of select="@package"/>;
</xsl:if>


import org.apache.poi.util.*;

/**
 * <xsl:value-of select="/record/description"/>
 * NOTE: This source is automatically generated please do not modify this file.  Either subclass or
 *       remove the record in src/records/definitions.
<xsl:apply-templates select="author"/>
 */
public class <xsl:value-of select="@name"/>SubRecord
    extends SubRecord
{
    public final static short      sid                             = <xsl:value-of select="@id"/>;
<xsl:for-each select="//fields/field">    private  <xsl:value-of select="recutil:getType(@size,@type,10)"/><xsl:text> </xsl:text><xsl:value-of select="recutil:getFieldName(position(),@name,0)"/><xsl:value-of select="recutil:initializeText(@size,@type)"/>;
<xsl:apply-templates select="./bit|./const|./bit/const"/>
</xsl:for-each>

    public <xsl:value-of select="@name"/>SubRecord()
    {
<xsl:for-each select="//fields/field"><xsl:if test="@default">
<xsl:text>        </xsl:text>
<xsl:value-of select="recutil:getFieldName(position(),@name,0)"/> = <xsl:value-of select="@default"/>;
</xsl:if></xsl:for-each>
    }

    /**
     * Constructs a <xsl:value-of select="@name"/> record and sets its fields appropriately.
     *
     * @param id    id must be <xsl:value-of select="@id"/> or an exception
     *              will be throw upon validation
     * @param size  size the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     */

    public <xsl:value-of select="@name"/>SubRecord(short id, short size, byte [] data)
    {
        super(id, size, data);
    <xsl:for-each select="//fields/field">
    <xsl:if test="@default">
        <xsl:text>        </xsl:text>
        <xsl:value-of select="recutil:getFieldName(position(),@name,0)"/> =
        <xsl:value-of select="@default"/>;

    </xsl:if>
    </xsl:for-each>
    }

    /**
     * Constructs a <xsl:value-of select="@name"/> record and sets its fields appropriately.
     *
     * @param id    id must be <xsl:value-of select="@id"/> or an exception
     *              will be throw upon validation
     * @param size  size the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     * @param offset of the record's data
     */

    public <xsl:value-of select="@name"/>SubRecord(short id, short size, byte [] data, int offset)
    {
        super(id, size, data, offset);
    <xsl:for-each select="//fields/field">
    <xsl:if test="@default">
        <xsl:text>        </xsl:text>
        <xsl:value-of select="recutil:getFieldName(position(),@name,0)"/> =
        <xsl:value-of select="@default"/>;

    </xsl:if>
    </xsl:for-each>
    }

    /**
     * Checks the sid matches the expected side for this record
     *
     * @param id   the expected sid.
     */
    protected void validateSid(short id)
    {
        if (id != sid)
        {
            throw new RecordFormatException(&quot;Not a <xsl:value-of select="@name"/> record&quot;);
        }
    }

    protected void fillFields(byte [] data, short size, int offset)
    {

<xsl:text>        int pos = 0;
</xsl:text>

 <xsl:variable name="fieldIterator" select="field:new()"/>
<xsl:for-each select="//fields/field">
    <xsl:text>        </xsl:text><xsl:value-of select="field:fillDecoder2($fieldIterator,position(),@name,@size,@type)"/>;
</xsl:for-each>
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[<xsl:value-of select="recutil:getRecordId(@name,@excel-record-id)"/>]\n");
<xsl:apply-templates select="//field" mode="tostring"/>
        buffer.append("[/<xsl:value-of select="recutil:getRecordId(@name,@excel-record-id)"/>]\n");
        return buffer.toString();
    }

    public int serialize(int offset, byte[] data)
    {
        int pos = 0;

        LittleEndian.putShort(data, 0 + offset, sid);
        LittleEndian.putShort(data, 2 + offset, (short)(getRecordSize() - 4));
<xsl:variable name="fieldIterator" select="field:new()"/>
<xsl:for-each select="//fields/field"><xsl:text>
        </xsl:text><xsl:value-of select="field:serialiseEncoder($fieldIterator,position(),@name,@size,@type)"/>
</xsl:for-each>

        return getRecordSize();
    }

    /**
     * Size of record (exluding 4 byte header)
     */
    public int getRecordSize()
    {
<xsl:variable name="fieldIterator" select="field:new()"/>
<xsl:text>        return 4 </xsl:text>
<xsl:for-each select="//fields/field">
    <xsl:value-of select="field:calcSize($fieldIterator,position(),@name,@size,@type)"/>
</xsl:for-each>;
    }

    public short getSid()
    {
        return this.sid;
    }

    public Object clone() {
        <xsl:value-of select="@name"/>SubRecord rec = new <xsl:value-of select="@name"/>SubRecord();
    <xsl:for-each select="//fields/field">
        <xsl:text>
        </xsl:text><xsl:value-of select="recutil:clone(@name,@type,position())"/><xsl:text>;</xsl:text>
    </xsl:for-each>
        return rec;
    }

<xsl:apply-templates select="//field" mode="getset"/>
<xsl:apply-templates select="//field" mode="bits"/>

}  // END OF CLASS


</xsl:template>

<xsl:template match = "field" mode="bits">
<xsl:variable name="fieldNum" select="position()"/>
<xsl:for-each select="bit">
<xsl:if test="not (@mask)">
    /**
     * Sets the <xsl:value-of select="@name"/> field value.
     * <xsl:value-of select="@description"/>
     */
    public void set<xsl:value-of select="recutil:getFieldName1stCap(@name,0)"/>(boolean value)
    {
        <xsl:value-of select="recutil:getFieldName($fieldNum,../@name,0)"/> = <xsl:value-of select="recutil:getFieldName(@name,0)"/>.set<xsl:value-of select="recutil:getType1stCap(../@size,../@type,0)"/>Boolean(<xsl:value-of select="recutil:getFieldName($fieldNum,../@name,0)"/>, value);
    }

    /**
     * <xsl:value-of select="@description"/>
     * @return  the <xsl:value-of select="@name"/> field value.
     */
    public boolean is<xsl:value-of select="recutil:getFieldName1stCap(@name,0)"/>()
    {
        return <xsl:value-of select="recutil:getFieldName(@name,0)"/>.isSet(<xsl:value-of select="recutil:getFieldName($fieldNum,../@name,0)"/>);
    }
</xsl:if>
<xsl:if test="@mask">
    /**
     * Sets the <xsl:value-of select="@name"/> field value.
     * <xsl:value-of select="@description"/>
     */
    public void set<xsl:value-of select="recutil:getFieldName1stCap(@name,0)"/>(short value)
    {
        <xsl:value-of select="recutil:getFieldName($fieldNum,../@name,0)"/> = <xsl:value-of select="recutil:getFieldName(@name,0)"/>.set<xsl:value-of select="recutil:getType1stCap(../@size,../@type,0)"/>Value(<xsl:value-of select="recutil:getFieldName($fieldNum,../@name,0)"/>, value);
    }

    /**
     * <xsl:value-of select="@description"/>
     * @return  the <xsl:value-of select="@name"/> field value.
     */
    public short get<xsl:value-of select="recutil:getFieldName1stCap(@name,0)"/>()
    {
        return <xsl:value-of select="recutil:getFieldName(@name,0)"/>.getShortValue(<xsl:value-of select="recutil:getFieldName($fieldNum,../@name,0)"/>);
    }
</xsl:if>
</xsl:for-each>
</xsl:template>

<xsl:template match = "bit" ><xsl:if test="not (@mask)">    private  BitField   <xsl:value-of select="recutil:getFieldName(@name,42)"/>  = new BitField(<xsl:value-of select="recutil:getMask(@number)"/>);
</xsl:if><xsl:if test="@mask">    private BitField   <xsl:value-of select="recutil:getFieldName(@name,42)"/> = new BitField(<xsl:value-of select="@mask"/>);
</xsl:if>
</xsl:template>
<xsl:template match = "const">    public final static <xsl:value-of select="recutil:getType(../@size,../@type,10)"/><xsl:text>  </xsl:text><xsl:value-of select="recutil:getConstName(../@name,@name,30)"/> = <xsl:value-of select="@value"/>;
</xsl:template>

<xsl:template match = "const" mode="listconsts">
<xsl:text>
     *        </xsl:text>
<xsl:value-of select="recutil:getConstName(../@name,@name,0)"/></xsl:template>
<xsl:template match="field" mode="getset">
    /**
     * Get the <xsl:value-of select="@name"/> field for the <xsl:value-of select="../../@name"/> record.<xsl:if test="./const">
     *
     * @return  One of <xsl:apply-templates select="./const" mode="listconsts"/></xsl:if>
     */
    public <xsl:value-of select="recutil:getType(@size,@type,0)"/> get<xsl:value-of select="recutil:getFieldName1stCap(@name,0)"/>()
    {
        return <xsl:value-of select="recutil:getFieldName(position(),@name,0)"/>;
    }

    /**
     * Set the <xsl:value-of select="@name"/> field for the <xsl:value-of select="../../@name"/> record.<xsl:if test="./const">
     *
     * @param <xsl:value-of select="recutil:getFieldName(position(),@name,0)"/>
     *        One of <xsl:apply-templates select="./const" mode="listconsts"/></xsl:if>
     */
    public void set<xsl:value-of select="recutil:getFieldName1stCap(@name,0)"/>(<xsl:value-of select="recutil:getType(@size,@type,0)"/><xsl:text> </xsl:text><xsl:value-of select="recutil:getFieldName(position(),@name,0)"/>)
    {
        this.<xsl:value-of select="recutil:getFieldName(position(),@name,0)"/> = <xsl:value-of select="recutil:getFieldName(position(),@name,0)"/>;
    }
</xsl:template>

<xsl:template match="field" mode="tostring">
    <xsl:value-of select="recutil:getToString(@name,@type,@size)"/>
    <xsl:text>
        buffer.append(System.getProperty("line.separator")); </xsl:text>
    <xsl:apply-templates select="bit" mode="bittostring"/>
    <xsl:text>&#10;</xsl:text>
</xsl:template>

    <xsl:template match="bit" mode="bittostring">
        <xsl:if test="not (@mask)">
            <xsl:text>&#10;        buffer.append("         .</xsl:text>
            <xsl:value-of select="recutil:getFieldName(@name,20)"/>
            <xsl:text>     = ").append(is</xsl:text>
            <xsl:value-of select="recutil:getFieldName1stCap(@name,0)"/>
            <xsl:text>()).append('\n'); </xsl:text>
        </xsl:if>
        <xsl:if test="@mask">
        <xsl:text>&#10;            buffer.append("         .</xsl:text>
            <xsl:value-of select="recutil:getFieldName(@name,20)"/>
            <xsl:text>     = ").append(get</xsl:text>
            <xsl:value-of select="recutil:getFieldName1stCap(@name,0)"/>
            <xsl:text>()).append('\n'); </xsl:text>
        </xsl:if>
    </xsl:template>

<xsl:template match="author">
 * @author <xsl:value-of select="."/>
</xsl:template>

</xsl:stylesheet>
