package no.imr.nmdapi.nmdbiotic.utility.files;

import java.io.File;

import org.apache.commons.io.filefilter.IOFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LeafFolderDirectoryFilter extends SubFolderFilter {

    /**
     *
     */
    private static final long serialVersionUID = -5154839653345820098L;

    public static Logger logger = LoggerFactory.getLogger(LeafFolderDirectoryFilter.class.getName());

    public static final IOFileFilter DIRECTORY = new LeafFolderDirectoryFilter();
    /**
     * Singleton instance of directory filter. values when using static imports.
     */
    public static final IOFileFilter INSTANCE = DIRECTORY;

    private String[] directories = null;
    private String leafDirectory = "biotic";
    private String leafFile = "data.xml";

    public LeafFolderDirectoryFilter() {
    }

    public LeafFolderDirectoryFilter(String leafFile, String leafDirectory, String... directories) {
        this.directories = directories;
        this.leafDirectory = leafDirectory != null ? leafDirectory : this.leafDirectory;
        this.leafFile = leafFile;
    }

    private boolean isLeafDirectoryName(String path) {
        return path.endsWith(this.leafDirectory);
    }

    private boolean matchesDirFilter(String path) {
        boolean match = false;
        if (isLeafDirectoryName(path) && isLeafFile(path, this.leafFile)) {
            match = (directories != null);
            int counter = 0;
            while (match && (counter < directories.length)) {
                match = match && matchesDirectory(path, directories[counter++]);
            }

        }
        return match;
    }

    @Override
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

    private boolean isLeafFile(String path, String fileName) {
        File file = new File(path + "//" + fileName);
        return (file != null) && (file.isFile());
    }

    private boolean acceptSubdir(File dir) {
        String path = dir.getAbsolutePath();
        return matchesDirFilter(path);
    }

    @Override
    public boolean accept(File file) {
        return acceptSubdir(file);
    }

}
