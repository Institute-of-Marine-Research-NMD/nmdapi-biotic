

import com.meterware.httpunit.DeleteMethodWebRequest;
import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebResponse;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import static org.junit.Assert.assertEquals;
import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.SAXException;

/**
 * Integration tests for biotic data.
 *
 * @author kjetilf
 */
public class TestBioticIT {

    @Test
    @Ignore
    public void testPostGetDeleteData() throws IOException, SAXException {
        System.out.println("testGetTables");
        WebConversation wc = new WebConversation();
        InputStream source = Thread.currentThread().getContextClassLoader().getResourceAsStream("4-2015-4174-1.xml");
        PostMethodWebRequest postMethod = new PostMethodWebRequest("http://localhost:8080/apis/nmdapi/biotic/v1/Forskningsfart%C3%B8y/2015/GOSars-LMEL/2015101", source, "application/xml");
        WebResponse response = wc.sendRequest(postMethod);
    }


}
