/*
 * Copyright (c) 2022 See AUTHORS file.
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

/**
 * Properties of nouns needed to correctly conjugate those nouns and refer to them with pronouns, such as genders.
 * Includes parts of speech, which only are concerned with whether they refer to a singular noun or a plural noun,
 * and genders for when a gendered pronoun is needed. This provides substantial support for uncommon cases regarding
 * gender and pronoun preferences. That said, gender and pronoun preference can be incredibly hard to handle.
 * The simplest cases are for first- and second-person pronouns; here we have "I/me/my/myself" for
 * {@link #FIRST_PERSON_SINGULAR}, "you/you/your/yourself" for {@link #SECOND_PERSON_SINGULAR},
 * "we/us/our/ourselves" for {@link #FIRST_PERSON_PLURAL}, and "you/you/your/yourselves" for
 * {@link #SECOND_PERSON_PLURAL}; there are more pronouns this can produce, but they aren't listed here.
 * Third-person pronouns are considerably more challenging because English sporadically considers gender as part of
 * conjugation, but doesn't provide a universally-acceptable set of gendered pronouns.
 * <br>
 * This at least tries to provide pronoun handling for the common cases, such as "you" not needing a gendered
 * pronoun at all (it uses {@link #SECOND_PERSON_SINGULAR}), and supports {@link #MALE_GENDER male},
 * {@link #FEMALE_GENDER female}, {@link #NO_GENDER genderless} (using "it" and related forms; preferred especially
 * for things that aren't alive, and in most cases not recommended for people),
 * {@link #UNSPECIFIED_GENDER "unspecified"} (using "they" in place of "he" or "she"; preferred in some cases when
 * describing someone with a non-specific gender or an unknown gender) pronouns, and {@link #GROUP group} for when a
 * group of individuals, regardless of gender or genders, is referred to with a single pronoun. As mentioned, this
 * has support for some uncommon situations, like {@link #ADDITIONAL_GENDER additional gender} (as in, a gender that
 * is in addition to male and female but that is not genderless, which has a clear use case when describing
 * non-human species, and a more delicate use for humans who use non-binary gender pronouns; hopefully "xe" will be
 * acceptable), and finally a {@link #SPECIAL_CASE_GENDER "special case"} pronoun that is unpronounceable and, if
 * given special processing, can be used as a replacement target for customized pronouns. For the additional gender,
 * the non-binary gendered pronouns are modified from the male pronouns by replacing 'h' with 'x' (he becomes xe,
 * his becomes xis). The "special case" pronouns replace the 'h' in the male pronouns with 'qvq', except for in one
 * case. Where, if the female pronoun were used, it would be "hers", but the male pronoun in that case would be "his",
 * changing the male pronoun would lead to a difficult-to-replace case because "his" is also used in the case where
 * the female pronoun is the usefully distinct "her". Here, the "special case" gender diverges from what it usually
 * does, and uses "qvqims" in place of "his" or "hers". The "special case" pronouns should be replaced before being
 * displayed, since they look like gibberish or a glitch and so are probably confusing out of context.
 * <br>
 * The documentation for each constant has a section at the end for the terms that are most-commonly changed by the
 * Pronoun, like "name" for "@name" and "myself" for "@myself", with the section below that containing the terms
 * those change to with that Pronoun. For instance, with SECOND_PERSON_SINGULAR, "@mine" changes to "yours".
 */
