<?xml version="1.0"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0">

  <xsl:template match="book">
    <body>
      <title><xsl:value-of select="title"/></title>
            <table class="centered" align="center" width="100%">
              <tbody>
                <tr>
                  <td align="center">
                    <table class="title" cellspacing="0" cellpadding="1" border="0">
                      <tbody>
                        <tr>
                          <td bgcolor="#525d76">
                            <table class="centered" cellspacing="0" cellpadding="2" border="0" width="100%">
                              <tbody>
                                <tr>
                                  <td bgcolor="#f3dd61">
                                    <span class="title"><xsl:value-of select="title"/><xsl:if test="subtitle"><xsl:text>: </xsl:text><xsl:value-of select="subtitle"/></xsl:if></span>
                                  </td>
                                </tr>
                              </tbody>
                            </table>
                          </td>
                        </tr>
                      </tbody>
                    </table>
                  </td>
                </tr>
              </tbody>
            </table>
      <xsl:apply-templates select="dedication"/>
      <xsl:apply-templates select="bookinfo"/>
    </body>
  </xsl:template>

  <xsl:template match="chapter|article|appendix">
    <body>
    <xsl:if test="normalize-space(title)!=''">
      <title><xsl:value-of select="title"/></title>
            <table class="centered" align="center" width="100%">
              <tbody>
                <tr>
                  <td align="center">
                    <table class="title" cellspacing="0" cellpadding="1" border="0">
                      <tbody>
                        <tr>
                          <td bgcolor="#525d76">
                            <table class="centered" cellspacing="0" cellpadding="2" border="0" width="100%">
                              <tbody>
                                <tr>
                                  <td bgcolor="#f3dd61">
                                    <span class="title"><xsl:value-of select="title"/></span>
                                  </td>
                                </tr>
                              </tbody>
                            </table>
                          </td>
                        </tr>
                      </tbody>
                    </table>
                  </td>
                </tr>
              </tbody>
            </table>
      </xsl:if>
      
      <xsl:if test="subtitle">
        <font color="#525D76"><i><xsl:value-of select="subtitle"/></i></font><br/>
      </xsl:if>

      <font color="#000000" size="-2">
        <p>
          <xsl:for-each select="//authorgroup/author">
            <xsl:choose>
              <xsl:when test="position()=1">by</xsl:when>
              <xsl:otherwise>, </xsl:otherwise>
            </xsl:choose>
          </xsl:for-each>
          <a href="mailto:{address/email}">
            <xsl:if test="honorific"><xsl:value-of select="honorific"/>. </xsl:if>
            <xsl:if test="firstname"><xsl:value-of select="firstname"/> </xsl:if>
            <xsl:value-of select="surname"/>
          </a>
        </p>
      </font>

      
        <xsl:apply-templates select="para"/>


      <xsl:apply-templates select="section">
        <xsl:with-param name="level" select="1"/>
      </xsl:apply-templates>

      <xsl:apply-templates select="//footnote" mode="base"/>
    </body>
  </xsl:template>

  <xsl:template match="title|subtitle"/>

  <xsl:template match="author">
    <body>
      <title>
        <xsl:value-of select="honorific"/><xsl:text>. </xsl:text>
        <xsl:value-of select="firstname"/><xsl:text> </xsl:text>
        <xsl:value-of select="surname"/>
      </title>
      <center>
        <table width="80%">
          <tr>
            <td bgcolor="#F3DD61">
              <br/><center><b><xsl:text>Author: </xsl:text><xsl:value-of select="honorific"/><xsl:text>. </xsl:text><xsl:value-of select="firstname"/><xsl:text> </xsl:text><xsl:value-of select="surname"/></b></center>
              <br/>
            </td>
          </tr>
        </table>
      </center><br/>
      <div align="right">
        <table border="0" cellpadding="2" cellspacing="0" width="100%">
          <tr>
            <td bgcolor="#525D76">
              <font color="#ffffff" size="+1"><b>Affiliations</b></font>
            </td>
          </tr>
          <tr>
            <td>
              <br/>
                <ul>
                  <xsl:apply-templates select="affiliation"/>
                </ul>
            
            </td>
          </tr>
        </table>
      </div><br/>
      <xsl:apply-templates select="authorblurb"/>
    </body>
  </xsl:template>

  <xsl:template match="affiliation">
    <li>
      <xsl:text>[</xsl:text><xsl:value-of select="shortaffil"/><xsl:text>] </xsl:text>
      <b><xsl:value-of select="jobtitle"/></b>
      <i><xsl:value-of select="orgname"/><xsl:if test="orgdiv"><xsl:text>/</xsl:text><xsl:value-of select="orgdiv"/></xsl:if></i>
    </li>
  </xsl:template>

  <xsl:template match="authorblurb">
    <div align="right">
      <table border="0" cellpadding="2" cellspacing="0" width="100%">
        <tr>
          <td bgcolor="#525D76">
            <font color="#ffffff" size="+1"><b>Bio</b></font>
          </td>
        </tr>
        <tr>
          <td>
            <br/>
              <ul>
                <xsl:apply-templates/>
              </ul>
            
          </td>
        </tr>
      </table>
    </div>
  </xsl:template>

  <xsl:template match="honorific|firstname|surname|orgdiv|orgname|shortaffil|jobtitle"/>

  <xsl:template match="revhistory">
    <body>
      <title>Revision History</title>
      <center>
        <table width="80%">
          <tr>
            <td bgcolor="#F3DD61">
              <br/><center><b>Revision History</b></center>
              <br/>
            </td>
          </tr>
        </table>
      </center><br/>
      <div align="right">
        <table border="0" cellpadding="2" cellspacing="0" width="100%">
          <xsl:variable name="unique-revisions" 
            select="revision[not(revnumber=preceding-sibling::revision/revnumber)]/revnumber"/>
          <xsl:variable name="base" select="."/>
          <xsl:for-each select="$unique-revisions">
          <tr>
            <td bgcolor="#525D76">
              <font color="#ffffff">
                <b>Revision <xsl:value-of select="."/> 
                   (<xsl:value-of select="$base/revision[revnumber=current()]/date"/>)
                </b>
              </font>
            </td>
          </tr>
          <tr>
            <td>
              <br/>
                <ul>
                  <xsl:apply-templates select="$base/revision[revnumber=current()]"/>
                </ul>
              
            </td>
          </tr>
          </xsl:for-each>
        </table>
      </div>
    </body>
  </xsl:template>

  <xsl:template match="para">
    <p align="justify"><xsl:apply-templates/></p>
  </xsl:template>

  <xsl:template match="emphasis"><em><xsl:apply-templates/></em></xsl:template>

  <xsl:template match="revision">
    <li>
      <xsl:choose>
        <xsl:when test="@revisionflag='added'">
          <img align="absmiddle" alt="added" border="0" src="images/add.jpg"/>
        </xsl:when>
        <xsl:when test="@revisionflag='changed'">
          <img align="absmiddle" alt="changed" border="0" src="images/update.jpg"/>
        </xsl:when>
        <xsl:when test="@revisionflag='deleted'">
          <img align="absmiddle" alt="deleted" border="0" src="images/remove.jpg"/>
        </xsl:when>
        <xsl:when test="@revisionflag='off'">
          <img align="absmiddle" alt="off" border="0" src="images/fix.jpg"/>
        </xsl:when>
        <xsl:otherwise>
          <img align="absmiddle" alt="changed" border="0" src="images/update.jpg"/>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:value-of select="revremark"/>
      <xsl:text> (</xsl:text><xsl:value-of select="authorinitials"/><xsl:text>)</xsl:text>
    </li>
  </xsl:template>

  <xsl:template match="revnumber|revremark|authorinitials|date"/>

  <xsl:template match="section">
    <xsl:param name="level"/>

    <div align="right">
      <table border="0" cellpadding="2" cellspacing="0">
        <xsl:attribute name="width"><xsl:value-of select="number(100)-(1*(number($level)-1))"/>%</xsl:attribute>
        <tr>
          <td bgcolor="#525D76">
            <font color="#ffffff">
              <xsl:attribute name="size">
                <xsl:choose>
                  <xsl:when test="number($level)=1">+1</xsl:when>
                  <xsl:when test="number($level)=2">+0</xsl:when>
                  <xsl:otherwise>-<xsl:value-of select="number($level)-2"/></xsl:otherwise>
                </xsl:choose>
              </xsl:attribute>
              <font face="Arial,sans-serif"><b><xsl:value-of select="title"/></b></font>
            </font>
          </td>
        </tr>
        <tr>
          <td>
              <br/>
              <xsl:apply-templates>
                <xsl:with-param name="level" select="number($level)+1"/>
              </xsl:apply-templates>
          </td>
        </tr>
      </table>
    </div><br/>
  </xsl:template>

  <xsl:template match="bookinfo">
    <div align="right">
      <table border="0" cellpadding="2" cellspacing="0" width="100%">
        <tr>
          <td bgcolor="#525D76">
            <font color="#ffffff" size="+1">
              <b><xsl:value-of select="edition"/></b><xsl:text> </xsl:text>
              <i><font size="0">pub. <xsl:value-of select="pubdate"/></font></i>
            </font>
          </td>
        </tr>
        <tr>
          <td>
              <br/>
              <xsl:apply-templates/>
          </td>
        </tr>
      </table>
    </div><br/>
  </xsl:template>

  <xsl:template match="dedication">
    <div align="right">
      <table border="0" cellpadding="2" cellspacing="0" width="100%">
        <tr>
          <td bgcolor="#525D76">
            <font color="#ffffff" size="+1">
              <b>Dedication</b>
            </font>
          </td>
        </tr>
        <tr>
          <td>
              <br/>
              <xsl:apply-templates/>
          </td>
        </tr>
      </table>
    </div><br/>
  </xsl:template>

  <xsl:template match="edition|pubdate|year|holder"/>

  <xsl:template match="copyright">
    <p>Copyright &#x00A9;<xsl:value-of select="year"/> by <xsl:value-of select="holder"/>.<br/>
      <i>All rights reserved.</i>
    </p>
  </xsl:template>

  <xsl:template match="legalnotice">
    <div align="center">
      <table border="1" cellpadding="2" cellspacing="2">
        <tr>
          <td><xsl:apply-templates/></td>
        </tr>
      </table>
    </div>
  </xsl:template>

  <xsl:template match="programlisting">
    <div align="center">
      <table border="1" cellpadding="2" cellspacing="2">
        <tr>
          <td>
            <pre>
              <xsl:apply-templates/>
            </pre>
          </td>
        </tr>
      </table>
    </div>
  </xsl:template>

  <xsl:template match="orderedlist"><ol><xsl:apply-templates/></ol></xsl:template>

  <xsl:template match="listitem"><li><xsl:apply-templates/></li></xsl:template>

  <xsl:template match="itemizedlist"><ul><xsl:apply-templates/></ul></xsl:template>

  <xsl:template match="classname|function|parameter"><code><xsl:apply-templates/><xsl:if test="name(.)='function'"><xsl:text>()</xsl:text></xsl:if></code></xsl:template>

  <xsl:template match="blockquote">
    <div align="center">
      <table border="1" cellpadding="2" cellspacing="2">
        <xsl:if test="title">
          <tr>
            <td bgcolor="#525D76">
              <font color="#ffffff"><xsl:value-of select="title"/></font>
            </td>
          </tr>
        </xsl:if>
        <tr>
          <td bgcolor="#c0c0c0">
            <font color="#023264" size="-1"><xsl:apply-templates/></font>
          </td>
        </tr>
      </table>
    </div>
  </xsl:template>

  <xsl:template match="warning">
    <div align="center">
      <table border="1" cellpadding="2" cellspacing="2">
        <xsl:if test="title">
          <tr>
            <td bgcolor="#800000">
              <font color="#ffffff"><xsl:value-of select="title"/></font>
            </td>
          </tr>
        </xsl:if>
        <tr>
          <td bgcolor="#c0c0c0">
            <font color="#023264" size="-1"><xsl:apply-templates/></font>
          </td>
        </tr>
      </table>
    </div>
  </xsl:template>

  <xsl:template match="ulink"><a href="{@uri}"><xsl:apply-templates/></a></xsl:template>

  <xsl:template match="footnote"><sup><a href="#{generate-id(.)}"><xsl:value-of select="generate-id(.)"/></a></sup></xsl:template>

  <xsl:template match="footnote" mode="base">
    <div align="left">
      <a name="{generate-id(.)}"/><font size="-2"><xsl:value-of select="generate-id(.)"/><xsl:text>) </xsl:text><i><xsl:value-of select="."/></i></font>
    </div>
  </xsl:template>

  <xsl:template match="figure">
    <div align="center">
      <table border="0" cellpadding="2" cellspacing="2">
        <tr>
          <td bgcolor="#525D76"><font color="#ffffff" size="0"><xsl:value-of select="title"/></font></td>
        </tr>
        <xsl:apply-templates/>
      </table>
    </div>
  </xsl:template>

  <xsl:template match="graphic">
    <tr>
      <td><img border="0" alt="{@srccredit}" src="{@fileref}"/></td>
    </tr>
    <xsl:if test="@srccredit">
      <tr>
        <td><font size="-1"><ul><li><xsl:value-of select="@srccredit"/></li></ul></font></td>
      </tr>
    </xsl:if>
  </xsl:template>

  <xsl:template match="table">
    <table border="0" cellpadding="2" cellspacing="2" width="100%">
      <xsl:apply-templates/>
    </table>
  </xsl:template>

  <xsl:template match="tgroup">
    <xsl:apply-templates select="thead|tbody|tfoot"/>
  </xsl:template>

  <xsl:template match="thead">
    <xsl:apply-templates select="row" mode="head"/>
  </xsl:template>

  <xsl:template match="row" mode="head">
    <th><xsl:apply-templates/></th>
  </xsl:template>

  <xsl:template match="row">
    <tr><xsl:apply-templates/></tr>
  </xsl:template>

  <xsl:template match="tbody|tfoot">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="entry">
    <td align="left" bgcolor="#a0ddf0" valign="top">
      <font color="#000000" size="-1"><xsl:apply-templates/></font>
    </td>
  </xsl:template>

  <xsl:template match="trademark"><xsl:apply-templates/><sup>TM</sup></xsl:template>

  <xsl:template match="node()|@*" priority="-1">
    <xsl:copy>
      <xsl:apply-templates select="node()|@*"/>
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>

