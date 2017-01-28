package ut.com.isroot.stash.plugin.commits;

import com.atlassian.bitbucket.repository.RefChange;
import com.atlassian.bitbucket.repository.Repository;
import com.isroot.stash.plugin.YaccCommit;
import com.isroot.stash.plugin.commits.CommitsServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * @author Sean Ford
 * @since 2017-01-28
 */
public class CommitsServiceImplTest {
    private CommitsServiceImpl commitsService;

    @Mock private Repository repository;
    @Mock private RefChange refChange;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        commitsService = new CommitsServiceImpl(null);
    }

    @Test
    public void testGetNewCommits_noCommitsReturnedForUnsupportedScm() {
        when(repository.getScmId()).thenReturn("unsupported");

        Set<YaccCommit> commits = commitsService.getNewCommits(repository, refChange);

        assertThat(commits).isEmpty();
    }

}
