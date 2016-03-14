package no.imr.nmdapi.nmdbiotic.utility.search;

import java.util.Comparator;

import no.imr.nmdapi.nmdbiotic.utility.cache.SerialNumberIndexedData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SerialNumberComparator implements Comparator<SerialNumberIndexedData> {

    private static Logger logger = LoggerFactory.getLogger(SerialNumberComparator.class.getName());

    private int direction = 1;

    public SerialNumberComparator() {
        this(1);
    }

    public SerialNumberComparator(int direction) {
        this.direction = direction;
    }

    @Override
    public int compare(SerialNumberIndexedData o1, SerialNumberIndexedData o2) {
        int result = o1.getSerialno().compareTo(o2.getSerialno());
        if (result == 0) {
            logger.info(" " + o1.getSerialno());
        }
        result = (direction == 1 || result == 0) ? result : result == 1 ? -1 : 1;
        return result;
    }

}
