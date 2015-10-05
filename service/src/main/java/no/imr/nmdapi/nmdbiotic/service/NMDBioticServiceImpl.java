package no.imr.nmdapi.nmdbiotic.service;

import java.nio.file.Path;
import java.util.GregorianCalendar;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import no.imr.nmd.commons.dataset.jaxb.DataTypeEnum;
import no.imr.nmd.commons.dataset.jaxb.QualityEnum;
import no.imr.nmdapi.dao.file.NMDDatasetDao;
import no.imr.nmdapi.exceptions.S2DException;
import no.imr.nmdapi.generic.nmdbiotic.domain.v1.MissionType;
import no.imr.nmdapi.generic.response.v1.OptionKeyValueListType;
import no.imr.nmdapi.generic.response.v1.OptionKeyValueType;
import org.apache.commons.configuration.Configuration;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * NMDEchosounder service layer implementation.
 *
 * @author kjetilf
 */
public class NMDBioticServiceImpl implements NMDBioticService {

    /**
     * Class logger.
     */
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(NMDBioticServiceImpl.class);

    /**
     * Dataset name.
     */
    private static final String DATASET_NAME = "data";

    @Autowired
    private NMDDatasetDao nmdDatasetDao;

    @Autowired
    private Configuration configuration;

    @Override
    public Object getData(final String missiontype, final String year, final String platform, final String delivery) {
        return nmdDatasetDao.get(DataTypeEnum.BIOTIC, DATASET_NAME, missiontype, year, platform, delivery);
    }

    @Override
    public void deleteData(final String missiontype, final String year, final String platform, final String delivery) {
        nmdDatasetDao.delete(DataTypeEnum.BIOTIC, DATASET_NAME, true, missiontype, year, platform, delivery);
        nmdDatasetDao.removeDataset(DataTypeEnum.BIOTIC, DATASET_NAME, missiontype, year, platform, delivery);
    }

    @Override
    public void insertData(final String missiontype, final String year, final String platform, final String delivery, final MissionType dataset) {
        try {
            String readRole = configuration.getString("default.readrole");
            String writeRole = configuration.getString("default.writerole");
            String owner = configuration.getString("default.owner");

            GregorianCalendar gregorianCalendar = new GregorianCalendar();
            DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();
            XMLGregorianCalendar now = datatypeFactory.newXMLGregorianCalendar(gregorianCalendar);

            nmdDatasetDao.insert(DataTypeEnum.BIOTIC, DATASET_NAME, dataset, missiontype, year, platform, delivery);
            nmdDatasetDao.createDataset(writeRole, readRole, "", owner, QualityEnum.NONE, DataTypeEnum.BIOTIC, DATASET_NAME, now, missiontype, year, platform, delivery);
        } catch (DatatypeConfigurationException ex) {
            LOGGER.error("Error creating xml calendar", ex);
            throw new S2DException("Error creating xml calendar", ex);
        }
    }

    @Override
    public void updateData(final String missiontype, final String year, final String platform, final String delivery, final MissionType dataset) {
        try {
            nmdDatasetDao.update(DataTypeEnum.BIOTIC, DATASET_NAME, dataset, missiontype, year, platform, delivery);
            GregorianCalendar gregorianCalendar = new GregorianCalendar();
            DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();
            XMLGregorianCalendar now = datatypeFactory.newXMLGregorianCalendar(gregorianCalendar);
            nmdDatasetDao.updateDataset(DataTypeEnum.BIOTIC, DATASET_NAME, now, missiontype, year, platform, delivery);
        } catch (DatatypeConfigurationException ex) {
            LOGGER.error("Error creating xml calendar", ex);
            throw new S2DException("Error creating xml calendar", ex);
        }
    }

    @Override
    public boolean hasData(String missiontype, String year, String platform, String delivery) {
        return nmdDatasetDao.hasData(DataTypeEnum.BIOTIC, DATASET_NAME, missiontype, year, platform, delivery);
    }

    @Override
    public Object getDataByCruiseNr(final String cruisenr, final String shipname, String contextpath) {
        Path path = nmdDatasetDao.getByCruisenr(DataTypeEnum.BIOTIC, DATASET_NAME, cruisenr, shipname);
        OptionKeyValueListType keyValueListType = new OptionKeyValueListType();
        keyValueListType.getElement().add(getOptionKeyValueType("url", getUrl(contextpath, path)));
        return keyValueListType;
    }

    @Override
    public boolean hasDataByCruiseNr(final String cruisenr, final String shipname) {
        return nmdDatasetDao.hasDataByCruisenr(DataTypeEnum.BIOTIC, DATASET_NAME, cruisenr, shipname);
    }

    @Override
    public Object getInfo(String missiontype, String year, String platform, String delivery) {
        String format = nmdDatasetDao.getRootNamespace(DataTypeEnum.BIOTIC, DATASET_NAME, missiontype, year, platform, delivery);
        long checksum = nmdDatasetDao.getChecksum(DataTypeEnum.BIOTIC, DATASET_NAME, missiontype, year, platform, delivery);
        long lastModified = nmdDatasetDao.getLastModified(DataTypeEnum.BIOTIC, DATASET_NAME, missiontype, year, platform, delivery);
        OptionKeyValueListType keyValueListType = new OptionKeyValueListType();
        keyValueListType.getElement().add(getOptionKeyValueType("format", format));
        keyValueListType.getElement().add(getOptionKeyValueType("checksum", String.valueOf(checksum)));
        keyValueListType.getElement().add(getOptionKeyValueType("lastModified", String.valueOf(lastModified)));
        return keyValueListType;
    }

    private OptionKeyValueType getOptionKeyValueType(String key, String value) {
        OptionKeyValueType formatType = new OptionKeyValueType();
        formatType.setKey(key);
        formatType.setValue(value);
        return formatType;
    }

    private String getUrl(String contextpath, Path path) {
        StringBuilder builder = new StringBuilder();
        builder.append(contextpath);
        builder.append("/");
        builder.append(path.getName(path.getNameCount() - 5));
        builder.append("/");
        builder.append(path.getName(path.getNameCount() - 4));
        builder.append("/");
        builder.append(path.getName(path.getNameCount() - 3));
        builder.append("/");
        builder.append(path.getName(path.getNameCount() - 2));
        return builder.toString();
    }

}
