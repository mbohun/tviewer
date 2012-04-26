package au.org.ala.tviewer

class TviewerTagLib {
    static namespace = "tv"

    /**
     * Builds the unordered list that implements the appropriate pagination links
     * for the supplied values.
     * @attr total number of items
     * @attr pageSize items per page
     * @attr start the first item to display
     * @attr params other url params to be added if they have a value
     */
    def paginate = { attrs ->
        //println attrs.params
        int total = attrs.total as int
        int pageSize = attrs.pageSize as int ?: 10
        def pageSizeParameter = pageSize == 10 ? "" : "&pageSize=${pageSize}"
        int start = attrs.start as int ?: 0
        def otherParams = ""
        attrs.params.each { k,v ->
            if (v) { otherParams += "&${k}=${v}" }
        }
        //println "from ${start} to ${total} step ${pageSize}"
        if (total > pageSize) {
            out << "<ul>"
            if (start == 0) {
                out << "<li id='prevPage'>« Previous</li>"
            }
            else {
                out << "<li id='prevPage'><a href='?start=${start-pageSize}${pageSizeParameter}${otherParams}'>« Previous</a></li>"
            }
            (0..total - 1).step(pageSize, {
                int page = it == 0 ? 0 : it/pageSize
                if (it == start) {
                    out << "<li class='currentPage'>${page}</li>"
                }
                else {
                    out << "<li><a href='?start=${it}${pageSizeParameter}${otherParams}'>${page}</a></li>"
                }
            })
            if (start + pageSize >= total) {
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
            if (attrs.val.equals(null)) {
                return ""
            }
            return body()
        }
    }
}
