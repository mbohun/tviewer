package au.org.ala.tviewer

import grails.converters.JSON
import org.codehaus.groovy.grails.commons.ConfigurationHolder

class ResultsService {

    def webService

    def getResultsPage(String key, String facets, String includeFacetMembers, boolean includeHierarchy, Map params) {
        def model = [:]
        if (key) {
            def action = "looking up page from results cache"
            def url = ConfigurationHolder.config.results.cache.baseUrl + '/getPage' +
                    "?key=${key}"
            if (facets) { url += "&facets=${facets}" }
            if (includeFacetMembers) { url += "&includeFacetMembers=${includeFacetMembers}" }
            if (includeHierarchy) { url += "&taxonHierarchy=true" }
            ['start','pageSize','sortBy','sortOrder'].each {
                if (params[it]) {
                    url += "&${it}=${params[it]}"
                }
            }
            //println "url = " + url
            def resp = webService.getJson(url)
            if (resp.error) {
                model.error = resp.error
            }
            else {
                model = [key: key, query: resp.query, list: resp.list,
                        facets: resp.facetResults, taxonHierarchy: resp.taxonHierarchy]
            }

/*
            withHttp(uri: grailsApplication.config.results.cache.baseUrl + '/') {
                def query = [key: key, facets: 'family', includeFacetMembers: 'guid']
                ['start','pageSize','sortBy','sortOrder'].each {
                    if (params[it]) {query[it] = params[it]}
                }
                def resp = get(path: 'results/getPage', query: query)
                println resp
                //def data = JSON.parse(resp as String)
                if (resp.error) {
                    model.error = resp.error
                }
                else {
                    model = [key: key, list: resp.list]
                }
            }*/
        }
        else {
            model.error = 'no key passed'
        }

        return model
    }
}
