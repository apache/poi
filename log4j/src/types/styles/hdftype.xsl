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
   xmlns:recutil="org.apache.poi.hwpf.dev.RecordUtil"
   xmlns:field="org.apache.poi.hwpf.dev.FieldIterator"
   xmlns:java="java" >

    <xsl:output method="text"/>

    <xsl:template name="outputClassName">
        <xsl:value-of select="/record/@name"/>
        <xsl:value-of select="/record/suffix"/>
    </xsl:template>

	<xsl:template match="record">
		<xsl:text>/* ====================================================================
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
</xsl:text>

<xsl:if test="@package">
package <xsl:value-of select="@package"/>;
</xsl:if>

import org.apache.poi.hwpf.usermodel.*;
import org.apache.poi.util.*;

/**
 * <xsl:value-of select="/record/description"/>
 * &lt;p&gt;
 * NOTE: This source is automatically generated please do not modify this file.  Either subclass or
 *       remove the record in src/types/definitions.
 * &lt;p&gt;
 * This class is internal. It content or properties may change without notice 
 * due to changes in our knowledge of internal Microsoft Word binary structures.
<xsl:apply-templates select="author"/><xsl:text>
 */
@Internal
public abstract class </xsl:text><xsl:call-template name="outputClassName"/><xsl:text>
{

</xsl:text>
    <xsl:for-each select="//fields/field">
        <xsl:if test="@deprecated='true'">
            <xsl:call-template name="indent"/>
            <xsl:text>@Deprecated</xsl:text>
            <xsl:call-template name="linebreak"/>
        </xsl:if>
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
            <xsl:when test="@type='byte[]'">
                <xsl:call-template name="indent"/>
                <xsl:call-template name="indent"/>
                <xsl:text>this.</xsl:text>
                <xsl:value-of select="recutil:getFieldName(position(),@name,0)"/>
                <xsl:text> = new byte[</xsl:text>
                <xsl:value-of select="@size"/>
                <xsl:text>];</xsl:text>
                <xsl:call-template name="linebreak"/>
            </xsl:when>
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

    <xsl:if test='/*/@fromfile="true"'>
    <xsl:call-template name="indent"/>
<xsl:text>protected void fillFields( byte[] data, int offset )
    {
</xsl:text>
<xsl:variable name="fieldIterator" select="field:new()"/>
    <xsl:for-each select="//fields/field">
        <xsl:call-template name="indent"/>
        <xsl:call-template name="indent"/>
        <xsl:value-of select="recutil:getFieldName(position(),@name,30)"/>
        <xsl:text> = </xsl:text>
        <xsl:value-of select="field:fillDecoder($fieldIterator,@size,@type)"/>
        <xsl:text>;
</xsl:text>
    </xsl:for-each>
    <xsl:call-template name="indent"/>
    <xsl:text>}</xsl:text>
    <xsl:call-template name="linebreak"/>

    <xsl:call-template name="linebreak"/>

    <xsl:call-template name="indent"/>
    <xsl:text>public void serialize( byte[] data, int offset )
    {
</xsl:text>
<xsl:variable name="fieldIterator" select="field:new()"/>
    <xsl:for-each select="//fields/field">
        <xsl:call-template name="indent"/>
        <xsl:call-template name="indent"/>
        <xsl:value-of select="field:serialiseEncoder($fieldIterator,position(),@name,@size,@type)"/>
        <xsl:call-template name="linebreak"/>
    </xsl:for-each>
    <xsl:call-template name="indent"/>
    <xsl:text>}</xsl:text>
    <xsl:call-template name="linebreak"/>
    <xsl:text>
    public byte[] serialize()
    {
        final byte[] result = new byte[ getSize() ];
        serialize( result, 0 );
        return result;
    }
</xsl:text>
    <xsl:text>
    /**
     * Size of record
     */
    public static int getSize()
    {
</xsl:text>
    <xsl:call-template name="indent"/>
    <xsl:call-template name="indent"/>
    <xsl:text>return 0</xsl:text>
    <xsl:variable name="fieldIterator" select="field:new()"/>
    <xsl:for-each select="//fields/field">
        <xsl:value-of select="field:calcSize($fieldIterator,position(),@name,@size,@type)"/>
    </xsl:for-each>
    <xsl:text>;</xsl:text>
    <xsl:call-template name="linebreak"/>
    <xsl:call-template name="indent"/>
    <xsl:text>}</xsl:text>
    <xsl:call-template name="linebreak"/>
</xsl:if>

