package com.opensource.leo.localtask.entrance;

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * User:leo.lx
 * Date:15/10/10
 */
public class OptionsParser {
    protected final Logger logger = LoggerFactory.getLogger(OptionsParser.class);
    final Options opt = new Options();

    public OptionsParser() {
        opt.addOption(TaskConfig.RUN_CMD, true, "which tash to run");
    }

    public Set<String> parse(final String[] args) {
        if (args.length == 0) {
            return Collections.emptySet();
        }
        CommandLineParser parser = new PosixParser();
        CommandLine cl;
        try {
            cl = parser.parse(opt, args);
        } catch (ParseException e) {
            logger.error("[OptionsParser] : parser.parse error", e);
            throw new TaskException("[OptionsParser] : parse args error", e);
        }
        Set<String> params = new HashSet<String>();
        // run param
        if (cl.hasOption(TaskConfig.RUN_CMD)) {
            String[] tasks = cl.getOptionValues(TaskConfig.RUN_CMD);
            params.addAll(Arrays.asList(tasks));
        }
        return params;
    }
}
