package au.org.ala.tviewer

import org.codehaus.groovy.grails.web.json.JSONObject

class TviewerTagLib {
    static namespace = "tv"
    static final MAX_PAGE_LINKS = 22

    /**
     * Builds the unordered list that implements the appropriate pagination links
     * for the supplied values.
     *
     * Implements an algorithm to truncate the pagination list if it is too long.
     *
     * @attr total number of items
     * @attr pageSize items per page
     * @attr start the first item to display
     * @attr params other url params to be added if they have a value
     */
    def paginate = { attrs ->
        // total num items
        int total = attrs.total as int
        // num items per page
        int pageSize = attrs.pageSize as int ?: 10
        // the first item to display on this page
        int start = attrs.start as int ?: 0
        // the num of the page being displayed
        int currentPage = start/pageSize
        // the num of pages
        int totalPages = total/pageSize
        // the index of the last page (zero-based)
        int lastPage = totalPages - 1
        // whether we need to abbreviate the list of page links
        boolean abbreviateLinks = totalPages > MAX_PAGE_LINKS
        // in general the abbreviated list starts MAX_PAGE_LINKS/2 from the current page so that the current
        // page is in the centre of the list - however this value increases as we approach the last page (because
        // there are no more pages on the right end to fill the list) so that we maintain the same number
        // of links in the list - simple, yes?
        int startOffset = Math.max(currentPage - (lastPage - MAX_PAGE_LINKS), MAX_PAGE_LINKS/2 as int)
        // the num of the page for the first link - 0 unless we are abbreviating the list
        int startingPageLink = abbreviateLinks ? Math.max(currentPage - startOffset, 0) : 0
        // the num of the page for the last link - total/pageSize unless we are abbreviating the list
        int endingPageLink = abbreviateLinks ? Math.min(startingPageLink + MAX_PAGE_LINKS, lastPage) : lastPage

        // params to include in page links
        def otherParams = ""
        attrs.params.each { k,v ->
            if (v) { otherParams += "&${k}=${v}" }
        }
        // optional pageSize parameter for links
        def pageSizeParameter = pageSize == 10 ? "" : "&pageSize=${pageSize}"

        // closure to write a single page link
        // page numbers are 0-based but the displayed text is 1-based
        def writePageLink = { page ->
            if (page == currentPage) {
                out << "<li class='currentPage'>${page + 1}</li>"
            }
            else {
                out << "<li><a href='?start=${page*pageSize}${pageSizeParameter}${otherParams}'>${page + 1}</a></li>"
            }
        }

        if (total > pageSize) {
            out << "<ul>"

            // « Previous link
            if (start == 0) {
                out << "<li id='prevPage'>« Previous</li>"
            }
            else {
                out << "<li id='prevPage'><a href='?start=${start-pageSize}${pageSizeParameter}${otherParams}'>« Previous</a></li>"
            }

            // first page link (always show even if abbreviating)
            writePageLink 0

            // show ellipsis if we are abbreviating the list at the front
            if (startingPageLink > 0) {
                out << " ... "
            }

            // show the page links btw start and end - bearing in mind the absolute first and
            // last pages are shown separately
            def s = startingPageLink == 0 ? 1 : startingPageLink // skip page 0 as already shown
            def e = endingPageLink == lastPage ? lastPage -1 : endingPageLink // skip last page as will always be shown
            (s..e).each {
                writePageLink it
            }

            // show ellipsis if we are abbreviating the list at the end
            if (endingPageLink < lastPage) {
                out << " ... "
            }

            // last page link (always show even if abbreviating)
            writePageLink lastPage

            // Next » link
            if (currentPage == lastPage) {
                out << "<li id='nextPage'>Next »</li>"
            }
            else {
                out << "<li id='nextPage'><a href='?start=${start+pageSize}${pageSizeParameter}${otherParams}'>Next »</a></li>"
            }

            out << "</ul>"
        }
    }

    /**
     * Provides the plural form of the rank.
     * @attr rank
     */
    def pluraliseRank = { attrs ->
        def plural
        switch(attrs.rank) {
            case 'phylum': plural = 'phyla'; break
            case 'class': plural = 'classes'; break
            case 'family': plural = 'families'; break
            case 'genus': plural = 'genera'; break
            case 'species': plural = 'species'; break
            default: plural = attrs.rank + 's'
        }
        out << plural
    }

    def removeSpaces = { attrs ->
        if (attrs.str) {
            out << attrs.str.tokenize(' ').join()
        }
    }

    def displayPrimaryEcosystem = { attrs ->
        def codes = attrs.codes
        def text = []
        if (codes =~ 'e') {
            text << 'estuarine'
        }
        if (codes =~ 'c') {
            text << 'coastal'
        }
        if (codes =~ 'd') {
            text << 'demersal'
        }
        if (codes =~ 'p') {
            text << 'pelagic'
        }

        out << text.join(', ')
    }
    
    def notNull = { attrs, body ->
        if (attrs.val) {
            if (attrs.val==JSONObject.NULL) {
                return ""
            }
            return body()
        }
    }
}
