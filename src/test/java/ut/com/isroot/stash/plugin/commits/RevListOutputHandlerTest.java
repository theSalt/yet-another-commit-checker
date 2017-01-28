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
                "9a1ced131648d5481e4a3f00b9c5522d466ec693\u0002a293f806780249dc855ff560cb70f3c21c7f9c1e\u0002Commit Last\u0002some@email.com\n" +
                "this is my commit message\n" +
                "\u0003END\u0004");

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
                "9a1ced131648d5481e4a3f00b9c5522d466ec693\u0002a293f806780249dc855ff560cb70f3c21c7f9c1e\u0002Commit Last\u0002some@email.com\n" +
                "this is my commit message\n" +
                "\u0003END\u0004\n" +
                "commit 1060dc57a0c0b27fdd7aef1481ca914a1d7d084e\n" +
                "1060dc57a0c0b27fdd7aef1481ca914a1d7d084e\u0002a293f806780249dc855ff560cb70f3c21c7f9c1e\u0002Commit SecondCommit\u0002secondcommit@email.com\n" +
                "second commit\n" +
                "\u0003END\u0004");

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
                "9a1ced131648d5481e4a3f00b9c5522d466ec693\u0002a293f806780249dc855ff560cb70f3c21c7f9c1e 2f4d8f6c46e0a8814ea447dcdc1ea5f3a1bba30f\u0002Commit Last\u0002some@email.com\n" +
                "this is my commit message\n" +
                "\u0003END\u0004");

        assertThat(commits.get(0).isMerge()).isTrue();
    }

    @Test
    public void testGetOutput_multiLineCommitMessage() {
        List<YaccCommit> commits = parse("commit 9a1ced131648d5481e4a3f00b9c5522d466ec693\n" +
                "9a1ced131648d5481e4a3f00b9c5522d466ec693\u0002a293f806780249dc855ff560cb70f3c21c7f9c1e 2f4d8f6c46e0a8814ea447dcdc1ea5f3a1bba30f\u0002Commit Last\u0002some@email.com\n" +
                "Multiple\n" +
                "\n" +
                "Lines\n" +
                "\u0003END\u0004");

        assertThat(commits.get(0).getMessage()).isEqualTo("Multiple\n\nLines");
    }

    @Test
    public void testGetOutput_emptyEmailAllowed() {
        List<YaccCommit> commits = parse("commit 9a1ced131648d5481e4a3f00b9c5522d466ec693\n" +
                "9a1ced131648d5481e4a3f00b9c5522d466ec693\u00029a1ced131648d5481e4a3f00b9c5522d466ec693\u0002Commit Last\u0002\n" +
                "this is my commit message\n" +
                "\u0003END\u0004");


        assertThat(commits.get(0).getCommitter().getEmailAddress()).isEmpty();
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
