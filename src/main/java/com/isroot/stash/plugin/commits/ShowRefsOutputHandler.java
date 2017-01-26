package com.isroot.stash.plugin.commits;

import com.atlassian.bitbucket.io.LineReader;
import com.atlassian.bitbucket.io.LineReaderOutputHandler;
import com.atlassian.bitbucket.scm.CommandOutputHandler;
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
public class ShowRefsOutputHandler extends LineReaderOutputHandler implements CommandOutputHandler<List<String>> {
    private static final Logger log = LoggerFactory.getLogger(ShowRefsOutputHandler.class);

    private List<String> refs = new ArrayList<>();

    public ShowRefsOutputHandler() {
        super("UTF-8");
    }

    @Nullable
    @Override
    public List<String> getOutput() {
        return refs;
    }

    @Override
    protected void processReader(LineReader lineReader) throws IOException {
        String line;
        while ((line = lineReader.readLine()) != null) {
            log.debug("show-refs line: {}", line);

            refs.add(line.split(" ")[1]);
        }
    }
}
