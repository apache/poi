<?xml version="1.0"?>

<xsl:stylesheet
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    version="1.0">

 <xsl:import href="copyover.xsl"/>

  <xsl:template match="faqs">
   <document>
    <header>
     <title><xsl:value-of select="@title"/></title>
    </header>
    <body>
      <s1 title="Questions">
       <ul>
        <xsl:apply-templates select="faq" mode="index"/>
       </ul>
      </s1>
      <s1 title="Answers">
        <xsl:apply-templates select="faq"/>
      </s1>
    </body>
   </document>  
  </xsl:template>

  <xsl:template match="faq" mode="index">
    <li>
      <jump anchor="faq-{position()}">
        <xsl:value-of select="question"/>
      </jump>
    </li>
  </xsl:template>

  <xsl:template match="faq">
    <anchor id="faq-{position()}"/>
    <s2 title="{question}">
      <xsl:apply-templates/>
    </s2>
  </xsl:template>

  <xsl:template match="question">
    <!-- ignored since already used -->
  </xsl:template>

  <xsl:template match="answer">
    <xsl:apply-templates/>
  </xsl:template>

</xsl:stylesheet>