public enum Pronoun {
    /**
     * As in, "I am my own boss." Doesn't reference gender.
     * <br>
     * name/name_s/i/me/my/mine/myself
     * <br>
     * I/my/I/me/my/mine/myself
     * "^i hurr^$$$" becomes "I hurry".
     * Does not pluralize.
     */
    FIRST_PERSON_SINGULAR,
    /**
     * As in, "You are your own boss." Doesn't reference gender.
     * <br>
     * name/name_s/i/me/my/mine/myself
     * <br>
     * you/your/you/you/your/yours/yourself
     * "^i hurr^$$$" becomes "you hurry".
     * Does not pluralize.
     */
    SECOND_PERSON_SINGULAR,
    /**
     * As in, "We are our own bosses." Doesn't reference gender, and applies to groups.
     * <br>
     * name/name_s/i/me/my/mine/myself
     * <br>
     * we/our/we/us/our/ours/ourselves
     * "^i hurr^$$$" becomes "we hurry".
     * Pluralizes.
     */
    FIRST_PERSON_PLURAL,
    /**
     * As in, "You are your own bosses." Doesn't reference gender, and applies to groups.
     * <br>
     * name/name_s/i/me/my/mine/myself
     * <br>
     * you/your/you/you/your/yours/yourselves
     * "^i hurr^$$$" becomes "you hurry".
     * Pluralizes.
     */
    SECOND_PERSON_PLURAL,
    /**
     * Inanimate objects or beings without gender, as in "It is its own boss."
     * <br>
     * name/name_s/i/me/my/mine/myself
     * <br>
     * Name/Name's/it/it/its/its/itself
     * "^i hurr^$$$" becomes "it hurries".
     * Does not pluralize.
     */
    NO_GENDER,
    /**
     * Male pronoun preference, as in "He is his own boss."
     * <br>
     * name/name_s/i/me/my/mine/myself
     * <br>
     * Name/Name's/he/he/his/his/himself
     * "^i hurr^$$$" becomes "he hurries".
     * Does not pluralize.
     */
    MALE_GENDER,
    /**
     * Female pronoun preference, as in "She is her own boss."
     * <br>
     * name/name_s/i/me/my/mine/myself
     * <br>
     * Name/Name's/she/she/her/hers/herself
     * "^i hurr^$$$" becomes "she hurries".
     * Does not pluralize.
     */
    FEMALE_GENDER,
    /**
     * "Singular they" pronoun preference or to be used when preference is unknown, as in "They are their own boss."
     * <br>
     * name/name_s/i/me/my/mine/myself
     * <br>
     * Name/Name's/they/them/their/theirs/themself
     * "^i hurr^$$$" becomes "they hurry".
     * Does not pluralize.
     */
    UNSPECIFIED_GENDER,
    /**
     * Third-gender pronoun preference, potentially relevant for cultures with non-binary gender terms. As in, "Xe
     * is xis own boss."
     * <br>
     * name/name_s/i/me/my/mine/myself
     * <br>
     * Name/Name's/xe/xim/xis/xis/ximself
     * "^i hurr^$$$" becomes "xe hurries".
     * Does not pluralize.
     */
    ADDITIONAL_GENDER,
    /**
     * Unpronounceable words that can be processed specially for more complex cases of pronoun preference. As in,
     * "Qvqe is qvqis own boss."
     * <br>
     * name/name_s/i/me/my/mine/myself
     * <br>
     * Name/Name's/qvqe/qvqim/qvqis/qvqims/qvqimself
     * "^i hurr^$$$" becomes "qvqe hurries".
     * Does not pluralize.
     */
    SPECIAL_CASE_GENDER,
    /**
     * Any third-person plural, as in "They are their own bosses." Not to be confused with UNSPECIFIED_GENDER, which
     * is for singular beings, but usually uses "they" in the same way (not always).
     * <br>
     * name/name_s/i/me/my/mine/myself
     * <br>
     * Name/Name's/they/them/their/theirs/themselves
     * "^i hurr^$$$" becomes "they hurry".
     * Pluralizes.
     */
    GROUP;

    public String nameText(String term) {
        switch (this) {
            case FIRST_PERSON_SINGULAR:
                return "I";
            case FIRST_PERSON_PLURAL:
                return "we";
            case SECOND_PERSON_SINGULAR:
            case SECOND_PERSON_PLURAL:
                return "you";
            default:
                return term;
        }
    }

    public String name_sText(String term) {
        switch (this) {
            case FIRST_PERSON_SINGULAR:
                return "my";
            case FIRST_PERSON_PLURAL:
                return "our";
            case SECOND_PERSON_SINGULAR:
            case SECOND_PERSON_PLURAL:
                return "your";
            default:
                if (term.isEmpty()) return "";
                else if (term.endsWith("s")) return term + '\'';
                else return term + "'s";
        }
    }

    public String iText() {
        switch (this) {
            case FIRST_PERSON_SINGULAR:
                return "I";
            case FIRST_PERSON_PLURAL:
                return "we";
            case SECOND_PERSON_SINGULAR:
            case SECOND_PERSON_PLURAL:
                return "you";
            case NO_GENDER:
                return "it";
            case MALE_GENDER:
                return "he";
            case FEMALE_GENDER:
                return "she";
            //case UNSPECIFIED_GENDER: return "they";
            case ADDITIONAL_GENDER:
                return "xe";
            case SPECIAL_CASE_GENDER:
                return "qvqe";
            default:
                return "they";
        }
    }

