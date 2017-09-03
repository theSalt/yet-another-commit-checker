package it.com.isroot.stash.plugin;

import com.google.common.collect.ImmutableMap;
import it.com.isroot.stash.plugin.util.YaccRule;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Sean Ford
 * @since 2017-09-02
 */
public class ErrorsTest {
    @Rule
    public YaccRule gitRepoRule = new YaccRule();

    @Before
    public void setup() {
        gitRepoRule.enableYaccRepoHook();
        gitRepoRule.configureYaccRepoHook(ImmutableMap
                .of("commitMessageRegex", "[A-Z]+-[0-9]+: .*"));
    }

    @Test
    public void testStandardError() {
        PushResult pushResult = gitRepoRule.getGitRepo()
                .commitFile("file.java", "will be blocked")
                .push();

        assertThat(pushResult.getRemoteUpdates()).extracting(RemoteRefUpdate::getStatus)
                .containsExactly(RemoteRefUpdate.Status.REJECTED_OTHER_REASON);

        assertThat(pushResult.getMessages())
                .startsWith("\n" +
                        "  (c)")
                .contains("|| E ||")
                .contains("Push rejected.\n" +
                        "\n" +
                        "refs/heads/master: ")
                .contains(": commit message doesn't match regex: [A-Z]+-[0-9]+: .*");
    }

    @Test
    public void testMultipleErrors() {
        gitRepoRule.configureYaccRepoHook(ImmutableMap
                .of("commitMessageRegex", "[A-Z]+-[0-9]+: .*",
                        "requireMatchingAuthorEmail", "true",
                        "requireMatchingAuthorName", "true"));

        PushResult pushResult = gitRepoRule.getGitRepo()
                .commitFile("file.java", "will be blocked")
                .push();

        assertThat(pushResult.getRemoteUpdates()).extracting(RemoteRefUpdate::getStatus)
                .containsExactly(RemoteRefUpdate.Status.REJECTED_OTHER_REASON);

        assertThat(pushResult.getMessages())
                .contains("expected committer email 'admin@example.com' but found 'yacc@email.com'")
                .contains("expected committer name 'Administrator' but found 'YaccName'")
                .contains("commit message doesn't match regex: [A-Z]+-[0-9]+: .*");
    }

    @Test
    public void testHeader_customHeaderReplacesErrorBears() {
        gitRepoRule.configureYaccRepoHook(ImmutableMap
                .of("commitMessageRegex", "[A-Z]+-[0-9]+: .*",
                        "errorMessageHeader", "custom header"));

        PushResult pushResult = gitRepoRule.getGitRepo()
                .commitFile("file.java", "will be blocked")
                .push();

        assertThat(pushResult.getRemoteUpdates()).extracting(RemoteRefUpdate::getStatus)
                .containsExactly(RemoteRefUpdate.Status.REJECTED_OTHER_REASON);

        assertThat(pushResult.getMessages())
                .startsWith("custom header\n" +
                        "\n" +
                        "refs/heads/master:");
    }

    @Test
    public void testCustomizedErrorMessages() {
        Map<String, String> settings = new HashMap<>();
        settings.put("commitMessageRegex", "[A-Z]+-[0-9]+: .*");
        settings.put("requireMatchingAuthorEmail", "true");
        settings.put("requireMatchingAuthorName", "true");
        settings.put("committerEmailRegex", "foo");
        settings.put("errorMessage.COMMIT_REGEX", "message is wrong");
        settings.put("errorMessage.COMMITTER_EMAIL", "email is wrong");
        settings.put("errorMessage.COMMITTER_NAME", "name is wrong");
        settings.put("errorMessage.COMMITTER_EMAIL_REGEX", "email domain is wrong");

        gitRepoRule.configureYaccRepoHook(settings);

        PushResult pushResult = gitRepoRule.getGitRepo()
                .commitFile("file.java", "will be blocked")
                .push();

        assertThat(pushResult.getRemoteUpdates()).extracting(RemoteRefUpdate::getStatus)
                .containsExactly(RemoteRefUpdate.Status.REJECTED_OTHER_REASON);

        assertThat(pushResult.getMessages())
                .contains("'yacc@email.com'\n" +
                        "\n" +
                        "    email is wrong")
                .contains("found 'YaccName'\n" +
                        "\n" +
                        "    name is wrong")
                .contains("[A-Z]+-[0-9]+: .*\n" +
                        "\n" +
                        "    message is wrong")
                .contains("user email 'yacc@email.com'\n" +
                        "\n" +
                        "    email domain is wrong");
    }

    @Test
    public void testFooter() {
        gitRepoRule.configureYaccRepoHook(ImmutableMap
                .of("commitMessageRegex", "[A-Z]+-[0-9]+: .*",
                        "errorMessageFooter", "custom footer"));

        PushResult pushResult = gitRepoRule.getGitRepo()
                .commitFile("file.java", "will be blocked")
                .push();

        assertThat(pushResult.getRemoteUpdates()).extracting(RemoteRefUpdate::getStatus)
                .containsExactly(RemoteRefUpdate.Status.REJECTED_OTHER_REASON);

        assertThat(pushResult.getMessages())
                .contains("[A-Z]+-[0-9]+: .*\n" +
                        "\n" +
                        "custom footer");
    }
}
