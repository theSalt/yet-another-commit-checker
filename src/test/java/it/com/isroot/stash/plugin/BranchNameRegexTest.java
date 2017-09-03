package it.com.isroot.stash.plugin;

import com.google.common.collect.ImmutableMap;
import it.com.isroot.stash.plugin.util.YaccRule;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.junit.Rule;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Sean Ford
 * @since 2017-08-20
 */
public class BranchNameRegexTest {
    @Rule
    public YaccRule gitRepoRule = new YaccRule();

    @Test
    public void testYaccDisabled_newBranchesAllowed() throws Exception {
        Git git = gitRepoRule.getGitRepo().getGit();

        git.branchCreate()
                .setName("mybranch")
                .call();

        gitRepoRule.getGitRepo().push("mybranch");
    }

    @Test
    public void testYaccEnabled_blocksBranchCreationIfNameIsInvalid() throws Exception {
        Git git = gitRepoRule.getGitRepo().getGit();

        gitRepoRule.enableYaccRepoHook();
        gitRepoRule.configureYaccRepoHook(ImmutableMap
                .of("branchNameRegex", "master|feature/.*"));

        git.branchCreate()
                .setName("invalid-name")
                .call();

        PushResult pushResult = gitRepoRule.getGitRepo().push("invalid-name");

        assertThat(pushResult.getRemoteUpdates()).extracting(RemoteRefUpdate::getStatus)
                .containsExactly(RemoteRefUpdate.Status.REJECTED_OTHER_REASON);

        assertThat(pushResult.getMessages())
                .contains("refs/heads/invalid-name: Invalid branch name. 'invalid-name' " +
                        "does not match regex 'master|feature/.*'");
    }

    @Test
    public void testYaccEnabled_allowsPushIfBranchNameIsValid() throws Exception {
        Git git = gitRepoRule.getGitRepo().getGit();

        gitRepoRule.enableYaccRepoHook();
        gitRepoRule.configureYaccRepoHook(ImmutableMap
                .of("branchNameRegex", "master|feature/.*"));

        git.branchCreate()
                .setName("feature/correct-branch-name")
                .call();

        PushResult pushResult = gitRepoRule.getGitRepo().push("feature/correct-branch-name");

        assertThat(pushResult.getRemoteUpdates()).extracting(RemoteRefUpdate::getStatus)
                .containsExactly(RemoteRefUpdate.Status.OK);

        assertThat(pushResult.getMessages())
                .contains("Create pull request");
    }

    @Test
    public void testYaccEnabled_alreadyExistingBranchesAreCanStillBePushedTo() throws Exception {
        Git git = gitRepoRule.getGitRepo().getGit();

        gitRepoRule.configureYaccRepoHook(ImmutableMap
                .of("branchNameRegex", "master|feature/.*"));

        git.branchCreate()
                .setName("invalid-name")
                .call();

        PushResult pushResult = gitRepoRule.getGitRepo().push("invalid-name");

        assertThat(pushResult.getRemoteUpdates()).extracting(RemoteRefUpdate::getStatus)
                .containsExactly(RemoteRefUpdate.Status.OK);

        gitRepoRule.enableYaccRepoHook();

        gitRepoRule.getGitRepo().commitFile("newfile", "commit will be allowed");
        gitRepoRule.getGitRepo().push("invalid-name");

        assertThat(pushResult.getRemoteUpdates()).extracting(RemoteRefUpdate::getStatus)
                .containsExactly(RemoteRefUpdate.Status.OK);
    }

    /**
     * Due to the way commit callback works in BBS 5 APIs, commit checks won't be ran if branch
     * name regex fails. So, it will be the only error. The rest of the checks will be ran once
     * branch name is fixed.
     */
    @Test
    public void testOtherChecksAreNotRanIfBranchNameRegexFails() throws Exception {
        Git git = gitRepoRule.getGitRepo().getGit();

        gitRepoRule.enableYaccRepoHook();
        gitRepoRule.configureYaccRepoHook(ImmutableMap
                .of("branchNameRegex", "master|feature/.*",
                        "commitMessageRegex", "commit_regex"));

        git.branchCreate()
                .setName("invalid-name")
                .call();

        PushResult pushResult = gitRepoRule.getGitRepo().push("invalid-name");

        assertThat(pushResult.getRemoteUpdates()).extracting(RemoteRefUpdate::getStatus)
                .containsExactly(RemoteRefUpdate.Status.REJECTED_OTHER_REASON);

        assertThat(pushResult.getMessages())
                .contains("Invalid branch name")
                .doesNotContain("commit_regex");
    }
}
