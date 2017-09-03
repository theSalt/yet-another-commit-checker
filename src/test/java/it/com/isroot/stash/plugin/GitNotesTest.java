package it.com.isroot.stash.plugin;

import com.google.common.collect.ImmutableMap;
import it.com.isroot.stash.plugin.util.YaccRule;
import org.eclipse.jgit.api.Git;
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
public class GitNotesTest {
    @Rule
    public YaccRule gitRepoRule = new YaccRule();

    @Before
    public void setup() {
        gitRepoRule.enableYaccRepoHook();
    }

    @Test
    public void testGitNotesAreExcluded() throws Exception {
        gitRepoRule.configureYaccRepoHook(ImmutableMap
                .of("commitMessageRegex", "[A-Z]+-[0-9]+: .*"));

        Git git = gitRepoRule.getGitRepo().getGit();
        Repository repository = git.getRepository();

        RevCommit revCommit = repository.parseCommit(repository.resolve("master"));
        git.notesAdd()
                .setMessage("notes message")
                .setObjectId(revCommit)
                .call();
        
        PushResult pushResult = gitRepoRule.getGitRepo()
                .push("refs/notes/*:refs/notes/*");

        assertThat(pushResult.getRemoteUpdates()).extracting(RemoteRefUpdate::getStatus)
                .containsExactly(RemoteRefUpdate.Status.OK);
    }
}
