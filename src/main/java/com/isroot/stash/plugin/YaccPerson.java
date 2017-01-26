package com.isroot.stash.plugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A git person identity.
 */
public class YaccPerson {
    private static final Logger log = LoggerFactory.getLogger(YaccPerson.class);

    /** Name of person. */
    private final String name;

    /** E-mail address of person */
    private final String emailAddress;

    /**
     * Construct a new person value.
     *
     * @param name The name of the person.
     * @param emailAddress The e-mail address of the person.
     */
    public YaccPerson(@Nonnull String name, @Nonnull String emailAddress) {
        this.name = name;
        this.emailAddress = emailAddress;
    }

    public YaccPerson(@Nonnull String ident) {
        Pattern pattern = Pattern.compile("^(.*)\\s*<([^>]*)>$");

        Matcher matcher = pattern.matcher(ident);
        if(matcher.matches()) {
            name = matcher.group(1).trim();
            emailAddress = matcher.group(2).trim();
        } else {
            log.error("unable to parse ident: {}", ident);
            name = "";
            emailAddress = "";
        }
    }

    /**
     * Return the name associated with this identity.
     *
     * @return Name of person.
     */
    @Nonnull
    public String getName() {
        return name;
    }

    /**
     * Return the e-mail address associated with this identity.
     *
     * @return E-mail of person.
     */
    @Nonnull
    public String getEmailAddress() {
        return emailAddress;
    }
}
