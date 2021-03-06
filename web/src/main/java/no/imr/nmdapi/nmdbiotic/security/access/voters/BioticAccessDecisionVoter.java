package no.imr.nmdapi.nmdbiotic.security.access.voters;

import java.util.Collection;
import java.util.HashSet;
import no.imr.nmd.commons.dataset.jaxb.DataTypeEnum;
import no.imr.nmdapi.dao.file.NMDDatasetDao;
import no.imr.nmdapi.nmdbiotic.controller.BioticController;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.AccessDecisionVoter;
import static org.springframework.security.access.AccessDecisionVoter.ACCESS_ABSTAIN;
import static org.springframework.security.access.AccessDecisionVoter.ACCESS_DENIED;
import static org.springframework.security.access.AccessDecisionVoter.ACCESS_GRANTED;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.FilterInvocation;
import org.springframework.stereotype.Service;

/**
 * Access decision voter for biotic data. As all data is standard available this
 * voter always returns access.
 *
 * @author kjetilf
 */
@Service
public class BioticAccessDecisionVoter implements AccessDecisionVoter<FilterInvocation> {

    /**
     * Class logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(BioticAccessDecisionVoter.class);

    /**
     * How long is the expected path when all arguments.
     */
    private static final int FULL_PATH_ARG_LENGTH = 5;
    /**
     * Argument number for delivery after split.
     */
    private static final int DELIVERY_PATH = 4;
    /**
     * Argument number for platform after split.
     */
    private static final int PLATFORM_PATH = 3;
    /**
     * Argument number for year after split.
     */
    private static final int YEAR_PATH = 2;
    /**
     * Argument number for mission type after split.
     */
    private static final int MISSIONTYPE_PATH = 1;

    /**
     * Denied string literal.
     */
    private static final String DENIED = "Denied";
    /**
     * Granted string literal.
     */
    private static final String GRANTED = "Granted";
    /**
     * Abstained string literal.
     */
    private static final String ABSTAINED = "Abstained";

    @Autowired
    private NMDDatasetDao datasetDao;

    @Autowired
    private Configuration configuration;

    @Override
    public boolean supports(ConfigAttribute attribute) {
        return true;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.isAssignableFrom(FilterInvocation.class);
    }

    @Override
    public int vote(Authentication auth, FilterInvocation obj, Collection<ConfigAttribute> confAttrs) {
        if (obj.getFullRequestUrl().contains(BioticController.BIOTIC_URL)) {
            return checkAccess(obj, auth);
        } else {
            LOGGER.info(ABSTAINED);
            return ACCESS_ABSTAIN;
        }
    }

    private int checkAccess(FilterInvocation obj, Authentication auth) {
        if (configuration.getBoolean("use.security", Boolean.TRUE)) {
            if (obj.getHttpRequest().getMethod().equalsIgnoreCase(HttpMethod.POST.name())) {
                return checkAccessInsert(auth);
            } else if (obj.getHttpRequest().getMethod().equalsIgnoreCase(HttpMethod.PUT.name()) || obj.getHttpRequest().getMethod().equalsIgnoreCase(HttpMethod.DELETE.name())) {
                return checkAccessUpdate(auth, obj);
            } else if (obj.getHttpRequest().getMethod().equalsIgnoreCase(HttpMethod.GET.name())) {
                return checkAccessGet(auth, obj);
            } else {
                LOGGER.info(GRANTED);
                return ACCESS_GRANTED;
            }
        } else {
            return ACCESS_GRANTED;
        }
    }

    private int checkAccessGet(Authentication auth, FilterInvocation obj) {
        Collection<String> auths = getAuths(auth.getAuthorities());
        String[] args = obj.getRequestUrl().split("/");
        if (args.length != FULL_PATH_ARG_LENGTH) {
            LOGGER.info(GRANTED);
            return ACCESS_GRANTED;
        } else if (datasetDao.hasReadAccess(auths, DataTypeEnum.BIOTIC, "data", args[MISSIONTYPE_PATH], args[YEAR_PATH], args[PLATFORM_PATH], args[DELIVERY_PATH])) {
            return ACCESS_GRANTED;
        } else {
            LOGGER.info(DENIED);
            return ACCESS_DENIED;
        }
    }

    private int checkAccessUpdate(Authentication auth, FilterInvocation obj) {
        Collection<String> auths = getAuths(auth.getAuthorities());
        String[] args = obj.getRequestUrl().split("/");
        if (auth.isAuthenticated() && datasetDao.hasWriteAccess(auths, DataTypeEnum.BIOTIC, "data", args[MISSIONTYPE_PATH], args[YEAR_PATH], args[PLATFORM_PATH], args[DELIVERY_PATH])) {
            LOGGER.info(GRANTED);
            return ACCESS_GRANTED;
        } else {
            LOGGER.info(DENIED);
            return ACCESS_DENIED;
        }
    }

    private int checkAccessInsert(Authentication auth) {
        if (auth.isAuthenticated() && auth.getAuthorities().contains(new SimpleGrantedAuthority(configuration.getString("default.writerole")))) {
            LOGGER.info(GRANTED);
            return ACCESS_GRANTED;
        } else {
            LOGGER.info(DENIED);
            return ACCESS_DENIED;
        }
    }

    private Collection<String> getAuths(Collection<? extends GrantedAuthority> auths) {
        Collection<String> authsStr = new HashSet<String>();
        for (GrantedAuthority authority : auths) {
            authsStr.add(authority.getAuthority());
        }
        return authsStr;
    }

}
