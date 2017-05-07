package com.isroot.stash.plugin;

import com.atlassian.bitbucket.hook.repository.CommitAddedDetails;
import com.atlassian.bitbucket.hook.repository.PreRepositoryHookCommitCallback;
import com.atlassian.bitbucket.hook.repository.RepositoryHookResult;
import com.atlassian.bitbucket.scm.git.GitRefPattern;
import com.atlassian.bitbucket.setting.Settings;
import com.isroot.stash.plugin.errors.YaccError;
import com.isroot.stash.plugin.errors.YaccErrorBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Sean Ford
 * @since 2017-05-06
 */
class YaccHookCommitCallback implements PreRepositoryHookCommitCallback {
    private static final Logger log = LoggerFactory.getLogger(YaccHookCommitCallback.class);

    private final Settings settings;
    private final YaccService yaccService;
    private final List<YaccError> errors;

    private RepositoryHookResult result = RepositoryHookResult.accepted();

    public YaccHookCommitCallback(YaccService yaccService, Settings settings) {
        this.settings = settings;
        this.yaccService = yaccService;
        this.errors = new ArrayList<>();
    }

    @Override
    public boolean onCommitAdded(@Nonnull CommitAddedDetails commitDetails) {
        log.debug("yacc commit callback, ref={} commit={}", commitDetails.getRef().getId(),
                commitDetails.getCommit().getId());

        YaccCommit yaccCommit = new YaccCommit(commitDetails.getCommit());

        String branchName = commitDetails.getRef().getId()
                .replace(GitRefPattern.HEADS.getPath(), "");

        List<YaccError> commitErrors = yaccService.checkCommit(settings, yaccCommit, true,
                branchName);

        for (YaccError e : commitErrors) {
            String refAndCommitId = String.format("%s: %s",
                    commitDetails.getRef().getId(), commitDetails.getCommit().getId());

            errors.add(e.prependText(refAndCommitId));
        }

        return true;
    }

    @Override
    public void onEnd() {
        log.info("callback onEnd");

        if (!errors.isEmpty()) {
            YaccErrorBuilder errorBuilder = new YaccErrorBuilder(settings);
            String message = errorBuilder.getErrorMessage(errors);

            result = RepositoryHookResult.rejected("Commit rejected by YACC", message);
        }
    }

    @Nonnull
    @Override
    public RepositoryHookResult getResult() {
        return result;
    }
}
