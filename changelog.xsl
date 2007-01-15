<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet
    xmlns:xsl='http://www.w3.org/1999/XSL/Transform'
    version='1.0'>

<!--
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
 -->
  <xsl:param name="title"/>
  <xsl:param name="module"/>
  <xsl:param name="cvsweb"/>

  <xsl:output method="html" indent="yes"  encoding="US-ASCII"/>

  <!-- Copy standard document elements.  Elements that
       should be ignored must be filtered by apply-templates
       tags. -->
  <xsl:template match="*">
    <xsl:copy>
      <xsl:copy-of select="attribute::*[. != '']"/>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="changelog">
    <HTML>
      <HEAD>
        <TITLE><xsl:value-of select="$title"/></TITLE>
      </HEAD>
      <BODY link="#000000" alink="#000000" vlink="#000000" text="#000000">
        <style type="text/css">
          body, p {
          font-family: verdana,arial,helvetica;
          font-size: 100%;
          color:#000000;
          }
	  .dateAndAuthor {
          font-family: verdana,arial,helvetica;
          font-size: 100%;
          font-weight: bold;
          text-align:left;
          background:#a6caf0;
	  }
          tr, td{
          font-family: verdana,arial,helvetica;
          font-size: 120%;
          background:#eeeee0;
          }	  
	  </style>        
          <h1>
            <a name="top"><xsl:value-of select="$title"/></a>
          </h1>

          <hr size="2"/>
        <TABLE BORDER="0" WIDTH="100%" CELLPADDING="3" CELLSPACING="1">
          
          <xsl:apply-templates select=".//entry">
            <xsl:sort select="date" data-type="text" order="descending"/>
            <xsl:sort select="time" data-type="text" order="descending"/>
          </xsl:apply-templates>
          
        </TABLE>
        
      </BODY>
    </HTML>
  </xsl:template>
  
  <xsl:template match="entry">
    <TR>
      <TD colspan="2" class="dateAndAuthor">
        <xsl:value-of select="date"/><xsl:text> </xsl:text><xsl:value-of select="time"/><xsl:text> </xsl:text><xsl:value-of select="author"/>
      </TD>
    </TR>
    <TR>
      <TD width="20">
        <xsl:text>    </xsl:text>
      </TD>
      <TD>
        <pre>
<xsl:apply-templates select="msg"/></pre>
        <ul>
          <xsl:apply-templates select="file"/>
        </ul>
      </TD>
    </TR>
  </xsl:template>

  <xsl:template match="date">
    <i><xsl:value-of select="."/></i>
  </xsl:template>

  <xsl:template match="time">
    <i><xsl:value-of select="."/></i>
  </xsl:template>

  <xsl:template match="author">
    <i>
      <a>
        <xsl:attribute name="href">mailto:<xsl:value-of select="."/></xsl:attribute>
        <xsl:value-of select="."/>
      </a>
    </i>
  </xsl:template>

  <xsl:template match="file">
    <li>
      <a>
        <xsl:choose>
          <xsl:when test="string-length(prevrevision) = 0 ">
            <xsl:attribute name="href"><xsl:value-of select="$cvsweb"/><xsl:value-of select="$module" />/<xsl:value-of select="name" />?rev=<xsl:value-of select="revision" />&amp;content-type=text/x-cvsweb-markup</xsl:attribute>
          </xsl:when>
          <xsl:otherwise>
            <xsl:attribute name="href"><xsl:value-of select="$cvsweb"/><xsl:value-of select="$module" />/<xsl:value-of select="name" />?r1=<xsl:value-of select="revision" />&amp;r2=<xsl:value-of select="prevrevision"/></xsl:attribute>
          </xsl:otherwise>
        </xsl:choose>
        <xsl:value-of select="name" /> (<xsl:value-of select="revision"/>)
      </a>
    </li>
  </xsl:template>

  <!-- Any elements within a msg are processed,
       so that we can preserve HTML tags. -->
  <xsl:template match="msg">
    <b><xsl:apply-templates/></b>
  </xsl:template>
  
</xsl:stylesheet>

