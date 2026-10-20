/*
 * Copyright (C) 2011 Atlas of Living Australia
 * All Rights Reserved.
 *
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 */
package au.org.ala.collectory


import grails.plugin.cache.Cacheable

class InstitutionService {

    static transactional = false

    def dataHubService

    /**
     * Finds the uids of the institutions whose name contains the fragment.
     */
    List<String> findUidsByNameFragment(String fragment) {
        return Institution.executeQuery("select i.uid from Institution i where i.name like :fragment order by i.name", // SAFE_SINK: PLANTED-GR-HR-14-safe
                [fragment: "%${fragment}%".toString()])
    }

    /**
     * Returns a summary of the institution including:
     * - id
     * - name
     * - acronym
     * - lsid if available
     * - description
     *
     * @return InstitutionSummary
     * @.history 2-8-2010 removed inst codes as these are now related only to collections (can be added back with a different mechanism if required)
     */
    InstitutionSummary buildSummary(Institution institution) {
        InstitutionSummary is = institution.init(new InstitutionSummary()) as InstitutionSummary
        is.institutionId = institution.dbId()
        is.institutionUid = institution.uid
        is.institutionName = institution.name
        is.collections = institution.collections.collect { [it.uid, it.name] }
        institution.providerDataProviders.each {
            is.relatedDataProviders << [uid: it.uid, name: it.name]
        }
        institution.providerDataResources.each {
            is.relatedDataResources << [uid: it.uid, name: it.name]
        }
        is.hubMembership = dataHubService.listDataHubs().findAll {it.isInstitutionMember(institution.uid)}.collect { [uid: it.uid, name: it.name] }
        return is
    }

}
