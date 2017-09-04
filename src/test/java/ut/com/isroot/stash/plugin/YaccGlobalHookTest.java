package ut.com.isroot.stash.plugin;

import com.atlassian.bitbucket.hook.repository.PreRepositoryHookContext;
import com.atlassian.bitbucket.hook.repository.RepositoryHookResult;
import com.atlassian.bitbucket.hook.repository.RepositoryHookService;
import com.atlassian.bitbucket.hook.repository.RepositoryPushHookRequest;
import com.atlassian.bitbucket.permission.Permission;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.setting.Settings;
import com.atlassian.bitbucket.user.EscalatedSecurityContext;
import com.atlassian.bitbucket.user.SecurityService;
import com.atlassian.bitbucket.util.UncheckedOperation;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.testresources.pluginsettings.MockPluginSettingsFactory;
import com.isroot.stash.plugin.YaccConfigServlet;
import com.isroot.stash.plugin.YaccGlobalHook;
import com.isroot.stash.plugin.YaccService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ut.com.isroot.stash.plugin.mock.MockSettingsBuilder;
import ut.com.isroot.stash.plugin.mock.StubRepositoryHook;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;


/**
 * Cloned from YaccHookTest.java and modified to test against the PreReceiveHook interface.
 *
 * @author Jim Bethancourt
 */
public class YaccGlobalHookTest {
    @Mock
    private YaccService yaccService;
    @Mock
    private RepositoryHookService repositoryHookService;
    @Mock
    private Repository repository;
    @Mock
    private SecurityService securityService;
    @Mock
    private EscalatedSecurityContext escalatedSecurityContext;
    @Mock
    private RepositoryPushHookRequest repositoryPushHookRequest;
    @Mock
    private PreRepositoryHookContext preRepositoryHookContext;

    @Captor
    private ArgumentCaptor<Settings> settingsCapture;

    private PluginSettingsFactory pluginSettingsFactory;

    private Map<String, Object> globalSettingsMap = new HashMap<>();

    private StubRepositoryHook repositoryHook;

    private YaccGlobalHook yaccPreReceiveHook;

    @Before
    public void setup() throws Throwable {
        MockitoAnnotations.initMocks(this);

        pluginSettingsFactory = new MockPluginSettingsFactory();

        yaccPreReceiveHook = new YaccGlobalHook(yaccService,
                pluginSettingsFactory, securityService, repositoryHookService);

        repositoryHook = new StubRepositoryHook();

        //mock hook retrieval
        when(securityService.withPermission(Permission.REPO_ADMIN, "Get plugin configuration"))
                .thenReturn(escalatedSecurityContext);
        when(escalatedSecurityContext.call(any(UncheckedOperation.class))).thenReturn(repositoryHook);

        when(repositoryHookService.createSettingsBuilder()).thenReturn(new MockSettingsBuilder());

        PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();
        pluginSettings.put(YaccConfigServlet.SETTINGS_MAP, globalSettingsMap);

        when(repositoryPushHookRequest.getRepository()).thenReturn(repository);
    }

    @Test
    public void testPreUpdate_globalHookAcceptsIfRepoHookEnabledAndConfigured() {
        globalSettingsMap.put("commitMessageRegex", "bar");
        repositoryHook.setConfigured(true);
        repositoryHook.setEnabled(true);

        RepositoryHookResult result = yaccPreReceiveHook.preUpdate(preRepositoryHookContext,
                repositoryPushHookRequest);

        verifyZeroInteractions(yaccService);
        assertThat(result).isEqualTo(RepositoryHookResult.accepted());
    }

    @Test
    public void testPreUpdate_nullSettingsMap_hookWorksBeforeItHasBeenConfigured() {
        pluginSettingsFactory.createGlobalSettings().put(YaccConfigServlet.SETTINGS_MAP, null);

        RepositoryHookResult result = yaccPreReceiveHook
                .preUpdate(preRepositoryHookContext, repositoryPushHookRequest);

        assertThat(result).isEqualTo(RepositoryHookResult.accepted());
    }

    @Test
    public void testPreUpdate_globalHookSettingsPassedToHook() {
        globalSettingsMap.put("commitMessageRegex", "bar");
        globalSettingsMap.put("requireMatchingAuthorEmail", "true");
        globalSettingsMap.put("committerEmailRegex", "email.com");

        yaccPreReceiveHook.preUpdate(preRepositoryHookContext, repositoryPushHookRequest);

        verify(yaccService).check(eq(preRepositoryHookContext), eq(repositoryPushHookRequest),
                settingsCapture.capture());

        Settings hookSettings = settingsCapture.getValue();

        assertThat(hookSettings.asMap()).contains(
                entry("commitMessageRegex", "bar"),
                entry("requireMatchingAuthorEmail", true),
                entry("committerEmailRegex", "email.com"));
    }

    @Test
    public void testPreUpdate_globalHookNotRunIfNoSettingsEnabled() {
        globalSettingsMap.put("disabledBooleanSetting", "false");
        globalSettingsMap.put("emptySetting", "");
        globalSettingsMap.put("errorMessage", "error messages ignored");

        RepositoryHookResult result = yaccPreReceiveHook
                .preUpdate(preRepositoryHookContext, repositoryPushHookRequest);

        assertThat(result).isEqualTo(RepositoryHookResult.accepted());

        verifyZeroInteractions(yaccService);
    }
}
