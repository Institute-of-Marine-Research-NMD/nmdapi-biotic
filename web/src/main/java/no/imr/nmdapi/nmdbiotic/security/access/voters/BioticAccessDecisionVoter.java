package no.imr.nmdapi.nmdbiotic.security.access.voters;

import java.util.Collection;
import no.imr.nmdapi.nmdbiotic.controller.BioticController;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.AccessDecisionVoter;
import static org.springframework.security.access.AccessDecisionVoter.ACCESS_ABSTAIN;
import static org.springframework.security.access.AccessDecisionVoter.ACCESS_DENIED;
import static org.springframework.security.access.AccessDecisionVoter.ACCESS_GRANTED;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;
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
     * Role to have to gain write access.
     */
    private static final String ROLE_WRITE_ACCESS = "SG-FAG-430-NMD";

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
            if (obj.getHttpRequest().getMethod().equalsIgnoreCase(HttpMethod.GET.name()) || obj.getHttpRequest().getMethod().equalsIgnoreCase(HttpMethod.HEAD.name())) {
                // Read access.
                return ACCESS_GRANTED;
            } else {
                // Write access.
                if (auth.isAuthenticated() && auth.getAuthorities().contains(new SimpleGrantedAuthority(BioticAccessDecisionVoter.ROLE_WRITE_ACCESS))) {
                    return ACCESS_GRANTED;
                } else {
                    return ACCESS_DENIED;
                }
            }
        } else {
            // Not reference data.
            return ACCESS_ABSTAIN;
        }
    }

}
