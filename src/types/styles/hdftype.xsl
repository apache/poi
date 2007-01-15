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
   xmlns:recutil="org.apache.poi.hdf.generator.HDFRecordUtil"
   xmlns:field="org.apache.poi.hdf.generator.HDFFieldIterator"
   xmlns:java="java" >

<xsl:template match="record">

<xsl:if test="@package">
package <xsl:value-of select="@package"/>;
</xsl:if>


import org.apache.poi.util.BitField;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.StringUtil;
import org.apache.poi.util.HexDump;
import org.apache.poi.hdf.model.hdftypes.HDFType;
import org.apache.poi.hwpf.usermodel.*;

/**
 * <xsl:value-of select="/record/description"/>
 * NOTE: This source is automatically generated please do not modify this file.  Either subclass or
 *       remove the record in src/records/definitions.
<xsl:apply-templates select="author"/>
 */
public abstract class <xsl:value-of select="@name"/>AbstractType
    implements HDFType
{

<xsl:for-each select="//fields/field">    protected  <xsl:value-of select="@type"/><xsl:text> field_</xsl:text><xsl:value-of select="position()"/>_<xsl:value-of select="@name"/>;
<xsl:apply-templates select="./bit|./const"/>
</xsl:for-each>

    public <xsl:value-of select="@name"/>AbstractType()
    {
<xsl:for-each select="//fields/field"><xsl:if test="@default">
<xsl:text>        </xsl:text>
<xsl:value-of select="recutil:getFieldName(position(),@name,0)"/> = <xsl:value-of select="@default"/>;
</xsl:if></xsl:for-each>
    }
<xsl:if test='//@fromfile="true"'>
    protected void fillFields(byte [] data, int offset)
    {
<xsl:variable name="fieldIterator" select="field:new()"/>
<xsl:for-each select="//fields/field">
<xsl:text>        </xsl:text><xsl:value-of select="recutil:getFieldName(position(),@name,30)"/>  = <xsl:value-of select="field:fillDecoder($fieldIterator,@size,@type)"/>;
</xsl:for-each>
    }

    public void serialize(byte[] data, int offset)
    {
<xsl:variable name="fieldIterator" select="field:new()"/>
<xsl:for-each select="//fields/field">
<xsl:text>        </xsl:text><xsl:value-of select="field:serialiseEncoder($fieldIterator,position(),@name,@size,@type)"/>;
</xsl:for-each>
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[<xsl:value-of select="@name"/>]\n");
<xsl:apply-templates select="//field" mode="tostring"/>
        buffer.append("[/<xsl:value-of select="@name"/>]\n");
        return buffer.toString();
    }
</xsl:if>
    /**
     * Size of record (exluding 4 byte header)
     */
    public int getSize()
    {
<xsl:variable name="fieldIterator" select="field:new()"/>
<xsl:text>        return 4 + </xsl:text>
<xsl:for-each select="//fields/field">
    <xsl:value-of select="field:calcSize($fieldIterator,position(),@name,@size,@type)"/>
</xsl:for-each>;
    }


<xsl:apply-templates select="//field" mode="getset"/>
<xsl:apply-templates select="//field" mode="bits"/>

}  // END OF CLASS




</xsl:template>

<xsl:template match = "field" mode="bits">
<xsl:variable name="fieldNum" select="position()"/>
<xsl:for-each select="bit">
    /**
     * Sets the <xsl:value-of select="@name"/> field value.
     * <xsl:value-of select="@description"/>
     */
    public void set<xsl:value-of select="recutil:getFieldName1stCap(@name,0)"/>(<xsl:value-of select="recutil:getBitFieldType(@name, @mask, ../@type)"/> value)
    {
        <xsl:value-of select="recutil:getFieldName($fieldNum,../@name,0)"/> = <xsl:value-of select="recutil:getBitFieldSet(@name, @mask, ../@type, recutil:getFieldName($fieldNum,../@name,0))"/>;

        <!--<xsl:value-of select="recutil:getFieldName(@name,0)"/>.setValue(<xsl:value-of select="recutil:getFieldName($fieldNum,../@name,0)"/>, value);-->
    }

    /**
     * <xsl:value-of select="@description"/>
     * @return  the <xsl:value-of select="@name"/> field value.
     */
    public <xsl:value-of select="recutil:getBitFieldFunction(@name,@mask,../@type, 'true')"/>()
    {
        return <xsl:value-of select="recutil:getBitFieldGet(@name, @mask,../@type, recutil:getFieldName($fieldNum,../@name,0))"/>
        <!--return <xsl:value-of select="recutil:getFieldName(@name,0)"/>.isSet(<xsl:value-of select="recutil:getFieldName($fieldNum,../@name,0)"/>);-->
    }
</xsl:for-each>
</xsl:template>

<xsl:template match = "bit" >        private static BitField  <xsl:value-of select="@name"/> = new BitField(<xsl:value-of select="@mask"/>);
</xsl:template>
<xsl:template match = "const">        public final static <xsl:value-of select="@type"/><xsl:text>  </xsl:text><xsl:value-of select="@name"/> = <xsl:value-of select="@value"/>;
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
    public <xsl:value-of select="@type"/> get<xsl:value-of select="recutil:getFieldName1stCap(@name,0)"/>()
    {
        return <xsl:value-of select="recutil:getFieldName(position(),@name,0)"/>;
    }

    /**
     * Set the <xsl:value-of select="@name"/> field for the <xsl:value-of select="../../@name"/> record.<xsl:if test="./const">
     *
     * @param <xsl:value-of select="recutil:getFieldName(position(),@name,0)"/>
     *        One of <xsl:apply-templates select="./const" mode="listconsts"/></xsl:if>
     */
    public void set<xsl:value-of select="recutil:getFieldName1stCap(@name,0)"/>(<xsl:value-of select="@type"/><xsl:text> </xsl:text><xsl:value-of select="recutil:getFieldName(position(),@name,0)"/>)
    {
        this.<xsl:value-of select="recutil:getFieldName(position(),@name,0)"/> = <xsl:value-of select="recutil:getFieldName(position(),@name,0)"/>;
    }
</xsl:template>

<xsl:template match="field" mode="tostring">
        buffer.append("    .<xsl:value-of select="recutil:getFieldName(@name,20)"/> = ");
        buffer.append(" (").append(get<xsl:value-of select="recutil:getFieldName1stCap(@name,0)"/>()).append(" )\n");
<xsl:apply-templates select="bit" mode="bittostring"/>
</xsl:template>

<xsl:template match="bit" mode="bittostring">        buffer.append("         .<xsl:value-of select="recutil:getFieldName(@name,20)"/>     = ").append(<xsl:value-of select="recutil:getBitFieldFunction(@name, @mask, ../@type, 'false')"/>()).append('\n');
</xsl:template>

<xsl:template match="author">
 * @author <xsl:value-of select="."/>
</xsl:template>

</xsl:stylesheet>
