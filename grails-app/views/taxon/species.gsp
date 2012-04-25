<%@ page import="org.codehaus.groovy.grails.commons.ConfigurationHolder" contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>Taxon list</title>
    <meta name="layout" content="ala2"/>
    <link rel="stylesheet" type="text/css" media="screen" href="${resource(dir:'css',file:'tview.css')}" />
    <link rel="stylesheet" type="text/css" media="screen" href="${resource(dir:'css',file:'colorbox.css')}" />
    <g:javascript library="jquery.ba-bbq.min"/>
    <g:javascript library="jquery.colorbox-min"/>
    <g:javascript library="tviewer"/>
</head>
<body class="landing">
<header id="page-header">
    <div class="inner">
        <nav id="breadcrumb"><ol>
            <li><a href="${ConfigurationHolder.config.distribution.search.baseUrl}">Home</a></li>
            <li><a href="${searchPage}">Search</a></li>
            <li class="last"><i>results</i></li></ol>
        </nav>
        <hgroup>
            <h1>Visual explorer - species list</h1>
            <h2>Query: ${queryDescription ?: 'Australia'}</h2>
            <p>Click on images to view at full size.</p>
        </hgroup>
    </div>
</header>
<div class="inner">
    <div id="controls">
        <label for="sortBy">Sort by:</label>
        <g:select from="[[text:'Family/genus/spp',id:'taxa'],[text:'Scientific name',id:'name'],[text:'Common name',id:'common'],[text:'CAAB code',id:'caabCode']]"
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
        <tr><th></th><th>Scientific name<br/><span style="font-weight: normal;">Family<br/>Common name<br/>CAAB code
            <a href="http://www.marine.csiro.au/caab/" class="external">more info</a></span></th>
            <th style="text-align: center;vertical-align: middle;">Representative image</th>
            <th>Distribution</th></tr>
        </thead>
        <tbody>
        <g:each in="${list}" var="i">
            <tr>
                <!-- checkbox -->
                <td><input type="checkbox" id="${i.spcode}"  alt="${i.guid}"/></td>
                <!-- name -->
                <td><em><a href="${ConfigurationHolder.config.bie.baseURL}/species/${i.name}" title="Show ${rank} page">${i.name}</a></em>
                <!-- common -->
                <g:if test="${i.common && i.common.toString() != 'null'}">
                    <div class="common">${i.common}</div>
                </g:if>
                <!-- family -->
                <div>Family: ${i.family}</div>
                <!-- CAAB code -->
                <g:if test="${i.caabCode}">
                    <div>CAAB: <a href="http://www.marine.csiro.au/caabsearch/caab_search.family_listing?ctg=${i.caabCode[0..1]}&fcde=${i.caabCode[4..5]}"
                            class="external" title="Lookup CAAB code">${i.caabCode}</a></div>
                </g:if></td>
                <!-- image -->
                <td class="mainImage"><g:if test="${i.image?.largeImageUrl}">
                    <a rel="list" class="imageContainer" href="#${i.name.replace(' ','_')}-popup">
                        <img class="list" src="${i.image.largeImageUrl}" alt title="Click to view full size"/>
                    </a>
                    <div style="display: none">
                        <div class="popupContent" id="${i.name.replace(' ','_')}-popup">
                            <img src="${i.image.largeImageUrl}" alt />
                            <details open="open" data-mdurl="${i.image.imageMetadataUrl}">
                                <summary id="${i.name.replace(' ','_')}-summary"><strong><em>${i.name}</em></strong></summary>
                                <dl>
                                    <dt>Image by</dt><dd class="creator">${i.image?.creator}</dd>
                                    <dt>License</dt><dd class="license">${i.image?.license}</dd>
                                    <dt>Rights</dt><dd class="rights">${i.image?.rights}</dd>
                                </dl>
                            </details>
                        </div>
                    </div>
                </g:if></td>
                <!-- distribution -->
                <td><g:if test="${i.gidx}">
                    <a rel="dist" class="distributionImageContainer" href="#${i.name.replace(' ','_')}-dist">
                        <img class="dist" src="${ConfigurationHolder.config.distribution.image.cache}/dist${i.gidx}.png"
                             alt title="Click for larger view"/>
                    </a>
                    <div style="display: none">
                        <div class="popupContent" id="${i.name.replace(' ','_')}-dist">
                            <img src="${ConfigurationHolder.config.distribution.image.cache}/dist${i.gidx}.png" alt width="400" height="400"/>
                            <details open="open" style="padding-bottom: 10px;">
                                <summary id="${i.name.replace(' ','_')}-distsummary"><strong><em>${i.name}</em></strong></summary>
                            </details>
                        </div>
                    </div>
                </g:if></td>
            </tr>
        </g:each>
        </tbody>
    </table>
    <section id="pagination">
        <tv:paginate start="${start}" pageSize="${pageSize}" total="${total}"
                     params="${[taxa:taxa,key:key,sortBy:sortBy,sortOrder:sortOrder]}"/>
        <p>
            Total <tv:pluraliseRank rank="species"/>: ${total}
            <span class="link" id="speciesData">Show data table for checked <tv:pluraliseRank rank="${rank}"/></span>
            <g:link style="padding-left:20px;" action="view" params="[key: key]">Show all results by family</g:link>
            <g:link style="padding-left:20px;" action="data" params="[key: key]">Show data table for all species</g:link><br/>
            <button id="selectAll" type="button">Select all</button>
            <button id="clearAll" type="button">Clear all</button>
        </p>
    </section>
</div>
<script type="text/javascript">
    $(document).ready(function () {
        // wire link to species data
        $('#speciesData').click(function () {
            // collect the selected ranks
            var checked = "";
            $('input[type="checkbox"]:checked').each(function () {
                checked += (checked === "" ? '' : ',') + $(this).attr('id');
            });
            if (checked === "") {
                alert("No species selected");
            }
            else {
                document.location.href = "${ConfigurationHolder.config.grails.serverURL}" +
                        "/taxon/data?taxa=" + checked + "&key=${key}";
            }
        });
        tviewer.init("${ConfigurationHolder.config.grails.serverURL}");
    });
</script>
</body>
</html>