    <!-- equals() -->
    <xsl:call-template name="linebreak"/>
    <xsl:call-template name="indent"/>
    <xsl:text>@Override</xsl:text>
    <xsl:call-template name="linebreak"/>
    <xsl:call-template name="indent"/>
    <xsl:text>public boolean equals( Object obj )
    {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
</xsl:text>
    <xsl:call-template name="indent"/>
    <xsl:call-template name="indent"/>
    <xsl:call-template name="outputClassName"/>
    <xsl:text> other = (</xsl:text>
    <xsl:call-template name="outputClassName"/>
    <xsl:text>) obj;</xsl:text>
    <xsl:call-template name="linebreak"/>
    <xsl:for-each select="//fields/field">
        <xsl:variable name="fieldName" select="recutil:getFieldName(position(),@name,0)"/>
        <xsl:call-template name="indent"/>
        <xsl:call-template name="indent"/>
        <xsl:choose>
            <xsl:when test="substring(@type, string-length(@type)-1)='[]'">
                <xsl:text>if ( </xsl:text>
                <xsl:text>!Arrays.equals( </xsl:text>
                <xsl:value-of select="$fieldName"/>
                <xsl:text>, other.</xsl:text>
                <xsl:value-of select="$fieldName"/>
                <xsl:text> )</xsl:text>
                <xsl:text> )</xsl:text>
                <xsl:call-template name="linebreak"/>
                <xsl:call-template name="indent"/>
                <xsl:call-template name="indent"/>
                <xsl:call-template name="indent"/>
                <xsl:text>return false;</xsl:text>
                <xsl:call-template name="linebreak"/>
            </xsl:when>
            <xsl:when test="@type='boolean' or @type='byte' or @type='double' or @type='int' or @type='long' or @type='short'">
                <xsl:text>if ( </xsl:text>
                <xsl:value-of select="$fieldName"/>
                <xsl:text> != other.</xsl:text>
                <xsl:value-of select="$fieldName"/>
                <xsl:text> )</xsl:text>
                <xsl:call-template name="linebreak"/>
                <xsl:call-template name="indent"/>
                <xsl:call-template name="indent"/>
                <xsl:call-template name="indent"/>
                <xsl:text>return false;</xsl:text>
                <xsl:call-template name="linebreak"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:text>if ( </xsl:text>
                <xsl:value-of select="$fieldName"/>
                <xsl:text> == null )</xsl:text>
                <xsl:call-template name="linebreak"/>
                <xsl:call-template name="indent"/>
                <xsl:call-template name="indent"/>
                <xsl:text>{</xsl:text>
                <xsl:call-template name="linebreak"/>
                <xsl:call-template name="indent"/>
                <xsl:call-template name="indent"/>
                <xsl:call-template name="indent"/>
                <xsl:text>if ( other.</xsl:text>
                <xsl:value-of select="$fieldName"/>
                <xsl:text> != null )</xsl:text>
                <xsl:call-template name="linebreak"/>
                <xsl:call-template name="indent"/>
                <xsl:call-template name="indent"/>
                <xsl:call-template name="indent"/>
                <xsl:call-template name="indent"/>
                <xsl:text>return false;</xsl:text>
                <xsl:call-template name="linebreak"/>
                <xsl:call-template name="indent"/>
                <xsl:call-template name="indent"/>
                <xsl:text>}</xsl:text>
                <xsl:call-template name="linebreak"/>
                <xsl:call-template name="indent"/>
                <xsl:call-template name="indent"/>
                <xsl:text>else if ( !</xsl:text>
                <xsl:value-of select="$fieldName"/>
                <xsl:text>.equals( other.</xsl:text>
                <xsl:value-of select="$fieldName"/>
                <xsl:text> ) )</xsl:text>
                <xsl:call-template name="linebreak"/>
                <xsl:call-template name="indent"/>
                <xsl:call-template name="indent"/>
                <xsl:call-template name="indent"/>
                <xsl:text>return false;</xsl:text>
                <xsl:call-template name="linebreak"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:for-each>
    <xsl:call-template name="indent"/>
    <xsl:call-template name="indent"/>
    <xsl:text>return true;</xsl:text>
    <xsl:call-template name="linebreak"/>
    <xsl:call-template name="indent"/>
    <xsl:text>}</xsl:text>
    <xsl:call-template name="linebreak"/>

