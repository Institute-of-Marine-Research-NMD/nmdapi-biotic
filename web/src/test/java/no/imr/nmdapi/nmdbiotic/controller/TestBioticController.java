package no.imr.nmdapi.nmdbiotic.controller;

import java.io.File;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import no.imr.nmdapi.generic.nmdbiotic.domain.v1.MissionType;
import no.imr.nmdapi.nmdbiotic.service.NMDBioticService;
import org.apache.commons.io.FileUtils;
import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.XMLUnit;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import static org.mockito.Matchers.any;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

/**
 *
 * @author kjetilf
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:applicationContext.xml"})
@WebAppConfiguration
public class TestBioticController {

    private MockMvc mockMvc;

    @InjectMocks
    private BioticController controller;

    @Mock
    private NMDBioticService mockBioticService;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mockMvc = standaloneSetup(controller)
                .build();
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreAttributeOrder(true);
        XMLUnit.setIgnoreComments(true);
    }

    /**
     * Test that expected GET method works.
     *
     * @throws Exception
     */
    @Test
    public void testFindByMission() throws Exception {
        MissionType missionType = (MissionType) ((JAXBElement) JAXBContext.newInstance("no.imr.nmdapi.generic.nmdbiotic.domain.v1").createUnmarshaller().unmarshal(Thread.currentThread().getContextClassLoader().getResource("4-2015-4174-1.xml"))).getValue();
        doReturn(missionType).when(mockBioticService).getData(any(String.class), any(String.class), any(String.class), any(String.class));
        mockMvc.perform(get("/Forskningsfartøy/2014/Johan%20Hjort-LDGJ/2014201"))
                .andExpect(status().isOk());
    }

    /**
     * Test that delete method works
     *
     * @throws Exception
     */
    @Test
    public void testDeleteByMission() throws Exception {
        mockMvc.perform(delete("/Forskningsfartøy/2014/Johan%20Hjort-LDGJ/2014201"))
                .andExpect(status().isOk());
    }

    /**
     * Test that insert method works.
     *
     * @throws Exception
     */
    @Test
    public void testInsertByMission() throws Exception {
        Mockito.doAnswer(new Answer() {

            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                String originalData = FileUtils.readFileToString(new File(this.getClass().getClassLoader().getResource("4-2015-4174-1.xml").getFile()), "UTF-8");
                File tempfile = File.createTempFile("biotictest", ".xml");
                Marshaller marshaller = JAXBContext.newInstance("no.imr.nmd.commons.dataset.jaxb:no.imr.nmdapi.generic.nmdbiotic.domain.v1").createMarshaller();
                marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
                marshaller.marshal(invocation.getArguments()[4], tempfile);
                String resultData = FileUtils.readFileToString(tempfile, "UTF-8");

                DetailedDiff diff = new DetailedDiff(XMLUnit.compareXML(originalData, resultData));
                assertEquals(0, diff.getAllDifferences().size());
                return null;
            }

        }).when(mockBioticService).insertData(any(String.class), any(String.class), any(String.class), any(String.class), any(MissionType.class));
        mockMvc.perform(
                post("/Forskningsfartøy/2014/Johan%20Hjort-LDGJ/2014201")
                .contentType(MediaType.APPLICATION_XML)
                .content(FileUtils.readFileToString(new File(this.getClass().getClassLoader().getResource("4-2015-4174-1.xml").getFile()), "UTF-8"))
        )
                .andExpect(status().isOk());
        verify(mockBioticService).insertData(any(String.class), any(String.class), any(String.class), any(String.class), any(MissionType.class));
    }

    /**
     * Test update.
     *
     * @throws Exception
     */
    @Test
    public void testUpdateByMission() throws Exception {
        Mockito.doAnswer(new Answer() {

            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                String originalData = FileUtils.readFileToString(new File(this.getClass().getClassLoader().getResource("4-2015-4174-1.xml").getFile()), "UTF-8");
                File tempfile = File.createTempFile("biotictest", ".xml");
                Marshaller marshaller = JAXBContext.newInstance("no.imr.nmd.commons.dataset.jaxb:no.imr.nmdapi.generic.nmdbiotic.domain.v1").createMarshaller();
                marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
                marshaller.marshal(invocation.getArguments()[4], tempfile);
                String resultData = FileUtils.readFileToString(tempfile, "UTF-8");
                DetailedDiff diff = new DetailedDiff(XMLUnit.compareXML(originalData, resultData));
                assertEquals(0, diff.getAllDifferences().size());
                return null;
            }

        }).when(mockBioticService).updateData(any(String.class), any(String.class), any(String.class), any(String.class), any(MissionType.class));
        mockMvc.perform(
                put("/Forskningsfartøy/2014/Johan%20Hjort-LDGJ/2014201")
                .contentType(MediaType.APPLICATION_XML)
                .content(FileUtils.readFileToString(new File(this.getClass().getClassLoader().getResource("4-2015-4174-1.xml").getFile()), "UTF-8"))
        )
                .andExpect(status().isOk());
        verify(mockBioticService).updateData(any(String.class), any(String.class), any(String.class), any(String.class), any(MissionType.class));
    }

}
