<xsl:stylesheet version="1.0"
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
   xmlns:recutil="org.apache.poi.hdf.generator.HDFRecordUtil"
   xmlns:field="org.apache.poi.hdf.generator.HDFFieldIterator"
   xmlns:java="java" >

<xsl:template match="record">
/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
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
 *       &quot;This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/).&quot;
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names &quot;Apache&quot; and &quot;Apache Software Foundation&quot; and
 *    &quot;Apache POI&quot; must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called &quot;Apache&quot;,
 *    &quot;Apache POI&quot;, nor may &quot;Apache&quot; appear in their name, without
 *    prior written permission of the Apache Software Foundation.
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
 * &lt;http://www.apache.org/&gt;.
 */

<xsl:if test="@package">
package <xsl:value-of select="@package"/>;
</xsl:if>


import org.apache.poi.util.BitField;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.StringUtil;
import org.apache.poi.util.HexDump;
import org.apache.poi.hdf.model.hdftypes.HDFType;

/**
 * <xsl:value-of select="/record/description"/>
 * NOTE: This source is automatically generated please do not modify this file.  Either subclass or
 *       remove the record in src/records/definitions.
<xsl:apply-templates select="author"/>
 */
public abstract class <xsl:value-of select="@name"/>AbstractType
    implements HDFType
{

<xsl:for-each select="//fields/field">    private  <xsl:value-of select="@type"/><xsl:text> field_</xsl:text><xsl:value-of select="position()"/>_<xsl:value-of select="@name"/>;
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
    protected void fillFields(byte [] data, short size, int offset)
    {
<xsl:variable name="fieldIterator" select="field:new()"/>
<xsl:for-each select="//fields/field">
<xsl:text>        </xsl:text><xsl:value-of select="recutil:getFieldName(position(),@name,30)"/>  = <xsl:value-of select="field:fillDecoder($fieldIterator,@size,@type)"/>;
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
</xsl:for-each>
</xsl:template>

<xsl:template match = "bit" >    private BitField  <xsl:value-of select="@name"/> = new BitField(<xsl:value-of select="@mask"/>);
</xsl:template>
<xsl:template match = "const">    public final static <xsl:value-of select="@type"/><xsl:text>  </xsl:text><xsl:value-of select="@name"/> = <xsl:value-of select="@value"/>;
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
        buffer.append("    .<xsl:value-of select="recutil:getFieldName(@name,20)"/> = ");<xsl:choose><xsl:when test="@type != 'string' and @type != 'float' and @size != 'varword'">
        buffer.append("0x");
        buffer.append(HexDump.toHex((<xsl:value-of select="@type"/>)get<xsl:value-of select="recutil:getFieldName1stCap(@name,0)"/>()));</xsl:when></xsl:choose>
        buffer.append(" (").append(get<xsl:value-of select="recutil:getFieldName1stCap(@name,0)"/>()).append(" )\n");
<xsl:apply-templates select="bit" mode="bittostring"/>
</xsl:template>

<xsl:template match="bit" mode="bittostring">        buffer.append("         .<xsl:value-of select="recutil:getFieldName(@name,20)"/>     = ").append(is<xsl:value-of select="recutil:getFieldName1stCap(@name,20)"/>()).append('\n');
</xsl:template>

<xsl:template match="author">
 * @author <xsl:value-of select="."/>
</xsl:template>

</xsl:stylesheet>
