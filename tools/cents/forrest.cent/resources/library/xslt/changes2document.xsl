<?xml version="1.0"?>

<xsl:stylesheet
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    version="1.0">

 <xsl:import href="copyover.xsl"/>

 <xsl:param name="name"/>

 <xsl:variable name="bugzilla">http://nagoya.apache.org/bugzilla/show_bug.cgi?id=</xsl:variable>

 <xsl:template match="changes">
  <document>
   <header>
    <title><xsl:value-of select="@title"/></title>
   </header>
   <body>
    <xsl:apply-templates/>
   </body>
  </document>
 </xsl:template>

 <xsl:template match="release">
  <s2 title="{$name} {@version} ({@date})">
   <sl>
    <xsl:apply-templates/>
   </sl>
  </s2>
 </xsl:template>

 <xsl:template match="action">
  <li>
   <icon src="images/{@type}.jpg" alt="{@type}"/>
   <xsl:apply-templates/>
   <xsl:text>(</xsl:text><xsl:value-of select="@dev"/><xsl:text>)</xsl:text>

   <xsl:if test="@due-to">
    <xsl:text> Thanks to </xsl:text>
    <link href="mailto:{@due-to-email}"><xsl:value-of select="@due-to"/></link>
    <xsl:text>.</xsl:text>
   </xsl:if>

   <xsl:if test="@fixes-bug">
    <xsl:text> Fixes </xsl:text>
    <link href="{$bugzilla}{@fixes-bug}">
     <xsl:text>bug </xsl:text><xsl:value-of select="@fixes-bug"/>
    </link>
    <xsl:text>.</xsl:text>
   </xsl:if>
  </li>
 </xsl:template>

 <xsl:template match="devs">
  <!-- remove -->
 </xsl:template>

</xsl:stylesheet>
