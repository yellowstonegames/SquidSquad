/*
 * Copyright (c) 2020-2024 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.yellowstonegames.text;

import com.github.tommyettinger.ds.ObjectObjectMap;
import regexodus.MatchResult;
import regexodus.Pattern;
import regexodus.Replacer;
import regexodus.Substitution;
import regexodus.TextBuffer;

/**
 * Helps handle formation of messages from a template, using correct pronouns and helping handle various idiosyncrasies
 * in English-language text. You call the static method
 * {@link #transform(CharSequence, String, Pronoun, String, Pronoun)} (or one of its overloads) with a template that
 * has specific placeholder glyphs, along with a user name, optional target name, user Pronoun (an enum in this class)
 * to specify how the user should be addressed, including their gender, optional target Pronoun, and possibly extra
 * terms that should be inserted. The placeholder glyphs are usually followed by a specific word that is conjugated in
 * the template for the first-person case, and will be changed to fit the Pronoun for the user or target. For example,
 * you could use "@Name hit$ ^ for ~ damage!" as a message. You could transform it with user "Heero Supra", userTrait
 * Pronoun.SECOND_PERSON_SINGULAR, target "the beast", targetTrait Pronoun.UNSPECIFIED_GENDER, and extra "10" to get
 * the message "You hit the beast for 10 damage!". You could swap the user and target (along with their traits) to get
 * the message "The beast hits you for 10 damage!" You can handle more complex verbs in some cases, such as "@I hurr$$$
 * to catch up!" can be transformed to "You hurry to catch up!" or "He hurries to catch up!". The rules are fairly
 * simple; @word conjugates a specific word from a list to the correct kind for the user, while ^word does a similar
 * thing but conjugates for the target. Between 1 and 3 $ chars can be used at the end of verbs to conjugate them
 * appropriately for the present tense when the verb is performed by the user (with just $) or alternately the target
 * (if the $ chars are preceded by a ^), while @s, @ss, @sss, ^s, ^ss, or ^sss can be added at the end of nouns to
 * pluralize them if appropriate. Using one $ or s will add s or nothing, as in the case of hit becoming hits, using two
 * $ or s chars will add es or nothing, as in the case of scratch becoming scratches, and using three will add ies or y,
 * as in the case of carry becoming carries. Some unusual pluralization forms are handled; @usi will turn octop@usi into
 * octopus or octopi, and radi@usi into radius or radii, while @fves will turn el@fves into elf or elves, or dwar@fves
 * into dwarf or dwarves.
 * <br>
 * The words you can put after a @ or ^ start with a small list and can be added to with
 * {@link #learnIrregularWord(String, String, String, String, String, String, String)}. The initial list is: name,
 * name_s, i, me, my, mine, myself, am, have, do, haven_t, don_t, or any of these with the first char capitalized (meant
 * for words at the start of sentences). The non-word shortened terms "m" and "ve" can be used for "I'm" and "I've",
 * respectively, as well as "you're" and "you've", plus "he's" for "he is" and "he's" for "he has". Most of the rest
 * conjugate as you would expect; @me will become him, her, it, them, you, or still more forms depending on userTrait.
 * You can also use @ or ^ on its own as an equivalent to @name or ^name.
 * <br>
 * Examples:
 * <br>
 * {@code Messaging.transform("@I @am @my own boss@ss.", "unused", changingTrait)}
 * <ul>
 *     <li>When changingTrait is {@code Pronoun.FIRST_PERSON_SINGULAR}, this returns "I am my own boss."</li>
 *     <li>When changingTrait is {@code Pronoun.FIRST_PERSON_PLURAL}, this returns "We are our own bosses."</li>
 *     <li>When changingTrait is {@code Pronoun.SECOND_PERSON_SINGULAR}, this returns "You are your own boss."</li>
 *     <li>When changingTrait is {@code Pronoun.SECOND_PERSON_PLURAL}, this returns "You are your own bosses."</li>
 *     <li>When changingTrait is {@code Pronoun.NO_GENDER}, this returns "It is its own boss."</li>
 *     <li>When changingTrait is {@code Pronoun.MALE_GENDER}, this returns "He is his own boss."</li>
 *     <li>When changingTrait is {@code Pronoun.FEMALE_GENDER}, this returns "She is her own boss."</li>
 *     <li>When changingTrait is {@code Pronoun.UNSPECIFIED_GENDER}, this returns "They are their own boss."</li>
 *     <li>When changingTrait is {@code Pronoun.ADDITIONAL_GENDER}, this returns "Xe is xis own boss."</li>
 *     <li>When changingTrait is {@code Pronoun.SPECIAL_CASE_GENDER}, this returns "Qvqe is qvqis own boss."</li>
 *     <li>When changingTrait is {@code Pronoun.GROUP}, this returns "They are their own bosses."</li>
 * </ul>
 * {@code Messaging.transform("@Name spit$ in ^name_s face^s!", userName, userTrait, targetName, targetTrait)}
 * <ul>
 *     <li>When userTrait is {@code Pronoun.SECOND_PERSON_SINGULAR}, targetName is {@code "the goblin"}, and
 *     targetTrait is {@code Pronoun.MALE_GENDER}, this returns "You spit in the goblin's face!"</li>
 *     <li>When userName is {@code "the goblin"}, userTrait is {@code Pronoun.MALE_GENDER}, and targetTrait is
 *     {@code Pronoun.SECOND_PERSON_SINGULAR}, this returns "The goblin spits in your face!"</li>
 *     <li>When userTrait is {@code Pronoun.SECOND_PERSON_SINGULAR}, targetName is {@code "the goblins"}, and
 *     targetTrait is {@code Pronoun.GROUP}, this returns "You spit in the goblins' faces!"</li>
 *     <li>When userName is {@code "the goblins"}, userTrait is {@code Pronoun.GROUP}, and targetTrait is
 *     {@code Pronoun.SECOND_PERSON_SINGULAR}, this returns "The goblins spit in your face!"</li>
 * </ul>
 */
