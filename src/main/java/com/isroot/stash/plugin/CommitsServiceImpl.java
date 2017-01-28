package com.isroot.stash.plugin;

import com.atlassian.bitbucket.repository.RefChange;
import com.atlassian.bitbucket.repository.RefChangeType;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.scm.ScmService;
import com.atlassian.bitbucket.scm.git.GitRefPattern;
import com.atlassian.bitbucket.scm.git.command.GitScmCommandBuilder;
import com.atlassian.bitbucket.scm.git.command.revlist.GitRevListBuilder;
import com.google.common.collect.Sets;
import com.isroot.stash.plugin.commits.RevListOutputHandler;
import com.isroot.stash.plugin.commits.ShowRefsOutputHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Sean Ford
 * @since 2013-10-26
 */
public class CommitsServiceImpl implements CommitsService {
    private final Logger log = LoggerFactory.getLogger(CommitsServiceImpl.class);

    private final ScmService scmService;

    public CommitsServiceImpl(ScmService scmService) {
        this.scmService = scmService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<YaccCommit> getNewCommits(Repository repository, RefChange refChange) {
        log.debug("getNewCommits, refChange.toHash={}", refChange.getToHash());

        /* Tags are different to regular commits - they're just pointers.
         * The only relevent commitId is the destination one (and even then only for
         * ADD and UPDATE).
         * We need to work out whether or not the tag is lightweight (in which case
         * its commitid is an already-existing commit that we don't want to check -
         * it may have been made by someone else) or annotated (in which case we do
         * care.
         *
         * Stash's API to work out the tag type doesn't work (see STASH-4993)
         * and since we're using JGit anyway, just use it for the whole lot.
         */
        Set<YaccCommit> yaccCommits = Sets.newHashSet();

        if (refChange.getRefId().startsWith(GitRefPattern.TAGS.getPath())) {
            if (refChange.getType() == RefChangeType.DELETE) {
                // Deletes don't leave anything to check
                return yaccCommits;
            }

            // TODO FIXME!

//
//                RevObject obj = walk.parseAny(ObjectId.fromString(refChange.getToHash()));
//                if (!(obj instanceof RevTag)) {
//                    // Just a lightweight tag - nothing to check
//                    return yaccCommits;
//                }
//
//                RevTag tag = (RevTag) obj;
//
//                PersonIdent ident = tag.getTaggerIdent();
//                final String message = tag.getFullMessage();
//                final YaccPerson committer = new YaccPerson(ident.getName(), ident.getEmailAddress());
//                final YaccCommit yaccCommit = new YaccCommit(refChange.getToHash(), committer, message, 1);
//
//                yaccCommits.add(yaccCommit);
        }
        else {
            Set<String> branches = getBranches(getGitScmCommandBuilder(repository));

            log.debug("finding commits reachable from {} but not {}", refChange.getToHash(),
                    branches);

            GitRevListBuilder revListBuilder = getGitScmCommandBuilder(repository).revList()
                    .format(RevListOutputHandler.FORMAT)
                    .rev(refChange.getToHash());
            for (String branch : branches) {
                revListBuilder = revListBuilder.rev("^" + branch);
            }

            List<YaccCommit> found = revListBuilder.build(new RevListOutputHandler())
                    .call();

            if (found != null) {
                yaccCommits.addAll(found);
            }
        }

        log.debug("found {} commits that need checking", yaccCommits.size());

        return yaccCommits;
    }

    private Set<String> getBranches(GitScmCommandBuilder builder) {
        List<String> branches = builder.command("show-ref")
                .argument("--heads")
                .build(new ShowRefsOutputHandler()).call();

        log.debug("refs={}", branches);

        return new HashSet<>(branches);
    }

    private GitScmCommandBuilder getGitScmCommandBuilder(Repository repository) {
        return (GitScmCommandBuilder) scmService.createBuilder(repository);
    }
}
