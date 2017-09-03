package it.com.isroot.stash.plugin;

import com.google.common.collect.ImmutableMap;
import it.com.isroot.stash.plugin.util.YaccRule;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
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
public class AnnotatedTagsTest {
    @Rule
    public YaccRule gitRepoRule = new YaccRule();

    @Before
    public void setup() {
        gitRepoRule.enableYaccRepoHook();

        gitRepoRule.configureYaccRepoHook(ImmutableMap
                .of("commitMessageRegex", "[A-Z]+-[0-9]+: .*",
                        "requireMatchingAuthorEmail", "true",
                        "requireMatchingAuthorName", "true"));
    }

    @Test
    public void testAllowed() throws Exception {
        PushResult pushResult = pushAnnotatedTag(
                new PersonIdent("Administrator", "admin@example.com"));

        assertThat(pushResult.getRemoteUpdates()).extracting(RemoteRefUpdate::getStatus)
                .containsExactly(RemoteRefUpdate.Status.OK);
    }

    @Test
    public void testBlocked() throws Exception {
        PushResult pushResult = pushAnnotatedTag(
                new PersonIdent("wrong", "wrong@example.com"));

        assertThat(pushResult.getRemoteUpdates()).extracting(RemoteRefUpdate::getStatus)
                .containsExactly(RemoteRefUpdate.Status.REJECTED_OTHER_REASON);

        assertThat(pushResult.getMessages())
                .contains("refs/tags/my-annotated-tag")
                .contains("expected committer name 'Administrator' but found 'wrong'")
                .contains("expected committer email 'admin@example.com' but found 'wrong@example.com'");
    }

    private PushResult pushAnnotatedTag(PersonIdent personIdent) throws Exception {
        Git git = gitRepoRule.getGitRepo().getGit();
        Repository repository = git.getRepository();

        RevCommit revCommit = repository.parseCommit(repository.resolve("master"));

        git.tag()
                .setAnnotated(true)
                .setObjectId(revCommit)
                .setMessage("tag message")
                .setName("my-annotated-tag")
                .setTagger(personIdent)
                .call();

        return git.push()
                .setPushTags()
                .setCredentialsProvider(gitRepoRule.getGitRepo().getCredentialsProvider())
                .call().iterator().next();
    }
}
