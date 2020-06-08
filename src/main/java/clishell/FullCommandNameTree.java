/**
 *
 *
 * Command Line Interface Harness
 *
 *
 *
 *
 * @author MRoss
 *
 */

package clishell;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;


/**
 *
 * Class to manages multi-word "command names" as tree of words.
 * Examples of "command names" include:
 *
 * <ul>
 *   <li><code>help</code></li>
 *   <li><code>set color</code></li>
 *   <li><code>set style</code></li>
 *   <li><code>load static file from</code></li>
 * </ul>
 *
 */
public class FullCommandNameTree {


    //
    // Private class data
    //

    /** */
    private static final long serialVersionUID = 320949328L;


    //
    // Private instance data
    //

    /** */
    private final Map<String, FullCommandNameTree> mSubcommandTreeMap
        = new LinkedHashMap<String, FullCommandNameTree>();


    //
    //  Public instance methods
    //

    /**
     * Prints the command name tree to stdout
     */
    public void printAsTree() {
        printAsTree(new PrintWriter(System.out));
    }

    /**
     * @param out destination of tree output
     */
    public void printAsTree(PrintWriter out) {
        printAsTree(0, out);
    }

    /**
     * @param commandName
     */
    public void addCommandName(CommandName commandName) {

        FullCommandNameTree currentNode = this;
        FullCommandNameTree nextNode = null;
        for (String commandNameWord : commandName.getCommandNameAsWordArray()) {
            nextNode = currentNode.mSubcommandTreeMap.get(commandNameWord);
            if (nextNode == null) {
                nextNode = new FullCommandNameTree();
                currentNode.mSubcommandTreeMap.put(commandNameWord, nextNode);
            }
            currentNode = nextNode;
        }

    }

    /**
     * @param commandName
     */
    public void removeCommandName(CommandName commandName) {

        String[] commandNameAsWordArray = commandName.getCommandNameAsWordArray();
        for (int i = commandNameAsWordArray.length - 1; i >= 0; i--) {
            FullCommandNameTree currentNode = this;
            FullCommandNameTree nextNode = null;
            // navigate down to one before the leaf
            for (int j = 0; j < i; j++) {
                nextNode = currentNode.mSubcommandTreeMap.get(commandNameAsWordArray[j]);
                if (nextNode == null) {
                    return;
                }
                currentNode = nextNode;
            }
            // if the leaf has no siblings, then remove it
            if (currentNode.mSubcommandTreeMap.get(commandNameAsWordArray[i])
                    .mSubcommandTreeMap.size() == 0) {
                currentNode.mSubcommandTreeMap.remove(commandNameAsWordArray[i]);
            }
        }

    }

    /**
     * @return the complete set of command names loaded into the command tree
     */
    public Set<CommandName> getComandNames() {
        Set<CommandName> commandNames = new LinkedHashSet<CommandName>();
        collectCommandNames(new String[] {}, commandNames);
        return commandNames;
    }

    /**
     * @param commandWithParameters full command line within which the
     * "command name" part is being searched
     * @return set of known command names that were found as initial part
     * of <code>commandWithParameters</code> (looks for unique command name
     * in initial tokens of <code>commandWithParameters</code>)
     */
    public Set<CommandName> findCommandNamesFromCommandLine(
        CommandName commandWithParameters) {

        // split input into string tokens
        String[] commandNameAsWordArray = commandWithParameters
            .getCommandNameAsWordArray();

        // construct initially empty set of command names to return to user
        Set<CommandName> commandNameSet = new LinkedHashSet<CommandName>();

        // loop across tokens of input string, from left to right
        for (int i = 0; i < commandNameAsWordArray.length; i++) {

            // retrieve the set of matching known commands
            Set<CommandName> tryCommandNameSet = findMatchingCommandNames(
                new CommandName(Arrays.copyOf(commandNameAsWordArray, i + 1)));

            // if nothing matched from this, break
            if (tryCommandNameSet.size() == 0) {
                break;
            }

            // else we found something, set this as the best set of commands
            commandNameSet = tryCommandNameSet;

            // if we found a unique command; good enough - break
            if (tryCommandNameSet.size() == 1) {
                break;
            }
        }

        // return what we have
        return commandNameSet;
    }

    /**
     * @param abbreviatedCommandName command name, possibly using abbreviated words
     * @return set of known command names matching <code>abbreviatedCommandName</code>
     * (i.e., will have an equal number of words)
     */
    public Set<CommandName> findMatchingCommandNames(CommandName abbreviatedCommandName) {
        Set<CommandName> matchingFullCommandNames = new LinkedHashSet<CommandName>();
        collectFullCommandWordsFromAbbreviated(new String[] {}, matchingFullCommandNames, 0,
            abbreviatedCommandName.getCommandNameAsWordArray());
        return matchingFullCommandNames;
    }


    //
    //  Private instance methods
    //

