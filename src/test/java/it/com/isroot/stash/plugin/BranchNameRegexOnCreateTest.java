package it.com.isroot.stash.plugin;

import com.google.common.collect.ImmutableMap;
import it.com.isroot.stash.plugin.util.YaccRule;
import org.junit.Rule;
import org.junit.Test;

import javax.json.JsonObject;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Sean Ford
 * @since 2018-12-02
 */
public class BranchNameRegexOnCreateTest {
    @Rule
    public YaccRule gitRepoRule = new YaccRule();

    @Test
    public void testYaccDisabled_newBranchesCanBeCreated() {
        String branchName = "" + System.nanoTime();

        JsonObject response = gitRepoRule.createBranch(branchName);

        assertSuccess(response, branchName);
    }

    @Test
    public void testRepoHook_newBranchRegexOnCreate() {
        gitRepoRule.enableYaccRepoHook();
        gitRepoRule.configureYaccRepoHook(ImmutableMap
                .of("branchNameRegex", "branch_\\d+"));

        JsonObject response = gitRepoRule.createBranch("invalid");
        assertError(response);

        String branchName = "branch_" + System.nanoTime();
        response = gitRepoRule.createBranch(branchName);
        assertSuccess(response, branchName);
    }

    @Test
    public void testProjectHook_newBranchRegexOnCreate() {
        gitRepoRule.enableYaccProjectHook();
        gitRepoRule.configureYaccProjectHook(ImmutableMap
                .of("branchNameRegex", "branch_\\d+"));

        JsonObject response = gitRepoRule.createBranch("invalid");
        assertError(response);

        String branchName = "branch_" + System.nanoTime();
        response = gitRepoRule.createBranch(branchName);
        assertSuccess(response, branchName);
    }

    @Test
    public void testProjectHook_canBeExemptedIfRepoHookDisabled() {
        gitRepoRule.enableYaccProjectHook();
        gitRepoRule.configureYaccProjectHook(ImmutableMap
                .of("branchNameRegex", "branch_\\d+"));

        // Sanity check to make sure project settings are active
        JsonObject response = gitRepoRule.createBranch("hook_disable_override");
        assertError(response);

        gitRepoRule.disableYaccRepoHook();
        response = gitRepoRule.createBranch("hook_disable_override");
        assertSuccess(response, "hook_disable_override");
    }

    @Test
    public void testGlobalSettings_newBranchRegexOnCreate() {
        gitRepoRule.configureYaccGlobalHook(ImmutableMap
                .of("branchNameRegex", "global_\\d+"));

        assertError(gitRepoRule.createBranch("invalid"));

        String branchName = "global_" + System.nanoTime();
        assertSuccess(gitRepoRule.createBranch(branchName), branchName);
    }

    @Test
    public void testGlobalSettings_repoHookSettingsUsedInsteadIfPresent() {
        gitRepoRule.configureYaccGlobalHook(ImmutableMap
                .of("branchNameRegex", "global_\\d+"));

        String branchName = "repo_" + System.nanoTime();
        assertError(gitRepoRule.createBranch(branchName));

        gitRepoRule.enableYaccRepoHook();
        gitRepoRule.configureYaccRepoHook(ImmutableMap
                .of("branchNameRegex", "repo_\\d+"));

        assertSuccess(gitRepoRule.createBranch(branchName), branchName);
    }

    @Test
    public void testGlobalSettings_projectHookSettingsUsedInsteadIfPresent() {
        gitRepoRule.configureYaccGlobalHook(ImmutableMap
                .of("branchNameRegex", "global_\\d+"));

        String branchName = "repo_" + System.nanoTime();
        assertError(gitRepoRule.createBranch(branchName));

        gitRepoRule.enableYaccProjectHook();
        gitRepoRule.configureYaccProjectHook(ImmutableMap
                .of("branchNameRegex", "repo_\\d+"));

        assertSuccess(gitRepoRule.createBranch(branchName), branchName);
    }

    private void assertError(JsonObject response) {
        JsonObject firstError = response.getJsonArray("errors").getJsonObject(0);

        assertThat(firstError.getString("message"))
                .isEqualTo("Branch creation canceled");

        String details = firstError.getJsonArray("details").getString(0);
        assertThat(details)
                .startsWith("Branch name does not comply with repository requirements.\n" +
                        "Invalid branch name.")
                .contains("does not match regex");
    }

    private void assertSuccess(JsonObject response, String branchName) {
        assertThat(response.getString("id"))
                .contains(branchName);
    }
}
