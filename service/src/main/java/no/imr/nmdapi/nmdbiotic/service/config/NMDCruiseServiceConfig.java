package no.imr.nmdapi.nmdbiotic.service.config;

import no.imr.nmdapi.nmdbiotic.service.NMDBioticService;
import no.imr.nmdapi.nmdbiotic.service.NMDBioticServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This contains all configuration for the reference services.
 *
 * @author kjetilf
 */
@Configuration
public class NMDCruiseServiceConfig {

    /**
     * Creates the service implementation.
     *
     * @return  A reference service implementation.
     */
    @Bean(name="nmdCruiseService")
    public NMDBioticService getNMDCruiseService() {
        return new NMDBioticServiceImpl();
    }

}
