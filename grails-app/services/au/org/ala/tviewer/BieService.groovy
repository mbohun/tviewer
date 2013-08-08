package au.org.ala.tviewer

import grails.converters.JSON

class BieService {

    def webService, grailsApplication

    def injectGenusMetadata(list) {

        // build a list of genus guids to lookup
        def guids = []
        list.each { fam ->
            fam.genera.each { gen ->
                if (gen.guid) {
                    guids << gen.guid
                }
                if (gen.repSppGuid) {
                    guids << gen.repSppGuid
                }
            }
        }

        // look up the metadata
        def md = betterBulkLookup(guids)

        // inject the metadata
        list.each { fam ->
            fam.genera.each { gen ->
                def genData = md[gen.guid]
                if (genData) {
                    gen.common = genData.common
                }
                else {
                    log.debug "No metadata found for genus ${gen.name} (guid = ${gen.guid})"
                }
                def sppData = md[gen.repSppGuid]
                if (sppData) {
                    if (sppData.image && sppData.image.largeImageUrl?.toString() != "null" &&
                            sppData.image.imageSource == grailsApplication.config.image.source.dataResourceUid) {
                        gen.image = sppData.image
                    }
                }
                else {
                    log.debug "No image found for genus ${gen.name} (guid = ${gen.guid})"
                }
            }
        }

        return list
    }

    def injectSpeciesMetadata(list) {

        // build a list of guids to lookup
        def guids = []
        list.each { sp ->
            if (sp.guid) {
                guids << sp.guid
            }
        }

        // look up the metadata
        def md = betterBulkLookup(guids)

        // inject the metadata
        list.each { sp ->
            def data = md[sp.guid]
            if (data) {
                //sp.common = data.common  // don't override common name with name from bie as CMAR is more authoritative
                if (data.image && data.image.largeImageUrl?.toString() != "null" &&
                        data.image.imageSource == grailsApplication.config.image.source.dataResourceUid) {
                    sp.image = data.image
                }
            }
            else {
                log.debug "No metadata found for species ${sp.name} (guid = ${sp.guid})"
            }
        }

        return list
    }

    def betterBulkLookup(list) {
        def url = grailsApplication.config.bie.baseURL + "/species/guids/bulklookup.json"
        def data = webService.doPost(url, "", (list as JSON).toString())
        Map results = [:]
        data.resp.searchDTOList.each {item ->
            results.put item.guid, [
                   common: item.commonNameSingle,
                   image: [largeImageUrl: item.largeImageUrl,
                           smallImageUrl: item.smallImageUrl,
                           thumbnailUrl: item.thumbnailUrl,
                           imageMetadataUrl: item.imageMetadataUrl,
                           imageSource: item.imageSource]]
        }
        return results
    }

    static bieNameGuidCache = [:]  // temp cache while services are made more efficient

    def getBieMetadata(name, guid) {
        // use guid if known
        def key = guid ?: name

        // check cache first
        if (bieNameGuidCache[name]) {
            return bieNameGuidCache[name]
        }
        def resp = getJson(grailsApplication.config.bie.baseURL + "/species/" + key + ".json")
        if (!resp || resp.error) {
            return [name: name, guid: guid]
        }
        def details = [name: resp?.taxonConcept?.nameString ?: name, guid: resp?.taxonConcept?.guid,
                common: extractBestCommonName(resp?.commonNames),
                image: extractPreferredImage(resp?.images)]
        bieNameGuidCache[name] = details
        return details
    }

    def getPreferredImage(name) {
        def resp = getJson(grailsApplication.config.bie.baseUrl + "/species/${name}.json")
        return extractPreferredImage(resp.images)
    }

    def extractPreferredImage(images) {
        if (images) {
            def preferred = images.findAll {it.preferred}
            // return first preferred name
            if (preferred) {
                return [repoLocation: preferred[0].repoLocation, thumbnail: preferred[0].thumbnail, rights: preferred[0].rights]
            }
            // else return first image
            return [repoLocation: images[0].repoLocation, thumbnail: images[0].thumbnail, rights: images[0].rights]
        }
        return null
    }

