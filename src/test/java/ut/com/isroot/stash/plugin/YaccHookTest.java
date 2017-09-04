package ut.com.isroot.stash.plugin;

import com.atlassian.bitbucket.hook.repository.PreRepositoryHookContext;
import com.atlassian.bitbucket.hook.repository.RepositoryPushHookRequest;
import com.atlassian.bitbucket.setting.Settings;
import com.isroot.stash.plugin.YaccHook;
import com.isroot.stash.plugin.YaccService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ut.com.isroot.stash.plugin.mock.MockSettings;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Sean Ford
 * @since 2014-01-15
 */
public class YaccHookTest {
    @Mock private YaccService yaccService;
    @Mock private PreRepositoryHookContext repositoryHookContext;
    @Mock private RepositoryPushHookRequest repositoryPushHookRequest;

    private Settings settings;

    private YaccHook yaccHook;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        yaccHook = new YaccHook(yaccService);

        settings = new MockSettings();
        when(repositoryHookContext.getSettings()).thenReturn(settings);
    }

    @Test
    public void testPreUpdate_callsYaccService() {
        yaccHook.preUpdate(repositoryHookContext, repositoryPushHookRequest);

        verify(yaccService).check(repositoryHookContext, repositoryPushHookRequest, settings);
    }
}
