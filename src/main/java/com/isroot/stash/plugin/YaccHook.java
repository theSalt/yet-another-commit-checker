package com.isroot.stash.plugin;

import com.atlassian.bitbucket.hook.repository.PreRepositoryHook;
import com.atlassian.bitbucket.hook.repository.PreRepositoryHookContext;
import com.atlassian.bitbucket.hook.repository.RepositoryHookCommitFilter;
import com.atlassian.bitbucket.hook.repository.RepositoryHookResult;
import com.atlassian.bitbucket.hook.repository.RepositoryPushHookRequest;
import com.atlassian.bitbucket.repository.RefChange;
import com.atlassian.bitbucket.repository.RefChangeType;
import com.atlassian.bitbucket.setting.Settings;
import com.isroot.stash.plugin.checks.BranchNameCheck;
import com.isroot.stash.plugin.errors.YaccError;
import com.isroot.stash.plugin.errors.YaccErrorBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

        return check(context, repositoryPushHookRequest, settings);

    }

    public RepositoryHookResult check(PreRepositoryHookContext context,
            RepositoryPushHookRequest repositoryPushHookRequest, Settings settings) {
        log.debug("YaccHook preUpdate, registering commit callback. settings={}", settings);

        List<YaccError> errors = checkRefs(settings, repositoryPushHookRequest.getRefChanges());
        if (!errors.isEmpty()) {
            YaccErrorBuilder errorBuilder = new YaccErrorBuilder(settings);
            String message = errorBuilder.getErrorMessage(errors);

            return RepositoryHookResult.rejected("Push rejected by YACC", message);
        }

        context.registerCommitCallback(
                new YaccHookCommitCallback(yaccService, settings),
                RepositoryHookCommitFilter.ADDED_TO_REPOSITORY);

        // Will be accepted unless commit callback rejects a commit
        return RepositoryHookResult.accepted();
    }

    private List<YaccError> checkRefs(Settings settings, Collection<RefChange> refChanges) {
        List<YaccError> errors = new ArrayList<>();

        for (RefChange refChange : refChanges) {
            log.debug("refChange: ref={} type={} fromHash={} toHash={}", refChange.getRef(),
                    refChange.getType(), refChange.getFromHash(), refChange.getToHash());

            if (refChange.getType() == RefChangeType.ADD) {
                errors.addAll(new BranchNameCheck(settings, refChange.getRef().getId()).check());
            }
        }

        return errors;
    }
}
