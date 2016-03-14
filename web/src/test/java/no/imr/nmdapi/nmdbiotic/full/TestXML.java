package no.imr.nmdapi.nmdbiotic.full;

import java.io.IOException;
import java.io.InputStream;
import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.XMLUnit;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author kjetilf
 */
public class TestXML {

    @Before
    public void setUp() throws Exception {
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreAttributeOrder(true);
        XMLUnit.setIgnoreComments(true);
    }

    @Test
    public void test() throws SAXException, IOException {
        InputStream datastream1 = TestXML.class.getClassLoader().getResourceAsStream("4-2015-4174-1.xml");
        InputStream datastream2 = TestXML.class.getClassLoader().getResourceAsStream("4-2015-4174-1_failure.xml");
        InputSource source1 = new InputSource(datastream1);
        InputSource source2 = new InputSource(datastream2);

        Diff d = new Diff(source2, source1);
        DetailedDiff dd = new DetailedDiff(d);
        for (Object differenceObj : dd.getAllDifferences()) {
            Difference difference = (Difference) differenceObj;
            System.out.println(difference.toString());
        }
        assertEquals(1, dd.getAllDifferences().size());
    }

}
