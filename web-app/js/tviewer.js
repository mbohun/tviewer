/**
 * Created by IntelliJ IDEA.
 * User: markew
 * Date: 13/01/12
 * Time: 10:59 AM
 * To change this template use File | Settings | File Templates.
 */
function buildUrl() {
    var params = $.deparam.querystring(true),
            sortBy = $('#sortBy').val(),
            sortOrder = $('#sortOrder').val(),
            pageSize = $('#perPage').val(),
            url;
    if (sortOrder === 'reverse') {
        params.sortOrder = 'reverse';
    }
    else if (params.sortOrder !== undefined) {
        delete params.sortOrder;
    }
    if (pageSize == '10') {
        delete params.pageSize;
    }
    else {
        params.pageSize = pageSize;
    }
    if (sortBy === 'name') {
        delete params.sortBy;
    }
    else {
        params.sortBy = sortBy;
    }
    url = $.param.querystring("", params);
    if ($.bbq.getState('showGenera') === 'true') {
        url = $.param.fragment(url, 'showGenera=true');
    }
    return url;
}
var tviewer = {
    serverUrl: null,
    init: function (serverUrl) {
        this.serverUrl = serverUrl;

        var that = this,
            params = $.deparam.querystring(true);

        // set search controls based on url params - !this must be done before binding events
        if (params.pageSize !== undefined) {
            $('#perPage').val(params.pageSize);
        }
        if (params.sortBy !== undefined) {
            $('#sortBy').val(params.sortBy);
        }
        if (params.sortOrder !== undefined) {
            $('#sortOrder').val(params.sortOrder);
        }

        // select all checkboxes
        $('#selectAll').click(function () {
            $('input[type="checkbox"]').attr('checked','checked');
        });

        // clear all checkboxes
        $('#clearAll').click(function () {
            $('input[type="checkbox"]').removeAttr('checked');
        });

        // handle change to search controls
        $('#controls select').change(function () {
            document.location.href = buildUrl();
        });

        // wire lightbox for images
        $('.imageContainer').colorbox({
            rel: 'list',
            opacity: 0.5,
            inline: true,
            onLoad:function () {
                var $popup = $(this.hash),
                    mdUrl = $popup.find('details').data('mdurl'),
                    $title = $popup.find('span.title');

                if ($title[0].innerText.trim() === "") {
                    // add 'loading..' status
                    $popup.find('dd').html('loading..');
                    // load and inject metadata
                    that.injectImageMetadata(mdUrl, this);
                }
            }
        });
    },
    // asynchronous loading of image metadata
    injectImageMetadata: function (mdUrl, box) {
        $.getJSON(this.serverUrl + "/taxon/imageMetadataLookup", {url: mdUrl }, function (data) {
            $(box.hash).find('dd.creator').html(data["http://purl.org/dc/elements/1.1/creator"]);
            $(box.hash).find('dd.license').html(data["http://purl.org/dc/elements/1.1/license"]);
            $(box.hash).find('dd.rights').html(data["http://purl.org/dc/elements/1.1/rights"]);
            $(box).colorbox.resize();
        });
    }
};

