package it.com.isroot.stash.plugin;

import com.google.common.collect.ImmutableMap;
import it.com.isroot.stash.plugin.util.YaccRule;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Sean Ford
 * @since 2017-09-02
 */
public class ExcludeMergeCommitsTest {
    @Rule
    public YaccRule gitRepoRule = new YaccRule();

    @Before
    public void setup() {
        gitRepoRule.enableYaccRepoHook();
    }

    @Test
    public void testMergeCommits() throws Exception {
        gitRepoRule.configureYaccRepoHook(ImmutableMap
                .of("commitMessageRegex", "[A-Z]+-[0-9]+: .*"));

        createMergeCommitOnMaster();

        PushResult pushResult = gitRepoRule.getGitRepo().push();

        // This push should fail because merge commit doesn't match commit message regex
        assertThat(pushResult.getRemoteUpdates()).extracting(RemoteRefUpdate::getStatus)
                .containsExactly(RemoteRefUpdate.Status.REJECTED_OTHER_REASON);
        assertThat(pushResult.getMessages())
                .contains("commit message doesn't match regex");

        gitRepoRule.configureYaccRepoHook(ImmutableMap
                .of("commitMessageRegex", "[A-Z]+-[0-9]+: .*",
                        "excludeMergeCommits", "true"));

        pushResult = gitRepoRule.getGitRepo()
                .push();

        assertThat(pushResult.getRemoteUpdates()).extracting(RemoteRefUpdate::getStatus)
                .containsExactly(RemoteRefUpdate.Status.OK);
    }

    private void createMergeCommitOnMaster() throws GitAPIException, IOException {
        Git git = gitRepoRule.getGitRepo().getGit();

        git.checkout()
                .setCreateBranch(true)
                .setName("a-branch")
                .call();

        gitRepoRule.getGitRepo()
                .commitFile("myfile", "ABC-123: my change");

        git.checkout()
                .setName("master")
                .call();

        git.merge()
                .include(git.getRepository().resolve("a-branch"))
                .setFastForward(MergeCommand.FastForwardMode.NO_FF)
                .call();
    }
}
