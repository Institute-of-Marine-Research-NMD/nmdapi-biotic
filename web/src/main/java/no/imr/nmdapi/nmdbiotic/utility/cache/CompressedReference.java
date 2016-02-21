package no.imr.nmdapi.nmdbiotic.utility.cache;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CompressedReference<T extends Serializable> implements Serializable {

    private static final long serialVersionUID      = 7967994340450625830L;
    
    private static final Logger logger        = LoggerFactory.getLogger(CompressedReference.class);

    private byte[]            theCompressedReferent = null;

    public CompressedReference(T referent) {
        try {
            compress(referent);
        }
        catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public int size() {
        return theCompressedReferent.length;
    }

    public T get() {
        try {
            return decompress();
        }
        catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        catch (ClassNotFoundException e) {
            logger.error(e.getMessage(), e);;
        }
        return null;
    }

    private void compress(T referent) throws IOException {

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        GZIPOutputStream zos = new GZIPOutputStream(bos);
        ObjectOutputStream ous = new ObjectOutputStream(zos);

        ous.writeObject(referent);

        zos.finish();
        bos.flush();

        theCompressedReferent = bos.toByteArray();
        
        ous.close();
        zos.close();
        bos.close();
    }

    @SuppressWarnings("unchecked")
    private T decompress() throws IOException, ClassNotFoundException {
        T tmpObject = null;
        ByteArrayInputStream bis = new ByteArrayInputStream(theCompressedReferent);
        GZIPInputStream zis = new GZIPInputStream(bis);
        ObjectInputStream ois = new ObjectInputStream(zis);
        tmpObject = (T) ois.readObject();

        ois.close();
        zis.close();
        bis.close();

        return tmpObject;
    }
    
    public void dispose() {
        theCompressedReferent = null;
    }
    
}
