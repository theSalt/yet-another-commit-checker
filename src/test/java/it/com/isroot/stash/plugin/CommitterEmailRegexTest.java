package it.com.isroot.stash.plugin;

import com.google.common.collect.ImmutableMap;
import it.com.isroot.stash.plugin.util.YaccRule;
import org.eclipse.jgit.lib.PersonIdent;
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
public class CommitterEmailRegexTest {
    @Rule
    public YaccRule gitRepoRule = new YaccRule();

    @Before
    public void setup() {
        gitRepoRule.enableYaccRepoHook();
        gitRepoRule.configureYaccRepoHook(ImmutableMap
                .of("committerEmailRegex", ".*@domain.com"));
    }

    @Test
    public void testAllowed() throws Exception {
        PushResult pushResult = gitRepoRule.getGitRepo()
                .commitFile("someFile", "message", new PersonIdent("Admin", "admin@domain.com"))
                .push();

        assertThat(pushResult.getRemoteUpdates()).extracting(RemoteRefUpdate::getStatus)
                .containsExactly(RemoteRefUpdate.Status.OK);
    }

    @Test
    public void testBlocked() {
        PushResult pushResult = gitRepoRule.getGitRepo()
                .commitFile("someFile", "message", new PersonIdent("Admin", "admin@unknown.com"))
                .push();

        assertThat(pushResult.getRemoteUpdates()).extracting(RemoteRefUpdate::getStatus)
                .containsExactly(RemoteRefUpdate.Status.REJECTED_OTHER_REASON);

        assertThat(pushResult.getMessages())
                .contains("committer email regex '.*@domain.com' does not match user email 'admin@unknown.com'");
    }

}
