package ut.com.isroot.stash.plugin.mock;

import com.atlassian.applinks.api.ApplicationId;
import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.api.ApplicationType;
import com.atlassian.applinks.api.TypeNotInstalledException;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * @author Sean Ford
 * @since 2016-03-19
 */
public class MockApplicationLinkService implements ApplicationLinkService {
    private List<ApplicationLink> links;

    public MockApplicationLinkService(ApplicationLink... links) {
        this.links = Lists.newArrayList(links);
    }

    @Override
    public ApplicationLink getApplicationLink(ApplicationId applicationId) throws TypeNotInstalledException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<ApplicationLink> getApplicationLinks() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<ApplicationLink> getApplicationLinks(Class<? extends ApplicationType> aClass) {
        return links;
    }

    @Override
    public ApplicationLink getPrimaryApplicationLink(Class<? extends ApplicationType> aClass) {
        return links.get(0);
    }
}