    def extractBestCommonName(names) {
        if (names) {
            def preferred = names.findAll {it.preferred}
            // return first preferred name
            if (preferred) { return preferred[0].nameString}
            // else return first name
            return names[0].nameString
        }
        return ""
    }

    def lookupCAABCodeForFamily(name) {
        return tempCache[name]?.CAABCode ?: ""
    }

    def lookupCommonNameForFamily(name) {
        return tempCache[name]?.preferredCommonName ?: ""
    }

    def CAABCache = [
            Alopiidae: '37 012',
            Lamnidae: '37 010',
            Rajidae: '37 031',
            Orectolobidae: '37 013 (part)',
            Rhinopteridae: '37 040',
            Brachaeluridae: "37 013 (part)",
            Dalatiidae: "37 020 (part)",
            Dasyatidae: '37 035',
            Gymnuridae: "37 037 (part)",
            Heterodontidae: "37 007",
            Odontaspididae: "37 008",
            Pristidae: "37 025",
            Sphyrnidae: "37 019",
            Squalidae: "37 020 (part)",
            Squatinidae: "37 024",
            Stegostomatidae: "37 013 (part)",
            Triakidae: "37 017",
            Urolophidae: "37 038 (part)",
            Carcharhinidae: "37 018 (part)",
            Hemiscylliidae: "37 013 (part)",
            Hexanchidae: "37 005"
    ]

