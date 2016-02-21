package no.imr.nmdapi.nmdbiotic.utility.cache;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.bind.JAXBException;

import no.imr.framework.logging.slf4j.aspects.stereotype.PerformanceLogging;
import no.imr.nmdapi.generic.nmdbiotic.domain.v1.CatchSampleType;
import no.imr.nmdapi.generic.nmdbiotic.domain.v1.FishStationType;
import no.imr.nmdapi.generic.nmdbiotic.domain.v1.MissionType;
import no.imr.nmdapi.generic.nmdbiotic.domain.v1.MissionsType;
import no.imr.nmdapi.nmdbiotic.utility.common.CommonUtil;
import no.imr.nmdapi.nmdbiotic.utility.common.JAXBUtility;
import no.imr.nmdapi.nmdbiotic.utility.files.FilesUtil;
import no.imr.nmdapi.nmdbiotic.utility.search.CatchSamplesSearch;
import no.imr.nmdapi.nmdbiotic.utility.search.SerialNumberComparator;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.MapDifference;
import com.google.common.collect.MapDifference.ValueDifference;
import com.google.common.collect.Maps;

public class CacheUtil extends EThread {

    private final static String    TAG_MISSIONS             = "<missions ";
    private final static String    TAG_MISSION_YEAR         = "<mission year";
    private final static String    TAG_FISHSTATION_SERIALNO = "fishstation serialno";
    private final static String    XML_MISSIONS_XMLNS       = TAG_MISSIONS + JAXBUtility.XML_XMLNS;
    private final static String    XML_NMD_BIOTIC_FORMAT    = "http://www.imr.no/formats/nmdbiotic/v1";
    public final static String     XML_MISSIONS             = XML_MISSIONS_XMLNS + XML_NMD_BIOTIC_FORMAT + "\">";
    public final static String     XML_CR                   = "\n";

    public final static String     DIFF_UPDATED_FILES       = "UPDATED_FILES";
    public final static String     DIFF_REMOVED_FILES       = "REMOVED_FILES";
    public final static String     DIFF_NEW_FILES           = "NEW_FILES";

    protected static String        KEY_PATH                 = "pre.data.dir";
    protected static String        KEY_FORMAT               = "memory.format";
    protected static String        KEY_TEST                 = "data.test";
    protected static String        KEY_TEST_PATH            = "path.test";
    protected static String        KEY_WAIT_INIT            = "wait.init";
    protected static String        KEY_YEAR                 = "data.year";
    protected static String        KEY_INTERVAL             = "checkfiles.interval";
    protected static String        path_data                = "/san/test/datasets/";
    protected static boolean       memory_format            = true;
    protected static boolean       data_test                = false;
    protected static String        path_test                = "D:\\biotictest\\";
    protected static long          wait_init                = 10000;
    protected static long          interval_check           = 300000;
    protected static String        data_year                = null;

    private static final Logger    logger                   = LoggerFactory.getLogger(CacheUtil.class);

    private static final Runtime   runtime                  = Runtime.getRuntime();

    protected JAXBUtility          jaxbUtility              = null;
    protected FilesUtil            filesUtil                = new FilesUtil();
    private SerialNumberComparator serialNumberComparator   = new SerialNumberComparator();

    protected Map<File, Long> getBioticFiles() {
        Map<File, Long> bioticMap = null;
        try {
            if ((data_year != null) && !data_year.isEmpty()) {
                bioticMap = filesUtil.getFileMapByFilter(path_data, "biotic", data_year);
            }
            else {
                bioticMap = filesUtil.getFileMapByFilter(path_data, "biotic");
                logger.info("" + bioticMap.size());
            }
        }
        catch (IOException exp) {
            logger.error(exp.getMessage(), exp);
        }
        return bioticMap;
    }

    protected Map<File, Long> getCheckBioticFiles(Map<File, Long> bioticFilesMap) {
        while (bioticFilesMap == null) {
            logger.error("InitCache error :could not read files");
            msSleep(wait_init);
            bioticFilesMap = getBioticFiles();
        }
        return bioticFilesMap;
    }