public final class Messaging {
    /**
     * No need to instantiate.
     */
    private Messaging(){
    }

    /**
     * Takes message and replaces any of the special terms this recognizes, like @, ^, and $, with the appropriately-
     * conjugated terms for the given user and their associated Pronoun.
     * @param message the message to transform; should contain "@" or "$" in it, at least, to be replaced
     * @param user the name of the user for cases where it can replace text like "@" or "@Name"
     * @param userTrait the {@link Pronoun} enum that determines how user should be referred to
     * @return a String resulting from the processing of message
     */
    public static String transform(CharSequence message, String user, Pronoun userTrait)
    {
        Replacer ur = new Replacer(userPattern, new BeingSubstitution(user, userTrait, true));
        return ur.replace(message);
    }

    /**
     * Takes message and replaces any of the special terms this recognizes, like @, ^, and $, with the appropriately-
     * conjugated terms for the given group of users and that group's associated Pronoun. The Pronoun only matters
     * if it is first-person or second-person (in which case this uses the plural form) or if users contains one
     * member (in which case it uses any gendered pronouns specified by userTrait); it uses {@link Pronoun#GROUP} in
     * any other case.
     * @param message the message to transform; should contain "@" or "$" in it, at least, to be replaced
     * @param users a String array as a group of users for cases where it can replace text, like "@" or "@Name"
     * @param userTrait the {@link Pronoun} enum that determines how users should be referred to
     * @return a String resulting from the processing of message
     */
    public static String transform(CharSequence message, String[] users, Pronoun userTrait)
    {
        Replacer ur = new Replacer(userPattern, new BeingSubstitution(userTrait, true, users));
        return ur.replace(message);
    }

    /**
     * Takes message and replaces any of the special terms this recognizes, like @, ^, and $, with the appropriately-
     * conjugated terms for the given user, their associated Pronoun, the given target, and their Pronoun.
     * @param message the message to transform; should contain "@", "^", or "$" in it, at least, to be replaced
     * @param user the name of the user for cases where it can replace text like "@" or "@Name"
     * @param userTrait the {@link Pronoun} enum that determines how user should be referred to
     * @param target the name of the target for cases where it can replace text like "^" or "^Name"
     * @param targetTrait the {@link Pronoun} enum that determines how the target should be referred to
     * @return a String resulting from the processing of message
     */
    public static String transform(CharSequence message, String user, Pronoun userTrait, String target, Pronoun targetTrait)
    {
        Replacer tr = new Replacer(targetPattern, new BeingSubstitution(target, targetTrait, false)),
                ur = new Replacer(userPattern, new BeingSubstitution(user, userTrait, true));
        return ur.replace(tr.replace(message));
    }

