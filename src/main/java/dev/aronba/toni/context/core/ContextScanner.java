package dev.aronba.toni.context.core;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContextScanner {
  private static final Logger logger = LoggerFactory.getLogger(ContextScanner.class);

  public ApplicationContext scan() throws Exception {
    try (ScanResult scanResult = new ClassGraph().enableAllInfo().acceptPackages("").scan()) {
      ClassInfoList componentList =
          scanResult.getClassesWithAnnotation("dev.aronba.toni.context.annotation.Component");
      ApplicationContext applicationContext = new BasicApplicationContext();
      applicationContext.register(componentList.loadClasses().toArray(new Class<?>[0]));
      return applicationContext;
    } catch (Exception e) {
      logger.error(e.getMessage());
      throw e;
    }
  }
}
