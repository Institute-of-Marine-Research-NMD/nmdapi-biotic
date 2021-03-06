package no.imr.nmdapi.nmdbiotic.controller;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import no.imr.framework.logging.slf4j.aspects.stereotype.ArgumentLogging;
import no.imr.framework.logging.slf4j.aspects.stereotype.PerformanceLogging;
import no.imr.nmdapi.exceptions.BadRequestException;
import no.imr.nmdapi.generic.nmdbiotic.domain.v1.MissionType;
import no.imr.nmdapi.generic.response.v1.ResultElementType;
import no.imr.nmdapi.nmdbiotic.service.NMDBioticService;
import no.imr.nmdapi.nmdbiotic.utility.cache.CacheHolder;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 *
 * @author sjurl
 */
@Controller
public class BioticController {

    /**
     * Url part that defines it as biotic.
     */
    public static final String BIOTIC_URL = "/biotic";

    /**
     * Class logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(BioticController.class);

    /**
     * Service layer object for nmd biotic queries.
     */
    @Autowired
    private NMDBioticService nmdBioticService;

    @Autowired
    private PropertiesConfiguration config;

    /**
     * Get biotic data for mission.
     *
     * @param missiontype
     * @param year
     * @param platform
     * @param delivery
     * @return Response object.
     */
    @PerformanceLogging
    @ArgumentLogging
    @RequestMapping(value = "/{missiontype}/{year}/{platform}/{delivery}", method = RequestMethod.GET, produces = {"application/xml;charset=UTF-8", "application/json;charset=UTF-8"})
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Object findByMission(@PathVariable(value = "missiontype") String missiontype, @PathVariable(value = "year") String year, @PathVariable(value = "platform") String platform, @PathVariable(value = "delivery") String delivery) {
        LOGGER.info("Start BioticController.findByMission");
        return nmdBioticService.getData(missiontype, year, platform, delivery);
    }

    /**
     * Get biotic data by filter of serialno interval and specie.
     *
     * @param year
     * @param from
     * @param to
     * @param specie
     * @return Response object.
     */
    @PerformanceLogging
    @ArgumentLogging
    @RequestMapping(value = "/{year}/{from}/{to}/{specie}/serial", method = RequestMethod.GET, produces = {"application/xml;charset=UTF-8", "application/json;charset=UTF-8"})
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Object findBySpecie(@PathVariable(value = "year") String year, @PathVariable(value = "from") String from, @PathVariable(value = "to") String to, @PathVariable(value = "specie") String specie) {
        LOGGER.info("Start BioticController.findBySpecie");
        Object result = CacheHolder.getInstance().find(year, specie, from, to);
        return result;
    }

    /**
     * Query for data based on year and serialnumber (from /to)
     *
     * @param year
     * @param from
     * @param to
     * @return
     */
    @PerformanceLogging
    @ArgumentLogging
    @RequestMapping(value = "/{year}/{from}/{to}/serial", method = RequestMethod.GET, produces = {"application/xml;charset=UTF-8", "application/json;charset=UTF-8"})
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Object findBySerialNo(@PathVariable(value = "year") String year, @PathVariable(value = "from") String from, @PathVariable(value = "to") String to) {
        LOGGER.info("Start BioticController.findBySpecie");
        Object result = CacheHolder.getInstance().find(year, from, to);
        return result;
    }

    /**
     * Refreshing cache by checking files.
     *
     * @return Response object.
     */
    @PerformanceLogging
    @ArgumentLogging
    @RequestMapping(value = "/refreshCache", method = RequestMethod.GET, produces = {"application/xml;charset=UTF-8", "application/json;charset=UTF-8"})
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Object refreshCache() {
        LOGGER.info("Start BioticController.refreshCache");
        CacheHolder.getInstance().checkFiles();
        ResultElementType element = new ResultElementType();
        element.setResult("refreshCache OK");
        return element;
    }

    /**
     * Refreshing cache by checking files.
     *
     * @return Response object.
     */
    @PerformanceLogging
    @ArgumentLogging
    @RequestMapping(value = "/clearCache", method = RequestMethod.GET, produces = {"application/xml;charset=UTF-8", "application/json;charset=UTF-8"})
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Object clearCache() {
        LOGGER.info("Start BioticController.clearCache");
        CacheHolder.getInstance().clearCache();
        ResultElementType element = new ResultElementType();
        element.setResult("clearCache OK");
        return element;
    }

    /**
     * Delete biotic data for mission.
     *
     * @param missiontype
     * @param year
     * @param platform
     * @param delivery
     */
    @PerformanceLogging
    @ArgumentLogging
    @RequestMapping(value = "/{missiontype}/{year}/{platform}/{delivery}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void deleteByMission(@PathVariable(value = "missiontype") String missiontype, @PathVariable(value = "year") String year, @PathVariable(value = "platform") String platform, @PathVariable(value = "delivery") String delivery) {
        LOGGER.info("Start BioticController.deleteByMission");
        nmdBioticService.deleteData(missiontype, year, platform, delivery);
    }