    protected MapDifference<File, Long> mapsDifference(Map<File, Long> refMap, Map<File, Long> newMap) {
        List<File> nameList = new ArrayList<File>();
        MapDifference<File, Long> mapDiff = Maps.difference(refMap, newMap);
        if (!mapDiff.areEqual()) {
            if ((mapDiff.entriesOnlyOnLeft().size() > 0) || (mapDiff.entriesOnlyOnRight().size() > 0)) {
                // Need a full refresh of API reference data
                nameList = null;
            }
            else {
                Map<File, ValueDifference<Long>> map = mapDiff.entriesDiffering();
                nameList = ImmutableList.copyOf(map.keySet());
            }
        }
        return mapDiff;
    }

    protected Map<String, List<File>> getFileDifferences(Map<File, Long> refMap, Map<File, Long> newMap) {
        Map<String, List<File>> differenceMap = new ConcurrentHashMap<String, List<File>>();
        MapDifference<File, Long> mapDiff = Maps.difference(refMap, newMap);
        if (!mapDiff.areEqual()) {
            if (!mapDiff.entriesDiffering().isEmpty()) {
                Map<File, ValueDifference<Long>> map = mapDiff.entriesDiffering();
                differenceMap.put(DIFF_UPDATED_FILES, ImmutableList.copyOf(map.keySet()));
            }
            if (!mapDiff.entriesOnlyOnLeft().isEmpty()) {
                Map<File, Long> map = mapDiff.entriesOnlyOnLeft();
                differenceMap.put(DIFF_REMOVED_FILES, ImmutableList.copyOf(map.keySet()));
            }
            if (!mapDiff.entriesOnlyOnRight().isEmpty()) {
                Map<File, Long> map = mapDiff.entriesOnlyOnRight();
                differenceMap.put(DIFF_NEW_FILES, ImmutableList.copyOf(map.keySet()));
            }
        }
        return differenceMap;
    }

    protected void test(Map<File, Long> currentMap) {
        Map<File, Long> tmpNameMap = ImmutableMap.copyOf(currentMap);

    }

    protected boolean compareList(List<File> currentList, List<File> newList) {
        boolean ok = true;
        return ok;
    }

    protected List<Long> getLastModifiedList(List<File> currentList) {
        List<Long> list = new ArrayList<Long>();
        for (File file : currentList) {
            list.add(file.lastModified());
        }
        return list;

    }

    protected void initProperties(PropertiesConfiguration config) {
        if (config != null) {
            path_data = config.getString(KEY_PATH, path_data);
            path_test = config.getString(KEY_TEST_PATH, path_test);

            memory_format = config.getBoolean(KEY_FORMAT, memory_format);
            data_test = config.getBoolean(KEY_TEST, data_test);
            wait_init = config.getLong(KEY_WAIT_INIT, wait_init);
            interval_check = config.getLong(KEY_INTERVAL, interval_check);
            data_year = config.getString(KEY_YEAR, data_year);
            data_year = filesUtil.isYear(data_year) ? data_year : "";
            jaxbUtility = new JAXBUtility(memory_format);
        }
    }

    private boolean isLegalIndexes(int... indexes) {
        boolean ok = true;
        for (int inx = 0; inx < indexes.length; inx++) {
            ok = ok && indexes[inx] > -1;
        }
        return ok;
    }

    private String generateHeaderKey(String xmlData) {
        String result = null;
        try {
            int beginIndex = xmlData.indexOf(TAG_MISSION_YEAR);
            int endIndex = xmlData.indexOf(TAG_FISHSTATION_SERIALNO) - 1;
            if (isLegalIndexes(beginIndex, endIndex)) {
                result = xmlData.substring(beginIndex, endIndex);
            }
        }
        catch (Exception exp) {
            result = null;
            logger.error("Not legal biotic file", exp);
        }
        return result;
    }

    private boolean isLegalMission(File file, String header, MissionsType missions) {
        boolean ok = false;
        String path = "Not legal file";
        String message = "Not legal missions for File :";
        try {
            path = file.getName();
            ok = header != null && !missions.getMission().isEmpty();
        }
        catch (Exception e) {
            ok = false;
        }
        if (!ok) {
            logger.error(message + path);
        }
        return ok;
    }

