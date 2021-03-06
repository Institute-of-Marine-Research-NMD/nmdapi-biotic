package no.imr.nmdapi.nmdbiotic.service;

import no.imr.nmdapi.generic.nmdbiotic.domain.v1.MissionType;

/**
 * Service API for mission data.
 *
 * @author kjetilf
 */
public interface NMDBioticService {

    /**
     * Get .
     *
     * @param missiontype
     * @param year
     * @param platform
     * @param delivery
     * @return              Mission data.
     */
    Object getData(String missiontype, String year, String platform, String delivery);

    /**
     * Delete
     *
     * @param missiontype
     * @param year
     * @param platform
     * @param delivery
     */
    void deleteData(String missiontype, String year, String platform, String delivery);

    /**
     * Update
     *
     * @param missiontype
     * @param year
     * @param platform
     * @param delivery
     * @param dataset
     */
    void updateData(String missiontype, String year, String platform, String delivery, MissionType dataset);

    /**
     * Insert
     *
     * @param missiontype
     * @param year
     * @param platform
     * @param delivery
     * @param dataset
     */
    void insertData(String missiontype, String year, String platform, String delivery, MissionType dataset);

    /**
     *
     * @param missiontype
     * @param year
     * @param platform
     * @param delivery
     * @return
     */
    boolean hasData(String missiontype, String year, String platform, String delivery);

    /**
     *
     * @param cruisenr
     * @param shipname
     * @param contextpath
     * @return
     */
    Object getDataByCruiseNr(String cruisenr, String shipname, String contextpath);

    /**
     *
     * @param cruisenr
     * @param shipname
     * @return
     */
    boolean hasDataByCruiseNr(String cruisenr, String shipname);

    /**
     *
     * @param missiontype
     * @param year
     * @param platform
     * @param delivery
     * @return
     */
    Object getInfo(String missiontype, String year, String platform, String delivery);

}
