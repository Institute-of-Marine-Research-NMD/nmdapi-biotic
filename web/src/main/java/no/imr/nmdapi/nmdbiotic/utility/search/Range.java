package no.imr.nmdapi.nmdbiotic.utility.search;


public class Range {

    
    private int fromInclusivePosition = -1;
    private int toInclusivePosition = -1;
    

    public int getFromInclusivePosition() {
        return fromInclusivePosition;
    }


    public int getToInclusivePosition() {
        return toInclusivePosition;
    }



    /**                                                                                                                    
     * Creates an instance.                                                                                                
     *                                                                                                                     
     * @param fromInclusivePosition  the first element, not null                                                                        
     * @param toInclusivePosition  the second element, not null                                                                       
     * @param comp  the comparator to be used, null for natural ordering                                                   
     */                                                                                                                    
    public Range(final int fromInclusivePosition, final int toInclusivePosition) {

        this.fromInclusivePosition = fromInclusivePosition;
        this.toInclusivePosition = toInclusivePosition;
        
    }    
    

    

    
   

}
