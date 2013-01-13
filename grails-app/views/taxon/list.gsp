<%@ page import="org.codehaus.groovy.grails.web.json.JSONObject" contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
  <title>Taxon list</title>
  <meta name="layout" content="ala2"/>
  <link rel="stylesheet" type="text/css" media="screen" href="${resource(dir:'css',file:'tview.css')}" />
  <link rel="stylesheet" type="text/css" media="screen" href="${resource(dir:'css',file:'colorbox.css')}" />
  <r:require module="application"/>
</head>
<body class="family-list">
<header id="page-header">
    <div class="inner">
        <hgroup>
            <h1>Visual explorer - ${rank} list</h1>
            <h2>Query: ${queryDescription ?: 'Australia'}</h2>
            <p>Click on images to view at full size.</p>
        </hgroup>
        <nav id="breadcrumb"><ol>
            <li><a href="${searchPage}">Search</a></li>
            <li class="last"><i>Results by family</i></li></ol>
        </nav>
    </div>
</header>
    <div class="inner">
        <div id="controls">
            <label for="sortBy">Sort by:</label>
            <g:select from="[[text:'Scientific name',id:'name'],[text:'Common name',id:'common'],[text:'CAAB code',id:'caabCode']]"
                      name="sortBy" optionKey="id" optionValue="text"/>
            <label for="sortOrder">Sort order:</label>
            <g:select from="['normal','reverse']" name="sortOrder"/>
            <label for="perPage">Results per page:</label>
            <g:select from="[5,10,20,50,100]" name="perPage" value="10"/>
        </div>
        <table class="taxonList">
            <colgroup>
                <col id="tlCheckbox"> <!-- checkbox -->
                <col id="tlName"> <!-- taxon name -->
                <col id="tlImage"> <!-- image -->
                <col id="tlGenera"> <!-- genera -->
            </colgroup>
            <thead>
            <tr><th></th><th>Family name<br/><span style="font-weight: normal;">Common name<br/>CAAB code
                <a href="http://www.marine.csiro.au/caab/" class="external">more info</a></span></th>
                <th style="text-align: center;vertical-align: middle;">Representative image</th>
                <th>Genera</th></tr>
            </thead>
            <tbody>
            <g:each in="${list}" var="i">
                <tr>
                    <!-- checkbox -->
                    <td><input type="checkbox" id="${i.name}" alt="${i.guid}"/></td>
                    <!-- name -->
                    <td><div class="name"><a href="${grailsApplication.config.bie.baseURL}/species/${i.name}" title="Show ${rank} page">${i.name}</a></div>
                    <!-- common name -->
                    <g:if test="${i.common && i.common.toString() != 'null'}">
                        <div class="common">${i.common}</div>
                    </g:if>
                    <!-- CAAB code -->
                    <g:if test="${i.caabCode}">
                        <div><a href="http://www.marine.csiro.au/caabsearch/caab_search.family_listing?ctg=${i.caabCode.size() > 1 ? i.caabCode[0..1] : ''}&fcde=${i.caabCode.size() > 5 ? i.caabCode[4..5] : ''}"
                                class="external" title="Lookup CAAB code">${i.caabCode}</a></div>
                    </g:if>
                    <!-- image -->
                    <td class="mainImage">
                        <g:set var="largeImageUrl" value="${i.image==JSONObject.NULL ? '' : i.image?.largeImageUrl}"/>
                        <g:set var="imageMetadataUrl" value="${i.image==JSONObject.NULL ? '' : i.image?.imageMetadataUrl}"/>
                        <g:set var="creator" value="${i.image==JSONObject.NULL ? '' : i.image?.creator}"/>
                        <g:set var="license" value="${i.image==JSONObject.NULL ? '' : i.image?.license}"/>
                        <g:set var="rights" value="${i.image==JSONObject.NULL ? '' : i.image?.rights}"/>
                        <a rel="list" class="imageContainer" href="#${i.name}-popup">
                          <img class="list" src="${largeImageUrl}" alt title="Click to view full size"/>
                        </a>
                        <div style="display: none">
                            <div class="popupContent" id="${i.name}-popup">
                                <img src="${largeImageUrl}" alt />
                                <details open="open" data-mdurl="${imageMetadataUrl}">
                                    <summary id="${i.name}-summary">${i.name}
                                    </summary>
                                    <dl>
                                        <dt>Image by</dt><dd class="creator">${creator}</dd>
                                        <dt>License</dt><dd class="license">${license}</dd>
                                        <dt>Rights</dt><dd class="rights">${rights}</dd>
                                    </dl>
                                </details>
                            </div>
                        </div>
                    </td>
                    <!-- genera -->
                    <td>
                        <table class="genera">
                            <g:each in="${i.genera}" var="g" status="count">
                                <g:if test="${count % 4 == 0}">
                                    <tr>
                                </g:if>
                                <td>
                                    <g:if test="${g.image}">
                                        <a rel="${i.name}" class="imageContainer" href="#${g.name}-popup">
                                            <img class="thumb" src="${g.image?.largeImageUrl}"/>
                                        </a>
                                    </g:if>
                                    <g:link action="species" params="[key: key, genus: g.name]"
                                    title="${g.speciesCount} species">${g.name}</g:link>
                                    <g:if test="${g.image}">
                                        <div style="display: none">
                                          <div class="popupContent" id="${g.name}-popup">
                                            <img src="${g.image?.largeImageUrl}" alt />
                                            <details open="open" data-mdurl="${g.image?.imageMetadataUrl}">
                                                <summary id="${g.name}-summary">${g.name}</summary>
                                                <dl>
                                                    <dt>Image by</dt><dd class="creator">${g.image?.creator}</dd>
                                                    <dt>License</dt><dd class="license">${g.image?.license}</dd>
                                                    <dt>Rights</dt><dd class="rights">${g.image?.rights}</dd>
                                                </dl>
                                            </details>
                                          </div>
                                        </div>
                                    </g:if>
                                </td>
                                <g:if test="${count % 4 == 3 || count == i.genera.size()}">
                                    </tr>
                                </g:if>
                            </g:each>
                        </table>
                    </td>
                </tr>
            </g:each>
            </tbody>
        </table>
        <section id="pagination">
            <tv:paginate start="${start}" pageSize="${pageSize}" total="${total}"
                         params="${[key:key,sortBy:sortBy,sortOrder:sortOrder]}"/>
            <p>
                Total <tv:pluraliseRank rank="${rank}"/>: ${total}
                <span class="link" id="speciesList">Show species list for checked <tv:pluraliseRank rank="${rank}"/></span>
                <g:link style="padding-left:20px;" action="species" params="[key: key]">Show all results by species</g:link>
                <g:link style="padding-left:20px;" action="data" params="[key: key]">Show data table for all species</g:link><br/>
                <button id="selectAll" type="button">Select all</button>
                <button id="clearAll" type="button">Clear all</button>
            </p>
        </section>
    </div>
    <r:script>
        var serverUrl = "${grailsApplication.config.grails.serverURL}";

        $(document).ready(function () {
            // wire link to species list
            $('#speciesList,#speciesData').click(function () {
                // collect the selected ranks
                var checked = "", which = 'species';
                $('input[type="checkbox"]:checked').each(function () {
                    checked += (checked === "" ? '' : ',') + $(this).attr('id');
                });
                if (checked === "") {
                    alert("No families selected");
                }
                else {
                    if (this.id === 'speciesData') { which = 'data' }
                    document.location.href = "${grailsApplication.config.grails.serverURL}" +
                    "/taxon/" + which + "?taxa=" + checked + "&key=${key}";
                }
            });
            tviewer.init(serverUrl);
        });
    </r:script>
    <r:layoutResources/>
</body>
</html>