    <!-- hashCode() -->
    <xsl:call-template name="linebreak"/>
    <xsl:call-template name="indent"/>
    <xsl:text>@Override</xsl:text>
    <xsl:call-template name="linebreak"/>
    <xsl:call-template name="indent"/>
    <xsl:text>public int hashCode()
    {
        final int prime = 31;
        int result = 1;
</xsl:text>
    <xsl:for-each select="//fields/field">
        <xsl:call-template name="indent"/>
        <xsl:call-template name="indent"/>
        <xsl:text>result = prime * result</xsl:text>
        <xsl:variable name="fieldName" select="recutil:getFieldName(position(),@name,0)" />
        <xsl:choose>
            <xsl:when test="substring(@type, string-length(@type)-1)='[]'">
        		<xsl:text> + </xsl:text>
                <xsl:text>Arrays.hashCode( </xsl:text>
                <xsl:value-of select="$fieldName"/>
                <xsl:text> )</xsl:text>
            </xsl:when>
            <xsl:when test="@type='boolean'">
        		<xsl:text> + </xsl:text>
                <xsl:text>( </xsl:text>
                <xsl:value-of select="$fieldName"/>
                <xsl:text>? 1231 : 1237 )</xsl:text>
            </xsl:when>
            <xsl:when test="@type='byte' or @type='double' or @type='int' or @type='short'">
        		<xsl:text> + </xsl:text>
                <xsl:value-of select="$fieldName"/>
            </xsl:when>
            <xsl:when test="@type='long'">
				<xsl:call-template name="linebreak" />
				<xsl:call-template name="indent" />
				<xsl:call-template name="indent" />
				<xsl:call-template name="indent" />
				<xsl:call-template name="indent" />
        		<xsl:text> + (int) ( </xsl:text>
                <xsl:value-of select="$fieldName"/>
        		<xsl:text> ^ ( </xsl:text>
                <xsl:value-of select="$fieldName"/>
        		<xsl:text> >>> 32 ) )</xsl:text>
            </xsl:when>
            <xsl:otherwise>
				<xsl:call-template name="linebreak" />
				<xsl:call-template name="indent" />
				<xsl:call-template name="indent" />
				<xsl:call-template name="indent" />
				<xsl:call-template name="indent" />
				<xsl:text>+ ((</xsl:text>
				<xsl:value-of select="$fieldName" />
				<xsl:text> == null) ? 0 : </xsl:text>
				<xsl:value-of select="$fieldName" />
				<xsl:text>.hashCode())</xsl:text>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:text>;</xsl:text>
        <xsl:call-template name="linebreak"/>
    </xsl:for-each>
    <xsl:call-template name="indent"/>
    <xsl:call-template name="indent"/>
    <xsl:text>return result;</xsl:text>
    <xsl:call-template name="linebreak"/>
    <xsl:call-template name="indent"/>
    <xsl:text>}</xsl:text>
    <xsl:call-template name="linebreak"/>

    <xsl:call-template name="linebreak"/>
    
    <xsl:call-template name="toString" />

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
    @Internal
    public void set<xsl:value-of select="recutil:getFieldName1stCap(@name,0)"/>( <xsl:value-of select="recutil:getBitFieldType(@name, @mask, ../@type)"/> value )
    {
        <xsl:value-of select="recutil:getFieldName($fieldNum,../@name,0)"/> = <xsl:value-of select="recutil:getBitFieldSet(@name, @mask, ../@type, recutil:getFieldName($fieldNum,../@name,0))"/>;
    }

    /**
     * <xsl:value-of select="@description"/>
     * @return  the <xsl:value-of select="@name"/><xsl:text> field value.</xsl:text>
    <xsl:call-template name="linebreak"/>
    <xsl:if test="@deprecated='true'">
        <xsl:call-template name="indent"/>
        <xsl:text> * @deprecated This field should not be used according to specification</xsl:text>
        <xsl:call-template name="linebreak"/>
    </xsl:if>
    <xsl:call-template name="indent"/>
    <xsl:text> */</xsl:text>
    <xsl:call-template name="linebreak"/>
    <xsl:call-template name="indent"/>
    <xsl:text>@Internal</xsl:text>
    <xsl:call-template name="linebreak"/>
    <xsl:if test="@deprecated='true'">
        <xsl:call-template name="indent"/>
        <xsl:text>@Deprecated</xsl:text>
        <xsl:call-template name="linebreak"/>
    </xsl:if>
    <xsl:call-template name="indent"/>
    <xsl:text>public </xsl:text><xsl:value-of select="recutil:getBitFieldFunction(@name,@mask,../@type, 'true')"/><xsl:text>()</xsl:text>
    {
        return <xsl:value-of select="recutil:getBitFieldGet(@name, @mask,../@type, recutil:getFieldName($fieldNum,../@name,0))"/>
    }
</xsl:for-each>
</xsl:template>

