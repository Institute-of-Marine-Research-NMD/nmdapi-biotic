package no.imr.nmdapi.nmdbiotic.utility.cache;

import java.io.File;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.bind.JAXBException;

import no.imr.framework.logging.slf4j.aspects.stereotype.PerformanceLogging;
import no.imr.nmdapi.exceptions.ApplicationException;
import no.imr.nmdapi.generic.nmdbiotic.domain.v1.MissionsType;
import no.imr.nmdapi.nmdbiotic.utility.common.CommonUtil;
import no.imr.nmdapi.nmdbiotic.utility.files.FilesUtil;
import no.imr.nmdapi.nmdbiotic.utility.search.CatchSamplesSearch;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class CacheHolder extends CacheUtil {

    private static final Logger logger = LoggerFactory.getLogger(CacheHolder.class);

    @Autowired
    private PropertiesConfiguration config = null;

    private Map<String, Integer> specieMap = new ConcurrentHashMap<String, Integer>();
    private Map<File, String> fileHeaderMap = new ConcurrentHashMap<File, String>();
    private Map<String, List<SerialNumberIndexedData>> yearSerialMap = null;
    private Map<File, Long> currentFilesMap = null;
    private Map<File, Long> newFilesMap = null;

    private Object lock = null;
    private boolean checkingFiles = false;

    private static CacheHolder cacheHolder = null;

    public CacheHolder() {
        init();
    }

    public static CacheHolder getInstance() {
        return cacheHolder;
    }

    public void setCacheHolder(CacheHolder cacheHolder) {
        this.cacheHolder = cacheHolder;
    }

    public void run() {
        logger.info("entering run method...");
        msSleep(wait_init);
        logger.info("initCache synchronized...");
        synchronized (lock) {
            initCache();
        }
        logger.info("initCache synchronized done");
        long interval = wait_init;
        while (!isCancelled()) {
            interval = checkFilesForUpdates(interval);
        }
    }

    private Object getLock() {
        if (lock == null) {
            lock = new Object();
        }
        return lock;
    }

    public void init() {
        long rtime = CommonUtil.printTimeElapsed("init");
        lock = getLock();
        start();
        CommonUtil.printTimeElapsed(rtime, "init");
    }

    private long checkFilesForUpdates(long interval) {
        logger.info("sleeping...");
        if (msSleep(interval)) {
            setCheckingFiles(true);
            checkFiles();
        }
        setCheckingFiles(false);
        return interval_check;
    }

    public void checkFiles() {
        long rtime = CommonUtil.printTimeElapsed("checkFiles");
        newFilesMap = getCheckBioticFiles(getBioticFiles());
        Map<String, List<File>> differenceMapList = getFileDifferences(currentFilesMap, newFilesMap);
        if (!differenceMapList.isEmpty()) {
            currentFilesMap = clearMap(currentFilesMap);
            currentFilesMap = newFilesMap;
            synchronized (lock) {
                yearSerialMap = updateSerialNumberYearMap(specieMap, fileHeaderMap, differenceMapList, yearSerialMap);
            }
        }
        CommonUtil.printTimeElapsed(rtime, "checkFiles");
    }

    private void initCache() {
        long rtime = CommonUtil.printTimeElapsed("initCache");
        initProperties(config);
        currentFilesMap = getCheckBioticFiles(getBioticFiles());
        Map<String, List<File>> yearMap = filesUtil.fileListToYearMap(currentFilesMap);
        yearSerialMap = createSerialNumberYearMap(yearMap, specieMap, fileHeaderMap);
        yearMap = clearYearMap(yearMap);
        CommonUtil.printTimeElapsed(rtime, "initCache");
    }

    public void clearCache() {
        long rtime = CommonUtil.printTimeElapsed("clearCache");
        try {

            shutdown();
            yearSerialMap = clearYearSerialMap(yearSerialMap);
            currentFilesMap = clearMap(currentFilesMap);
            newFilesMap = clearMap(newFilesMap);
            specieMap = clearMap(specieMap);
            fileHeaderMap = clearMap(fileHeaderMap);
            newFilesMap = clearMap(newFilesMap);
            lock = null;
            config = null;
            jaxbUtility = null;
            filesUtil = null;
            serialNumberComparator = null;
            cacheHolder = null;
            System.gc();
            gcRunFinalization();

        } catch (Exception exp) {
            logger.error(exp.getMessage(), exp);
        }
        CommonUtil.printTimeElapsed(rtime, "clearCache");
    }

    private void lInterrupt() {
        long rtime = CommonUtil.printTimeElapsed("lInterrupt");
        yield();
        interrupt();
        yield();
        logger.info(isAlive() ? "isAlive" : "isNotAlive");
        logger.info(isInterrupted() ? "isInterrupted" : "isNotisInterrupted");
        CommonUtil.printTimeElapsed(rtime, "lInterrupt");
    }

    public void shutdown() {
        long rtime = CommonUtil.printTimeElapsed("shutdown");
        try {
            setCancelled(true);
            while (isCheckingFiles()) {
                msSleep(10);
            }
            int count = 0;
            while ((!isInterrupted()) && (++count < 10)) {
                lInterrupt();
            }
        } catch (Exception exp) {
            logger.error(exp.getMessage(), exp);
        }
        CommonUtil.printTimeElapsed(rtime, "shutdown");

    }

    @PerformanceLogging
    public synchronized MissionsType find(String year, String specie, String fromSerialNo, String toSerialNo) {
        String message = "find : parameters year :" + year + " specie :" + specie + " from :" + fromSerialNo + " to :" + toSerialNo;
        long rtime = CommonUtil.printTimeElapsed(message);
        MissionsType missions = null;
        synchronized (lock) {
            CatchSamplesSearch search = new CatchSamplesSearch();
            String resultString = search.find(year, specie, new BigInteger(fromSerialNo), new BigInteger(toSerialNo), yearSerialMap, specieMap, fileHeaderMap, this);
            try {
                missions = (MissionsType) jaxbUtility.unmarshal(resultString, MissionsType.class);
                testWriteToFile(resultString);
                resultString = null;
            } catch (JAXBException exp) {
                logger.error(exp.getMessage(), exp);
                throw new ApplicationException(exp.getMessage(), exp);
            }
        }
        CommonUtil.printTimeElapsed(rtime, message);
        return missions;
    }

    @PerformanceLogging
    public synchronized MissionsType find(String year, String fromSerialNo, String toSerialNo) {
        String message = "find : parameters year :" + year + " from :" + fromSerialNo + " to :" + toSerialNo;
        long rtime = CommonUtil.printTimeElapsed(message);
        MissionsType missions = null;
        synchronized (lock) {
            CatchSamplesSearch search = new CatchSamplesSearch();
            String resultString = search.find(year, new BigInteger(fromSerialNo), new BigInteger(toSerialNo), yearSerialMap, specieMap, fileHeaderMap, this);
            try {
                missions = (MissionsType) jaxbUtility.unmarshal(resultString, MissionsType.class);
                testWriteToFile(resultString);
                resultString = null;
            } catch (JAXBException exp) {
                logger.error(exp.getMessage(), exp);
                throw new ApplicationException(exp.getMessage(), exp);
            }
        }
        CommonUtil.printTimeElapsed(rtime, message);
        return missions;
    }

    public PropertiesConfiguration getConfig() {
        return config;
    }

    public void setConfig(PropertiesConfiguration config) {
        this.config = config;
    }

    public boolean isCheckingFiles() {
        logger.info("isCheckingFiles " + this.checkingFiles);
        return checkingFiles;
    }

    public void setCheckingFiles(boolean checkingFiles) {
        this.checkingFiles = checkingFiles;
        logger.info("setCheckingFiles " + this.checkingFiles);
    }

    public static void main(String[] args) {

        path_data = "D:\\san\\";
        path_data = "D:\\santomcat7test\\datasets\\";
        path_data = "D:\\santomcat7prod\\datasets\\";

        FilesUtil filesUtil = new FilesUtil();

        List<File> bioticList = filesUtil.getFilesByFilter(path_data, "biotic", "2014");
        for (int inx = 1990; inx < 2017; inx++) {
            bioticList = filesUtil.getFilesByFilter(path_data, "biotic", inx + "");
            logger.info("year " + inx + " size " + bioticList.size());
        }

    }

}