    /**
     * Takes message and replaces any of the special terms this recognizes, like @, ^, and $, with the appropriately-
     * conjugated terms for the given array of users, that group's associated Pronoun, the given target, and their
     * Pronoun. The Pronoun for users only matters if it is first-person or second-person (in which case this uses
     * the plural form) or if users contains one member (in which case it uses any gendered pronouns specified by
     * userTrait); it uses {@link Pronoun#GROUP} in any other case.
     * @param message the message to transform; should contain "@", "^", or "$" in it, at least, to be replaced
     * @param users a String array as a group of users for cases where they can replace text like "@" or "@Name"
     * @param userTrait the {@link Pronoun} enum that determines how users should be referred to
     * @param target the name of the target for cases where it can replace text like "^" or "^Name"
     * @param targetTrait the {@link Pronoun} enum that determines how the target should be referred to
     * @return a String resulting from the processing of message
     */
    public static String transform(CharSequence message, String[] users, Pronoun userTrait, String target, Pronoun targetTrait)
    {
        Replacer tr = new Replacer(targetPattern, new BeingSubstitution(target, targetTrait, false)),
                ur = new Replacer(userPattern, new BeingSubstitution(userTrait, true, users));
        return ur.replace(tr.replace(message));
    }

    /**
     * Takes message and replaces any of the special terms this recognizes, like @, ^, and $, with the appropriately-
     * conjugated terms for the given array of users, that group's associated Pronoun, the given array of targets, and
     * that group's Pronoun. The Pronouns only matter if they are is first-person or second-person (in which case
     * this uses the plural form) or if an array contains one member (in which case it uses any gendered pronouns
     * specified by userTrait or targetTrait); it uses {@link Pronoun#GROUP} in any other case.
     * @param message the message to transform; should contain "@", "^", or "$" in it, at least, to be replaced
     * @param users a String array as a group of users for cases where they can replace text like "@" or "@Name"
     * @param userTrait the {@link Pronoun} enum that determines how users should be referred to
     * @param targets a String array as a group of targets for cases where they can replace text like "@" or "@Name"
     * @param targetTrait the {@link Pronoun} enum that determines how the targets should be referred to
     * @return a String resulting from the processing of message
     */
    public static String transform(CharSequence message, String[] users, Pronoun userTrait, String[] targets, Pronoun targetTrait)
    {
        Replacer tr = new Replacer(targetPattern, new BeingSubstitution(targetTrait, false, targets)),
                ur = new Replacer(userPattern, new BeingSubstitution(userTrait, true, users));
        return ur.replace(tr.replace(message));
    }


    /**
     * Takes message and replaces any of the special terms this recognizes, like @, ^, and $, with the appropriately-
     * conjugated terms for the given user, that user's associated Pronoun, the given array of targets, and that
     * group's Pronoun. The Pronoun for targets only matters if it is first-person or second-person (in which case
     * this uses the plural form) or if the array contains one member (in which case it uses any gendered pronouns
     * specified by targetTrait); it uses {@link Pronoun#GROUP} in any other case.
     * @param message the message to transform; should contain "@", "^", or "$" in it, at least, to be replaced
     * @param user the name of the user for cases where it can replace text like "@" or "@Name"
     * @param userTrait the {@link Pronoun} enum that determines how user should be referred to
     * @param targets a String array as a group of targets for cases where they can replace text like "@" or "@Name"
     * @param targetTrait the {@link Pronoun} enum that determines how the targets should be referred to
     * @return a String resulting from the processing of message
     */
    public static String transform(CharSequence message, String user, Pronoun userTrait, String[] targets, Pronoun targetTrait)
    {
        Replacer tr = new Replacer(targetPattern, new BeingSubstitution(targetTrait, false, targets)),
                ur = new Replacer(userPattern, new BeingSubstitution(user, userTrait, true));
        return ur.replace(tr.replace(message));
    }

