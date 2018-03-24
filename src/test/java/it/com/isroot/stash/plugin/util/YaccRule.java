package it.com.isroot.stash.plugin.util;

import org.junit.rules.ExternalResource;
import org.junit.rules.TemporaryFolder;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Sean Ford
 * @since 2017-09-02
 */
public class YaccRule extends ExternalResource {
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

        gitRepo = new GitRepo(temporaryFolder.newFolder().toPath(), slug);
    }

    @Override
    public void after() {
        temporaryFolder.delete();
    }

    public GitRepo getGitRepo() {
        return gitRepo;
    }

    public void enableYaccRepoHook() {
        restClient.enableHook(repoSlug, YACC_HOOK_KEY);
    }

    public void disableYaccRepoHook() {
        restClient.disableHook(repoSlug, YACC_HOOK_KEY);
    }

    public void configureYaccRepoHook(Map<String, String> settings) {
        restClient.setHookSettings(repoSlug, YACC_HOOK_KEY, settings);
    }

    public void configureYaccGlobalHook(Map<String, String> settings) {
        restClient.doFormPost("/plugins/servlet/yaccHook/config", settings);
    }
}

