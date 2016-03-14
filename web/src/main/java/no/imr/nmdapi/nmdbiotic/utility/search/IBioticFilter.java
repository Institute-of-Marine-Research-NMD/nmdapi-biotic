package no.imr.nmdapi.nmdbiotic.utility.search;

import no.imr.nmdapi.nmdbiotic.utility.cache.SerialNumberIndexedData;

public interface IBioticFilter {

    public boolean accept(SerialNumberIndexedData element, Object key);

}