    /**
     * Takes message and replaces any of the special terms this recognizes, like @, ^, and $, with the appropriately-
     * conjugated terms for the given user, their associated Pronoun, the given target, and their Pronoun. Also
     * replaces the nth occurrence of "~" with the matching nth item in extra, so the first "~" is replaced with the
     * first item in extra, the second "~" with the second item, and so on until one is exhausted.
     * @param message the message to transform; should contain "@", "^", "$", or "~" in it, at least, to be replaced
     * @param user the name of the user for cases where it can replace text like "@" or "@Name"
     * @param userTrait the {@link Pronoun} enum that determines how user should be referred to
     * @param target the name of the target for cases where it can replace text like "^" or "^Name"
     * @param targetTrait the {@link Pronoun} enum that determines how the target should be referred to
     * @param extra an array or vararg of String where the nth item in extra will replace the nth occurrence of "~"
     * @return a String resulting from the processing of message
     */
    public static String transform(CharSequence message, String user, Pronoun userTrait, String target, Pronoun targetTrait, String... extra)
    {
        Replacer tr = new Replacer(targetPattern, new BeingSubstitution(target, targetTrait, false)),
                ur = new Replacer(userPattern, new BeingSubstitution(user, userTrait, true));
        String text = ur.replace(tr.replace(message));
        if(extra != null && extra.length > 0)
        {
            for (int i = 0; i < extra.length; i++) {
                text = text.replaceFirst("~", extra[i]);
            }
        }
        return text;
    }

    /**
     * Takes message and replaces any of the special terms this recognizes, like @, ^, and $, with the appropriately-
     * conjugated terms for the given array of users, that group's associated Pronoun, the given target, and their
     * Pronoun. Also replaces the nth occurrence of "~" with the matching nth item in extra, so the first "~" is
     * replaced with the first item in extra, the second "~" with the second item, and so on until one is exhausted. The
     * Pronoun for users only matters if it is first-person or second-person (in which case this uses the plural form)
     * or if the array contains one member (in which case it uses any gendered pronouns specified by userTrait); it uses
     * {@link Pronoun#GROUP} in any other case.
     * @param message the message to transform; should contain "@", "^", or "$" in it, at least, to be replaced
     * @param users a String array as a group of users for cases where they can replace text like "@" or "@Name"
     * @param userTrait the {@link Pronoun} enum that determines how users should be referred to
     * @param target the name of the target for cases where it can replace text like "^" or "^Name"
     * @param targetTrait the {@link Pronoun} enum that determines how the target should be referred to
     * @param extra an array or vararg of String where the nth item in extra will replace the nth occurrence of "~"
     * @return a String resulting from the processing of message
     */
    public static String transform(CharSequence message, String[] users, Pronoun userTrait, String target, Pronoun targetTrait, String... extra)
    {
        Replacer tr = new Replacer(targetPattern, new BeingSubstitution(target, targetTrait, false)),
                ur = new Replacer(userPattern, new BeingSubstitution(userTrait, true, users));
        String text = ur.replace(tr.replace(message));
        if(extra != null && extra.length > 0)
        {
            for (int i = 0; i < extra.length; i++) {
                text = text.replaceFirst("~", extra[i]);
            }
        }
        return text;
    }

    /**
     * Takes message and replaces any of the special terms this recognizes, like @, ^, and $, with the appropriately-
     * conjugated terms for the given array of users, that group's associated Pronoun, the given group of targets, and
     * that group's Pronoun. Also replaces the nth occurrence of "~" with the matching nth item in extra, so the first
     * "~" is replaced with the first item in extra, the second "~" with the second item, and so on until one is
     * exhausted. The Pronouns only matter if they are is first-person or second-person (in which case
     * this uses the plural form) or if an array contains one member (in which case it uses any gendered pronouns
     * specified by userTrait or targetTrait); it uses {@link Pronoun#GROUP} in any other case.
     * @param message the message to transform; should contain "@", "^", or "$" in it, at least, to be replaced
     * @param users a String array as a group of users for cases where they can replace text like "@" or "@Name"
     * @param userTrait the {@link Pronoun} enum that determines how users should be referred to
     * @param targets a String array as a group of targets for cases where they can replace text like "@" or "@Name"
     * @param targetTrait the {@link Pronoun} enum that determines how the targets should be referred to
     * @param extra an array or vararg of String where the nth item in extra will replace the nth occurrence of "~"
     * @return a String resulting from the processing of message
     */
    public static String transform(CharSequence message, String[] users, Pronoun userTrait, String[] targets, Pronoun targetTrait, String... extra)
    {
        Replacer tr = new Replacer(targetPattern, new BeingSubstitution(targetTrait, false, targets)),
                ur = new Replacer(userPattern, new BeingSubstitution(userTrait, true, users));
        String text = ur.replace(tr.replace(message));
        if(extra != null && extra.length > 0)
        {
            for (int i = 0; i < extra.length; i++) {
                text = text.replaceFirst("~", extra[i]);
            }
        }
        return text;
    }


