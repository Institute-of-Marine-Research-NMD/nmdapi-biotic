package no.imr.nmdapi.nmdbiotic.utility.files;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import no.imr.nmdapi.nmdbiotic.utility.common.CommonUtil;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FilesUtil {

    private static final Logger logger = LoggerFactory.getLogger(FilesUtil.class);

    public static final String XML_DATA_FILE = "data.xml";
    public static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    public static final String END_DATE_STR = "-01-01";
    public static final int STR_YEAR_FOLDER_INDEX = 5;

    public List<File> getFilesByFilter(String dir, String leafDirectory, String... directoryNameFilter) {

        Collection<File> list = FileUtils.listFilesAndDirs(new File(dir), new NotFileFilter(TrueFileFilter.INSTANCE), DirectoryFileFilter.DIRECTORY);

        IOFileFilter filter = new LeafFolderDirectoryFilter(XML_DATA_FILE, leafDirectory, directoryNameFilter);
        ArrayList<File> rlist = new ArrayList<File>();

        for (File file : list) {
            String path = file.getAbsolutePath();
            if (filter.accept(file)) {
                File sfile = new File(path + "/" + XML_DATA_FILE);
                rlist.add(sfile);
            }
        }
        return rlist;
    }

    public Map<File, Long> getFileMapByFilter(String dir, String leafDirectory, String... directoryNameFilter) throws IOException {
        long rtime = CommonUtil.printTimeElapsed("getFileMapByFilter");
        Map<File, Long> map = new ConcurrentHashMap<File, Long>();
        List<File> list = getFilesByFilter(dir, leafDirectory, directoryNameFilter);
        for (File file : list) {
            long lastModified = file.lastModified();
            map.put(file, lastModified);
        }
        CommonUtil.printTimeElapsed(rtime, "getFileMapByFilter");
        return map;
    }

    public String[] pathToSubfolders(File file) {
        String pathName = file.getAbsolutePath();
        return pathName.split("[\\\\|/]");
    }

    public boolean isYear(String str) {

        boolean ok = (str != null && str.matches("\\d{4}"));
        if (ok) {
            try {
                format.parse(str + END_DATE_STR);
            } catch (ParseException e) {
                ok = false;
            }
        }
        return ok;
    }

    private String getYearFolder(String[] subFolders) {

        int index = 0;
        int length = subFolders.length;
        while ((index < length) && !isYear(subFolders[index])) {
            index++;
        }
        return (index < length ? subFolders[index] : null);
    }

    private Map<Object, List<Object>> addMapList(Object key, Object file, Map<Object, List<Object>> map) {
        List<Object> list = map.get(key);
        if (list == null) {
            list = new ArrayList<Object>();
            map.put(key, list);
        }
        list.add(file);
        return map;
    }

    public String getYear(File file) {
        String year = null;
        String[] subfolderNames = pathToSubfolders(file);

        if (subfolderNames != null) {
            year = (subfolderNames.length > STR_YEAR_FOLDER_INDEX) ? subfolderNames[STR_YEAR_FOLDER_INDEX] : null;
            if (!isYear(year)) {
                year = getYearFolder(subfolderNames);
            }
        }
        return year;
    }

    public Map<String, List<File>> fileListToYearMap(Map<File, Long> paths) {
        Map<String, List<File>> yearMap = new HashMap<String, List<File>>();
        try {
            for (Map.Entry<File, Long> entry : paths.entrySet()) {
                File file = entry.getKey();
                String year = getYear(file);
                yearMap = (year != null ? (Map) addMapList(year, file, (Map) yearMap) : yearMap);
            }
        } catch (Exception exp) {
            logger.error(exp.getMessage(), exp);
        }
        return yearMap;
    }

    public Map<File, Long> fileListToMap(List<File> paths) {
        Map<File, Long> fileMap = new HashMap<File, Long>();
        try {
            for (File file : paths) {
                fileMap.put(file, file.lastModified());
            }
        } catch (Exception exp) {
            logger.error(exp.getMessage(), exp);
        }
        return fileMap;
    }

}
