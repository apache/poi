<?xml version="1.0"?>

<xsl:stylesheet
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    version="1.0">

 <xsl:import href="copyover.xsl"/>
 
 <xsl:template match="todo">
  <document>
   <header>
    <title><xsl:value-of select="@title"/></title>
   </header>
   <body>
    <xsl:apply-templates/>
   </body>
  </document>
 </xsl:template>

 <xsl:template match="actions">
  <s2 title="{@priority}">
   <sl>
    <xsl:for-each select="action">
     <li>
      <strong><xsl:text>[</xsl:text><xsl:value-of select="@context"/><xsl:text>]</xsl:text></strong><xsl:text> </xsl:text>
      <xsl:apply-templates/>
     </li>
    </xsl:for-each>
   </sl>
  </s2>
 </xsl:template>
 
</xsl:stylesheet>
