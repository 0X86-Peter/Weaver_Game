import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility class to load words from dictionary.txt.
 */
public class DictionaryLoader {
    public static Set<String> load(String filePath) {
        try {
            return Files.lines(Paths.get(filePath))
                    .map(String::trim)
                    .filter(word -> word.length() == 4)
                    .filter(word -> word.matches("[a-zA-Z]+"))
                    .map(String::toLowerCase)
                    .collect(Collectors.toCollection(HashSet::new));
        } catch (IOException e) {
            System.err.println("Error loading dictionary: " + e.getMessage());
            return Collections.emptySet();
        }
    }
}