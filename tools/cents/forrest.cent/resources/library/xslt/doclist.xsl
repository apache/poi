<?xml version="1.0"?>

<xsl:stylesheet
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    version="1.0">

 <xsl:param name="uri"/>

 <xsl:template match="book">
  <book title="{@title}" uri="{$uri}">
    <xsl:copy-of select="node()"/>
  </book>
 </xsl:template>

</xsl:stylesheet>
