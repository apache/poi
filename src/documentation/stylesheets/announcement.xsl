<?xml version="1.0"?>

<xsl:stylesheet version="1.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:template match="changes">
    <xsl:variable name="file" select="concat('../', @file)"/>
    <xsl:variable name="version" select="@version"/>
    <xsl:apply-templates select="document($file)/changes/release[attribute::version=string($version)]"/>
  </xsl:template>
</xsl:stylesheet>
