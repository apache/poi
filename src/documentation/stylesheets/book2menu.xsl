<?xml version="1.0"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0">

  <xsl:template match="book">
    <menu>
      <xsl:apply-templates/>
    </menu>
  </xsl:template>

  <xsl:template match="project">
    <br/><a href="{@href}"><font color="#F3510C" size="+1"><xsl:value-of select="@label"/></font></a><br/>
  </xsl:template>

  <xsl:template match="menu">
    <font color="#000000" size="+1"><xsl:value-of select="@label"/></font>
    <font size="-1">
      <ul>
        <xsl:apply-templates/>
      </ul>
    </font><br/>
  </xsl:template>

  <xsl:template match="menu-item">
    <xsl:if test="not(@type) or @type!='hidden'">
      <li><a href="{@href}"><font size="-1"><xsl:value-of select="@label"/></font></a></li>
    </xsl:if>
  </xsl:template>

  <xsl:template match="node()|@*" priority="-1"/>
</xsl:stylesheet>

