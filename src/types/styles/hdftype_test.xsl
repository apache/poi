<xsl:stylesheet version="1.0"
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
   xmlns:recutil="org.apache.poi.generator.RecordUtil"
   xmlns:field="org.apache.poi.generator.FieldIterator"
   xmlns:java="java" >

<xsl:template match="type">
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

import junit.framework.TestCase;

/**
 * Tests the serialization and deserialization of the <xsl:value-of select="@name"/>Type
 * class works correctly.  Test data taken directly from a real
 * Excel file.
 *
<xsl:apply-templates select="author"/>
 */
public class Test<xsl:value-of select="@name"/>Type
        extends TestCase
{
    byte[] data = new byte[] {
        // PASTE DATA HERE
    };

    public Test<xsl:value-of select="@name"/>Type(String name)
    {
        super(name);
    }

    public void testLoad()
            throws Exception
    {

        <xsl:value-of select="@name"/>Type type = new <xsl:value-of select="@name"/>Type((short)<xsl:value-of select="@id"/>, (short)data.length, data);
<xsl:for-each select="//fields/field">        assertEquals( XXX, type.get<xsl:value-of select="recutil:getFieldName1stCap(@name,0)"/>());
<xsl:apply-templates select="./bit" mode="get"/>
</xsl:for-each>

        assertEquals( XXX, type.getSize() );

        type.validateSid((short)<xsl:value-of select="@id"/>);
    }

    public void testStore()
    {
        <xsl:value-of select="@name"/>Type type = new <xsl:value-of select="@name"/>Type();
<xsl:for-each select="//fields/field">        type.set<xsl:value-of select="recutil:getFieldName1stCap(@name,0)"/>( XXXX );
<xsl:apply-templates select="./bit" mode="set"/>
</xsl:for-each>

        byte [] typeBytes = type.serialize();
        assertEquals(typeBytes.length - 4, data.length);
        for (int i = 0; i &lt; data.length; i++)
            assertEquals("At offset " + i, data[i], typeBytes[i+4]);
    }
}
</xsl:template>

<xsl:template match="author">
 * @author <xsl:value-of select="."/>
</xsl:template>

<xsl:template match="bit" mode="get">
<xsl:text>        </xsl:text>assertEquals( XXX, type.is<xsl:value-of select="recutil:getFieldName1stCap(@name,0)"/>() );<xsl:text>
</xsl:text>
</xsl:template>

<xsl:template match="bit" mode="set">
<xsl:text>        </xsl:text>type.set<xsl:value-of select="recutil:getFieldName1stCap(@name,0)"/>( XXX );<xsl:text>
</xsl:text>
</xsl:template>

</xsl:stylesheet>
