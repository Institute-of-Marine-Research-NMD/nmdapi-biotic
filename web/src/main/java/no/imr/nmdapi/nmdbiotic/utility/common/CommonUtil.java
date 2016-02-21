package no.imr.nmdapi.nmdbiotic.utility.common;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommonUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(CommonUtil.class);  
    
    public static long printTimeElapsed(long start, String startStr, String endString) {
        long time = 0;
        // if (logger.isDebugEnabled()) {
        if (logger.isInfoEnabled()) {
            time = System.currentTimeMillis() - start;
            if (start > 0) {
                if (logger.isInfoEnabled()) {
                    String str = String.format("%s%8d%s%3.2f%s\n", "milliseconds :", time, "  minutes :", (double) ((double) time / (double) (1000 * 60)), "   -    " + endString);
                    logger.info(str);
                }
                else {
                    logger.info(endString);
                }
            }
            else {
                logger.info(startStr);
            }
        }
        return time;
    }
    
    public static long printTimeElapsed(long start, String message) {
        return printTimeElapsed(start, start == 0 ? "START :"+message :"" , start == 0 ? "" : "END  :"+message);
    } 
    
    public static long printTimeElapsed(String message) {
        return printTimeElapsed(0, message);
    }     
    
    public static long printAverageTime(long time, int iterations, String endString) {
        if (logger.isInfoEnabled()) {
            logger.info(iterations+"");
            String str = (double) ((double) time / (double) (iterations))+ "   - average   " + endString;
            logger.info(str);
        }
        return time;
    }

}
