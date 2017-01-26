package ut.com.isroot.stash.plugin;

import com.isroot.stash.plugin.YaccCommit;
import com.isroot.stash.plugin.YaccPerson;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * @author Sean Ford
 * @since 2014-05-01
 */
public class YaccCommitTest {
    @Test
    public void testConstructor_trailingNewLineInCommitMessageIsRemoved() {
        YaccCommit yaccCommit = new YaccCommit("id", new YaccPerson("Name", "email@address.com"),
                "contains trailing newline\n", false);

        assertThat(yaccCommit.getMessage()).isEqualTo("contains trailing newline");

    }
}
