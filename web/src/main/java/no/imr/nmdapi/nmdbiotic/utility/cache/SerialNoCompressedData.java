package no.imr.nmdapi.nmdbiotic.utility.cache;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class SerialNoCompressedData implements Serializable {
    
    private static final long serialVersionUID = 3375883681764303052L;
    private CompressedReference<String> fishstation = null;
    private List<CompressedReference<String>> catchsample = new ArrayList<CompressedReference<String>>();

    public String getFishstation() {
        return fishstation.get();
    }
    public void setFishstation(String fishstation) {
        this.fishstation = new CompressedReference<String>(fishstation);
    }
    public List<CompressedReference<String>> getCatchsample() {
        return catchsample;
    }
    public void setCatchsample(List<String> catchsample) {
        //long rtime = CommonUtil.printTimeElapsed(0, "start setCatchsample", "");
        for (String string : catchsample) {
            this.catchsample.add(new CompressedReference<String>(string));
        }
        //CommonUtil.printTimeElapsed(rtime, "", "end setCatchsample");           
    }
    public List<String> getDecompressedCatchsampleList() {
        List<String> list = new ArrayList<String>();
        //long rtime = CommonUtil.printTimeElapsed(0, "start getCatchsampleDecompress", "");        
        for (CompressedReference<String> compressedReference : catchsample) {
            list.add(compressedReference.get());
        }
        //CommonUtil.printTimeElapsed(rtime, "", "end getCatchsampleDecompress");    
        return list;
    }  
    
    public List<String> getDecompressedCatchsampleList(int[] indexArr) {
        List<String> list = new ArrayList<String>();
        //long rtime = CommonUtil.printTimeElapsed(0, "start getCatchsampleDecompress", "");        
        for (CompressedReference<String> compressedReference : catchsample) {
            list.add(compressedReference.get());
        }
        //CommonUtil.printTimeElapsed(rtime, "", "end getCatchsampleDecompress");    
        return list;
    }    
    
    public void dispose() {
        fishstation.dispose();
        fishstation = null;
        for (CompressedReference<String> compressedReference : catchsample) {
            compressedReference.dispose();
            compressedReference = null;
        }
        catchsample.clear();
        catchsample = null;
        
    }
}