    /**
     * @param indentLevel beginning "indentation level" for output produced
     * @param out destination of output produced
     */
    private void printAsTree(int indentLevel, PrintWriter out) {

        int newIndentLevel = indentLevel + 1;;
        for (String commandWord : mSubcommandTreeMap.keySet()) {
            out.println(getIndentation(indentLevel) + commandWord);
            FullCommandNameTree next = mSubcommandTreeMap.get(commandWord);
            if (next == null) {
                break;
            }
            next.printAsTree(newIndentLevel, out);
        }
    }

    /**
     * @param indentLevel desired indentation level
     * @return leading string which can be used as a prefix on subs
     */
    private String getIndentation(int indentLevel) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < indentLevel; i++) {
            sb.append("--");
        }
        return sb.toString();
    }

    /**
     * @param nodeCommandWords array of command words in current tree traversal path
     * @param matchingFullCommandNames (user output) collection of matching command names
     * @param inputIndex index of word within <code>abbreviatedCommandNameAsWordArray</code>
     * for whom match is currently sought with one of the subcommands of this tree instance
     * @param abbreviatedCommandNameAsWordArray array of (possibly partial) command name
     * words comprising "search filter" - for whom the set of matching command names is
     * being requested
     */
    private void collectFullCommandWordsFromAbbreviated(
        String[] nodeCommandWords,
        Collection<CommandName> matchingFullCommandNames,
        int inputIndex, String[] abbreviatedCommandNameAsWordArray) {

        // an exact match - return it
        // NOTE: input may be longer than command
        if (mSubcommandTreeMap.size() == 0) {
            if (nodeCommandWords.length > 0) {
                // only add if there is a command name
                //(will be length 0 for initial "primer" value)
                matchingFullCommandNames.add(new CommandName(nodeCommandWords));
            }
            return;
        }

        // the following block includes matches from "partial" inputs
        // i.e., those shorter than the actual command
        // collect all remaining entries from the tree downwards of this node
        if (inputIndex >= abbreviatedCommandNameAsWordArray.length) {
            // add this word, since it's a potential match
            matchingFullCommandNames.add(new CommandName(nodeCommandWords));
            // and add all other commands that share this same "prefix"
            // (set of initial command tokens)
            collectCommandNames(nodeCommandWords, matchingFullCommandNames);
            return;
        }

        // get another input word to match
        int nextInputIndex = inputIndex + 1;
        String matchInputWord = abbreviatedCommandNameAsWordArray[inputIndex];

        // check to see if we have a perfect match with any child entries
        if (mSubcommandTreeMap.get(matchInputWord) != null) {
            // if so, collect it and keep going for more
            collectWord(matchInputWord, nodeCommandWords,
                matchingFullCommandNames, nextInputIndex,
                abbreviatedCommandNameAsWordArray);
            return;
        }


        // look across all of the child entries,
        // collecting any that match (recursively)
        for (String commandWord : mSubcommandTreeMap.keySet()) {
            if (!commandWord.startsWith(matchInputWord)) {
                continue;
            }
            collectWord(commandWord, nodeCommandWords,
                matchingFullCommandNames, nextInputIndex,
                abbreviatedCommandNameAsWordArray);
        }

    }

    /**
     * Method to collect (append, actaully) a new word into the current full command
     * name being generated as part of generating the set of command names matching
     * the possibly abbreviated command, <code>commandWord</code>.
     * @param nextWord next full command word to collect to words already collected
     * @param nodeCommandWords string array form of words already collected
     * @param matchingFullCommandNames set of matching command names thus far collected
     * @param nextInputIndex index of next word to collect into
     * <code>nodeCommandWords</code>
     * @param abbreviatedCommandNameAsWordArray string array of input (possibly
     * abbreviated) command word(s)
     */
    private void collectWord(String nextWord, String[] nodeCommandWords,
        Collection<CommandName> matchingFullCommandNames, int nextInputIndex,
        String[] abbreviatedCommandNameAsWordArray) {
        String[] extendedCommandWords = Arrays.copyOf(nodeCommandWords,
                nodeCommandWords.length + 1);
        extendedCommandWords[nodeCommandWords.length] = nextWord;
        mSubcommandTreeMap.get(nextWord).collectFullCommandWordsFromAbbreviated(
            extendedCommandWords, matchingFullCommandNames, nextInputIndex,
            abbreviatedCommandNameAsWordArray);
    }

    /**
     * @param nodeCommandWords the array of command words that has been
     * generated so far via traversing the command tree
     * @param commandNames the output collection of command names
     */
    private void collectCommandNames(String[] nodeCommandWords,
        Collection<CommandName> commandNames) {

        if (mSubcommandTreeMap.size() == 0) {
            commandNames.add(new CommandName(nodeCommandWords));
            return;
        }

        for (String commandWord : mSubcommandTreeMap.keySet()) {
            String[] extendedCommandWords = Arrays.copyOf(nodeCommandWords,
                nodeCommandWords.length + 1);
            extendedCommandWords[nodeCommandWords.length] = commandWord;
            mSubcommandTreeMap.get(commandWord).collectCommandNames(
                extendedCommandWords, commandNames);
        }

    }

}
