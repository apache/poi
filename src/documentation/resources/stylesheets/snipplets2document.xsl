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

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
                xmlns:ext="http://exslt.org/common"
                xmlns:math="http://exslt.org/math"
                exclude-result-prefixes="ext math"
>

    <!-- the obligatory copy-everything -->
    <xsl:template match="node() | @*">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>

    <xsl:variable name="ASCII">!"#$%&amp;'()*+,-./0123456789:;=&amp;&lt;>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[]^_`abcdefghijklmnopqrstuvwxyz{|}~</xsl:variable>
    <xsl:variable name="DOTS">..............................................................................................</xsl:variable>

    <xsl:template match="source">

        <xsl:variable name="codelines">
            <xsl:apply-templates mode="source"/>
        </xsl:variable>

        <xsl:variable name="indent" select="string-length(substring-before(translate(text(),$ASCII,$DOTS),'.'))"/>

        <div class="code">
            <xsl:for-each select="ext:node-set($codelines)/*">
                <div class="codeline"><span class="lineno"></span><span class="codebody"><xsl:value-of select="substring(.,$indent)"/></span></div>
            </xsl:for-each>
        </div>
    </xsl:template>

    <xsl:template match="text()" name="split" mode="source">
        <xsl:param name="pText" select="."/>
        <xsl:if test="string-length($pText) >0">
            <line>
                <xsl:value-of select="substring-before(concat($pText, '&#10;'), '&#10;')"/>
            </line>

            <xsl:call-template name="split">
                <xsl:with-param name="pText" select="substring-after($pText, '&#10;')"/>
            </xsl:call-template>
        </xsl:if>
    </xsl:template>
</xsl:stylesheet>