    private Map<String, Integer> updateSpecieMap(List<CatchSampleType> catchSampleList, Map<String, Integer> specieMap) {
        int nextSpecieIndex = specieMap.size();
        for (CatchSampleType catchSampleType : catchSampleList) {
            String specie = catchSampleType.getSpecies();
            try {
                if (specie != null && !specieMap.containsKey(specie)) {
                    specieMap.put(specie, nextSpecieIndex);
                    nextSpecieIndex++;
                    if (specieMap.size() != nextSpecieIndex) {
                        logger.error("SW bug :specieMap.size() != nextSpecieIndex " + specieMap.size() + " != " + nextSpecieIndex);
                    }
                }
                else if (specie == null) {
                    logger.error("specie null");

                }
            }
            catch (Exception exp) {
                logger.error(exp.getMessage(), exp);
            }

        }
        return specieMap;
    }

    private List<Integer> catchListToSpecieList(List<CatchSampleType> list, Map<String, Integer> specieMap) {
        List<Integer> specieList = new ArrayList<Integer>();
        for (CatchSampleType catchSampleType : list) {
            String specie = catchSampleType.getSpecies();
            boolean ok = specie != null ? specieList.add(specieMap.get(catchSampleType.getSpecies())) : false;
        }
        return specieList;
    }

    private List<String> catchSampleToStringList(CatchSampleType[] list) throws JAXBException {
        List<String> strList = new ArrayList<String>();
        for (CatchSampleType catchSample : list) {
            strList.add(jaxbUtility.marshalChildren(catchSample, XML_NMD_BIOTIC_FORMAT));
        }
        return strList;
    }

    private List<String> sortCatchSamples(List<CatchSampleType> catchSamples, int[] speciesArr, Map<String, Integer> specieMap) throws JAXBException, Exception {
        CatchSampleType[] orderedCatchSamples = new CatchSampleType[catchSamples.size()];
        // int prevIndex = -1;
        CatchSamplesSearch catchSamplesSearch = new CatchSamplesSearch();
        int index = 0;
        while (index < catchSamples.size()) {
            String specie = catchSamples.get(index).getSpecies();
            int[] foundArray = catchSamplesSearch.binarySearchHandleDuplicates(speciesArr, specieMap.get(specie));
            if (foundArray != null) {
                for (int fIndex = 0; fIndex < foundArray.length; fIndex++) {
                    // index += fIndex;
                    try {
                        int foundIndex = foundArray[fIndex];
                        orderedCatchSamples[foundIndex] = catchSamples.get(index + fIndex);
                    }
                    catch (Exception exp) {
                        logger.info(exp.getMessage(), exp);
                    }
                }
                index += foundArray.length - 1;
            }
            else {
                throw new Exception("SW bug sortCatchSamples : index not found");
            }
            index++;
        }
        return catchSampleToStringList(orderedCatchSamples);
    }

    public SerialNoCompressedData addCompressedData(List<String> catchSamples, FishStationType fishStationType) throws JAXBException {

        boolean ok = false;
        SerialNoCompressedData compressData = null;
        compressData = new SerialNoCompressedData();

        compressData.setCatchsample(catchSamples);
        fishStationType.getCatchsample().clear();
        compressData.setFishstation(jaxbUtility.marshalChildren(fishStationType, XML_NMD_BIOTIC_FORMAT));

        return compressData;
    }

    private boolean addFileIndexedMap(String year, File file, FishStationType fishStationType, Map<String, Integer> specieMap, Map<String, List<SerialNumberIndexedData>> listMap) {

        SerialNumberIndexedData data = null;
        boolean ok = false;
        try {
            List<CatchSampleType> catchSamples = fishStationType.getCatchsample();
            if (!catchSamples.isEmpty()) {
                specieMap = updateSpecieMap(catchSamples, specieMap);

                List<SerialNumberIndexedData> serialList = listMap.get(year);

                if (serialList == null) {
                    serialList = new ArrayList<SerialNumberIndexedData>();
                    listMap.put(year, serialList);
                }
                data = new SerialNumberIndexedData(catchSamples.size());
                data.setSerialno(fishStationType.getSerialno());
                data.setFile(file);

                data.setSpecies(catchListToSpecieList(catchSamples, specieMap));
                java.util.Arrays.sort(data.getSpecies());

                List<String> xmlList = sortCatchSamples(catchSamples, data.getSpecies(), specieMap);
                data.setCommpressedData(addCompressedData(xmlList, fishStationType));
                ok = serialList.add(data);

            }
            else {
                // logger.info("catchSamples.isEmpty "+file.getAbsolutePath());
            }
            catchSamples = clearList(catchSamples);
        }
        catch (Exception exp) {
            data = null;
            logger.error(exp.getMessage(), exp);
        }

        return ok;
    }

