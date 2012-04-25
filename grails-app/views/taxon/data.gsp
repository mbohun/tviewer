<%@ page import="org.codehaus.groovy.grails.commons.ConfigurationHolder" contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>Species data</title>
    <meta name="layout" content="ala2"/>
    <link rel="stylesheet" type="text/css" media="screen" href="${resource(dir:'css',file:'tview.css')}" />
    <link rel="stylesheet" type="text/css" media="screen" href="${resource(dir:'css',file:'colorbox.css')}" />
    <g:javascript library="jquery.ba-bbq.min"/>
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
            <h1>Data explorer - species data</h1>
            <h2>Query: ${queryDescription ?: 'Australia'}</h2>
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
    </div>
    <table class="taxonData">
        <colgroup>
            <col id="tdCaabCode">
            <col id="tdFamily">
            <col id="tdSciName">
            <col id="tdCommon">
            <col id="tdGroup">
        </colgroup>
        <thead>
        <tr>
            <th>CAAB Code</th>
            <th>Family</th>
            <th>Scientific name</th>
            <th>Common name</th>
            <th>Fish group</th>
            <th>Min depth</th>
            <th>Max depth</th>
            <th>Primary ecosystem</th>
        </thead>
        <tbody>
        <g:each in="${list}" var="i">
            <tr>
                <!-- caab -->
                <td>${i.caabCode}</td>
                <!-- family -->
                <td>${i.family}</td>
                <!-- name -->
                <td><em><a href="${ConfigurationHolder.config.bie.baseURL}/species/${i.name}" title="Show ${rank} page">${i.name}</a></em></td>
                <!-- common -->
                <td>${i.common}</td>
                <!-- group -->
                <td>${i.group}</td>
                <!-- min depth -->
                <td>${i.minDepth}</td>
                <!-- max depth -->
                <td>${i.maxDepth}</td>
                <!-- ecosystem -->
                <td><tv:displayPrimaryEcosystem codes="${i.primaryEcosystem}"/></td>
            </tr>
        </g:each>
        </tbody>
    </table>
    <section id="pagination">
        <p>
            Total <tv:pluraliseRank rank="species"/>: ${total}
        </p>
    </section>
</div>
<script type="text/javascript">
    $(document).ready(function () {
        tviewer.init("${ConfigurationHolder.config.grails.serverURL}");
    });
</script>
</body>
</html>