<?xml version="1.0"?>

<xsl:stylesheet version="1.0"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 xmlns:html="http://www.w3.org/1999/xhtml">

<!-- @author <a href="mailto:barozzi@nicolaken.com">Nicola Ken Barozzi</a> -->

<xsl:template match="html:body">
  <patch-queue>
   <xsl:apply-templates/>
  </patch-queue>
</xsl:template>

<xsl:template match="html:tr">
  <xsl:if test="contains(@class,'th')">
    <bug>
      <xsl:attribute name="id"><xsl:value-of select="html:td[1]/html:a"/></xsl:attribute>
      <xsl:attribute name="url">http://nagoya.apache.org/bugzilla/<xsl:value-of select="html:td[1]/html:a/@href"/></xsl:attribute>
      <xsl:attribute name="severity"><xsl:value-of select="html:td[2]"/></xsl:attribute>
      <xsl:attribute name="priority"><xsl:value-of select="html:td[3]"/></xsl:attribute>
      <xsl:attribute name="platform"><xsl:value-of select="html:td[4]"/></xsl:attribute>
      <xsl:attribute name="owner"><xsl:value-of select="html:td[5]"/></xsl:attribute>
      <xsl:attribute name="status"><xsl:value-of select="html:td[6]"/></xsl:attribute>
      <xsl:attribute name="resolution"><xsl:value-of select="html:td[7]"/></xsl:attribute>
      <xsl:attribute name="summary"><xsl:value-of select="html:td[8]"/></xsl:attribute>
    </bug>
  </xsl:if>
</xsl:template>

  <xsl:template match="@*|*|text()|processing-instruction()">
      <xsl:apply-templates select="@*|*|text()|processing-instruction()"/>
  </xsl:template>

</xsl:stylesheet>