    public List clearList(List list) {
        list.clear();
        list = null;
        // gc();
        return list;
    }

    public Map clearMap(Map map) {
        map.clear();
        map = null;
        // gc();
        return map;
    }

    public void runFinalizationEnd() {
        long rtime = CommonUtil.printTimeElapsed(0, "runFinalization");
        gc();
        runtime.runFinalization();
        gc();
        rtime = CommonUtil.printTimeElapsed(rtime, "runFinalization");
    }

    public void gc() {
        long rtime = CommonUtil.printTimeElapsed(0, "gc");
        runtime.gc();
        rtime = CommonUtil.printTimeElapsed(rtime, "gc");
    }

    private long usedMemory() {
        runtime.gc();
        // Thread.yield();
        long mem = runtime.totalMemory() - runtime.freeMemory();
        return mem;
    }

    private Map<String, List<SerialNumberIndexedData>> removeFiles(List<File> fileList, Map<String, Integer> specieMap, Map<File, String> fileHeaderMap, Map<String, List<File>> diffMap, Map<String, List<SerialNumberIndexedData>> fileIndexedMap) {
        for (File file : fileList) {
            String year = filesUtil.getYear(file);
            fileHeaderMap.remove(file);
            List<SerialNumberIndexedData> serialList = fileIndexedMap.get(year);
            logger.info("serialList.size() pre remove" + serialList.size());
            List<SerialNumberIndexedData> elementsToBeRemoved = new ArrayList<SerialNumberIndexedData>();
            for (SerialNumberIndexedData serialNumberIndexedData : serialList) {
                if (serialNumberIndexedData.getFile().getAbsolutePath().equals(file.getAbsolutePath())) {
                    serialNumberIndexedData.dispose();
                    elementsToBeRemoved.add(serialNumberIndexedData);
                }
            }
            serialList.removeAll(elementsToBeRemoved);
            List<SerialNumberIndexedData> serialListPost = fileIndexedMap.get(year);
            logger.info("serialList.size() post remove" + serialListPost.size());
        }
        return fileIndexedMap;
    }

    private Map<String, List<SerialNumberIndexedData>>  sortSerialNoList(String year, Map<String, List<SerialNumberIndexedData>> fileIndexedMap) {
        List<SerialNumberIndexedData> serialNumberList = fileIndexedMap.get(year);
        if ((fileIndexedMap.size() > 0) && (serialNumberList != null) && (serialNumberList.size() > 1)) {
            Collections.sort(serialNumberList, serialNumberComparator);                    
        }
        return fileIndexedMap;
    }

    @PerformanceLogging
    protected synchronized Map<String, List<SerialNumberIndexedData>> updateSerialNumberYearMap(Map<String, Integer> specieMap, Map<File, String> fileHeaderMap, Map<String, List<File>> diffMap, Map<String, List<SerialNumberIndexedData>> fileIndexedMap) {

        for (Map.Entry<String, List<File>> entry : diffMap.entrySet()) {
            String key = entry.getKey();
            List<File> fileList = entry.getValue();
            if ((key.equals(DIFF_UPDATED_FILES)) || (key.equals(DIFF_REMOVED_FILES))) {
                fileIndexedMap = removeFiles(fileList, specieMap, fileHeaderMap, diffMap, fileIndexedMap);
            }
            if ((key.equals(DIFF_UPDATED_FILES)) || (key.equals(DIFF_NEW_FILES))) {
                List<String> yearList = new ArrayList<>();
                for (File file : fileList) {
                    String year = filesUtil.getYear(file);
                    yearList.add(year);
                    fileIndexedMap = insertFile(file, year, specieMap, fileHeaderMap, fileIndexedMap);
                }
                for (String year : yearList) {
                    fileIndexedMap = sortSerialNoList(year, fileIndexedMap);
                }
            }
        }
        runFinalizationEnd();
        return fileIndexedMap;
    }

