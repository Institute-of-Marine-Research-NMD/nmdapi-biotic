package no.imr.nmdapi.nmdbiotic.utility.files;

import java.io.File;

import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SubFolderFilter extends DirectoryFileFilter {

    private static final long serialVersionUID = -4148299011814563686L;
    public static Logger logger = LoggerFactory.getLogger(SubFolderFilter.class.getName());

    private String subfolderMatch = "1900";

    public String getSubfolderMatch() {
        return subfolderMatch;
    }

    public void setSubfolderMatch(String subfolderMatch) {
        this.subfolderMatch = subfolderMatch;
    }

    public SubFolderFilter() {
    }

    public boolean matchesDirectory(String path, String subfolderName) {
        int index = path.indexOf(subfolderName);
        boolean match = index > -1;
        if (match) {
            String dirName = path.substring(0, index) + "//" + subfolderName + "//";
            File file = new File(dirName);
            match = (file != null) && (file.isDirectory());
        }
        return match;
    }

    @Override
    public boolean accept(File file) {
        return matchesDirectory(file.getAbsolutePath(), getSubfolderMatch());
    }
}