    /**
     * Takes message and replaces any of the special terms this recognizes, like @, ^, and $, with the appropriately-
     * conjugated terms for the given user, that user's associated Pronoun, the given array of targets, and that
     * group's Pronoun. Also replaces the nth occurrence of "~" with the matching nth item in extra, so the first "~"
     * is replaced with the first item in extra, the second "~" with the second item, and so on until one is exhausted.
     * The Pronoun for targets only matters if it is first-person or second-person (in which case this uses the plural
     * form) or if the array contains one member (in which case it uses any gendered pronouns specified by targetTrait);
     * it uses {@link Pronoun#GROUP} in any other case.
     * @param message the message to transform; should contain "@", "^", or "$" in it, at least, to be replaced
     * @param user the name of the user for cases where it can replace text like "@" or "@Name"
     * @param userTrait the {@link Pronoun} enum that determines how user should be referred to
     * @param targets a String array as a group of targets for cases where they can replace text like "@" or "@Name"
     * @param targetTrait the {@link Pronoun} enum that determines how the targets should be referred to
     * @param extra an array or vararg of String where the nth item in extra will replace the nth occurrence of "~"
     * @return a String resulting from the processing of message
     */
    public static String transform(CharSequence message, String user, Pronoun userTrait, String[] targets, Pronoun targetTrait, String... extra)
    {
        Replacer tr = new Replacer(targetPattern, new BeingSubstitution(targetTrait, false, targets)),
                ur = new Replacer(userPattern, new BeingSubstitution(user, userTrait, true));
        String text = ur.replace(tr.replace(message));
        if(extra != null && extra.length > 0)
        {
            for (int i = 0; i < extra.length; i++) {
                text = text.replaceFirst("~", extra[i]);
            }
        }
        return text;
    }


    private static final Pattern
            userPattern = Pattern.compile("({at_sign}\\\\@)|({caret_sign}\\\\\\^)|({dollar_sign}\\\\\\$)|({tilde_sign}\\\\~)|" +
            "({$$$}\\$\\$\\$)|({$$}\\$\\$)|({$}\\$)|({sss}@sss\\b)|({ss}@ss\\b)|({s}@s\\b)|({usi}@usi\\b)|({fves}@fves\\b)|" +
            "({name_s}@name_s\\b)|({Name_s}@Name_s\\b)|({name}@name\\b)|({Name}@Name\\b)|({i}@i\\b)|({I}@I\\b)|({me}@me\\b)|({Me}@Me\\b)|" +
            "({myself}@myself\\b)|({Myself}@Myself\\b)|({my}@my\\b)|({My}@My\\b)|({mine}@mine\\b)|({Mine}@Mine\\b)|({direct}@direct\\b)|" +
            "({Direct}@Direct\\b)|(?:@({Other}\\p{Lu}\\w*)\\b)|(?:@({other}\\p{Ll}\\w*)\\b)|({=name}@)"),
            targetPattern = Pattern.compile("({at_sign}\\\\@)|({caret_sign}\\\\\\^)|({dollar_sign}\\\\\\$)|({tilde_sign}\\\\~)|" +
                    "({$$$}\\^\\$\\$\\$)|({$$}\\^\\$\\$)|({$}\\^\\$)|({sss}\\^sss\\b)|({ss}\\^ss\\b)|({s}\\^s\\b)|({usi}\\^usi\\b)|({fves}\\^fves\\b)|" +
                    "({name_s}\\^name_s\\b)|({Name_s}\\^Name_s\\b)|({name}\\^name\\b)|({Name}\\^Name\\b)|({i}\\^i\\b)|({I}\\^I\\b)|({me}\\^me\\b)|({Me}\\^Me\\b)|" +
                    "({myself}\\^myself\\b)|({Myself}\\^Myself\\b)|({my}\\^my\\b)|({My}\\^My\\b)|({mine}\\^mine\\b)|({Mine}\\^Mine\\b)|({direct}^direct\\b)|" +
                    "({Direct}^Direct\\b)|(?:\\^({Other}\\p{Lu}\\w*)\\b)|(?:\\^({other}\\p{Ll}\\w*)\\b)|({=name}\\^)");

    private static final ObjectObjectMap<String, String[]> irregular = new ObjectObjectMap<>(64);

