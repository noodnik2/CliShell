package clishell.ideas;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class FullCommandManager {

    private FullCommandNameNode mFullCommandNameNode = new FullCommandNameNode();
    private int mIndentLevel;
    
    /**
     * @param partialCommandWords
     * @return
     */
    public Set<String[]> getFullCommandsFromPartial(String[] partialCommandWords) {

        Set<String[]> fullCommandWordsSet = mFullCommandNameNode
            .getFullCommandWordsFromPartial(partialCommandWords);

        for (String[] commandWords : fullCommandWordsSet) {
            printFullCommandStringFromWords("result: ", commandWords);
        }

        return fullCommandWordsSet;
    }

    public void printAsTree() {
        mFullCommandNameNode.printAsTree();
    }
    
    private void printFullCommandStringFromWords(String prefix, String[] fullCommandWords) {
        if (prefix != null) {
            System.out.print(prefix);
        }
        boolean isFirst = true;
        for (String fullCommandWord : fullCommandWords) {
            if (!isFirst) {
                System.out.print(",");
            } else {
                isFirst = false;
            }
            System.out.print(fullCommandWord);
        }
        System.out.println();
    }
    
}
