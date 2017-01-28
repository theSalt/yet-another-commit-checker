package ut.com.isroot.stash.plugin;

import com.atlassian.bitbucket.user.SimplePerson;
import com.isroot.stash.plugin.YaccCommit;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * @author Sean Ford
 * @since 2014-05-01
 */
public class YaccCommitTest {
    @Test
    public void testConstructor_trailingNewLineInCommitMessageIsRemoved() {
        SimplePerson simplePerson = new SimplePerson("Name", "email@address.com");

        YaccCommit yaccCommit = new YaccCommit("id", simplePerson,
                "contains trailing newline\n", false);

        assertThat(yaccCommit.getMessage()).isEqualTo("contains trailing newline");
    }
}
