package no.imr.nmdapi.nmdbiotic.utility.cache;

import java.util.concurrent.ThreadFactory;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EThread extends Thread implements ThreadFactory {

    public static Logger logger     = LoggerFactory.getLogger(EThread.class.getName());

    private String       root       = null;
    private boolean      cancelled = false;
    private Object       userData   = null;

    public EThread() {
        super();
    }

    public EThread newThread(Runnable r) {
        return new EThread();
    }

    public void setRootSrc(EThread thread) {
        this.root = thread.toString() + " " + thread.getClass().getSimpleName();
    }

    public String getRootSrc() {
        return this.root;
    }

    public void setUserData(Object userData) {
        this.userData = userData;
    }

    public Object getUserData() {
        return userData;
    }

    public void msSleep(long ms) {
        try {
            Thread.sleep(ms);
        }
        catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean terminated) {
        this.cancelled = terminated;
    }

}