    /**
     * Adds a given {@code word}, which should start with a lower-case letter and use lower-case letters and underscores
     * only, to the dictionary this stores. The 6 additional arguments are used for first person singular ("I am"),
     * first person plural ("we are"), second person singular ("you are"), second person plural ("you are", the same
     * as the last one usually, but not always), third person singular ("he is"), third person plural ("they are").
     * @param word the word to learn; must start with a letter and use only lower-case letters and underscores
     * @param firstPersonSingular the conjugated form of the word for first-person singular ("I do", "I am")
     * @param firstPersonPlural the conjugated form of the word for first-person plural ("we do", "we are")
     * @param secondPersonSingular the conjugated form of the word for second-person singular ("you do", "you are")
     * @param secondPersonPlural the conjugated form of the word for second-person plural ("you do", "you are")
     * @param thirdPersonSingular the conjugated form of the word for third-person singular ("he does", "he is")
     * @param thirdPersonPlural the conjugated form of the word for third-person plural and unspecified-gender singular ("they do", "they are")
     */
    public static void learnIrregularWord(String word, String firstPersonSingular, String firstPersonPlural,
                                          String secondPersonSingular, String secondPersonPlural,
                                          String thirdPersonSingular, String thirdPersonPlural)
    {
        irregular.put(word, new String[]{firstPersonSingular, firstPersonPlural, secondPersonSingular, secondPersonPlural,
                thirdPersonSingular, thirdPersonSingular, thirdPersonSingular, thirdPersonPlural, thirdPersonSingular, thirdPersonSingular,
                thirdPersonPlural});
    }

    static {
        learnIrregularWord("m", "'m", "'re", "'re", "'re", "'s", "'re");
        learnIrregularWord("am", "am", "are", "are", "are", "is", "are");
        learnIrregularWord("ve", "'ve", "'ve", "'ve", "'ve", "'s", "'ve");
        learnIrregularWord("have", "have", "have", "have", "have", "has", "have");
        learnIrregularWord("haven_t", "haven't", "haven't", "haven't", "haven't", "hasn't", "haven't");
        learnIrregularWord("do", "do", "do", "do", "do", "does", "do");
        learnIrregularWord("don_t", "don't", "don't", "don't", "don't", "doesn't", "don't");
        learnIrregularWord("this", "this", "these", "this", "these", "this", "these");
    }

    /**
     * Undocumented; use at your own peril.
     * (It's a RegExodus Substitution that handles a really complicated regex and its replacements.)
     */
    protected static class BeingSubstitution implements Substitution {

        public String term;
        public Pronoun pronoun;
        public boolean finisher;
        public BeingSubstitution()
        {
            term = "Joe";
            pronoun = Pronoun.MALE_GENDER;
            finisher = true;
        }

