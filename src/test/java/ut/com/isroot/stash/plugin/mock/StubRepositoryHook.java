package ut.com.isroot.stash.plugin.mock;

import com.atlassian.bitbucket.hook.repository.RepositoryHook;
import com.atlassian.bitbucket.hook.repository.RepositoryHookDetails;

import javax.annotation.Nonnull;

/**
 * @author Sean Ford
 * @since 2017-09-04
 */
public class StubRepositoryHook implements RepositoryHook {
    private boolean enabled;
    private RepositoryHookDetails details;

    public StubRepositoryHook() {
        enabled = false;
    }

    public void setDetails(RepositoryHookDetails details) {
        this.details = details;
    }

    @Nonnull
    @Override
    public RepositoryHookDetails getDetails() {
        return details;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean isConfigured() {
        throw new UnsupportedOperationException();
    }
}
