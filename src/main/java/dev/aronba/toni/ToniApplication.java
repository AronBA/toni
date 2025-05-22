package dev.aronba.toni;

import dev.aronba.toni.context.core.ApplicationContext;
import dev.aronba.toni.context.core.ContextScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ToniApplication {
  private static final Logger logger = LoggerFactory.getLogger(ToniApplication.class);
  private static final String BANNER =
      """
                \s
                ___     ______                               __    \s
               /  /    /\\__  _\\                __           /\\ `\\  \s
              /  /     \\/_/\\ \\/   ___     ___ /\\_\\          \\ `\\ `\\\s
            /<  <         \\ \\ \\  / __`\\ /' _ `\\/\\ \\          `\\ >  >
            \\ `\\ `\\        \\ \\ \\/\\ \\L\\ \\/\\ \\/\\ \\ \\ \\           /  /\s
             `\\ `\\_|        \\ \\_\\ \\____/\\ \\_\\ \\_\\ \\_\\         /\\_/ \s
               `\\//   _______\\/_/\\/___/  \\/_/\\/_/\\/_/  _______\\//  \s
                     /\\______\\                        /\\______\\    \s
                     \\/______/                        \\/______/    \s
            """;
  private final ContextScanner contextScanner;

  public ToniApplication(final ContextScanner contextScanner) {
    this.contextScanner = contextScanner;
  }

  public static void run(final String[] args) throws Exception {
    logger.info(BANNER);
    logger.info("Starting ToniApplication");
    final ContextScanner contextScanner = new ContextScanner();
    final ToniApplication toniApplication = new ToniApplication(contextScanner);
    toniApplication.start();
  }

  private void start() throws Exception {
    logger.info("Scanning Classpath");
    final ApplicationContext applicationContext = this.contextScanner.scan();
  }
}
