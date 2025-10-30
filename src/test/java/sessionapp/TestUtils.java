package sessionapp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

public class TestUtils {
  public static void deleteRecursively(Path dir) throws IOException {
    if (!Files.exists(dir)) return;
    Files.walk(dir)
        .sorted(Comparator.reverseOrder())
        .forEach(
            path -> {
              try {
                Files.deleteIfExists(path);
              } catch (IOException e) {
                throw new RuntimeException(e);
              }
            });
  }
}
