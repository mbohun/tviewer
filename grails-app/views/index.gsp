<%@ page import="org.codehaus.groovy.grails.commons.ConfigurationHolder" %>
<html>
    <head>
        <title>Visual explorer prototype</title>
        <meta name="layout" content="ala2" />
        %{--<link rel="stylesheet" href="${resource(dir:'css',file:'main.css')}" />--}%
        <style type="text/css" media="screen">

        #nav {
            margin:20px 35px 0 30px;
            width:228px;
            float:right;
            text-align: left;
        }
        .homePagePanel * {
            margin: 0;
        }
        .homePagePanel .panelBody ul {
            list-style-type:none;
            margin-bottom:10px;
            margin-left:20px;
            padding-bottom: 0;
        }
        .homePagePanel .panelBody li {
            line-height: normal;
        }
        .homePagePanel .panelBody h1 {
            text-transform:uppercase;
            margin-bottom:10px;
            padding-bottom: 0;
            font: 11px verdana, arial, helvetica, sans-serif;
            color: #48802c;
            font-weight: normal;
        }
        .homePagePanel .panelBody {
            background: url(images/leftnav_midstretch.png) repeat-y top;
            margin:0;
            padding:15px;
        }
        .homePagePanel .panelBtm {
            background: url(images/leftnav_btm.png) no-repeat top;
            height:20px;
            margin:0;
        }

        .homePagePanel .panelTop {
            background: url(images/leftnav_top.png) no-repeat top;
            height:11px;
            margin:0;
        }
        h2 {
            margin-top:15px;
            margin-bottom:15px;
            font-size:1.2em;
        }
        #pageBody {
            margin: 40px 20px 0 40px;
            text-align: left;
            height: 500px;
        }
        #pageBody h1 {
            color: #48802c;
            font-weight: normal;
            margin: .8em 0 .3em 0;
            font: 15px verdana, arial, helvetica, sans-serif;
        }
        </style>
    </head>
    <body>
        <div id="nav">
            <div class="homePagePanel">
                <div class="panelTop"></div>
                <div class="panelBody">
                    <h1>Application Status</h1>
                    <ul>
                        <li>App version: <g:meta name="app.version"></g:meta></li>
                        <li>Grails version: <g:meta name="app.grails.version"></g:meta></li>
                        <li>Groovy version: ${org.codehaus.groovy.runtime.InvokerHelper.getVersion()}</li>
                        <li>JVM version: ${System.getProperty('java.version')}</li>
                        <li>Controllers: ${grailsApplication.controllerClasses.size()}</li>
                        <li>Domains: ${grailsApplication.domainClasses.size()}</li>
                        <li>Services: ${grailsApplication.serviceClasses.size()}</li>
                        <li>Tag Libraries: ${grailsApplication.tagLibClasses.size()}</li>
                    </ul>
                    <h1>Installed Plugins</h1>
                    <ul>
                        <g:set var="pluginManager"
                               value="${applicationContext.getBean('pluginManager')}"></g:set>

                        <g:each var="plugin" in="${pluginManager.allPlugins}">
                            <li>${plugin.name} - ${plugin.version}</li>
                        </g:each>

                    </ul>
                </div>
                <div class="panelBtm"></div>
            </div>
        </div>
        <div id="pageBody">
            <h1>This is a prototype for a visual explorer of species profiles and occurrence
            records.</h1>
            <p>This is based on the specifications for a fish finder but is implemented as
            a generic tool to explore records and taxa profiles for all lifeforms.</p>
            <p>The prototype is currently using pre-selected data. Biocache and bie web services
            are currently being refactored to improve efficiency for the required searches.
            The prototype will work against data in real-time once these services have been
            deployed.</p>
            <p>Only the viewing component of the fish tool has been prototyped at this time.</p>
            <p>No functionality or styles are fixed at this stage.</p>
            <p>The following links will give some idea of the look and feel of the viewer at
            this stage of prototyping:</p>
            <ul>
                <li><a href="${ConfigurationHolder.config.grails.serverURL}/taxon/list?search=1">
                    Sample search 1 - has genera for all families</a></li>
                <li><a href="${ConfigurationHolder.config.grails.serverURL}/taxon/list?search=2">
                    Sample search 2 - 8 families</a></li>
                <li><a href="${ConfigurationHolder.config.grails.serverURL}/taxon/list?search=3">
                    Sample search 3 - shows pagination</a></li>
                <li><a href="${ConfigurationHolder.config.grails.serverURL}/taxon/species?taxa=Lamnidae">
                    Species search - sample species list</a> - can be accessed by checking Lamnidae on any
                    page and clicking 'Show species list for checked families'</li>
            </ul>
        </div>
    </body>
</html>
