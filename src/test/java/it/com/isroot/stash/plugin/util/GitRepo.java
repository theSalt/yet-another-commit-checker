package it.com.isroot.stash.plugin.util;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Sean Ford
 * @since 2017-08-20
 */
public class GitRepo {
    private static final Logger log = LoggerFactory.getLogger(GitRepo.class);

    private final static String USER = "admin";
    private final static String PASSWORD = "admin";

    private final Git git;

    public GitRepo(Path location, String slug) {
        try {
            git = Git.cloneRepository()
                    .setDirectory(location.toFile())
                    .setURI("http://localhost:7990/bitbucket/scm/project_1/" + slug)
                    .setCredentialsProvider(getCredentialsProvider())
                    .call();

            initializeRepository();
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
    }

    public Git getGit() {
        return git;
    }

    public PushResult push() {
        return push("master");
    }

    public PushResult push(String ref) {
        try {
            log.info("pushing repo dir: ref={} in {}", ref, git.getRepository().getDirectory());

            PushResult result = git.push()
                    .add(ref)
                    .setCredentialsProvider(getCredentialsProvider())
                    .call().iterator().next();

            log.info("push message: {}", result.getMessages());

            return result;
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
    }

    public void addFile(String fileName) {
        try {
            Path file = git.getRepository().getDirectory().getParentFile()
                    .toPath().resolve(fileName);
            Files.write(file, "content".getBytes());

            git.add()
                    .addFilepattern(fileName)
                    .call();
        } catch (IOException | GitAPIException e) {
            throw new RuntimeException(e);
        }

    }

    public GitRepo commitFile(String fileName, String commitMessage) {
        commitFile(fileName, commitMessage, new PersonIdent("YaccName", "yacc@email.com"));
        return this;
    }

    public GitRepo commitFile(String fileName, String commitMessage, PersonIdent committer) {
        try {
            addFile(fileName);

            git.commit()
                    .setMessage(commitMessage)
                    .setCommitter(committer)
                    .call();
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }

        return this;
    }

    public CredentialsProvider getCredentialsProvider() {
        return new UsernamePasswordCredentialsProvider(USER, PASSWORD);
    }

    private void initializeRepository() {
        commitFile("testfile", "initial commit");
        push("master");
    }

}
