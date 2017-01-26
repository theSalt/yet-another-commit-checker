package com.isroot.stash.plugin.commits;

import com.atlassian.bitbucket.io.LineReader;
import com.atlassian.bitbucket.io.LineReaderOutputHandler;
import com.atlassian.bitbucket.scm.CommandOutputHandler;
import com.isroot.stash.plugin.YaccCommit;
import com.isroot.stash.plugin.YaccPerson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Sean Ford
 * @since 2017-01-25
 */
public class RevListOutputHandler extends LineReaderOutputHandler
        implements CommandOutputHandler<List<YaccCommit>> {
    private static final Logger log = LoggerFactory.getLogger(RevListOutputHandler.class);

    private List<YaccCommit> commits = new ArrayList<>();

    public RevListOutputHandler() {
        super("UTF-8");
    }

    @Nullable
    @Override
    public List<YaccCommit> getOutput() {
        return commits;
    }

    @Override
    protected void processReader(LineReader lineReader) throws IOException {
        String line;
        while ((line = lineReader.readLine()) != null) {
            log.debug("rev-list line: {}", line);

            if(line.startsWith("commit ")) {
                YaccCommit commit = parseCommit(line.split(" ")[1], lineReader);
                commits.add(commit);
            }
        }
    }

    private YaccCommit parseCommit(String commit, LineReader lineReader) throws IOException {
        boolean isMerge = false;
        YaccPerson yaccPerson = null;
        String message = null;

        String line;
        while ((line = lineReader.readLine()) != null) {
            if(line.startsWith("Merge: ")) {
                log.debug("found merge: {}", line);
                isMerge = true;
            } else if(line.startsWith("Commit: ")) {
                log.debug("found commit author: {}", line);
                yaccPerson = new YaccPerson(line.substring(8));
            } else if(line.isEmpty()) {
                message = parseMessage(lineReader);
                log.debug("parsed message: {}", message);
                break;
            }
        }

        return new YaccCommit(commit, yaccPerson, message, isMerge);
    }

    private String parseMessage(LineReader lineReader) throws IOException {
        String message = "";

        String line;
        while ((line = lineReader.readLine()) != null && !line.isEmpty()) {
            if(line.startsWith("    ")) {
                if(!message.isEmpty()) {
                    message += "\n";
                }

                message += line.substring(4);
            }
        }

        return message.trim();
    }
}
