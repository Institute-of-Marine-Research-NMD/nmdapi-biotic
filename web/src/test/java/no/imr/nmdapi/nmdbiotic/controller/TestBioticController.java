package no.imr.nmdapi.nmdbiotic.controller;

import java.io.File;
import javax.xml.bind.JAXBContext;
import no.imr.nmdapi.generic.nmdbiotic.domain.v1.MissionType;
import no.imr.nmdapi.nmdbiotic.service.NMDBioticService;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import static org.mockito.Matchers.any;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.Mockito.doReturn;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
/**
 *
 * @author kjetilf
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:applicationContext.xml" })
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
    }

    @Test
    public void testFindByMission() throws Exception {
        MissionType missionType = (MissionType)JAXBContext.newInstance("no.imr.nmdapi.generic.nmdbiotic.domain.v1").createUnmarshaller().unmarshal(Thread.currentThread().getContextClassLoader().getResource("4-2015-4174-1.xml"));
        doReturn(missionType).when(mockBioticService).getData(any(String.class), any(String.class), any(String.class), any(String.class));
        mockMvc.perform(get("/Forskningsfartøy/2014/Johan%20Hjort-LDGJ/2014201"))
                .andExpect(status().isOk());
    }

    @Test
    public void testDeleteByMission() throws Exception {
        mockMvc.perform(delete("/Forskningsfartøy/2014/Johan%20Hjort-LDGJ/2014201"))
                .andExpect(status().isOk());
    }

    @Test
    public void testInsertByMission() throws Exception {
        Mockito.doAnswer(new Answer() {

            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return null;
            }

        }).when(mockBioticService).insertData(any(String.class), any(String.class), any(String.class), any(String.class), any(MissionType.class));
        mockMvc.perform(
                post("/Forskningsfartøy/2014/Johan%20Hjort-LDGJ/2014201")
                        .contentType(MediaType.APPLICATION_XML)
                        .content(FileUtils.readFileToString(new File(this.getClass().getClassLoader().getResource("4-2015-4174-1.xml").getFile())))
            )
            .andExpect(status().isOk());
    }


}
