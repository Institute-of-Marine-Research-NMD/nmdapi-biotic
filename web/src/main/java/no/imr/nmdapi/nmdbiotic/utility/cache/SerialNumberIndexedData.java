package no.imr.nmdapi.nmdbiotic.utility.cache;

import java.io.File;
import java.math.BigInteger;
import java.util.List;

public class SerialNumberIndexedData {
    
    private File file     = null;
    private BigInteger serialno = null;
    private int[] species  = null;  
    private CompressedReference<SerialNoCompressedData> compressedData = null;
    
    public SerialNoCompressedData getCompressedData() {
        //long rtime = CommonUtil.printTimeElapsed(0, "start decompressing SerialNoCompressedData", "");
        SerialNoCompressedData data = compressedData.get();
        //CommonUtil.printTimeElapsed(rtime, "", "end +++++++++++++++++++++++++++++++++++++++++compressing SerialNoCompressedData");   
        return data;
    }

    public void setCommpressedData(SerialNoCompressedData compressedData) {
        //long rtime = CommonUtil.printTimeElapsed(0, "start compressing SerialNoCompressedData", "");
        this.compressedData = new CompressedReference<SerialNoCompressedData>(compressedData);
        //CommonUtil.printTimeElapsed(rtime, "", "end -----------------------------------------compressing SerialNoCompressedData");   
    }

    public int[] getSpecies() {
        return species;
    }
    
    public void setSpecies(List<Integer> list) {
        for (int index = 0; index < list.size(); index++) {
            this.species[index] = list.get(index); 
        }
    }         

    public SerialNumberIndexedData() {
       this(0);
    }
    
    public SerialNumberIndexedData(BigInteger serialno) {
        setSerialno(serialno);
     }    
    
    public SerialNumberIndexedData(int specieLengh) {
        if (specieLengh > 0) {
            this.species = new int[specieLengh];
        }
    }
    
    public File getFile() {
        return file;
    }
    
    public void setFile(File file) {
        this.file = new File(file.getAbsolutePath());
    }
    
    public BigInteger getSerialno() {
        return serialno;
    }
    
    public void setSerialno(BigInteger serialno) {
        this.serialno = serialno;
    }
    
    public void dispose() {
        file     = null;
        serialno = null;
        species  = null;  
        compressedData.dispose();
        compressedData = null;
        
    }
}                                
