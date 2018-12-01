package it.com.isroot.stash.plugin.util;

import org.junit.rules.ExternalResource;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Sean Ford
 * @since 2017-09-02
 */
public class YaccRule extends ExternalResource {
    private static final Logger log = LoggerFactory.getLogger(YaccRule.class);

    private static final String YACC_HOOK_KEY = "com.isroot.stash.plugin.yacc:yaccHook";

    private TemporaryFolder temporaryFolder;
    private GitRepo gitRepo;
    private String repoSlug;
    private BitbucketServerRestClient restClient;

    public YaccRule() {
        temporaryFolder = new TemporaryFolder();
    }

    @Override
    protected void before() throws Throwable {
        temporaryFolder.create();

        repoSlug = UUID.randomUUID().toString();

        restClient = new BitbucketServerRestClient();
        String slug = restClient.createRepo(repoSlug);

        // Clear global hook settings if present
        configureYaccGlobalHook(new HashMap<>());

        // Disable and clear project settings if present
        restClient.setHookSettings(YACC_HOOK_KEY, null, new HashMap<>());
        restClient.disableHook(YACC_HOOK_KEY, null);

        gitRepo = new GitRepo(temporaryFolder.newFolder().toPath(), slug);

        log.info("YaccRule before() finish");
    }

    @Override
    public void after() {
        temporaryFolder.delete();
    }

    public GitRepo getGitRepo() {
        return gitRepo;
    }

    public void enableYaccProjectHook() {
        restClient.enableHook(YACC_HOOK_KEY, null);
    }

    public void enableYaccRepoHook() {
        log.info("enable yacc repo hook");

        restClient.enableHook(YACC_HOOK_KEY, repoSlug);
    }

    public void disableYaccRepoHook() {
        restClient.disableHook(YACC_HOOK_KEY, repoSlug);
    }

    public void configureYaccRepoHook(Map<String, String> settings) {
        restClient.setHookSettings(YACC_HOOK_KEY, repoSlug, settings);
    }

    public void configureYaccProjectHook(Map<String, String> settings) {
        restClient.setHookSettings(YACC_HOOK_KEY, null, settings);
    }

    public void configureYaccGlobalHook(Map<String, String> settings) {
        restClient.doFormPost("/plugins/servlet/yaccHook/config", settings);
    }
}