    private Map<String, List<SerialNumberIndexedData>> insertFile(File file, String year, Map<String, Integer> specieMap, Map<File, String> fileHeaderMap, Map<String, List<SerialNumberIndexedData>> fileIndexedMap) {
        try {
            String xmlData = FileUtils.readFileToString(file, StandardCharsets.UTF_8.name());
            String headerKey = generateHeaderKey(xmlData);
            MissionsType missions = (MissionsType) jaxbUtility.unmarshal(xmlData, MissionsType.class);
            if (isLegalMission(file, headerKey, missions)) {
                fileHeaderMap.put(file, headerKey);
                List<MissionType> missionList = missions.getMission();
                for (MissionType missionType : missionList) {
                    List<FishStationType> fishStationList = missionType.getFishstation();
                    for (FishStationType fishStationType : fishStationList) {
                        addFileIndexedMap(year, file, fishStationType, specieMap, fileIndexedMap);
                        fishStationType = null;
                    }
                    fishStationList = clearList(fishStationList);
                    missionType = null;
                }
                missionList = clearList(missionList);
                missions = null;
                file = null;
            }
        }
        catch (Exception exp) {
            logger.error(exp.getMessage(), exp);
        }
        return fileIndexedMap;
    }

    protected Map<String, List<SerialNumberIndexedData>> createSerialNumberYearMap(Map<String, List<File>> yearMap, Map<String, Integer> specieMap, Map<File, String> fileHeaderMap) {
        Map<String, List<SerialNumberIndexedData>> fileIndexedMap = new ConcurrentHashMap<String, List<SerialNumberIndexedData>>();
        long rtime = CommonUtil.printTimeElapsed("createSerialNumberYearMap");
        try {

            int yearCounter = 0;
            int fileCounter = 0;
            for (Map.Entry<String, List<File>> entry : yearMap.entrySet()) {
                logger.info(++yearCounter + " fileMap size " + yearMap.size() + " year :"+entry.getKey());
                List<File> fileList = entry.getValue();
                String year = entry.getKey();
                for (File file : fileList) {
                    logger.info(++fileCounter + " fileList size " + fileList.size());
                    insertFile(file, year, specieMap, fileHeaderMap, fileIndexedMap);
                }
                fileIndexedMap = sortSerialNoList(year, fileIndexedMap);
                fileList = clearList(fileList);
            }
        }
        catch (Exception exp) {
            logger.error(exp.getMessage(), exp);
        }
        finally {
            runFinalizationEnd();
        }
        CommonUtil.printTimeElapsed(rtime, "createSerialNumberYearMap");
        return fileIndexedMap;
    }

    public Map<String, List<SerialNumberIndexedData>> clearYearSerialMap(Map<String, List<SerialNumberIndexedData>> serialMap) {
        try {
            for (Map.Entry<String, List<SerialNumberIndexedData>> entry : serialMap.entrySet()) {
                List<SerialNumberIndexedData> fileList = entry.getValue();
                fileList = clearList(fileList);
            }
            serialMap = clearMap(serialMap);
        }
        catch (Exception exp) {
            logger.error(exp.getMessage(), exp);
        }
        finally {
            runFinalizationEnd();
        }

        return serialMap;
    }

    public Map<String, List<File>> clearYearMap(Map<String, List<File>> yearMap) {
        try {
            for (Map.Entry<String, List<File>> entry : yearMap.entrySet()) {
                List<File> fileList = entry.getValue();
                fileList = clearList(fileList);
            }
            yearMap = clearMap(yearMap);
        }
        catch (Exception exp) {
            logger.error(exp.getMessage(), exp);
        }
        finally {
            runFinalizationEnd();
        }
        return yearMap;
    }

    protected void testWriteToFile(String xmlString) {
        if (data_test) {
            try {
                FileUtils.writeStringToFile(new File(path_test + "data.xml"), xmlString, StandardCharsets.UTF_8.name());
            }
            catch (IOException exp) {
                logger.error(exp.getMessage(), exp);
            }
        }

    }

}