    /**
     * Update biotic data for mission.
     *
     * @param missiontype
     * @param year
     * @param platform
     * @param delivery
     * @param data
     */
    @PerformanceLogging
    @ArgumentLogging
    @RequestMapping(value = "/{missiontype}/{year}/{platform}/{delivery}", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void insertByMission(@PathVariable(value = "missiontype") String missiontype, @PathVariable(value = "year") String year, @PathVariable(value = "platform") String platform, @PathVariable(value = "delivery") String delivery, @RequestBody MissionType data) {
        LOGGER.info("Start BioticController.insertByMission");
        nmdBioticService.insertData(missiontype, year, platform, delivery, data);
    }

    /**
     * insert biotic data for mission.
     *
     * @param missiontype
     * @param year
     * @param delivery
     * @param platform
     * @param missionType
     */
    @PerformanceLogging
    @ArgumentLogging
    @RequestMapping(value = "/{missiontype}/{year}/{platform}/{delivery}", method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void updateByMission(@PathVariable(value = "missiontype") String missiontype, @PathVariable(value = "year") String year, @PathVariable(value = "platform") String platform, @PathVariable(value = "delivery") String delivery, @RequestBody MissionType missionType) {
        LOGGER.info("Start BioticController.updateByMission");
        nmdBioticService.updateData(missiontype, year, platform, delivery, missionType);
    }

    /**
     * Get namepsace for data.
     *
     * @param missiontype
     * @param year
     * @param platform
     * @param delivery
     * @return
     */
    @PerformanceLogging
    @ArgumentLogging
    @RequestMapping(value = "/{missiontype}/{year}/{platform}/{delivery}", method = RequestMethod.GET, params = {"type=info"}, produces = {"application/xml;charset=UTF-8", "application/json;charset=UTF-8"})
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Object getInfo(@PathVariable(value = "missiontype") String missiontype, @PathVariable(value = "year") String year, @PathVariable(value = "platform") String platform, @PathVariable(value = "delivery") String delivery) {
        LOGGER.info("Start BioticController.getInfo");
        return nmdBioticService.getInfo(missiontype, year, platform, delivery);
    }

    /**
     * Does the mission have data
     *
     * @param httpServletResponse
     * @param missiontype
     * @param year
     * @param platform
     * @param delivery
     */
    @PerformanceLogging
    @ArgumentLogging
    @RequestMapping(value = "/{missiontype}/{year}/{platform}/{delivery}", method = RequestMethod.HEAD)
    @ResponseBody
    public void hasData(HttpServletResponse httpServletResponse, @PathVariable(value = "missiontype") String missiontype, @PathVariable(value = "year") String year, @PathVariable(value = "platform") String platform, @PathVariable(value = "delivery") String delivery) {
        LOGGER.info("Start BioticController.hasData");
        if (nmdBioticService.hasData(missiontype, year, platform, delivery)) {
            httpServletResponse.setStatus(HttpServletResponse.SC_OK);
        } else {
            httpServletResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * Get data by id or cruise number.
     *
     * @param cruisenr
     * @param shipname
     * @param request
     * @return Response object.
     * @throws java.net.MalformedURLException
     * @throws java.net.URISyntaxException
     */
    @PerformanceLogging
    @ArgumentLogging
    @RequestMapping(value = "/find", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Object find(@RequestParam(value = "cruisenr", required = true) String cruisenr, @RequestParam(value = "shipname", required = true) String shipname, HttpServletRequest request) throws MalformedURLException, URISyntaxException {
        LOGGER.info("Start BioticController.find");
        if (cruisenr != null) {
            URI uri = (new URI(request.getRequestURL().toString())).resolve(".");
            return nmdBioticService.getDataByCruiseNr(cruisenr, shipname, uri.toString());
        } else {
            throw new BadRequestException("Cruisenr parameters must be set.");
        }
    }

    /**
     * Get data by id or cruise number.
     *
     * @param httpServletResponse
     * @param cruisenr
     * @param shipname
     */
    @PerformanceLogging
    @ArgumentLogging
    @RequestMapping(value = "/find", method = RequestMethod.HEAD)
    @ResponseBody
    public void find(HttpServletResponse httpServletResponse, @RequestParam(value = "cruisenr", required = false) String cruisenr, @RequestParam(value = "shipname", required = true) String shipname) {
        LOGGER.info("Start BioticController.find");
        if (nmdBioticService.hasDataByCruiseNr(cruisenr, shipname)) {
            httpServletResponse.setStatus(HttpServletResponse.SC_OK);
        } else {
            httpServletResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

}
