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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Sean Ford
 * @since 2017-01-29
 */
public class AnnotatedTagOutputHandler extends LineReaderOutputHandler
        implements CommandOutputHandler<YaccCommit> {
    private static final Logger log = LoggerFactory.getLogger(AnnotatedTagOutputHandler.class);

    private String ref;
    private YaccCommit yaccCommit = null;

    public AnnotatedTagOutputHandler(String ref) {
        super("UTF-8");
        this.ref = ref;
    }

    @Nullable
    @Override
    public YaccCommit getOutput() {
        return yaccCommit;
    }

    @Override
    protected void processReader(LineReader lineReader) throws IOException {
        String line;

        SimplePerson tagger = new SimplePerson("", "");
        boolean isTag = false;
        String message = null;

        while ((line = readLine(lineReader)) != null) {
            if (line.startsWith("tag ")) {
                isTag = true;
            } else if (line.startsWith("tagger ")) {
                tagger = parseTagger(line);
            } else if (line.isEmpty()) {
                message = parseMessage(lineReader);
            }
        }

        if(isTag) {
            yaccCommit = new YaccCommit(ref, tagger, message, false);
        }
    }


    private SimplePerson parseTagger(String line) {
        Pattern pattern = Pattern.compile("^tagger (.*)\\s*<([^>]*)> .*$");
        Matcher matcher = pattern.matcher(line);
        if (matcher.matches()) {
            return new SimplePerson(matcher.group(1).trim(),
                    matcher.group(2).trim());
        } else {
            log.error("unable to parse tagger: {}", line);
            return new SimplePerson("", "");
        }
    }

    private String parseMessage(LineReader lineReader) throws IOException {
        log.debug("parsing message");

        String message = "";

        String line;
        while ((line = readLine(lineReader)) != null) {
            message += line + "\n";
        }

        return message;
    }

    private String readLine(LineReader lineReader) throws IOException {
        String line = lineReader.readLine();
        log.debug("line: {}", line);
        return line;
    }
}
