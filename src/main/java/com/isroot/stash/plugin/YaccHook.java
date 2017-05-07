package com.isroot.stash.plugin;

import com.atlassian.bitbucket.hook.repository.PreRepositoryHook;
import com.atlassian.bitbucket.hook.repository.PreRepositoryHookContext;
import com.atlassian.bitbucket.hook.repository.RepositoryHookCommitFilter;
import com.atlassian.bitbucket.hook.repository.RepositoryHookResult;
import com.atlassian.bitbucket.hook.repository.RepositoryPushHookRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

/**
 * @author Sean Ford
 * @since 2013-05-11
 */
public final class YaccHook implements PreRepositoryHook<RepositoryPushHookRequest> {
    private static final Logger log = LoggerFactory.getLogger(YaccHook.class);

    private final YaccService yaccService;

    public YaccHook(YaccService yaccService) {
        this.yaccService = yaccService;
    }

    @Nonnull
    @Override
    public RepositoryHookResult preUpdate(
            @Nonnull PreRepositoryHookContext context,
            @Nonnull RepositoryPushHookRequest repositoryPushHookRequest) {

        log.debug("YaccHook preUpdate, registering commit callback");

        // TODO: NEED TO TEST
        // git notes

        context.registerCommitCallback(
                new YaccHookCommitCallback(yaccService, context.getSettings()),
                RepositoryHookCommitFilter.ADDED_TO_REPOSITORY);

        // Will be accepted unless commit callback rejects a commit
        return RepositoryHookResult.accepted();
    }
}
