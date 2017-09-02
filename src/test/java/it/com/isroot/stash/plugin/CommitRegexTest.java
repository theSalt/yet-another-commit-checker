package it.com.isroot.stash.plugin;

import com.google.common.collect.ImmutableMap;
import it.com.isroot.stash.plugin.util.YaccRule;
import org.assertj.core.api.Assertions;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.junit.Rule;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Sean Ford
 * @since 2017-09-02
 */
public class CommitRegexTest {
    @Rule
    public YaccRule gitRepoRule = new YaccRule();

    @Test
    public void testCommitRegex_allowed() {
        gitRepoRule.enableYaccRepoHook();
        gitRepoRule.configureYaccRepoHook(ImmutableMap
                .of("commitMessageRegex", "[A-Z]+-[0-9]+: .*"));

        gitRepoRule.getGitRepo().commitFile("file.java", "ABC-123: allowed");
        PushResult pushResult = gitRepoRule.getGitRepo().push("master");

        RemoteRefUpdate update = pushResult.getRemoteUpdate("refs/heads/master");
        Assertions.assertThat(update.getStatus()).isEqualTo(RemoteRefUpdate.Status.OK);
    }

    @Test
    public void testCommitRegex_blocked() {
        gitRepoRule.enableYaccRepoHook();
        gitRepoRule.configureYaccRepoHook(ImmutableMap
                .of("commitMessageRegex", "[A-Z]+-[0-9]+: .*"));

        gitRepoRule.getGitRepo().commitFile("file.java", "invalid commit message");
        PushResult pushResult = gitRepoRule.getGitRepo().push("master");

        RemoteRefUpdate update = pushResult.getRemoteUpdate("refs/heads/master");
        assertThat(update.getStatus()).isEqualTo(
                RemoteRefUpdate.Status.REJECTED_OTHER_REASON);

        assertThat(pushResult.getMessages())
                .contains("commit message doesn't match regex:");
    }

}
