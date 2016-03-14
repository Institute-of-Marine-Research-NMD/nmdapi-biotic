package no.imr.nmdapi.nmdbiotic.utility.search;

import java.io.File;
import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import no.imr.framework.logging.slf4j.aspects.stereotype.PerformanceLogging;
import no.imr.nmdapi.exceptions.ApplicationException;
import no.imr.nmdapi.exceptions.NotFoundException;
import no.imr.nmdapi.exceptions.S2DException;
import no.imr.nmdapi.nmdbiotic.utility.cache.CacheHolder;
import no.imr.nmdapi.nmdbiotic.utility.cache.CacheUtil;
import no.imr.nmdapi.nmdbiotic.utility.cache.CompressedReference;
import no.imr.nmdapi.nmdbiotic.utility.cache.SerialNoCompressedData;
import no.imr.nmdapi.nmdbiotic.utility.cache.SerialNumberIndexedData;
import no.imr.nmdapi.nmdbiotic.utility.common.JAXBUtility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CatchSamplesSearch implements IBioticFilter {

    private final Logger logger = LoggerFactory.getLogger(CatchSamplesSearch.class);

    @Override
    public boolean accept(SerialNumberIndexedData element, Object key) {
        boolean ok = false;
        int[] specieArr = element.getSpecies();
        if ((specieArr != null) && (specieArr.length > 0)) {
            ok = java.util.Arrays.binarySearch(specieArr, (Integer) key) > -1;
        }
        return ok;
    }

    public int[] binarySearchHandleDuplicates(int[] intArr, int key) {
        int[] result = null;
        int index = java.util.Arrays.binarySearch(intArr, key);

        int leftIndex = moveIndexRangeBorder(index, false, key, intArr);
        int rightIndex = moveIndexRangeBorder(index, true, key, intArr);
        result = new int[rightIndex - leftIndex + 1];
        for (int inx = 0; inx < result.length; inx++) {
            result[inx] = leftIndex;
            leftIndex++;
        }

        return result;
    }

    private boolean isLegalPosition(int position, int size) {
        return position >= 0 && position < size;
    }

    private boolean isLegalRange(BigInteger fromSerialNo, BigInteger toSerialNo, List<SerialNumberIndexedData> list) {
        boolean legal = false;
        if (((list != null) && (!list.isEmpty()) && fromSerialNo.compareTo(toSerialNo) < 1)) {
            BigInteger max = list.get(list.size() - 1).getSerialno();
            BigInteger min = list.get(0).getSerialno();
            legal = (fromSerialNo.compareTo(max) < 1) && (toSerialNo.compareTo(min) > -1);
        }
        return legal;
    }

    private int findPosition(int searchedPosition, int size) {
        int pos = searchedPosition < 0 ? searchedPosition * (-1) - 1 : searchedPosition;
        return isLegalPosition(pos, size) ? pos : 0;
    }

    private int toPosition(int fromPosition, BigInteger value, BigInteger prevValue, int size) {
        int toPos = fromPosition;
        return toPos > -1 && prevValue.compareTo(value) == -1 ? toPos - 1 : -1;
    }

    private int moveIndexRangeBorder(int index, boolean moveRight, BigInteger serialNumber, List<SerialNumberIndexedData> list) {
        int step = moveRight ? 1 : -1;
        int position = index;
        while ((isLegalPosition(position, list.size())) && (list.get(position).getSerialno().equals(serialNumber))) {
            position += step;
        }
        return moveRight ? --position : ++position;
    }

    private int moveIndexRangeBorder(int index, boolean moveRight, int key, int[] list) {
        int step = moveRight ? 1 : -1;
        int position = index;
        while ((isLegalPosition(position, list.length)) && (list[position] == key)) {
            position += step;
        }
        return moveRight ? --position : ++position;
    }

    private int binarySearch(BigInteger serialNo, boolean toRange, List<SerialNumberIndexedData> list) {
        int result = -1;
        int size = list.size();
        SerialNumberIndexedData data = new SerialNumberIndexedData(serialNo);
        int foundIndex = Collections.binarySearch(list, data, new SerialNumberComparator());

        //Test
        if (foundIndex == -1) {
            logger.info("Not found");
        }

        if (foundIndex < 0) {
            //Not found
            if (toRange) {
                result = findPosition(foundIndex, size + 1);
                SerialNumberIndexedData prevValue = result > 0 ? list.get(result - 1) : list.get(result);
                result = toPosition(result, data.getSerialno(), prevValue.getSerialno(), size);
            } else {
                result = findPosition(foundIndex, size);
            }
        } else {
            result = moveIndexRangeBorder(foundIndex, toRange, data.getSerialno(), list);
        }
        return result;
    }

    private Range getRangeFiles(
            List<SerialNumberIndexedData> serialNumberList, BigInteger fromSerialNo, BigInteger toSerialNo, Map<File, BigInteger> fileMap
    ) {
        Range range = null;
        if (isLegalRange(fromSerialNo, toSerialNo, serialNumberList)) {
            int fromIndex = binarySearch(fromSerialNo, false, serialNumberList);
            int toIndex = binarySearch(toSerialNo, true, serialNumberList);
            range = new Range(fromIndex, toIndex);
            for (int index = fromIndex; index < (toIndex + 1); index++) {
                SerialNumberIndexedData element = serialNumberList.get(index);
                fileMap.put(element.getFile(), element.getSerialno());
            }
        }
        return range;
    }

    @PerformanceLogging
    public String find(
            String year, String specie, BigInteger fromSerialNo, BigInteger toSerialNo, Map<String, List<SerialNumberIndexedData>> serialNumberMap, Map<String, Integer> specieMap, Map<File, String> fileHeaderMap, CacheHolder instance) {

        logger.info("Start -------------------------------- fromSerialNo " + fromSerialNo + " toSerialNo " + toSerialNo);
        StringBuffer sb = new StringBuffer(JAXBUtility.XML_HEADER + CacheUtil.XML_CR + CacheUtil.XML_MISSIONS + CacheUtil.XML_CR);

        CatchSamplesSearch search = new CatchSamplesSearch();

        try {
            Map<File, String> map = new HashMap<File, String>();
            Integer specieIndex = specieMap.get(specie);
            if (specieIndex != null) {
                List<SerialNumberIndexedData> serialNumberList = serialNumberMap.get(year);
                Map<File, BigInteger> fileMap = new HashMap<File, BigInteger>();
                Range range = getRangeFiles(serialNumberList, fromSerialNo, toSerialNo, fileMap);
                logger.info("range size " + fromSerialNo + " --  " + toSerialNo);
                if (range != null) {
                    int counter = 0;
                    for (Map.Entry<File, BigInteger> entry : fileMap.entrySet()) {
                        logger.info(++counter + "   fileMap size -------------------------------" + fileMap.size());
                        File fileKey = entry.getKey();
                        for (int index = range.getFromInclusivePosition(); index < (range.getToInclusivePosition() + 1); index++) {
                            SerialNumberIndexedData element = serialNumberList.get(index);

                            File file = element.getFile();
                            if ((file.equals(fileKey)) && (search.accept(element, specieIndex))) {
                                SerialNoCompressedData data = element.getCompressedData();
                                if (!map.containsKey(file)) {
                                    sb.append(fileHeaderMap.get(file));
                                    map.put(file, " ");
                                }
                                int[] speciesArr = element.getSpecies();
                                int[] foundArray = binarySearchHandleDuplicates(speciesArr, specieMap.get(specie));
                                if (foundArray != null) {
                                    String fishStation = data.getFishstation();
                                    int lindex = fishStation.lastIndexOf("</");
                                    String lastLine = fishStation.substring(lindex);
                                    fishStation = fishStation.substring(0, lindex);
                                    sb.append(fishStation);
                                    fishStation = null;
                                    List<String> dlist = data.getDecompressedCatchsampleList();
                                    for (int sindex : foundArray) {
                                        String catchSampleString = dlist.get(sindex);
                                        sb.append(catchSampleString);
                                        catchSampleString = null;
                                    }
                                    dlist = instance.clearList(dlist);
                                    sb.append(lastLine);
                                }
                                foundArray = null;
                            }
                        }
                        if (map.containsKey(fileKey)) {
                            sb.append("</mission>");
                        }
                    }
                    sb.append("</missions>");
                } else {
                    throw new NotFoundException("Not legal serialno range :" + fromSerialNo + " - " + toSerialNo);
                }
                if (map.isEmpty()) {
                    sb = new StringBuffer();
                    throw new NotFoundException("No data found for specie " + specie + " withing serialno range :" + fromSerialNo + " - " + toSerialNo);
                }
            } else {
                throw new NotFoundException("Specie do not exist :" + specie);
            }
        } catch (Exception exp) {
            logger.error(exp.getMessage(), exp);
            logger.info("Excption ++++++++++++++++++++++++++++++++++ fromSerialNo " + fromSerialNo + " toSerialNo " + toSerialNo);
            if (exp instanceof S2DException) {
                throw new NotFoundException("Not legal serialno range :" + fromSerialNo + " - " + toSerialNo);
            } else {
                throw new ApplicationException(exp.getMessage(), exp);
            }
        }
        return sb.toString();
    }

    @PerformanceLogging
    public String find(
            String year, BigInteger fromSerialNo, BigInteger toSerialNo, Map<String, List<SerialNumberIndexedData>> serialNumberMap, Map<String, Integer> specieMap, Map<File, String> fileHeaderMap, CacheHolder instance) {

        logger.info("Start -------------------------------- fromSerialNo " + fromSerialNo + " toSerialNo " + toSerialNo);
        StringBuffer sb = new StringBuffer(JAXBUtility.XML_HEADER + CacheUtil.XML_CR + CacheUtil.XML_MISSIONS + CacheUtil.XML_CR);

        CatchSamplesSearch search = new CatchSamplesSearch();

        try {
            Map<File, String> map = new HashMap<File, String>();

            List<SerialNumberIndexedData> serialNumberList = serialNumberMap.get(year);
            Map<File, BigInteger> fileMap = new HashMap<File, BigInteger>();
            Range range = getRangeFiles(serialNumberList, fromSerialNo, toSerialNo, fileMap);
            logger.info("range size " + fromSerialNo + " --  " + toSerialNo);
            if (range != null) {
                int counter = 0;
                for (Map.Entry<File, BigInteger> entry : fileMap.entrySet()) {
                    logger.info(++counter + "   fileMap size -------------------------------" + fileMap.size());
                    File fileKey = entry.getKey();
                    for (int index = range.getFromInclusivePosition(); index < (range.getToInclusivePosition() + 1); index++) {
                        SerialNumberIndexedData element = serialNumberList.get(index);

                        File file = element.getFile();
                        if ((file.equals(fileKey))) {
                            SerialNoCompressedData data = element.getCompressedData();
                            if (!map.containsKey(file)) {
                                sb.append(fileHeaderMap.get(file));
                                map.put(file, " ");
                            }
//                                int[] speciesArr = element.getSpecies();
//                                int[] foundArray = binarySearchHandleDuplicates(speciesArr, specieMap.get(specie));
//                                if (foundArray != null) {
                            String fishStation = data.getFishstation();
                            int lindex = fishStation.lastIndexOf("</");
                            String lastLine = fishStation.substring(lindex);
                            fishStation = fishStation.substring(0, lindex);
                            sb.append(fishStation);
                            fishStation = null;
                            for (CompressedReference<String> catchSample : data.getCatchsample()) {
                                sb.append(catchSample.get());
                            }
//                                    List<String> dlist = data.getDecompressedCatchsampleList();
//                                    for (int sindex : foundArray) {
//                                        String catchSampleString = dlist.get(sindex);
//                                        sb.append(catchSampleString);
//                                        catchSampleString = null;
//                                    }
//                                    dlist = instance.clearList(dlist);
                            sb.append(lastLine);
//                                }
//                                foundArray = null;
                        }
                    }
                    if (map.containsKey(fileKey)) {
                        sb.append("</mission>");
                    }
                }
                sb.append("</missions>");
            } else {
                throw new NotFoundException("Not legal serialno range :" + fromSerialNo + " - " + toSerialNo);
            }
            if (map.isEmpty()) {
                sb = new StringBuffer();
                throw new NotFoundException("No data found for serialno range :" + fromSerialNo + " - " + toSerialNo);
            }

        } catch (Exception exp) {
            logger.error(exp.getMessage(), exp);
            logger.info("Excption ++++++++++++++++++++++++++++++++++ fromSerialNo " + fromSerialNo + " toSerialNo " + toSerialNo);
            if (exp instanceof S2DException) {
                throw new NotFoundException("Not legal serialno range :" + fromSerialNo + " - " + toSerialNo);
            } else {
                throw new ApplicationException(exp.getMessage(), exp);
            }
        }
        return sb.toString();
    }
}
