package it.com.isroot.stash.plugin;

import it.com.isroot.stash.plugin.util.YaccRule;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Sean Ford
 * @since 2017-09-03
 */
public class GlobalHookTest {
    @Rule
    public YaccRule gitRepoRule = new YaccRule();

    @After
    public void cleanup() {
        gitRepoRule.configureYaccGlobalHook(new HashMap<>());
    }

    @Test
    public void testBlock_commitChecks() throws Exception {
        Map<String, String> settings = new HashMap<>();
        settings.put("requireMatchingAuthorEmail", "true");
        settings.put("requireMatchingAuthorName", "true");

        gitRepoRule.configureYaccGlobalHook(settings);

        PushResult pushResult = gitRepoRule.getGitRepo()
                .commitFile("file.java", "commit message")
                .push();

        assertThat(pushResult.getRemoteUpdates()).extracting(RemoteRefUpdate::getStatus)
                .containsExactly(RemoteRefUpdate.Status.REJECTED_OTHER_REASON);

        assertThat(pushResult.getMessages())
                .contains("expected committer email 'admin@example.com' but found 'yacc@email.com'")
                .contains("expected committer name 'Administrator' but found 'YaccName'");

        // Repo hook overrides global hook
        gitRepoRule.enableYaccRepoHook();
        pushResult = gitRepoRule.getGitRepo()
                .push();

        assertThat(pushResult.getRemoteUpdates()).extracting(RemoteRefUpdate::getStatus)
                .containsExactly(RemoteRefUpdate.Status.OK);
    }

    @Test
    public void testBranchNameCheck_runsBeforeCommitChecks() throws Exception {
        Map<String, String> settings = new HashMap<>();
        settings.put("requireMatchingAuthorEmail", "true");
        settings.put("requireMatchingAuthorName", "true");
        settings.put("branchNameRegex", "dev");
        gitRepoRule.configureYaccGlobalHook(settings);

        Git git = gitRepoRule.getGitRepo().getGit();

        final String newBranchName = "new-branch";

        git.checkout()
                .setCreateBranch(true)
                .setName(newBranchName)
                .call();

        PushResult pushResult = gitRepoRule.getGitRepo()
                .commitFile("file.java", "commit message")
                .push(newBranchName);

        assertThat(pushResult.getRemoteUpdates()).extracting(RemoteRefUpdate::getStatus)
                .containsExactly(RemoteRefUpdate.Status.REJECTED_OTHER_REASON);

        assertThat(pushResult.getMessages())
                .contains("refs/heads/new-branch: Invalid branch name. 'new-branch' does not match regex 'dev'")
                .doesNotContain("expected committer");
    }
}
