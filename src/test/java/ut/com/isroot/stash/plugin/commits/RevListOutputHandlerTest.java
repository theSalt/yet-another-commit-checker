package ut.com.isroot.stash.plugin.commits;

import com.atlassian.utils.process.ProcessException;
import com.isroot.stash.plugin.YaccCommit;
import com.isroot.stash.plugin.commits.RevListOutputHandler;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Sean Ford
 * @since 2017-01-25
 */
public class RevListOutputHandlerTest {
    @Test
    public void testGetOutput_singleCommit() {
        List<YaccCommit> commits = parse("commit 9a1ced131648d5481e4a3f00b9c5522d466ec693\n" +
                "Author: Author Last <some@email.com>\n" +
                "Commit: Commit Last <some@email.com>\n" +
                "\n" +
                "    this is my commit message\n" +
                "\n");

        assertThat(commits).hasSize(1);
        YaccCommit commit = commits.get(0);
        assertThat(commit.getId()).isEqualTo("9a1ced131648d5481e4a3f00b9c5522d466ec693");
        assertThat(commit.isMerge()).isFalse();
        assertThat(commit.getCommitter().getName()).isEqualTo("Commit Last");
        assertThat(commit.getCommitter().getEmailAddress()).isEqualTo("some@email.com");
    }

    @Test
    public void testGetOutput_multipleCommits() {
        List<YaccCommit> commits = parse("commit 9a1ced131648d5481e4a3f00b9c5522d466ec693\n" +
                "Author: Author Last <some@email.com>\n" +
                "Commit: Commit Last <some@email.com>\n" +
                "\n" +
                "    this is my commit message\n" +
                "\n" +
                "commit 1060dc57a0c0b27fdd7aef1481ca914a1d7d084e\n" +
                "Author: Author SecondCommit <secondcommit@email.com>\n" +
                "Commit: Commit SecondCommit <secondcommit@email.com>\n" +
                "\n" +
                "    second commit\n" +
                "\n");

        assertThat(commits).hasSize(2);

        YaccCommit commit = commits.get(0);
        assertThat(commit.getId()).isEqualTo("9a1ced131648d5481e4a3f00b9c5522d466ec693");
        assertThat(commit.isMerge()).isFalse();
        assertThat(commit.getCommitter().getName()).isEqualTo("Commit Last");
        assertThat(commit.getCommitter().getEmailAddress()).isEqualTo("some@email.com");

        commit = commits.get(1);
        assertThat(commit.getId()).isEqualTo("1060dc57a0c0b27fdd7aef1481ca914a1d7d084e");
        assertThat(commit.isMerge()).isFalse();
        assertThat(commit.getCommitter().getName()).isEqualTo("Commit SecondCommit");
        assertThat(commit.getCommitter().getEmailAddress()).isEqualTo("secondcommit@email.com");
    }

    @Test
    public void testGetOutput_mergeCommit() {
        List<YaccCommit> commits = parse("commit 9a1ced131648d5481e4a3f00b9c5522d466ec693\n" +
                "Merge: c5b2f7b dc27df2\n" +
                "Author: Author Last <some@email.com>\n" +
                "Commit: Commit Last <some@email.com>\n" +
                "\n" +
                "    this is my commit message\n" +
                "\n");

        assertThat(commits.get(0).isMerge()).isTrue();
    }

    @Test
    public void testGetOutput_multiLineCommitMessage() {
        List<YaccCommit> commits = parse("commit 9a1ced131648d5481e4a3f00b9c5522d466ec693\n" +
                "Author: Author Last <some@email.com>\n" +
                "Commit: Commit Last <some@email.com>\n" +
                "Date:   Wed Jan 25 23:16:02 2017 -0800\n" +
                "\n" +
                "    Multiple\n" +
                "    \n" +
                "    Lines\n" +
                "\n");

        assertThat(commits.get(0).getMessage()).isEqualTo("Multiple\n\nLines");
    }

    private List<YaccCommit> parse(String revList) {
        RevListOutputHandler handler = new RevListOutputHandler();

        try {
            handler.process(new ByteArrayInputStream(revList.getBytes()));
        } catch (ProcessException e) {
            throw new RuntimeException(e);
        }

        return handler.getOutput();
    }
}