    public String meText() {
        switch (this) {
            case FIRST_PERSON_SINGULAR:
                return "me";
            case FIRST_PERSON_PLURAL:
                return "us";
            case SECOND_PERSON_SINGULAR:
            case SECOND_PERSON_PLURAL:
                return "you";
            case NO_GENDER:
                return "it";
            case MALE_GENDER:
                return "him";
            case FEMALE_GENDER:
                return "her";
            //case UNSPECIFIED_GENDER: return "them";
            case ADDITIONAL_GENDER:
                return "xim";
            case SPECIAL_CASE_GENDER:
                return "qvqim";
            default:
                return "them";
        }
    }

    public String myText() {
        switch (this) {
            case FIRST_PERSON_SINGULAR:
                return "my";
            case FIRST_PERSON_PLURAL:
                return "our";
            case SECOND_PERSON_SINGULAR:
            case SECOND_PERSON_PLURAL:
                return "your";
            case NO_GENDER:
                return "its";
            case MALE_GENDER:
                return "his";
            case FEMALE_GENDER:
                return "her";
            //case UNSPECIFIED_GENDER: return "their";
            case ADDITIONAL_GENDER:
                return "xis";
            case SPECIAL_CASE_GENDER:
                return "qvqis";
            default:
                return "their";
        }
    }

    public String mineText() {
        switch (this) {
            case FIRST_PERSON_SINGULAR:
                return "mine";
            case FIRST_PERSON_PLURAL:
                return "ours";
            case SECOND_PERSON_SINGULAR:
            case SECOND_PERSON_PLURAL:
                return "yours";
            case NO_GENDER:
                return "its";
            case MALE_GENDER:
                return "his";
            case FEMALE_GENDER:
                return "hers";
            //case UNSPECIFIED_GENDER: return "theirs";
            case ADDITIONAL_GENDER:
                return "xis";
            case SPECIAL_CASE_GENDER:
                return "qvqims";
            default:
                return "theirs";
        }
    }

    public String myselfText() {
        switch (this) {
            case FIRST_PERSON_SINGULAR:
                return "myself";
            case FIRST_PERSON_PLURAL:
                return "ourselves";
            case SECOND_PERSON_SINGULAR:
                return "yourself";
            case SECOND_PERSON_PLURAL:
                return "yourselves";
            case NO_GENDER:
                return "itself";
            case MALE_GENDER:
                return "himself";
            case FEMALE_GENDER:
                return "herself";
            case UNSPECIFIED_GENDER:
                return "themself";
            case ADDITIONAL_GENDER:
                return "ximself";
            case SPECIAL_CASE_GENDER:
                return "qvqimself";
            default:
                return "themselves";
        }
    }

    public String sText() {
        switch (this) {
            case FIRST_PERSON_PLURAL:
            case SECOND_PERSON_PLURAL:
            case GROUP:
                return "s";
            default:
                return "";
        }
    }

    public String ssText() {
        switch (this) {
            case FIRST_PERSON_PLURAL:
            case SECOND_PERSON_PLURAL:
            case GROUP:
                return "es";
            default:
                return "";
        }
    }

    public String sssText() {
        switch (this) {
            case FIRST_PERSON_PLURAL:
            case SECOND_PERSON_PLURAL:
            case GROUP:
                return "ies";
            default:
                return "y";
        }
    }

    public String usiText() {
        switch (this) {
            case FIRST_PERSON_PLURAL:
            case SECOND_PERSON_PLURAL:
            case GROUP:
                return "i";
            default:
                return "us";
        }
    }

    public String fvesText() {
        switch (this) {
            case FIRST_PERSON_PLURAL:
            case SECOND_PERSON_PLURAL:
            case GROUP:
                return "ves";
            default:
                return "f";
        }
    }

    public String $Text() {
        switch (this) {
            case FIRST_PERSON_SINGULAR:
            case FIRST_PERSON_PLURAL:
            case SECOND_PERSON_SINGULAR:
            case SECOND_PERSON_PLURAL:
            case UNSPECIFIED_GENDER:
            case GROUP:
                return "";
            default:
                return "s";
        }
    }

    public String $$Text() {
        switch (this) {
            case FIRST_PERSON_SINGULAR:
            case FIRST_PERSON_PLURAL:
            case SECOND_PERSON_SINGULAR:
            case SECOND_PERSON_PLURAL:
            case UNSPECIFIED_GENDER:
            case GROUP:
                return "";
            default:
                return "es";
        }
    }

    public String $$$Text() {
        switch (this) {
            case FIRST_PERSON_SINGULAR:
            case FIRST_PERSON_PLURAL:
            case SECOND_PERSON_SINGULAR:
            case SECOND_PERSON_PLURAL:
            case UNSPECIFIED_GENDER:
            case GROUP:
                return "y";
            default:
                return "ies";
        }
    }

    public String directText(String term) {
        return term;
    }

}
