<?xml version="1.0"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0">

  <xsl:param name="resource"/>

  <xsl:template match="book">
    <menu>
      <xsl:apply-templates/>
    </menu>
  </xsl:template>

  <xsl:template match="project">
  </xsl:template>

  <xsl:template match="menu[position()=1]">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="menu">
     <tr>
       <td align="left" valign="top">
         ----------
         <br/>
         <b><xsl:value-of select="@label"/></b>
       </td>
     </tr>
     <xsl:apply-templates/>
  </xsl:template>


  <xsl:template match="menu-item">
    <xsl:if test="not(@type) or @type!='hidden'">
     <tr>
      <td align="left" valign="top">
       <xsl:choose>
         <xsl:when test="@href=$resource">
          <xsl:value-of select="@label"/>
         </xsl:when>
         <xsl:otherwise>
          <a href="{@href}"><xsl:value-of select="@label"/></a>
        </xsl:otherwise>
       </xsl:choose>
      </td>
     </tr>
    </xsl:if>
  </xsl:template>

  <xsl:template match="external">
    <xsl:if test="not(@type) or @type!='hidden'">
     <tr>
	<td align="left" valign="top">
          <a href="{@href}"><xsl:value-of select="@label"/></a>
	</td>
     </tr>
    </xsl:if>
  </xsl:template>

  <xsl:template match="node()|@*" priority="-1"/>
</xsl:stylesheet>

