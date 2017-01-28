package com.isroot.stash.plugin.commits;

import com.atlassian.bitbucket.repository.RefChange;
import com.atlassian.bitbucket.repository.RefChangeType;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.scm.ScmService;
import com.atlassian.bitbucket.scm.git.GitRefPattern;
import com.atlassian.bitbucket.scm.git.GitScm;
import com.atlassian.bitbucket.scm.git.command.GitScmCommandBuilder;
import com.atlassian.bitbucket.scm.git.command.revlist.GitRevListBuilder;
import com.google.common.collect.Sets;
import com.isroot.stash.plugin.YaccCommit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        log.debug("getNewCommits, scmId={} refChange.toHash={}", repository.getScmId(),
                refChange.getToHash());

        Set<YaccCommit> yaccCommits = Sets.newHashSet();

        if (!GitScm.ID.equals(repository.getScmId())) {
            log.warn("scmId={} not supported", repository.getScmId());

            return yaccCommits;
        }

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
        } else {
            GitRevListBuilder revListBuilder = getGitScmCommandBuilder(repository).revList()
                    .format(RevListOutputHandler.FORMAT)
                    .revs(refChange.getToHash(), "--not", "--all");

            List<YaccCommit> found = revListBuilder.build(new RevListOutputHandler())
                    .call();

            if (found != null) {
                yaccCommits.addAll(found);
            }
        }

        log.debug("found {} commits that need checking", yaccCommits.size());

        return yaccCommits;
    }

    private GitScmCommandBuilder getGitScmCommandBuilder(Repository repository) {
        return (GitScmCommandBuilder) scmService.createBuilder(repository);
    }
}
