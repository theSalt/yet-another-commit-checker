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

    public StubRepositoryHook() {
        enabled = false;
    }

    @Nonnull
    @Override
    public RepositoryHookDetails getDetails() {
        throw new UnsupportedOperationException();
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
