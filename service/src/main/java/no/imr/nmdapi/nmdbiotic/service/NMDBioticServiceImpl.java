package no.imr.nmdapi.nmdbiotic.service;

import no.imr.nmdapi.dao.file.NMDDataDao;
import no.imr.nmdapi.generic.nmdbiotic.domain.v1.MissionType;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * NMDEchosounder service layer implementation.
 *
 * @author kjetilf
 */
public class NMDBioticServiceImpl implements NMDBioticService {

    @Autowired
    private NMDDataDao nmdDataDao;

    @Override
    public Object getData(final String missiontype, final String year, final String platform, final String delivery) {
        return nmdDataDao.get(missiontype, year, platform, delivery, MissionType.class);
    }

    @Override
    public void deleteData(final String missiontype, final String year, final String platform, final String delivery) {
        nmdDataDao.delete(missiontype, year, platform, delivery);
        nmdDataDao.deleteDataset(missiontype, year, platform, delivery, "BIOTIC");
    }

   @Override
    public void insertData(final String missiontype, final String year, final String platform, final String delivery, final MissionType dataset) {
        nmdDataDao.insert(missiontype, year, platform, delivery, dataset, MissionType.class);
        nmdDataDao.insertDataset(missiontype, year, platform, delivery, "BIOTIC");
    }


    @Override
    public void updateData(final String missiontype, final String year, final String platform, final String delivery, final MissionType dataset) {
        nmdDataDao.update(missiontype, year, platform, delivery, dataset, MissionType.class);
    }

    @Override
    public boolean hasData(String missiontype, String year, String platform, String delivery) {
        return nmdDataDao.hasData(missiontype, year, platform, delivery);
    }

    @Override
    public Object getDataByCruiseNr(String cruisenr) {
        return nmdDataDao.getByCruiseNr(MissionType.class, cruisenr);
    }

    @Override
    public boolean hasDataByCruiseNr(String cruisenr) {
        return nmdDataDao.hasDataByCruiseNr(cruisenr);
    }

}
