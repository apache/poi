<?xml version="1.0"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:output method="html" indent="yes"/>

  <xsl:template match="changes">
    <xsl:variable name="version" select="@version"/>
    <xsl:apply-templates select="document(@file,/)/changes/release[attribute::version=string($version)]"/>
  </xsl:template>

  <xsl:template match="announcement">
    <style>
      body { background-color: #FFFFFF }
      p { font-size: 10pt; font-family: Helvetica, Arial, sans-serif }
      li,ul { font-size: 10pt; font-family: Helvetica, Arial, sans-serif }
      div { font-size: 10pt; font-family: Helvetica, Arial, sans-serif; font-style:italic }
      h1 { font-size: 14pt; font-family: Helvetica, Arial, sans-serif; font-weight: bold }
      h2 { font-size: 12pt; font-family: Helvetica, Arial, sans-serif; font-weight: bold }
      h3 { font-size: 10pt; font-family: Helvetica, Arial, sans-serif; font-weight: bold }
      A:link { color: #0000A0 }          /* unvisited link */
      A:visited { color: #A00000 }       /* visited links */
      A:active { color: #00A000 }        /* active links */
    </style>
    <h1 align="center"><xsl:value-of select="title"/><xsl:text> Released</xsl:text></h1>
    <xsl:apply-templates select="abstract"/>

    <xsl:for-each select="project">
      <h2>About <xsl:value-of select="title"/></h2>
      <xsl:apply-templates select="."/>
    </xsl:for-each>

  </xsl:template>

  <xsl:template match="project">
    <p><xsl:apply-templates select="description"/></p>

    <p>For more information about <xsl:value-of select="title"/>, please go to
    <a><xsl:attribute name="href"><xsl:value-of select="@site"/></xsl:attribute>
    <xsl:value-of select="@site"/></a>.</p>

    <!-- print out ChangeLog if present --> 
    <!--
    <xsl:if test="changes">
      <h3>ChangeLog for <xsl:value-of select="title"/></h3>
      <xsl:apply-templates select="changes"/>
    </xsl:if>
    -->
  </xsl:template>

  <xsl:template match="abstract">
    <div align="center">
      <xsl:apply-templates/>
    </div>
  </xsl:template>

  <xsl:template match="para">
    <p><xsl:apply-templates/></p>
  </xsl:template>

  <xsl:template match="ulink">
    <a href="{@uri}"><xsl:value-of select="."/></a>
  </xsl:template>

  <xsl:template match="release">
    <ul>
    <xsl:for-each select="action">
      <li> 
        <xsl:value-of select="normalize-space(.)"/>
        <xsl:if test="@dev">
          <xsl:text>[</xsl:text><xsl:value-of select="@dev"/><xsl:text>]</xsl:text>
        </xsl:if>
      </li>
    </xsl:for-each>
    </ul>
  </xsl:template>

</xsl:stylesheet>
