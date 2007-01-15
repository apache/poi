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

import junit.framework.TestCase;

/**
 * Tests the serialization and deserialization of the <xsl:value-of select="@name"/>Record
 * class works correctly.  Test data taken directly from a real
 * Excel file.
 *
<xsl:apply-templates select="author"/>
 */
public class Test<xsl:value-of select="@name"/>Record
        extends TestCase
{
    byte[] data = new byte[] {
	<xsl:value-of select="recutil:getByteArrayString(testdata)"/>
    };

    public Test<xsl:value-of select="@name"/>Record(String name)
    {
        super(name);
    }

    public void testLoad()
            throws Exception
    {
        <xsl:value-of select="@name"/>Record record = new <xsl:value-of select="@name"/>Record((short)<xsl:value-of select="@id"/>, (short)data.length, data);
        
<xsl:for-each select="//fields/field">
            <xsl:choose><xsl:when test="@type='string' or @type='hbstring'">
        assertEquals( "<xsl:value-of select="./testresult/@value"/>", record.get<xsl:value-of select="recutil:getFieldName1stCap(@name,0)"/>());
</xsl:when><xsl:when test="@size='1'">
        assertEquals( (byte)<xsl:value-of select="./testresult/@value"/>, record.get<xsl:value-of select="recutil:getFieldName1stCap(@name,0)"/>());
</xsl:when><xsl:when test="@size='2'">
        assertEquals( (short)<xsl:value-of select="./testresult/@value"/>, record.get<xsl:value-of select="recutil:getFieldName1stCap(@name,0)"/>());
</xsl:when><xsl:when test="@size='4'">
        assertEquals( (int)<xsl:value-of select="./testresult/@value"/>, record.get<xsl:value-of select="recutil:getFieldName1stCap(@name,0)"/>());
</xsl:when><xsl:otherwise>
       assertEquals( (double)<xsl:value-of select="./testresult/@value"/>, record.get<xsl:value-of select="recutil:getFieldName1stCap(@name,0)"/>());
</xsl:otherwise></xsl:choose>
<xsl:apply-templates select="./bit" mode="get"/>
</xsl:for-each>

        assertEquals( <xsl:value-of select="./testsize"/>, record.getRecordSize() );

        record.validateSid((short)<xsl:value-of select="@id"/>);
    }

    public void testStore()
    {
        <xsl:value-of select="@name"/>Record record = new <xsl:value-of select="@name"/>Record();


<xsl:for-each select="//fields/field">
            <xsl:choose><xsl:when test="@type='string' or @type='hbstring'">
        record.set<xsl:value-of select="recutil:getFieldName1stCap(@name,0)"/>( "<xsl:value-of select="./testresult/@value"/>" );
</xsl:when><xsl:when test="@size='1'">
        record.set<xsl:value-of select="recutil:getFieldName1stCap(@name,0)"/>( (byte)<xsl:value-of select="./testresult/@value"/> );
</xsl:when><xsl:when test="@size='2'">
        record.set<xsl:value-of select="recutil:getFieldName1stCap(@name,0)"/>( (short)<xsl:value-of select="./testresult/@value"/> );
</xsl:when><xsl:when test="@size='4'">
        record.set<xsl:value-of select="recutil:getFieldName1stCap(@name,0)"/>( (int)<xsl:value-of select="./testresult/@value"/> );
</xsl:when><xsl:otherwise>
        record.set<xsl:value-of select="recutil:getFieldName1stCap(@name,0)"/>( (double)<xsl:value-of select="./testresult/@value"/> );
</xsl:otherwise></xsl:choose>

<xsl:apply-templates select="./bit" mode="set"/>
</xsl:for-each>

        byte [] recordBytes = record.serialize();
        assertEquals(recordBytes.length - 4, data.length);
        for (int i = 0; i &lt; data.length; i++)
            assertEquals("At offset " + i, data[i], recordBytes[i+4]);
    }
}
</xsl:template>

<xsl:template match="author">
 * @author <xsl:value-of select="."/>
</xsl:template>

<xsl:template match="bit" mode="get">
<xsl:text>        </xsl:text>assertEquals( <xsl:value-of select="./testresult/@value"/>, record.is<xsl:value-of select="recutil:getFieldName1stCap(@name,0)"/>() );<xsl:text>
</xsl:text>
</xsl:template>

<xsl:template match="bit" mode="set">
<xsl:text>        </xsl:text>record.set<xsl:value-of select="recutil:getFieldName1stCap(@name,0)"/>( <xsl:value-of select="./testresult/@value"/> );<xsl:text>
</xsl:text>
</xsl:template>

</xsl:stylesheet>