    // temp cache til bie has CAAB and images at higher taxon levels
    static tempCache = [
            Lamnidae: [CAABCode: '37 010',
                    guid: "urn:lsid:biodiversity.org.au:afd.taxon:af7628a0-288a-457a-8e3e-ee2ea99ef01b",
                    preferredImage: [ContentType: 'image/jpeg',
                            title: 'Isurus oxyrinchus',
                            thumbnail: 'http://bie.ala.org.au/repo/1111/174/1740514/thumbnail.jpg',
                            repoLocation: 'http://bie.ala.org.au/repo/1111/174/1740514/raw.jpg',
                            license: 'Creative Common Attribution 3.0 Australia',
                            rights: 'Australian National Fish Collection, CSIRO',
                            creator: 'Australian National Fish Collection, CSIRO']],
            Rajidae: [CAABCode: '37 031',
                    guid: "urn:lsid:biodiversity.org.au:afd.taxon:71003289-737d-429f-8301-79ac008a1ebc",
                    preferredImage: [ContentType: 'image/jpeg',
                            title: 'Amblyraja hyperborea',
                            thumbnail: 'http://bie.ala.org.au/repo/1111/174/1740706/thumbnail.jpg',
                            repoLocation: 'http://bie.ala.org.au/repo/1111/174/1740706/raw.jpg',
                            license: 'Creative Common Attribution 3.0 Australia',
                            rights: 'Australian National Fish Collection, CSIRO, Image enhancement funded by CSIRO/FRDC',
                            creator: 'Australian National Fish Collection, CSIRO']],
            Orectolobidae: [CAABCode: '37 013 (part)',
                    guid: "urn:lsid:biodiversity.org.au:afd.taxon:a8a0508d-e22f-44a8-a9df-d76358d2329e",
                    preferredCommonName: 'Western Wobbegong',
                    preferredImage: [ContentType: 'image/jpeg',
                            thumbnail: 'http://bie.ala.org.au/repo/1111/174/1740542/thumbnail.jpg',
                            repoLocation: 'http://bie.ala.org.au/repo/1111/174/1740542/raw.jpg',
                            license: 'Creative Common Attribution 3.0 Australia',
                            rights: 'Australian National Fish Collection, CSIRO',
                            creator: 'Australian National Fish Collection, CSIRO']],
            Rhinopteridae: [CAABCode: '37 040',
                    guid: "urn:lsid:biodiversity.org.au:afd.taxon:33ed36c8-e96b-4df2-bf87-9032e272c67d",
                    preferredCommonName: 'Cownose Rays',
                    preferredImage: [ContentType: 'image/jpeg',
                            thumbnail: 'http://bie.ala.org.au/repo/1111/174/1740809/thumbnail.jpg',
                            repoLocation: 'http://bie.ala.org.au/repo/1111/174/1740809/raw.jpg',
                            license: 'Creative Common Attribution 3.0 Australia',
                            rights: 'Australian National Fish Collection, CSIRO',
                            creator: 'Australian National Fish Collection, CSIRO']],
            Alopiidae: [CAABCode: "37 012",
                    guid: "urn:lsid:biodiversity.org.au:afd.taxon:7cb7f40d-143f-49cc-839d-613259786a42",
                    preferredImage: [contentType: 'image/jpeg',
                            creator: 'Australian National Fish Collection, CSIRO',
                            thumbnail: 'http://bie.ala.org.au/repo/1111/174/1740515/thumbnail.jpg',
                            repoLocation: 'http://bie.ala.org.au/repo/1111/174/1740515/raw.jpg',
                            rights: 'Australian National Fish Collection, CSIRO',
                            license: 'Creative Common Attribution 3.0 Australia']],
            Brachaeluridae: [CAABCode: "37 013 (part)",
                    guid: "urn:lsid:biodiversity.org.au:afd.taxon:80de0f3d-66e9-4526-a681-9af5765ead69",
                    preferredImage: [contentType: 'image/jpeg',
                            creator: 'Australian National Fish Collection, CSIRO',
                            thumbnail: 'http://bie.ala.org.au/repo/1111/174/1740781/thumbnail.jpg',
                            repoLocation: 'http://bie.ala.org.au/repo/1111/174/1740781/raw.jpg',
                            rights: 'Australian National Fish Collection, CSIRO',
                            license: 'Creative Common Attribution 3.0 Australia']],
            Dalatiidae: [CAABCode: "37 020 (part)",
                    guid: "urn:lsid:biodiversity.org.au:afd.taxon:f699eaeb-1337-4c3b-8743-8ff58318c06d",
                    preferredImage: [contentType: 'image/jpeg',
                            creator: 'Australian National Fish Collection, CSIRO',
                            thumbnail: 'http://bie.ala.org.au/repo/1111/174/1740528/thumbnail.jpg',
                            repoLocation: 'http://bie.ala.org.au/repo/1111/174/1740528/raw.jpg',
                            rights: 'Australian National Fish Collection, CSIRO',
                            license: 'Creative Common Attribution 3.0 Australia']],
            Dasyatidae: [CAABCode: '37 035',
                    guid: "urn:lsid:biodiversity.org.au:afd.taxon:43c35340-509c-48d9-bb6f-ad87916da26b",
                    preferredCommonName: 'Stingrays',
                    preferredImage: [ContentType: 'image/jpeg',
                            title: 'Dasyatis brevicaudata',
                            thumbnail: 'http://bie.ala.org.au/repo/1111/174/1740546/thumbnail.jpg',
                            repoLocation: 'http://bie.ala.org.au/repo/1111/174/1740546/raw.jpg',
                            license: 'Creative Common Attribution 3.0 Australia',
                            rights: 'Australian National Fish Collection, CSIRO',
                            creator: 'Australian National Fish Collection, CSIRO']],
            Gymnuridae: [CAABCode: "37 037 (part)",
                    guid: "urn:lsid:biodiversity.org.au:afd.taxon:6d32c970-fd70-4575-a308-b78b8295c654",
                    preferredImage: [contentType: 'image/jpeg',
                            creator: 'Australian National Fish Collection, CSIRO',
                            thumbnail: 'http://bie.ala.org.au/repo/1111/174/1740641/thumbnail.jpg',
                            repoLocation: 'http://bie.ala.org.au/repo/1111/174/1740641/raw.jpg',
                            rights: 'Australian National Fish Collection, CSIRO, Image enhancement funded by CSIRO/FRDC',
                            license: 'Creative Common Attribution 3.0 Australia']],
            Heterodontidae: [CAABCode: "37 007",
                    guid: "urn:lsid:biodiversity.org.au:afd.taxon:7fa709ac-94c2-4a0e-a11b-2cce7d893d2e",
                    preferredImage: [contentType: 'image/jpeg',
                            creator: 'Australian National Fish Collection, CSIRO',
                            thumbnail: 'http://bie.ala.org.au/repo/1111/174/1740603/thumbnail.jpg',
                            repoLocation: 'http://bie.ala.org.au/repo/1111/174/1740603/raw.jpg',
                            rights: 'Australian National Fish Collection, CSIRO',
                            license: 'Creative Common Attribution 3.0 Australia']],
            Odontaspididae: [CAABCode: "37 008",
                    guid: "urn:lsid:biodiversity.org.au:afd.taxon:66af5fc4-a5e8-45dc-a2be-ea615661154d",
                    preferredImage: [contentType: 'image/jpeg',
                            creator: 'Australian National Fish Collection, CSIRO',
                            thumbnail: 'http://bie.ala.org.au/repo/1111/174/1740506/thumbnail.jpg',
                            repoLocation: 'http://bie.ala.org.au/repo/1111/174/1740506/raw.jpg',
                            rights: 'Australian National Fish Collection, CSIRO',
                            license: 'Creative Common Attribution 3.0 Australia']],
            Pristidae: [CAABCode: "37 025",
                    guid: "urn:lsid:biodiversity.org.au:afd.taxon:69faf290-f074-45db-895a-101fb9c27c12",
                    preferredImage: [contentType: 'image/jpeg',
                            creator: 'Australian National Fish Collection, CSIRO',
                            thumbnail: 'http://bie.ala.org.au/repo/1111/174/1740662/thumbnail.jpg',
                            repoLocation: 'http://bie.ala.org.au/repo/1111/174/1740662/raw.jpg',
                            rights: 'Australian National Fish Collection, CSIRO, Image enhancement funded by CSIRO/FRDC',
                            license: 'Creative Common Attribution 3.0 Australia']],
            Sphyrnidae: [CAABCode: "37 019",
                    guid: "urn:lsid:biodiversity.org.au:afd.taxon:e74eee42-50d2-4cb5-9100-1b470c9bdaf3",
                    preferredImage: [contentType: 'image/jpeg',
                            creator: 'Australian National Fish Collection, CSIRO',
                            thumbnail: 'http://bie.ala.org.au/repo/1111/174/1740526/thumbnail.jpg',
                            repoLocation: 'http://bie.ala.org.au/repo/1111/174/1740526/raw.jpg',
                            rights: 'Australian National Fish Collection, CSIRO',
                            license: 'Creative Common Attribution 3.0 Australia']],
            Squalidae: [CAABCode: "37 020 (part)",
                    guid: "urn:lsid:biodiversity.org.au:afd.taxon:4991163e-88df-447e-ac38-948def2e70e0",
                    preferredImage: [contentType: 'image/jpeg',
                            creator: 'Australian National Fish Collection, CSIRO',
                            thumbnail: 'http://bie.ala.org.au/repo/1111/174/1740725/thumbnail.jpg',
                            repoLocation: 'http://bie.ala.org.au/repo/1111/174/1740725/raw.jpg',
                            rights: 'Australian National Fish Collection, CSIRO, Image enhancement funded by CSIRO/FRDC',
                            license: 'Creative Common Attribution 3.0 Australia']],
            Squatinidae: [CAABCode: "37 024",
                    guid: "urn:lsid:biodiversity.org.au:afd.taxon:71c6e8cd-0138-43d1-aeb8-ce5fc39d2f80",
                    preferredImage: [contentType: 'image/jpeg',
                            creator: 'Australian National Fish Collection, CSIRO',
                            thumbnail: 'http://bie.ala.org.au/repo/1111/174/1740606/thumbnail.jpg',
                            repoLocation: 'http://bie.ala.org.au/repo/1111/174/1740606/raw.jpg',
                            rights: 'Australian National Fish Collection, CSIRO',
                            license: 'Creative Common Attribution 3.0 Australia']],
            Stegostomatidae: [CAABCode: "37 013 (part)",
                    guid: "urn:lsid:biodiversity.org.au:afd.taxon:b23bc48c-ddb4-4b98-a57f-841d0eb319d2",
                    preferredImage: [contentType: 'image/jpeg',
                            creator: 'Australian National Fish Collection, CSIRO',
                            thumbnail: '',
                            repoLocation: 'http://bie.ala.org.au/repo/1111/174/1740625/raw.jpg',
                            rights: 'Australian National Fish Collection, CSIRO',
                            license: 'Creative Common Attribution 3.0 Australia']],
            Triakidae: [CAABCode: "37 017",
                    guid: "urn:lsid:biodiversity.org.au:afd.taxon:4ca31ca2-73c9-472d-9625-c3d3d5b08557",
                    preferredImage: [contentType: 'image/jpeg',
                            creator: 'Australian National Fish Collection, CSIRO',
                            thumbnail: '',
                            repoLocation: 'http://bie.ala.org.au/repo/1111/174/1740552/raw.jpg',
                            rights: 'Australian National Fish Collection, CSIRO',
                            license: 'Creative Common Attribution 3.0 Australia']],
            Urolophidae: [CAABCode: "37 038 (part)",
                    guid: "urn:lsid:biodiversity.org.au:afd.taxon:371ed595-dec8-4d5b-8f4d-61165ee0edd8",
                    preferredImage: [contentType: 'image/jpeg',
                            title: 'Trygonoptera galba',
                            thumbnail: 'http://bie.ala.org.au/repo/1111/174/1740604/thumbnail.jpg',
                            repoLocation: 'http://bie.ala.org.au/repo/1111/174/1740604/raw.jpg',
                            license: 'Creative Common Attribution 3.0 Australia',
                            rights: 'Australian National Fish Collection, CSIRO',
                            creator: 'Australian National Fish Collection, CSIRO']],
            Carcharhinidae: [CAABCode: "37 018 (part)",
                    guid: "urn:lsid:biodiversity.org.au:afd.taxon:62d6d958-e694-4038-a072-07f7d1f3fda2",
                    preferredImage: [contentType: 'image/jpeg',
                            title: 'Carcharhinus albimarginatus',
                            thumbnail: '',
                            repoLocation: 'http://bie.ala.org.au/repo/1111/174/1740711/raw.jpg',
                            license: 'Creative Common Attribution 3.0 Australia',
                            rights: 'Australian National Fish Collection, CSIRO',
                            creator: 'Australian National Fish Collection, CSIRO']],
            Hemiscylliidae: [CAABCode: "37 013 (part)",
                    guid: "urn:lsid:biodiversity.org.au:afd.taxon:3b4adcf5-8bb7-4e5c-84e2-13e8c9777965",
                    preferredImage: [contentType: 'image/jpeg',
                            title: 'Hemiscyllium trispeculare',
                            thumbnail: '',
                            repoLocation: 'http://bie.ala.org.au/repo/1111/174/1740815/raw.jpg',
                            license: 'Creative Common Attribution 3.0 Australia',
                            rights: 'Jack Randall',
                            creator: 'Jack Randall']],
            Hexanchidae: [CAABCode: "37 005",
                    guid: "urn:lsid:biodiversity.org.au:afd.taxon:3e0e15e9-dd24-46d8-9ea2-be9a7130fc5f",
                    preferredImage: [contentType: 'image/jpeg',
                            title: 'Heptranchias perlo',
                            thumbnail: '',
                            repoLocation: 'http://bie.ala.org.au/repo/1111/174/1740511/raw.jpg',
                            license: 'Creative Common Attribution 3.0 Australia',
                            rights: 'Australian National Fish Collection, CSIRO',
                            creator: 'Australian National Fish Collection, CSIRO']],
            Callorhinchidae: [CAABCode: "",
                    guid: "urn:lsid:biodiversity.org.au:afd.taxon:537c51b2-c05a-42bc-9091-58f0030e63a7"],
            Cetorhinidae: [CAABCode: "",
                    guid: "urn:lsid:biodiversity.org.au:afd.taxon:eb07c516-3045-4402-bfdc-ec5bd14f7f1c"],
            Echinorhinidae: [CAABCode: "",
                    guid: "urn:lsid:biodiversity.org.au:afd.taxon:15ef3c1c-acf5-4cdd-bbaf-96ade861f668"],
            Ginglymostomatidae: [CAABCode: "",
                    guid: "urn:lsid:biodiversity.org.au:afd.taxon:bf87605f-0b99-4849-8411-8fbc0d5369f4"],
            Parascylliidae: [CAABCode: "",
                    guid: "urn:lsid:biodiversity.org.au:afd.taxon:27bf34c6-fe57-44a5-a2ed-d596edf25138"],
            Hemigaleidae: [CAABCode: "",
                    guid: "urn:lsid:biodiversity.org.au:afd.taxon:824e9811-36fb-4642-9ffb-7392b3bcf316"],
            Hypnidae: [CAABCode: "",
                    guid: "urn:lsid:biodiversity.org.au:afd.taxon:9f07a4d1-d4d3-4c35-8009-69059583173c"],
            Mobulidae: [CAABCode: "",
                    guid: "urn:lsid:biodiversity.org.au:afd.taxon:5e932bb3-2f54-4341-91bc-abb3acdf7a1a"],
            Myliobatidae: [CAABCode: "",
                    guid: "urn:lsid:biodiversity.org.au:afd.taxon:975ea5d8-abc1-45d5-afa7-a64eafd540d3"],
            Narcinidae: [CAABCode: "",
                    guid: "urn:lsid:biodiversity.org.au:afd.taxon:fc63ff95-2d4a-48ab-8a4e-135aaa6ae697"],
            Pristiophoridae: [CAABCode: "",
                    guid: "urn:lsid:biodiversity.org.au:afd.taxon:633b7065-df8c-469a-b97f-6f8b88115299"],
            Pseudocarchariidae: [CAABCode: "",
                    guid: "urn:lsid:biodiversity.org.au:afd.taxon:c4185a83-c5dc-4edf-a114-529b3867d663"],
            Rhincodontidae: [CAABCode: "",
                    guid: "urn:lsid:biodiversity.org.au:afd.taxon:b7d10875-af94-4a4b-ba06-3f2141c383b4"],
            Rhinidae: [CAABCode: "",
                    guid: "urn:lsid:biodiversity.org.au:afd.taxon:359e6ffd-6065-4fa8-bdeb-5d2b796a4631"],
            Rhinobatidae: [CAABCode: "",
                    guid: "urn:lsid:biodiversity.org.au:afd.taxon:7edfee58-786b-4c5f-a88d-ecd61ccf4478"],
            Rhynchobatidae: [CAABCode: "",
                    guid: "urn:lsid:biodiversity.org.au:afd.taxon:77c15776-72a0-47b2-af67-fc69a00397b8"],
            Scyliorhinidae: [CAABCode: "",
                    guid: "urn:lsid:biodiversity.org.au:afd.taxon:ecc23b1f-3e27-452e-b090-535f6312fbd0"],
            e: [CAABCode: "",
                    guid: ""],
    ]

}
