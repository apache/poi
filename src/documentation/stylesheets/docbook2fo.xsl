<?xml version="1.0"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                version="1.0">

  <xsl:template match="book">
    <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
   
    <fo:layout-master-set>
        <fo:simple-page-master master-name="title"
                               page-height="11in"
                               page-width="8.5in"
                               margin-top="1in"
                               margin-bottom="1in"
                               margin-left="1.25in"
                               margin-right="1.5in">
          <fo:region-before extent=".5in" region-name="title-header"/>
          <fo:region-body margin-top="1in" margin-bottom="1in"/>
          <fo:region-after extent=".5in" region-name="title-footer"/>
        </fo:simple-page-master>

        <fo:simple-page-master master-name="inside"
                               page-height="11in"
                               page-width="8.5in"
                               margin-top="1in"
                               margin-bottom="1in"
                               margin-left="1.5in"
                               margin-right="1.25in">
          <fo:region-before extent=".5in" region-name="inside-header"/>
          <fo:region-body margin-top="1in" margin-bottom="1in"/>
          <fo:region-after extent=".5in" region-name="inside-footer"/>
        </fo:simple-page-master>

        <fo:simple-page-master master-name="even-page"
                               page-height="11in" 
                               page-width="8.5in"
                               margin-top="1in" 
                               margin-bottom="1in" 
                               margin-left="1.25in" 
                               margin-right="1in">
          <fo:region-before extent=".5in" region-name="even-header"/>
          <fo:region-body margin-top=".5in" margin-bottom=".5in"/>
          <fo:region-after extent=".5in" region-name="even-footer"/>
        </fo:simple-page-master>

        <fo:simple-page-master master-name="odd-page"
                               page-height="11in"
                               page-width="8.5in"
                               margin-top="1in"
                               margin-bottom="1in"
                               margin-left="1in"
                               margin-right="1.25in">
          <fo:region-before extent=".5in" region-name="odd-header"/>
          <fo:region-body margin-top=".5in" margin-bottom=".5in"/>
          <fo:region-after extent=".5in" region-name="odd-footer"/>
        </fo:simple-page-master>

        <fo:page-sequence-master master-name="chapter">
          <fo:repeatable-page-master-alternatives>
            <fo:conditional-page-master-reference master-name="title"
                                                  page-position="first"
                                                  odd-or-even="odd"/>
            <fo:conditional-page-master-reference master-name="odd-page"
                                                  odd-or-even="odd"
                                                  blank-or-not-blank="not-blank"/>
            <fo:conditional-page-master-reference master-name="even-page"
                                                  page-position="last"/>
            <fo:conditional-page-master-reference master-name="even-page"
                                                  odd-or-even="even"/>
          </fo:repeatable-page-master-alternatives>
        </fo:page-sequence-master>
      </fo:layout-master-set>

      <fo:page-sequence master-name="title">
        <xsl:if test="bookinfo/authorgroup">
          <fo:static-content flow-name="title-footer">
            <fo:block font-family="serif"
                      font-size="16pt"
                      font-style="italic"
                      text-align="end">
              <xsl:for-each select="bookinfo/authorgroup/author">
                <xsl:value-of select="firstname"/>
                <xsl:text> </xsl:text>
                <xsl:value-of select="surname"/>
              </xsl:for-each>
            </fo:block>
          </fo:static-content>
        </xsl:if>
        <fo:flow flow-name="xsl-region-body">
          <fo:block font-family="serif"
                    font-size="48pt"
                    font-weight="bold">
            <xsl:value-of select="title"/>
          </fo:block>
          <xsl:if test="subtitle">
            <fo:block font-family="serif"
                      font-size="24pt"
                      border-top-style="solid"
                      border-top-width=".5pt"
                      space-before="12pt"
                      text-align="end">
              <xsl:value-of select="subtitle"/>
            </fo:block>
          </xsl:if>
        </fo:flow>
      </fo:page-sequence>
      <xsl:apply-templates/>
      <xsl:call-template name="authors"/>
    </fo:root>
  </xsl:template>

  <xsl:template match="chapter|article|appendix">
    <fo:page-sequence force-page-count="end-on-even" master-name="chapter">
      <fo:title><xsl:value-of select="title"/></fo:title>
      <fo:static-content flow-name="even-header">
        <fo:block text-align="start"
                  line-height="12pt"
                  font-style="italic"
                  font-family="serif"
                  font-size="10pt"
                  border-after-style="solid"
                  border-after-width=".5pt">
          <xsl:value-of select="/book/title"/>
        </fo:block>
      </fo:static-content>
      <fo:static-content flow-name="even-footer">
        <fo:block text-align="start"
                  line-height="12pt"
                  border-before-style="solid"
                  border-before-width=".5pt"
                  font-family="serif"
                  font-size="10pt"><fo:page-number/></fo:block>
      </fo:static-content>
      <fo:static-content flow-name="odd-header">
        <fo:block text-align="end"
                  line-height="12pt"
                  font-style="italic"
                  font-family="serif"
                  font-size="10pt"
                  border-after-style="solid"
                  border-after-width=".5pt">
          <xsl:value-of select="title"/>
        </fo:block>
      </fo:static-content>
      <fo:static-content flow-name="odd-footer">
        <fo:block text-align="end"
                  line-height="12pt"
                  border-before-style="solid"
                  border-before-width=".5pt"
                  font-family="serif"
                  font-size="10pt"><fo:page-number/></fo:block>
      </fo:static-content>
      <fo:static-content flow-name="title-footer">
        <fo:block text-align="end"
                  line-height="12pt"
                  border-before-style="solid"
                  border-before-width=".5pt"
                  font-family="serif"
                  font-size="10pt"><fo:page-number/></fo:block>
      </fo:static-content>
      <fo:flow flow-name="xsl-region-body">
        <fo:block space-before="2in" font-family="serif" font-size="24pt" font-weight="bold">
          <xsl:value-of select="title"/>
        </fo:block>
        <xsl:if test="subtitle">
          <fo:block font-style="italic"
                    font-family="serif"
                    font-size="18pt"
                    space-after="10pt">
            <xsl:value-of select="subtitle"/>
          </fo:block>
        </xsl:if>
        <xsl:apply-templates/>
        <xsl:apply-templates select="/bookinfo/authorgroup"/>
      </fo:flow>
    </fo:page-sequence>
  </xsl:template>

  <xsl:template match="title|subtitle"/>
  <xsl:template match="honorific|firstname|surname|orgdiv|orgname|shortaffil|jobtitle"/>

  <xsl:template match="bookinfo/revhistory">
    <fo:block font-family="serif" font-size="10pt" font-weight="bold" space-before="10pt">
      Revision History:
    </fo:block>
    <xsl:variable name="unique-revisions" 
                  select="revision[not(revnumber=preceding-sibling::revision/revnumber)]/revnumber"/>
    <xsl:variable name="base" select="."/>
    <xsl:for-each select="$unique-revisions">
      <fo:block font-family="serif"
                font-size="8pt">
        <xsl:value-of select="$base/revision[revnumber=current()]/date"/>: Revision <xsl:value-of select="."/>
      </fo:block>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="book/revhistory">
    <fo:page-sequence force-page-count="end-on-even" master-name="chapter">
      <fo:title>Revision History</fo:title>
      <fo:static-content flow-name="even-header">
        <fo:block text-align="start"
                  line-height="12pt"
                  font-style="italic"
                  font-family="serif"
                  font-size="10pt"
                  border-after-style="solid"
                  border-after-width=".5pt">
          <xsl:value-of select="/book/title"/>
        </fo:block>
      </fo:static-content>
      <fo:static-content flow-name="even-footer">
        <fo:block text-align="start"
                  line-height="12pt"
                  border-before-style="solid"
                  border-before-width=".5pt"
                  font-family="serif"
                  font-size="10pt"><fo:page-number/></fo:block>
      </fo:static-content>
      <fo:static-content flow-name="odd-header">
        <fo:block text-align="end"
                  line-height="12pt"
                  font-style="italic"
                  font-family="serif"
                  font-size="10pt"
                  border-after-style="solid"
                  border-after-width=".5pt">
          <xsl:value-of select="title"/>
        </fo:block>
      </fo:static-content>
      <fo:static-content flow-name="odd-footer">
        <fo:block text-align="end"
                  line-height="12pt"
                  border-before-style="solid"
                  border-before-width=".5pt"
                  font-family="serif"
                  font-size="10pt"><fo:page-number/></fo:block>
      </fo:static-content>
      <fo:static-content flow-name="title-footer">
        <fo:block text-align="end"
                  line-height="12pt"
                  border-before-style="solid"
                  border-before-width=".5pt"
                  font-family="serif"
                  font-size="10pt"><fo:page-number/></fo:block>
      </fo:static-content>
      <fo:flow flow-name="xsl-region-body">
        <fo:block space-before="2in" font-family="serif" font-size="24pt" font-weight="bold" space-after="10pt">
          Revision History
        </fo:block>
        <xsl:variable name="unique-revisions" 
                      select="revision[not(revnumber=preceding-sibling::revision/revnumber)]/revnumber"/>
        <xsl:variable name="base" select="."/>
        <xsl:for-each select="$unique-revisions">
          <fo:block font-weight="bold"
                    font-family="serif"
                    font-size="14pt"
                    space-before="10pt">
            Revision <xsl:value-of select="."/>
            (<xsl:value-of select="$base/revision[revnumber=current()]/date"/>)
          </fo:block>
          <fo:list-block provisional-distance-between-starts="9mm"
                   provisional-label-separation="3mm">
            <xsl:apply-templates select="$base/revision[revnumber=current()]"/>
          </fo:list-block>
        </xsl:for-each>
      </fo:flow>
    </fo:page-sequence>
  </xsl:template>

  <xsl:template match="para">
    <fo:block space-after="8pt"
              font-family="serif"><xsl:apply-templates/></fo:block>
  </xsl:template>

  <xsl:template match="emphasis">
    <fo:inline font-style="italic"><xsl:apply-templates/></fo:inline>
  </xsl:template>

  <xsl:template match="revision">
    <fo:list-item>
      <fo:list-item-label start-indent="3mm" end-indent="label-end()">
        <fo:block>&#x2022;</fo:block>
      </fo:list-item-label>
      <fo:list-item-body start-indent="body-start()">
        <fo:block>
          <xsl:apply-templates/>
          <fo:inline font-variant="small-caps">
            <xsl:value-of select="@revisionflag"/><xsl:text>&#x2014;</xsl:text>
          </fo:inline>
          <xsl:value-of select="revremark"/>
          <xsl:text> (</xsl:text><xsl:value-of select="authorinitials"/><xsl:text>)</xsl:text>
        </fo:block>
      </fo:list-item-body>
    </fo:list-item>
  </xsl:template>

  <xsl:template match="revnumber|revremark|authorinitials|date"/>

  <xsl:template match="section">
    <xsl:param name="level">0</xsl:param>
    <xsl:variable name="size" select="16-(number($level)*2)"/>

    <fo:block font-family="serif"
              font-size="{$size}pt"
              font-weight="bold"
              space-before="12pt">
      <xsl:value-of select="title"/>
    </fo:block>
    <xsl:apply-templates>
      <xsl:with-param name="level" select="number($level)+1"/>
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template match="bookinfo">
    <fo:page-sequence master-name="inside">
      <fo:flow flow-name="xsl-region-body">
        <fo:block font-weight="bold" font-size="14pt">
          <xsl:value-of select="title"/>
        </fo:block>
        <xsl:if test="subtitle">
          <fo:block font-weight="bold" font-size="10pt">
            <xsl:value-of select="subtitle"/>
          </fo:block>
        </xsl:if>
        <xsl:if test="authorgroup">
          <fo:block font-size="10pt">
            <xsl:text>by </xsl:text>
            <xsl:for-each select="authorgroup/author">
              <xsl:if test="not(position()=1)">
                <xsl:text>, </xsl:text>
              </xsl:if>
              <xsl:if test="honorific">
                <xsl:value-of select="honorific"/><xsl:text>. </xsl:text>
              </xsl:if>
              <xsl:value-of select="firstname"/>
              <xsl:text> </xsl:text>
              <xsl:value-of select="surname"/>
            </xsl:for-each>
          </fo:block>
        </xsl:if>
        <xsl:apply-templates select="copyright"/>
        <fo:block space-before="10pt" font-size="10pt">
          <xsl:value-of select="edition"/><xsl:text> published </xsl:text>
          <xsl:value-of select="pubdate"/>
        </fo:block>
        <xsl:apply-templates select="revhistory"/>
        <xsl:apply-templates select="legalnotice"/>
      </fo:flow>
    </fo:page-sequence>
  </xsl:template>


  <xsl:template name="authors">
    <fo:page-sequence force-page-count="end-on-even" master-name="chapter">
      <fo:title>About the Authors</fo:title>
      <fo:static-content flow-name="even-header">
        <fo:block text-align="start"
                  line-height="12pt"
                  font-style="italic"
                  font-family="serif"
                  font-size="10pt"
                  border-after-style="solid"
                  border-after-width=".5pt">
          <xsl:value-of select="/book/title"/>
        </fo:block>
      </fo:static-content>
      <fo:static-content flow-name="even-footer">
        <fo:block text-align="start"
                  line-height="12pt"
                  border-before-style="solid"
                  border-before-width=".5pt"
                  font-family="serif"
                  font-size="10pt"><fo:page-number/></fo:block>
      </fo:static-content>
      <fo:static-content flow-name="odd-header">
        <fo:block text-align="end"
                  line-height="12pt"
                  font-style="italic"
                  font-family="serif"
                  font-size="10pt"
                  border-after-style="solid"
                  border-after-width=".5pt">
          About the Authors
        </fo:block>
      </fo:static-content>
      <fo:static-content flow-name="odd-footer">
        <fo:block text-align="end"
                  line-height="12pt"
                  border-before-style="solid"
                  border-before-width=".5pt"
                  font-family="serif"
                  font-size="10pt"><fo:page-number/></fo:block>
      </fo:static-content>
      <fo:static-content flow-name="title-footer">
        <fo:block text-align="end"
                  line-height="12pt"
                  border-before-style="solid"
                  border-before-width=".5pt"
                  font-family="serif"
                  font-size="10pt"><fo:page-number/></fo:block>
      </fo:static-content>
      <fo:flow flow-name="xsl-region-body">
        <fo:block space-before="2in" font-family="serif" font-size="24pt" font-weight="bold" space-after="10pt">
          About the Authors
        </fo:block>
        <xsl:apply-templates select="/book/bookinfo/authorgroup/author"/>
      </fo:flow>
    </fo:page-sequence>
  </xsl:template>

  <xsl:template match="author">
    <fo:block font-family="serif"
              font-size="16pt"
              font-weight="bold"
              space-before="12pt">
      <xsl:value-of select="honorific"/><xsl:text>. </xsl:text>
      <xsl:value-of select="firstname"/><xsl:text> </xsl:text>
      <xsl:value-of select="surname"/>
    </fo:block>
    <fo:block font-family="serif"
              font-size="14pt"
              font-weight="bold"
              space-before="12pt">
      Affiliations
    </fo:block>
    <fo:list-block provisional-distance-between-starts="9mm"
                   provisional-label-separation="3mm">
      <xsl:apply-templates select="affiliation"/>
    </fo:list-block>
    <xsl:apply-templates select="authorblurb"/>
  </xsl:template>

  <xsl:template match="affiliation">
    <fo:list-item>
      <fo:list-item-label start-indent="50%" end-indent="label-end()">
        <fo:block>&#x2022;</fo:block>
      </fo:list-item-label>
      <fo:list-item-body start-indent="body-start()">
        <fo:block>
          <xsl:text>[</xsl:text><xsl:value-of select="shortaffil"/><xsl:text>] </xsl:text>
          <fo:inline font-weight="bold"><xsl:value-of select="jobtitle"/><xsl:text> </xsl:text></fo:inline>
          <fo:inline font-style="italic">
            <xsl:value-of select="orgname"/>
            <xsl:if test="orgdiv"><xsl:text>/</xsl:text><xsl:value-of select="orgdiv"/></xsl:if>
          </fo:inline>
        </fo:block>
      </fo:list-item-body>
    </fo:list-item>
  </xsl:template>

  <xsl:template match="authorblurb">
    <fo:block font-family="serif"
              font-size="14pt"
              font-weight="bold"
              space-before="12pt">
      Bio
    </fo:block>
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="dedication">
    <fo:page-sequence master-name="title">
      <fo:flow flow-name="xsl-region-body">
        <xsl:for-each select="para">
          <fo:block font-style="italic" font-family="serif" space-before="3in" font-size="10pt" text-align="center">
            <xsl:apply-templates/>
          </fo:block>
        </xsl:for-each>
      </fo:flow>
    </fo:page-sequence>
    <fo:page-sequence master-name="inside">
      <fo:flow flow-name="xsl-region-body">
        <fo:block> </fo:block>
      </fo:flow>
    </fo:page-sequence>
  </xsl:template>

  <xsl:template match="edition|pubdate|year|holder"/>

  <xsl:template match="copyright">
    <fo:block font-size="10pt" space-before="10pt">
      Copyright &#x00A9;<xsl:value-of select="year"/> by <xsl:value-of select="holder"/>.
      All rights reserved.
    </fo:block>
  </xsl:template>

  <xsl:template match="legalnotice">
    <fo:block font-size="8pt"
              text-align="justify"
              space-before="20pt"
              width="7.5in"
              font-family="serif">
      <xsl:apply-templates/>
    </fo:block>
  </xsl:template>

  <xsl:template match="programlisting">
    <fo:block font-family="monospace"
              font-size="10pt"
              background-color="#f0f0f0"
              white-space-collapse="false"
              keep-together="always">
       <xsl:apply-templates/>
    </fo:block>
  </xsl:template>

  <xsl:template match="orderedlist|itemizedlist">
    <fo:list-block provisional-distance-between-starts="9mm"
                   provisional-label-separation="3mm">
      <xsl:apply-templates/>
    </fo:list-block>
  </xsl:template>

  <xsl:template match="orderedlist/listitem">
    <fo:list-item>
      <fo:list-item-label start-indent="50%" end-indent="label-end()">
        <fo:block>
          <xsl:number format="1."/>
        </fo:block>
      </fo:list-item-label>
      <fo:list-item-body start-indent="body-start()">
        <fo:block>
          <xsl:apply-templates/>
        </fo:block>
      </fo:list-item-body>
    </fo:list-item>
  </xsl:template>

  <xsl:template match="itemizedlist/listitem">
    <fo:list-item>
      <fo:list-item-label start-indent="50%" end-indent="label-end()">
        <fo:block>&#x2022;</fo:block>
      </fo:list-item-label>
      <fo:list-item-body start-indent="body-start()">
        <fo:block>
          <xsl:apply-templates/>
        </fo:block>
      </fo:list-item-body>
    </fo:list-item>
  </xsl:template>

  <xsl:template match="classname|function|parameter">
    <fo:inline font-family="monospace">
      <xsl:apply-templates/><xsl:if test="name(.)='function'"><xsl:text>()</xsl:text></xsl:if>
    </fo:inline>
  </xsl:template>

  <xsl:template match="blockquote">
    <fo:block margin-left="1in"
              margin-right="1in"
              font-weight="bold"
              font-size="10pt"
              font-family="serif"
              space-before="10pt"
              border-before-style="solid"
              border-start-style="solid"
              border-end-style="solid"
              background-color="#000000"
              color="#ffffff">
      <xsl:value-of select="title"/>
    </fo:block>
    <fo:block margin-left="1in"
              margin-right="1in"
              font-family="serif"
              font-size="8pt"
              border-after-style="solid"
              border-start-style="solid"
              border-end-style="solid"
              background-color="#f0f0f0"
              padding-start="3pt"
              padding-end="3pt"
              padding-before="3pt"
              padding-after="3pt"
              space-after="20pt">
      <xsl:apply-templates/>
    </fo:block>
  </xsl:template>

  <xsl:template match="warning">
    <fo:block margin-left="1in"
              margin-right="1in"
              font-weight="bold"
              font-size="10pt"
              font-family="serif"
              space-before="10pt"
              border-before-style="solid"
              border-start-style="solid"
              border-end-style="solid"
              background-color="#800000"
              color="#ffffff">
      Warning: <xsl:value-of select="title"/>
    </fo:block>
    <fo:block margin-left="1in"
              margin-right="1in"
              font-family="serif"
              font-size="8pt"
              border-after-style="solid"
              border-start-style="solid"
              border-end-style="solid"
              background-color="#f0f0f0"
              padding-start="3pt"
              padding-end="3pt"
              padding-before="3pt"
              padding-after="3pt">
      <xsl:apply-templates/>
    </fo:block>
  </xsl:template>

  <xsl:template match="ulink">
    <fo:basic-link external-destination="{@uri}"><xsl:apply-templates/></fo:basic-link>
  </xsl:template>

  <xsl:template match="footnote">
    <fo:footnote>
      <fo:inline>
        (see <xsl:value-of select="generate-id()"/> below)
      </fo:inline>
      <fo:footnote-body>
        <fo:block font-family="serif"
                  font-size="8pt"
                  line-height="12pt"
                  font-style="italic">
          <xsl:value-of select="generate-id()"/>) <xsl:value-of select="."/>
        </fo:block>
      </fo:footnote-body>
    </fo:footnote>
  </xsl:template>

  <xsl:template match="figure">
    <fo:block text-align="center" font-weight="bold" font-family="serif" space-before="10pt" space-after="20pt">
      <xsl:value-of select="title"/>
      <xsl:apply-templates/>
    </fo:block>
  </xsl:template>

  <xsl:template match="graphic">
    <fo:external-graphic src="build/documentation/resources/{@fileref}">
      <xsl:attribute name="content-type">
        <xsl:text>content-type:image/</xsl:text>
        <xsl:value-of select="translate(@format,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')"/>
      </xsl:attribute>
    </fo:external-graphic>
    <xsl:if test="@srccredit">
      <fo:block font-size="8pt" font-family="serif" font-style="italic">
        &#x2022; <xsl:value-of select="@srccredit"/>
      </fo:block>
    </xsl:if>
  </xsl:template>

  <xsl:template match="table">
    <fo:table width="100%" table-layout="fixed">
      <xsl:apply-templates/>
    </fo:table>
  </xsl:template>

  <xsl:template match="colspec">
    <fo:table-column>
      <xsl:attribute name="column-number">
        <xsl:number count="colspec"/>
      </xsl:attribute>
      <xsl:attribute name="column-width">
        <xsl:call-template name="calc.column.width">
          <xsl:with-param name="colwidth">
            <xsl:value-of select="@colwidth"/>
          </xsl:with-param>
        </xsl:call-template>
      </xsl:attribute>
    </fo:table-column>
  </xsl:template>

  <xsl:template match="tgroup">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="thead">
    <fo:table-header>
      <xsl:apply-templates/>
    </fo:table-header>
  </xsl:template>

  <xsl:template match="tfoot">
    <fo:table-footer>
      <xsl:apply-templates/>
    </fo:table-footer>
  </xsl:template>

  <xsl:template match="row">
    <fo:table-row><xsl:apply-templates/></fo:table-row>
  </xsl:template>

  <xsl:template match="tbody">
    <fo:table-body>
      <xsl:apply-templates/>
    </fo:table-body>
  </xsl:template>

  <xsl:template match="entry">
    <fo:table-cell>
      <xsl:apply-templates/>
    </fo:table-cell>
  </xsl:template>

  <xsl:template name="calc.column.width">
    <xsl:param name="colwidth">1*</xsl:param>

    <xsl:if test="contains($colwidth, '*')">
      <xsl:text>proportional-column-width(</xsl:text>
      <xsl:value-of select="substring-before($colwidth, '*')"/>
      <xsl:text>)</xsl:text>
    </xsl:if>

    <xsl:variable name="width-units">
      <xsl:choose>
        <xsl:when test="contains($colwidth, '*')">
          <xsl:value-of select="normalize-space(substring-after($colwidth, '*'))"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="normalize-space($colwidth)"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:variable name="width"
                  select="normalize-space(translate($width-units, '+-0123456789.abcdefghijklmnopqrstuvwxyz', '+-0123456789.'))"/>

    <xsl:variable name="units"
                 select="normalize-space(translate($width-units, 'abcdefghijklmnopqrstuvwxyz+-0123456789.', 'abcdefghijklmnopqrstuvwxyz'))"/>

    <xsl:value-of select="$width"/>

    <xsl:choose>
      <xsl:when test="$units='pi'">pc</xsl:when>
      <xsl:when test="$units='' and $width != ''">pt</xsl:when>
      <xsl:otherwise><xsl:value-of select="$units"/></xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="trademark"><xsl:apply-templates/>&#x2122;</xsl:template>

</xsl:stylesheet>

