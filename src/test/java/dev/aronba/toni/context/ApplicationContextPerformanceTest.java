package dev.aronba.toni.context;

import dev.aronba.toni.context.testClasses.perfomance.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ApplicationContextPerformanceTest {

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
            applicationContext.register(components);
        } catch (Exception e) {
            throw new RuntimeException("Failed to register components", e);
        }
        long endTime = System.nanoTime();

        long elapsedTime = (endTime - startTime) / 1_000_000;

        System.out.println("Time taken to build context: " + elapsedTime + " ms");

        G lastComponent = applicationContext.get(G.class);
        assertNotNull(lastComponent);
    }
}