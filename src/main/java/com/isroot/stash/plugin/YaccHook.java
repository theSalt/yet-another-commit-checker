package com.isroot.stash.plugin;

import com.atlassian.bitbucket.hook.repository.PreRepositoryHook;
import com.atlassian.bitbucket.hook.repository.PreRepositoryHookContext;
import com.atlassian.bitbucket.hook.repository.RepositoryHookResult;
import com.atlassian.bitbucket.hook.repository.RepositoryPushHookRequest;
import com.atlassian.bitbucket.setting.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

/**
 * @author Sean Ford
 * @since 2013-05-11
 */
public class YaccHook implements PreRepositoryHook<RepositoryPushHookRequest> {
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
        final Settings settings = context.getSettings();

        log.debug("yacc settings: {}", settings.asMap());

        return yaccService.check(context, repositoryPushHookRequest, settings);
    }
}
