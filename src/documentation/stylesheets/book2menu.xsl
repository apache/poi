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
    <hr/>
    <span class="s1"><xsl:value-of select="@label"/></span><br/>
        <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="menu-item">
    <xsl:if test="not(@type) or @type!='hidden'">
      <a href="{@href}" class="s1"><xsl:value-of select="@label"/></a><br/>
    </xsl:if>
  </xsl:template>

  <xsl:template match="node()|@*" priority="-1"/>
</xsl:stylesheet>