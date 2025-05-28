package dev.aronba.toni.context;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import dev.aronba.toni.context.core.ApplicationContext;
import dev.aronba.toni.context.core.BasicApplicationContext;
import dev.aronba.toni.context.testClasses.perfomance.*;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class ApplicationContextPerformanceTest {

  static Stream<ApplicationContext> provideImplementations() {
    return Stream.of(new BasicApplicationContext());
  }

  @ParameterizedTest
  @MethodSource("provideImplementations")
  void shouldTimeThePerformance(ApplicationContext applicationContext) {
    Class<?>[] components = {
      A.class, B.class, C.class, D.class, E.class, F.class, G.class, Q.class, R.class
    };

    long startTime = System.nanoTime();
    try {
      applicationContext.registerComponents(components);
    } catch (Exception e) {
      throw new RuntimeException("Failed to register components", e);
    }
    long endTime = System.nanoTime();

    long elapsedTime = (endTime - startTime) / 1_000_000;

    System.out.println("Time taken to build context: " + elapsedTime + " ms");

    G lastComponent = applicationContext.getComponent(G.class);
    assertNotNull(lastComponent);
  }
}
