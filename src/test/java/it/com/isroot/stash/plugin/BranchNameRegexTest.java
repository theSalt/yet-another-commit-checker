package it.com.isroot.stash.plugin;

import com.google.common.collect.ImmutableMap;
import it.com.isroot.stash.plugin.util.BitbucketServerRestClient;
import it.com.isroot.stash.plugin.util.TestRepository;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Sean Ford
 * @since 2017-08-20
 */
public class BranchNameRegexTest {
    private static final String YACC_HOOK_KEY = "com.isroot.stash.plugin.yacc:yaccHook";

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private BitbucketServerRestClient restClient;
    private TestRepository testRepository;
    private String repoSlug;

    @Before
    public void setup() throws IOException {
        repoSlug = UUID.randomUUID().toString();

        restClient = new BitbucketServerRestClient();
        String slug = restClient.createRepo(repoSlug);

        testRepository = new TestRepository(temporaryFolder.newFolder().toPath(), slug);
    }

    @Test
    public void testYaccDisabled_newBranchesAllowed() throws Exception {
        Git git = testRepository.getGit();

        git.branchCreate()
                .setName("mybranch")
                .call();

        testRepository.push("mybranch");
    }

    @Test
    public void testYaccEnabled_blocksBranchCreationIfNameIsInvalid() throws Exception {
        Git git = testRepository.getGit();

        restClient.enableHook(repoSlug, YACC_HOOK_KEY);
        restClient.setHookSettings(repoSlug, YACC_HOOK_KEY,
                ImmutableMap.of("branchNameRegex", "master|feature/.*"));

        git.branchCreate()
                .setName("invalid-name")
                .call();

        PushResult pushResult = testRepository.push("invalid-name");

        for (RemoteRefUpdate update : pushResult.getRemoteUpdates()) {
            assertThat(update.getStatus())
                    .isEqualTo(RemoteRefUpdate.Status.REJECTED_OTHER_REASON);
        }

        assertThat(pushResult.getMessages())
                .contains("refs/heads/invalid-name: Invalid branch name. 'invalid-name' " +
                        "does not match regex 'master|feature/.*'");
    }

    @Test
    public void testYaccEnabled_allowsPushIfBranchNameIsValid() throws Exception {
        Git git = testRepository.getGit();

        restClient.enableHook(repoSlug, YACC_HOOK_KEY);
        restClient.setHookSettings(repoSlug, YACC_HOOK_KEY,
                ImmutableMap.of("branchNameRegex", "master|feature/.*"));

        git.branchCreate()
                .setName("feature/correct-branch-name")
                .call();

        PushResult pushResult = testRepository.push("feature/correct-branch-name");

        for (RemoteRefUpdate update : pushResult.getRemoteUpdates()) {
            assertThat(update.getStatus())
                    .isEqualTo(RemoteRefUpdate.Status.OK);
        }

        assertThat(pushResult.getMessages())
                .contains("Create pull request");
    }

    @Test
    public void testYaccEnabled_alreadyExistingBranchesAreCanStillBePushedTo() throws Exception {
        Git git = testRepository.getGit();

        restClient.setHookSettings(repoSlug, YACC_HOOK_KEY,
                ImmutableMap.of("branchNameRegex", "master|feature/.*"));

        git.branchCreate()
                .setName("invalid-name")
                .call();

        PushResult pushResult = testRepository.push("invalid-name");

        for (RemoteRefUpdate update : pushResult.getRemoteUpdates()) {
            assertThat(update.getStatus())
                    .isEqualTo(RemoteRefUpdate.Status.OK);
        }

        restClient.enableHook(repoSlug, YACC_HOOK_KEY);

        testRepository.commitFile("newfile", "commit will be allowed");
        testRepository.push("invalid-name");

        for (RemoteRefUpdate update : pushResult.getRemoteUpdates()) {
            assertThat(update.getStatus())
                    .isEqualTo(RemoteRefUpdate.Status.OK);
        }
    }
}