        public BeingSubstitution(String term, Pronoun pronoun, boolean finish)
        {
            this.term = (term == null) ? "Nullberoth of the North" : term;
            this.pronoun = (pronoun == null) ? Pronoun.UNSPECIFIED_GENDER : pronoun;
            finisher = finish;
        }
        public BeingSubstitution(Pronoun firstPronoun, boolean finish, String... terms) {
            int len;
            if (terms == null || (len = terms.length) <= 0) {
                term = "Nihilatia of Voidetica";
                pronoun = (firstPronoun == null) ? Pronoun.UNSPECIFIED_GENDER : firstPronoun;
                finisher = finish;
            } else if (len == 1) {
                term = (terms[0] == null) ? "Nullberoth of the North" : terms[0];
                pronoun = Pronoun.UNSPECIFIED_GENDER;
                finisher = finish;
            } else if (len == 2) {
                term = terms[0] + " and " + terms[1];
                if (firstPronoun == null)
                    pronoun = Pronoun.GROUP;
                else {
                    switch (firstPronoun) {
                        case FIRST_PERSON_PLURAL:
                        case FIRST_PERSON_SINGULAR:
                            pronoun = Pronoun.FIRST_PERSON_PLURAL;
                            break;
                        case SECOND_PERSON_PLURAL:
                        case SECOND_PERSON_SINGULAR:
                            pronoun = Pronoun.SECOND_PERSON_PLURAL;
                            break;
                        default:
                            pronoun = Pronoun.GROUP;
                    }
                }
                finisher = finish;
            } else {
                StringBuilder sb = new StringBuilder().append(terms[0]).append(", ");
                for (int i = 1; i < len - 1; i++) {
                    sb.append(terms[i]).append(", ");
                }
                term = sb.append("and ").append(terms[len - 1]).toString();
                if (firstPronoun == null)
                    pronoun = Pronoun.GROUP;
                else {
                    switch (firstPronoun) {
                        case FIRST_PERSON_PLURAL:
                        case FIRST_PERSON_SINGULAR:
                            pronoun = Pronoun.FIRST_PERSON_PLURAL;
                            break;
                        case SECOND_PERSON_PLURAL:
                        case SECOND_PERSON_SINGULAR:
                            pronoun = Pronoun.SECOND_PERSON_PLURAL;
                            break;
                        default:
                            pronoun = Pronoun.GROUP;
                    }
                }
                finisher = finish;

            }
        }
        public static void appendCapitalized(String s, TextBuffer dest)
        {
            dest.append(Character.toUpperCase(s.charAt(0)));
            if(s.length() > 1)
                dest.append(s.substring(1));
        }
        @Override
        public void appendSubstitution(MatchResult match, TextBuffer dest) {
            if(match.isCaptured("at_sign"))
            {
                dest.append(finisher ? "@" : "\\@");
            }
            else if(match.isCaptured("caret_sign"))
            {
                dest.append(finisher ? "^" : "\\^");
            }
            else if(match.isCaptured("dollar_sign"))
            {
                dest.append(finisher ? "$" : "\\$");
            }
            else if(match.isCaptured("tilde_sign"))
            {
                dest.append(finisher ? "~" : "\\~");
            }
            else if(match.isCaptured("name"))
            {
                dest.append(pronoun.nameText(term));
            }
            else if(match.isCaptured("Name"))
            {
                appendCapitalized(pronoun.nameText(term), dest);
            }
            else if(match.isCaptured("name_s"))
            {
                dest.append(pronoun.name_sText(term));
            }
            else if(match.isCaptured("Name_s"))
            {
                appendCapitalized(pronoun.name_sText(term), dest);
            }
            else if(match.isCaptured("i"))
            {
                dest.append(pronoun.iText());
            }
            else if(match.isCaptured("I"))
            {
                appendCapitalized(pronoun.iText(), dest);
            }
            else if(match.isCaptured("me"))
            {
                dest.append(pronoun.meText());
            }
            else if(match.isCaptured("Me"))
            {
                appendCapitalized(pronoun.meText(), dest);
            }
            else if(match.isCaptured("my"))
            {
                dest.append(pronoun.myText());
            }
            else if(match.isCaptured("My"))
            {
                appendCapitalized(pronoun.myText(), dest);
            }
            else if(match.isCaptured("mine"))
            {
                dest.append(pronoun.mineText());
            }
            else if(match.isCaptured("Mine"))
            {
                appendCapitalized(pronoun.mineText(), dest);
            }
            else if(match.isCaptured("myself"))
            {
                dest.append(pronoun.myselfText());
            }
            else if(match.isCaptured("Myself"))
            {
                appendCapitalized(pronoun.myselfText(), dest);
            }
            else if(match.isCaptured("s"))
            {
                dest.append(pronoun.sText());
            }
            else if(match.isCaptured("ss"))
            {
                dest.append(pronoun.ssText());
            }
            else if(match.isCaptured("sss"))
            {
                dest.append(pronoun.sssText());
            }
            else if(match.isCaptured("usi"))
            {
                dest.append(pronoun.usiText());
            }
            else if(match.isCaptured("fves"))
            {
                dest.append(pronoun.fvesText());
            }
            else if(match.isCaptured("$"))
            {
                dest.append(pronoun.$Text());
            }
            else if(match.isCaptured("$$"))
            {
                dest.append(pronoun.$$Text());
            }
            else if(match.isCaptured("$$$"))
            {
                dest.append(pronoun.$$$Text());
            }
            else if(match.isCaptured("other"))
            {
                String[] others = irregular.get(match.group("other"));
                if(others != null && others.length == 11)
                    dest.append(others[pronoun.ordinal()]);
                else
                    match.getGroup(0, dest);
            }
            else if(match.isCaptured("Other"))
            {
                String[] others = irregular.get(match.group("Other").toLowerCase());
                if(others != null && others.length == 11)
                    appendCapitalized(others[pronoun.ordinal()], dest);
                else
                    match.getGroup(0, dest);
            }
            else if(match.isCaptured("direct"))
            {
                dest.append(pronoun.directText(term));
            }
            else if(match.isCaptured("Direct"))
            {
                appendCapitalized(pronoun.directText(term), dest);
            }
            else
                match.getGroup(0, dest);
        }
    }
}
