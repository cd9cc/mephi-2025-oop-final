package sessionapp.utis;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class Config {
  private final Path walletsDirectory;
  private final Path usersFilePath;
  private final Path exportsDirectory;

  private Config() {
    throw new RuntimeException();
  }

  private Config(Properties props) {
    var walletsDirectoryStr = props.getProperty("wallets-directory", "wallets");
    this.walletsDirectory = Paths.get(walletsDirectoryStr);

    var usersFilePathStr = props.getProperty("users-file", "users.dat");
    this.usersFilePath = Paths.get(usersFilePathStr);

    var exportsDirectoryStr = props.getProperty("exports-directory", "exports");
    this.exportsDirectory = Paths.get(exportsDirectoryStr);
  }

  public Path getWalletsDirectory() {
    return walletsDirectory;
  }

  public Path getUsersFilePath() {
    return usersFilePath;
  }

  public Path getExportsDirectory() {
    return exportsDirectory;
  }

  public static Config load() throws IOException {
    var props = new Properties();
    try (var fis = new FileInputStream("config.properties")) {
      props.load(fis);
      return new Config(props);
    }
  }
}
