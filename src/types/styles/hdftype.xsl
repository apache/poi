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

    <xsl:output method="text"/>

<xsl:template match="record">

<xsl:if test="@package">
package <xsl:value-of select="@package"/>;
</xsl:if>

import org.apache.poi.util.BitField;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.StringUtil;
import org.apache.poi.util.HexDump;
import org.apache.poi.hwpf.usermodel.*;

/**
 * <xsl:value-of select="/record/description"/>
 * NOTE: This source is automatically generated please do not modify this file.  Either subclass or
 *       remove the record in src/types/definitions.
<xsl:apply-templates select="author"/><xsl:text>
 */
public abstract class </xsl:text><xsl:value-of select="@name"/><xsl:text>AbstractType
{

</xsl:text>
    <xsl:for-each select="//fields/field">
        <xsl:call-template name="indent"/>
        <xsl:text>protected </xsl:text>
        <xsl:value-of select="@type"/>
        <xsl:text> field_</xsl:text>
        <xsl:value-of select="position()"/>
        <xsl:text>_</xsl:text>
        <xsl:value-of select="@name"/>
        <xsl:text>;</xsl:text>
        <xsl:call-template name="linebreak"/>
        <xsl:apply-templates select="./bit|./const"/>
    </xsl:for-each>

    <xsl:call-template name="linebreak"/>

    <xsl:call-template name="indent"/>
    <xsl:text>protected </xsl:text>
    <xsl:value-of select="@name"/>
    <xsl:text>AbstractType()</xsl:text>
    <xsl:call-template name="linebreak"/>
    <xsl:call-template name="indent"/>
    <xsl:text>{</xsl:text>
    <xsl:call-template name="linebreak"/>
    <xsl:for-each select="//fields/field">
        <!-- we don't include @default condition in for-each to preserve position() -->
        <xsl:if test="@default">
            <xsl:call-template name="indent"/>
            <xsl:call-template name="indent"/>
            <xsl:text>this.</xsl:text>
            <xsl:value-of select="recutil:getFieldName(position(),@name,0)"/>
            <xsl:text> = </xsl:text>
            <xsl:value-of select="@default"/>
            <xsl:text>;</xsl:text>
            <xsl:call-template name="linebreak"/>
        </xsl:if>
        <xsl:choose>
            <xsl:when test="@type='boolean'"/>
            <xsl:when test="@type='byte'"/>
            <xsl:when test="@type='int'"/>
            <xsl:when test="@type='short'"/>
            <xsl:when test="@type='long'"/>
            <xsl:when test="substring(@type, string-length(@type) - 1) = '[]'">
                <xsl:call-template name="indent"/>
                <xsl:call-template name="indent"/>
                <xsl:text>this.</xsl:text>
                <xsl:value-of select="recutil:getFieldName(position(),@name,0)"/>
                <xsl:text> = new </xsl:text>
                <xsl:value-of select="substring(@type, 0, string-length(@type) - 1)"/>
                <xsl:text>[0];</xsl:text>
                <xsl:call-template name="linebreak"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="indent"/>
                <xsl:call-template name="indent"/>
                <xsl:text>this.</xsl:text>
                <xsl:value-of select="recutil:getFieldName(position(),@name,0)"/>
                <xsl:text> = new </xsl:text>
                <xsl:value-of select="@type"/>
                <xsl:text>();</xsl:text>
                <xsl:call-template name="linebreak"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:for-each>
    <xsl:call-template name="indent"/>
    <xsl:text>}</xsl:text>
    <xsl:call-template name="linebreak"/>
    <xsl:call-template name="linebreak"/>

    <xsl:if test='/@fromfile="true"'>
        protected void fillFields(byte[] data, int offset)
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

    /**
     * Size of record (exluding 4 byte header)
     */
    public static int getSize()
    {
<xsl:variable name="fieldIterator" select="field:new()"/>
<xsl:text>        return 4 + </xsl:text>
<xsl:for-each select="//fields/field">
    <xsl:value-of select="field:calcSize($fieldIterator,position(),@name,@size,@type)"/>
</xsl:for-each>;
    }
</xsl:if>

