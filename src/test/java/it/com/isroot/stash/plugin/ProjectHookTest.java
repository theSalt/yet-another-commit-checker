package it.com.isroot.stash.plugin;

import com.google.common.collect.ImmutableMap;
import it.com.isroot.stash.plugin.util.YaccRule;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Sean Ford
 * @since 2017-09-02
 */
public class ProjectHookTest {
    @Rule
    public YaccRule gitRepoRule = new YaccRule();

    @Before
    public void setup() {
        gitRepoRule.enableYaccProjectHook();
        gitRepoRule.configureYaccProjectHook(ImmutableMap
                .of("commitMessageRegex", "[A-Z]+-[0-9]+: .*"));
    }

    @Test
    public void testProjectHook_settingsUsed_allow() {
        PushResult pushResult =  gitRepoRule.getGitRepo()
                .commitFile("file.java", "ABC-123: allowed")
                .push();

        assertThat(pushResult.getRemoteUpdates()).extracting(RemoteRefUpdate::getStatus)
                .containsExactly(RemoteRefUpdate.Status.OK);
    }

    @Test
    public void testProjectHook_settingsUsed_block() {
        PushResult pushResult = gitRepoRule.getGitRepo()
                .commitFile("file.java", "invalid commit message")
                .push();
        
        assertThat(pushResult.getRemoteUpdates()).extracting(RemoteRefUpdate::getStatus)
                .containsExactly(RemoteRefUpdate.Status.REJECTED_OTHER_REASON);

        assertThat(pushResult.getMessages())
                .contains("commit message doesn't match regex:");
    }

    @Test
    public void testProjectHook_hookSettingsUsedIfEnabled() {
        gitRepoRule.enableYaccProjectHook();
        gitRepoRule.configureYaccProjectHook(ImmutableMap
                .of("commitMessageRegex", "project_regex"));

        // Should be blocked because message doesn't pass project regex
        gitRepoRule.getGitRepo()
                .commitFile("file.java", "hook_regex");

        // Sanity check: should be blocked
        PushResult pushResult = gitRepoRule.getGitRepo()
                .push();

        assertThat(pushResult.getRemoteUpdates()).extracting(RemoteRefUpdate::getStatus)
                .containsExactly(RemoteRefUpdate.Status.REJECTED_OTHER_REASON);

        // Set hook repo settings so that push should be accepted
        gitRepoRule.enableYaccRepoHook();
        gitRepoRule.configureYaccProjectHook(ImmutableMap
                .of("commitMessageRegex", "hook_regex"));

        pushResult = gitRepoRule.getGitRepo()
                .push();

        assertThat(pushResult.getRemoteUpdates()).extracting(RemoteRefUpdate::getStatus)
                .containsExactly(RemoteRefUpdate.Status.OK);
    }

    @Test
    public void testProjectHook_repoCanBeExemptedByDisablingHookSettings() {
        gitRepoRule.getGitRepo()
                .commitFile("file.java", "blocked by project hook");

        // Sanity check: should be blocked
        PushResult pushResult = gitRepoRule.getGitRepo()
                .push();

        assertThat(pushResult.getRemoteUpdates()).extracting(RemoteRefUpdate::getStatus)
                .containsExactly(RemoteRefUpdate.Status.REJECTED_OTHER_REASON);

        // Disabling hook settings should allow push
        gitRepoRule.disableYaccRepoHook();

        pushResult = gitRepoRule.getGitRepo()
                .push();

        assertThat(pushResult.getRemoteUpdates()).extracting(RemoteRefUpdate::getStatus)
                .containsExactly(RemoteRefUpdate.Status.OK);
    }
}
