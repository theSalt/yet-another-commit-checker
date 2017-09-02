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
public class ExcludeBranchRegexTest {
    @Rule
    public YaccRule gitRepoRule = new YaccRule();

    @Before
    public void setup() {
        gitRepoRule.enableYaccRepoHook();
    }

    @Test
    public void testExcludeBranchRegex() {
        gitRepoRule.configureYaccRepoHook(ImmutableMap
                .of("commitMessageRegex", "[A-Z]+-[0-9]+: .*"));

        PushResult pushResult = gitRepoRule.getGitRepo()
                .commitFile("file.java", "invalid commit message")
                .push();

        assertThat(pushResult.getRemoteUpdates()).extracting(RemoteRefUpdate::getStatus)
                .containsExactly(RemoteRefUpdate.Status.REJECTED_OTHER_REASON);

        // Add exclude branch to match master branch
        gitRepoRule.configureYaccRepoHook(ImmutableMap
                .of("commitMessageRegex", "[A-Z]+-[0-9]+: .*",
                        "excludeBranchRegex", "m.+"));

        pushResult = gitRepoRule.getGitRepo()
                .push();

        assertThat(pushResult.getRemoteUpdates()).extracting(RemoteRefUpdate::getStatus)
                .containsExactly(RemoteRefUpdate.Status.OK);
    }
}