    <xsl:template match="bit">
        <xsl:call-template name="indent"/>
        <xsl:text>/**/private static final BitField </xsl:text>
        <xsl:value-of select="@name"/>
        <xsl:text> = new BitField(</xsl:text>
        <xsl:value-of select="@mask"/>
        <xsl:text>);</xsl:text>
        <xsl:call-template name="linebreak"/>
        <xsl:apply-templates select="const"/>
    </xsl:template>

	<xsl:template match="const">
		<xsl:if test="@description">
			<xsl:call-template name="indent" />
			<xsl:choose>
				<xsl:when test="name(..) = 'bit'">
					<xsl:text>/**   </xsl:text>
				</xsl:when>
				<xsl:otherwise>
					<xsl:text>/** </xsl:text>
				</xsl:otherwise>
			</xsl:choose>
			<xsl:value-of select="@description" />
			<xsl:text> */</xsl:text>
			<xsl:call-template name="linebreak" />
		</xsl:if>
		<xsl:call-template name="indent" />
		<xsl:choose>
			<xsl:when test="name(..) = 'bit'">
				<xsl:text>/*  */</xsl:text>
			</xsl:when>
			<xsl:otherwise>
				<xsl:text>/**/</xsl:text>
			</xsl:otherwise>
		</xsl:choose>
		<xsl:text>protected final static </xsl:text>
		<xsl:value-of select="@type" />
		<xsl:text> </xsl:text>
		<xsl:value-of select="recutil:getConstName(../@name,@name,0)" />
		<xsl:text> = </xsl:text>
		<xsl:value-of select="@value" />
		<xsl:text>;</xsl:text>
		<xsl:call-template name="linebreak" />
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
    @Internal
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
    @Internal
    public void set<xsl:value-of select="recutil:getFieldName1stCap(@name,0)"/>( <xsl:value-of select="@type"/><xsl:text> </xsl:text><xsl:value-of select="recutil:getFieldName(position(),@name,0)"/> )
    {
        this.<xsl:value-of select="recutil:getFieldName(position(),@name,0)"/> = <xsl:value-of select="recutil:getFieldName(position(),@name,0)"/>;
    }
</xsl:template>

	<xsl:template name="toString">
		<xsl:call-template name="indent" />
		<xsl:text>public String toString()</xsl:text>
		<xsl:call-template name="linebreak" />

		<xsl:call-template name="indent" />
		<xsl:text>{</xsl:text>
		<xsl:call-template name="linebreak" />

		<xsl:call-template name="indent" />
		<xsl:call-template name="indent" />
		<xsl:text>StringBuilder builder = new StringBuilder();</xsl:text>
		<xsl:call-template name="linebreak" />
		<xsl:call-template name="linebreak" />

		<xsl:call-template name="indent" />
		<xsl:call-template name="indent" />
		<xsl:text>builder.append("[</xsl:text>
		<xsl:value-of select="@name" />
		<xsl:text>]\n");</xsl:text>
		<xsl:call-template name="linebreak" />

		<xsl:apply-templates select="//field" mode="tostring" />

		<xsl:call-template name="linebreak" />
		<xsl:call-template name="indent" />
		<xsl:call-template name="indent" />
		<xsl:text>builder.append("[/</xsl:text>
		<xsl:value-of select="@name" />
		<xsl:text>]");
        return builder.toString();
    }
</xsl:text>
	</xsl:template>

	<xsl:template match="field" mode="tostring">
		<xsl:variable name="fieldName"
			select="recutil:getFieldName(position(),@name,0)" />

		<xsl:call-template name="indent" />
		<xsl:call-template name="indent" />
		<xsl:text>builder.append( "    .</xsl:text>
		<xsl:value-of select="recutil:getFieldName(@name,20)" />
		<xsl:text> = " );</xsl:text>
		<xsl:call-template name="linebreak" />

		<xsl:call-template name="indent" />
		<xsl:call-template name="indent" />
		<xsl:text>builder.append(" ( ").append( </xsl:text>
		<xsl:choose>
			<xsl:when
				test="@type='boolean' or @type='byte' or @type='double' or @type='int' or @type='long' or @type='short'">
				<xsl:value-of select="$fieldName" />
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$fieldName" />
				<xsl:text> == null ? "null" : </xsl:text>
				<xsl:value-of select="$fieldName" />
				<xsl:text>.toString().replaceAll( "\n", "\n    " )</xsl:text>
			</xsl:otherwise>
		</xsl:choose>
		<xsl:text> ).append( " )\n" );</xsl:text>
		<xsl:call-template name="linebreak" />

		<xsl:apply-templates select="bit" mode="bittostring" />
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
