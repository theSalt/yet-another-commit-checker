package it.com.isroot.stash.plugin;

import com.google.common.collect.ImmutableMap;
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
    public void testRepoHookUsedInsteadIfEnabled() {
        gitRepoRule.configureYaccGlobalHook(ImmutableMap
                .of("commitMessageRegex", "global"));

        gitRepoRule.enableYaccRepoHook();
        gitRepoRule.configureYaccRepoHook(ImmutableMap
                .of("commitMessageRegex", "repo"));

        // Push a commit that would have been allowed by global settings. It
        // should be rejected due to repo hook.
        PushResult pushResult = gitRepoRule.getGitRepo()
                .commitFile("file.java", "global")
                .push();
        assertThat(pushResult.getRemoteUpdates()).extracting(RemoteRefUpdate::getStatus)
                .containsExactly(RemoteRefUpdate.Status.REJECTED_OTHER_REASON);
    }

    @Test
    public void testGlobalHookUsedAfterRepoHookToggledOnOff() {
        // Make sure global hook configured
        gitRepoRule.configureYaccGlobalHook(ImmutableMap
                .of("commitMessageRegex", "global"));

        // Toggle hook settings on/off
        gitRepoRule.enableYaccRepoHook();
        gitRepoRule.configureYaccRepoHook(ImmutableMap
                .of("commitMessageRegex", "repo"));
        gitRepoRule.disableYaccRepoHook();

        // Push a commit... this should be rejected by global settings
        PushResult pushResult = gitRepoRule.getGitRepo()
                .commitFile("file.java", "repo")
                .push();
        assertThat(pushResult.getRemoteUpdates()).extracting(RemoteRefUpdate::getStatus)
                .containsExactly(RemoteRefUpdate.Status.REJECTED_OTHER_REASON);
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

    /**
     * Same idea as {@link #testGlobalHookUsedAfterRepoHookToggledOnOff()}, but spot checks ref
     * checks. Branch name check is a ref check, not a commit check, so works a little differently.
     */
    @Test
    public void testBranchNameCheck_globalHookUsedAfterRepoHookToggledOnOff() throws Exception {
        Git git = gitRepoRule.getGitRepo().getGit();

        gitRepoRule.configureYaccGlobalHook(ImmutableMap
                .of("branchNameRegex", "global"));

        // Toggle hook settings on/off
        gitRepoRule.enableYaccRepoHook();
        gitRepoRule.configureYaccRepoHook(ImmutableMap
                .of("branchNameRegex", "repo"));
        gitRepoRule.disableYaccRepoHook();

        // Push a branch... this should be rejected by global settings
        git.branchCreate()
                .setName("repo")
                .call();

        PushResult pushResult = gitRepoRule.getGitRepo()
                .push("repo");
        assertThat(pushResult.getRemoteUpdates()).extracting(RemoteRefUpdate::getStatus)
                .containsExactly(RemoteRefUpdate.Status.REJECTED_OTHER_REASON);
    }
}
