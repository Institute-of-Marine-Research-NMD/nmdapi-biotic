package no.imr.nmdapi.nmdbiotic.utility.cache;

import java.io.File;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.bind.JAXBException;

import no.imr.nmdapi.exceptions.ApplicationException;
import no.imr.nmdapi.generic.nmdbiotic.domain.v1.MissionsType;
import no.imr.nmdapi.nmdbiotic.utility.common.JAXBUtility;
import no.imr.nmdapi.nmdbiotic.utility.files.FilesUtil;
import no.imr.nmdapi.nmdbiotic.utility.search.CatchSamplesSearch;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class CacheHolder extends CacheUtil {

    private static final Logger                               logger        = LoggerFactory.getLogger(CacheHolder.class);

    @Autowired
    // @Qualifier("bioticConfig")
    private PropertiesConfiguration                           config        = null;

    private static Map<String, Integer>                       specieMap     = new ConcurrentHashMap<String, Integer>();
    private static Map<File, String>                          fileHeaderMap = new ConcurrentHashMap<File, String>();
    private static Map<String, List<SerialNumberIndexedData>> yearSerialMap = null;
    //private static List<File>                                 bioticList    = null;
    private static Map<File, Long>                            currentFilesMap = null;    
    private static Map<File, Long>                            newFilesMap = null;    

    private static CacheHolder                                cacheInstance = null;

    private static Object                                     lock          = new Object();

    private CacheHolder() {
    }
    
    public void runTest() {
        
    }
    
    public void run() {
        cacheInstance.msSleep(wait_init);
        logger.info("initCache synchronized...");
        synchronized (lock) {
            logger.info("initCache started...");
            cacheInstance.initCache();
        }
        while (!cacheInstance.isCancelled()) {
            cacheInstance.msSleep(interval_check);
            cacheInstance.checkFiles();
        }
    }
    
    private void checkFiles() {
        logger.info("Checking files ...");
        newFilesMap = getCheckBioticFiles(getBioticFiles());
        Map<String, List<File>> differenceMapList = getFileDifferences(currentFilesMap, newFilesMap);
        if (!differenceMapList.isEmpty()) {
            currentFilesMap = clearMap(currentFilesMap);
            currentFilesMap = newFilesMap;
            synchronized (lock) {
                yearSerialMap = updateSerialNumberYearMap(specieMap, fileHeaderMap, differenceMapList, yearSerialMap);
            }
        }
    }

    private void initCache() {
        initProperties(config);
        currentFilesMap = getCheckBioticFiles(getBioticFiles());
        Map<String, List<File>> yearMap = filesUtil.fileListToYearMap(currentFilesMap);
        yearSerialMap = createSerialNumberYearMap(yearMap, specieMap, fileHeaderMap);
        yearMap = clearYearMap(yearMap);
    }

    private void clearCache() {
        FilesUtil filesUtil = new FilesUtil();
        //bioticList = clearList(bioticList);
        currentFilesMap = clearMap(currentFilesMap);
        yearSerialMap = clearYearSerialMap(yearSerialMap);
    }
    
    

    public synchronized MissionsType find(String year, String specie, String fromSerialNo, String toSerialNo) {
        MissionsType missions = null;
        synchronized (lock) {
            CatchSamplesSearch search = new CatchSamplesSearch();
            String resultString = search.find(year, specie, new BigInteger(fromSerialNo), new BigInteger(toSerialNo), yearSerialMap, specieMap, fileHeaderMap, this);
            try {
                //long rtime = CommonUtil.printTimeElapsed("CacheHolder.unmarshal");
                missions = (MissionsType) jaxbUtility.unmarshal(resultString, MissionsType.class);
                //rtime = CommonUtil.printTimeElapsed(rtime, "CacheHolder.unmarshal");
                testWriteToFile(resultString);
                resultString = null;
            }
            catch (JAXBException exp) {
                logger.error(exp.getMessage(), exp);
                throw new ApplicationException(exp.getMessage(), exp); 
            }     
        }
        return missions;
    }

    public void attachShutDownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                logger.info("CacheHolder is shutting down...");
                cacheInstance.clearCache();
                cacheInstance.setCancelled(true);
                cacheInstance.yield();
                cacheInstance.interrupt();
            }
        });
    }

    public static CacheHolder getInstance() {
        if (cacheInstance == null) {
            if (cacheInstance == null) {
                // synchronized (lock) {
                cacheInstance = new CacheHolder();
                cacheInstance.start();
                cacheInstance.attachShutDownHook();
                // }

            }
        }
        return cacheInstance;
    }

    public static void main(String[] args) {

        path_data = "D:\\san\\";
        path_data = "D:\\santomcat7prod\\datasets\\";
        path_data = "D:\\santomcat7test\\datasets\\";
        
        data_year = "2015";
        data_year = "2007";
        
        //bioticList = get

        FilesUtil filesUtil = new FilesUtil();
        //CacheHolder cacheHolder = new CacheHolder();
        //currentFilesMap = cacheHolder.getBioticFiles();
        
        
        
//        if ((data_year != null) && !data_year.isEmpty()) {
//            bioticList = filesUtil.getFilesByFilter(path_data, "biotic", data_year);
//        }
//        else {
//            bioticList = filesUtil.getFilesByFilter(path_data, "biotic");
//            logger.info("" + bioticList.size());
//        }
//        
//        for (int inx = 0; inx < bioticList.size(); inx++) {
//            logger.info(bioticList.get(inx).getAbsolutePath());
//        }
//        
//        
//        for (int inx = 1990; inx < 2017; inx++) {
//            bioticList = filesUtil.getFilesByFilter(path_data, "biotic", inx + "");
//            logger.info("year " + inx + " size " + bioticList.size());
//        }
        
        
         //http://localhost:10112/apis/nmdapi/biotic/v1/2007/23001/23001/161722.G03/serial
        
         CacheHolder.getInstance().jaxbUtility = new JAXBUtility(memory_format);
         CacheHolder.getInstance().initCache();
         MissionsType mission = CacheHolder.getInstance().find(data_year, "161722.G03", "0", "100000");
         
         logger.info("mission "+mission.getMission().size());
         logger.info("mission "+mission.getMission().size());
         cacheInstance.checkFiles();
         
         mission = CacheHolder.getInstance().find(data_year, "161722.G03", "0", "100000");
         
         logger.info("mission "+mission.getMission().size());
         
//         mission = CacheHolder.getInstance().find(data_year, "171677", "24139", "24140");
//         mission = CacheHolder.getInstance().find(data_year, "171677", "24139", "24139");
//         mission = CacheHolder.getInstance().find(data_year, "171677", "24139", "24140");
         //FileUtils.writeStringToFile(new File("D:\\biotictest\\data.xml"), resultString, StandardCharsets.UTF_8.name());

    }

}
