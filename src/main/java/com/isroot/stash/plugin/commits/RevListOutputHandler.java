package com.isroot.stash.plugin.commits;

import com.atlassian.bitbucket.io.LineReader;
import com.atlassian.bitbucket.io.LineReaderOutputHandler;
import com.atlassian.bitbucket.scm.CommandOutputHandler;
import com.atlassian.bitbucket.user.SimplePerson;
import com.isroot.stash.plugin.YaccCommit;
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
    public static final String FORMAT = "%H%x02%P%x02%cN%x02%cE%n%B%n%x03END%x04";
    private static final String OBJECT_END = "\u0003END\u0004";

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

            if(!line.startsWith("commit ")) {
                throw new RuntimeException("unexpected line: "+ line);
            }

            line = lineReader.readLine();

            log.debug("parsing metadata from line: {}", line);

            String[] commitData = line.split("\u0002");

            String ref = commitData[0];
            boolean isMerge = commitData[1].contains(" ");
            String committerName = commitData[2];

            String committerEmail = "";
            if(commitData.length > 3) {
                committerEmail = commitData[3];
            }

            String message = parseMessage(lineReader);

            SimplePerson person = new SimplePerson(committerName, committerEmail);

            commits.add(new YaccCommit(ref, person, message, isMerge));
        }
    }

    private String parseMessage(LineReader lineReader) throws IOException {
        String message = "";

        String line;
        while ((line = lineReader.readLine()) != null && !line.equals(OBJECT_END)) {
            if(!message.isEmpty()) {
                message += "\n";
            }

            message += line.trim();
        }

        return message.trim();
    }
}