    <xsl:call-template name="linebreak"/>
    <xsl:call-template name="indent"/>
    <xsl:text>public String toString()
    {
        StringBuilder builder = new StringBuilder();
</xsl:text>
    <xsl:call-template name="indent"/>
    <xsl:call-template name="indent"/>
    <xsl:text>builder.append("[</xsl:text>
    <xsl:value-of select="@name"/>
    <xsl:text>]\n");</xsl:text>
    <xsl:call-template name="linebreak"/>
    <xsl:apply-templates select="//field" mode="tostring"/>
    <xsl:call-template name="linebreak"/>
    <xsl:call-template name="indent"/>
    <xsl:call-template name="indent"/>
    <xsl:text>builder.append("[/</xsl:text>
    <xsl:value-of select="@name"/>
    <xsl:text>]\n");
        return builder.toString();
    }
</xsl:text>

<xsl:apply-templates select="//field" mode="getset"/>
<xsl:apply-templates select="//field" mode="bits"/>
<xsl:text>
}  // END OF CLASS
</xsl:text>
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

    <xsl:template match="const">
        <xsl:if test="@description">
            <xsl:call-template name="indent"/>
            <xsl:text>/** </xsl:text>
            <xsl:value-of select="@description"/>
            <xsl:text> */</xsl:text>
            <xsl:call-template name="linebreak"/>
        </xsl:if>
        <xsl:call-template name="indent"/>
        <xsl:text>/**/</xsl:text>
        <xsl:text>public final static </xsl:text>
        <xsl:value-of select="@type"/>
        <xsl:text> </xsl:text>
        <xsl:value-of select="recutil:getConstName(../@name,@name,0)"/>
        <xsl:text> = </xsl:text>
        <xsl:value-of select="@value"/>
        <xsl:text>;</xsl:text>
        <xsl:call-template name="linebreak"/>
    </xsl:template>

    <xsl:template match="const" mode="listconsts">
        <xsl:call-template name="linebreak"/>
        <xsl:call-template name="indent"/>
        <xsl:text> * &lt;li&gt;{@link #</xsl:text>
        <xsl:value-of select="recutil:getConstName(../@name,@name,0)"/>
        <xsl:text>}</xsl:text>
    </xsl:template>

    <xsl:template name="linebreak">
        <xsl:text>
</xsl:text>
    </xsl:template>

    <xsl:template name="indent">
        <xsl:text>    </xsl:text>
    </xsl:template>

<xsl:template match="field" mode="getset">
    /**
     * <xsl:choose>
        <xsl:when test="@description">
            <xsl:value-of select="@description"/>
        </xsl:when>
        <xsl:otherwise>
            <xsl:text>Get the </xsl:text>
            <xsl:value-of select="@name"/>
            <xsl:text> field for the </xsl:text>
            <xsl:value-of select="../../@name"/>
            <xsl:text> record</xsl:text>
        </xsl:otherwise>
    </xsl:choose>
    <xsl:text>.</xsl:text> 
    <xsl:if test="./const">
     *
     * @return One of <xsl:apply-templates select="./const" mode="listconsts"/></xsl:if>
     */
    public <xsl:value-of select="@type"/> get<xsl:value-of select="recutil:getFieldName1stCap(@name,0)"/>()
    {
        return <xsl:value-of select="recutil:getFieldName(position(),@name,0)"/>;
    }

    /**
     * <xsl:choose>
        <xsl:when test="@description">
            <xsl:value-of select="@description"/>
        </xsl:when>
        <xsl:otherwise>
            <xsl:text>Set the </xsl:text>
            <xsl:value-of select="@name"/>
            <xsl:text> field for the </xsl:text>
            <xsl:value-of select="../../@name"/>
            <xsl:text> record</xsl:text>
        </xsl:otherwise>
    </xsl:choose>
    <xsl:text>.</xsl:text>
    <xsl:if test="./const">
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
        <xsl:call-template name="indent"/>
        <xsl:call-template name="indent"/>
        <xsl:text>builder.append("    .</xsl:text>
        <xsl:value-of select="recutil:getFieldName(@name,20)"/>
        <xsl:text> = ");</xsl:text>
        <xsl:call-template name="linebreak"/>
        <xsl:call-template name="indent"/>
        <xsl:call-template name="indent"/>
        <xsl:text>builder.append(" (").append(get</xsl:text>
        <xsl:value-of select="recutil:getFieldName1stCap(@name,0)"/>
        <xsl:text>()).append(" )\n");</xsl:text>
        <xsl:call-template name="linebreak"/>
        <xsl:apply-templates select="bit" mode="bittostring"/>
    </xsl:template>

    <xsl:template match="bit" mode="bittostring">
        <xsl:call-template name="indent"/>
        <xsl:call-template name="indent"/>
        <xsl:text>builder.append("         .</xsl:text>
        <xsl:value-of select="recutil:getFieldName(@name,20)"/>
        <xsl:text>     = ").append(</xsl:text>
        <xsl:value-of select="recutil:getBitFieldFunction(@name, @mask, ../@type, 'false')"/>
        <xsl:text>()).append('\n');</xsl:text>
        <xsl:call-template name="linebreak"/>
    </xsl:template>

<xsl:template match="author">
 * @author <xsl:value-of select="."/>
</xsl:template>

</xsl:stylesheet>
