package ut.com.isroot.stash.plugin.commits;

import com.atlassian.bitbucket.user.SimplePerson;
import com.atlassian.utils.process.ProcessException;
import com.isroot.stash.plugin.YaccCommit;
import com.isroot.stash.plugin.commits.AnnotatedTagOutputHandler;
import org.junit.Test;

import java.io.ByteArrayInputStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Sean Ford
 * @since 2017-01-29
 */
public class AnnotatedTagOutputHandlerTest {
    @Test
    public void testGetOutput_returnsNullIfNotAnAnnotatedTag() {
        YaccCommit commit = parse("commit 2e10dd2d1d5eea9291b296e78312e8a703964a95\n");

        assertThat(commit).isNull();
    }

    @Test
    public void testGetOutput_annotatedTagReturned() {
        YaccCommit commit = parse("object 1ba1cf7c2ad195c32a3743e3a78e09ca480c228c\n" +
                "type commit\n" +
                "tag annotatedtag\n" +
                "tagger Name <admin@example.com> 1485751987 -0800\n" +
                "\n" +
                "this is the tag\nmessage");

        assertThat(commit.getCommitter())
                .isEqualTo(new SimplePerson("Name", "admin@example.com"));

        assertThat(commit.getMessage()).isEqualTo("this is the tag\nmessage");

        assertThat(commit.isMerge()).isFalse();
        assertThat(commit.getId()).isEqualTo("passedInRef");
    }

    @Test
    public void testGetOutput_emptyEmailSupported() {
        YaccCommit commit = parse("object 1ba1cf7c2ad195c32a3743e3a78e09ca480c228c\n" +
                "type commit\n" +
                "tag annotatedtag\n" +
                "tagger First Last <> 1485751987 -0800\n" +
                "\n" +
                "message");

        assertThat(commit.getCommitter())
                .isEqualTo(new SimplePerson("First Last", ""));
    }

    @Test
    public void testGetOutput_emptyNameEmailIfThereIsAParseProblem() {
        YaccCommit commit = parse("object 1ba1cf7c2ad195c32a3743e3a78e09ca480c228c\n" +
                "type commit\n" +
                "tag annotatedtag\n" +
                "tagger First Last\n" +
                "\n" +
                "message");

        assertThat(commit.getCommitter())
                .isEqualTo(new SimplePerson("", ""));
    }

    private YaccCommit parse(String output) {
        AnnotatedTagOutputHandler handler = new AnnotatedTagOutputHandler("passedInRef");

        try {
            handler.process(new ByteArrayInputStream(output.getBytes()));
        } catch (ProcessException e) {
            throw new RuntimeException(e);
        }

        return handler.getOutput();
    }
}
