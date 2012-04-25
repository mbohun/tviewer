/**
 * Created by IntelliJ IDEA.
 * User: markew
 * Date: 13/01/12
 * Time: 10:59 AM
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
    params.sortBy = sortBy;

    url = $.param.querystring("", params);
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

        // wire selection of all checkboxes
        $('#selectAll').click(function () {
            $('input[type="checkbox"]').attr('checked','checked');
        });

        // wire clearing of all checkboxes
        $('#clearAll').click(function () {
            $('input[type="checkbox"]').removeAttr('checked');
        });

        // handle change to search controls
        $('#controls select').change(function () {
            document.location.href = buildUrl();
        });

        // wire lightbox for images
        $('.imageContainer,.distributionImageContainer').colorbox({
            opacity: 0.5,
            inline: true,
            onLoad:function () {
                var $popup = $(this.hash),
                    mdUrl = $popup.find('details').data('mdurl');

                if (mdUrl) {
                    // add 'loading..' status
                    $popup.find('dd').html('loading..');
                    // load and inject metadata
                    that.injectImageMetadata(mdUrl, this);
                }
            }
        });
        // change main image on mouseover of genera images
        $('img.thumb').on('mouseenter', function () {
            var $mainImageTd = $(this).closest('table.genera').parent().prev(),
                    $genusTd = $(this).parent().parent(),
                    $mainImage = $mainImageTd.find('a.imageContainer img'),
                    $popupContent = $mainImageTd.find('div.popupContent'),
                    $popImage = $popupContent.find('img'),
                    newImageSrc = $(this).attr('src'),
                    mdUrl = $genusTd.find('details').data('mdurl');

            // change the image src
            $mainImage.attr('src',newImageSrc);
            // change the popup img
            $popImage.attr('src',newImageSrc);
            // change the metadata url
            $popupContent.find('details').data('mdurl', mdUrl);
            that.injectImageMetadata(mdUrl, $(this).parent());
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

