package dev.aronba.toni.context;

import static org.junit.jupiter.api.Assertions.*;

import dev.aronba.toni.context.testClasses.*;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class ApplicationContexTest {

  static Stream<ApplicationContext> provideImplementations() {
    return Stream.of(new BasicApplicationContext());
  }

  @ParameterizedTest
  @MethodSource("provideImplementations")
  void shouldRegisterComponent(ApplicationContext applicationContext) {
    assertDoesNotThrow(() -> applicationContext.register(EmptyComponent.class));
    EmptyComponent emptyComponent = applicationContext.get(EmptyComponent.class);
    assertNotNull(emptyComponent);
  }

  @ParameterizedTest
  @MethodSource("provideImplementations")
  void shouldRegisterMultipleComponents(ApplicationContext applicationContext) {
    assertDoesNotThrow(
        () -> applicationContext.register(EmptyComponent.class, SimpleComponent.class));
    EmptyComponent emptyComponent = applicationContext.get(EmptyComponent.class);
    SimpleComponent simpleComponent = applicationContext.get(SimpleComponent.class);
    assertNotNull(emptyComponent);
    assertNotNull(simpleComponent);
  }

  @ParameterizedTest
  @MethodSource("provideImplementations")
  void shouldRegisterComponentWithOneDependency(ApplicationContext applicationContext) {
    assertDoesNotThrow(
        () -> applicationContext.register(EmptyComponent.class, SimpleComponent.class));
    SimpleComponent simpleComponent = applicationContext.get(SimpleComponent.class);
    assertNotNull(simpleComponent);
    assertNotNull(simpleComponent.getEmptyComponent());
  }

  @ParameterizedTest
  @MethodSource("provideImplementations")
  void shouldRegisterComponentWithMultipleDependencies(ApplicationContext applicationContext) {
    assertDoesNotThrow(
        () ->
            applicationContext.register(
                EmptyComponent.class, SimpleComponent.class, ComplexComponent.class));
    ComplexComponent complexComponent = applicationContext.get(ComplexComponent.class);
    assertNotNull(complexComponent);
    assertNotNull(complexComponent.getEmptyComponent());
    assertNotNull(complexComponent.getSimpleComponent());
    assertNotNull(complexComponent.getSimpleComponent().getEmptyComponent());
  }

  @ParameterizedTest
  @MethodSource("provideImplementations")
  void shouldRegisterComponentsAndInstantiateComponentsInTopologicalOrder(
      ApplicationContext applicationContext) {
    assertDoesNotThrow(
        () ->
            applicationContext.register(
                ComplexComponent.class, EmptyComponent.class, SimpleComponent.class));
    ComplexComponent complexComponent = applicationContext.get(ComplexComponent.class);
    assertNotNull(complexComponent);
  }

  @ParameterizedTest
  @MethodSource("provideImplementations")
  void shouldHandleMultipleConstructors(ApplicationContext applicationContext) {

    assertDoesNotThrow(
        () ->
            applicationContext.register(
                SimpleComponent.class, ComplexComponent.class, EmptyComponent.class));
    ComplexComponent complexComponent = applicationContext.get(ComplexComponent.class);

    assertNotNull(complexComponent.getSimpleComponent());
    assertNotNull(complexComponent);
    assertNotNull(complexComponent.getEmptyComponent());
    assertNull(complexComponent.getComponentDescription());
    assertNull(complexComponent.getComponentName());
  }

  @ParameterizedTest
  @MethodSource("provideImplementations")
  void shouldGetCorrectInstance(ApplicationContext applicationContext) {
    assertDoesNotThrow(() -> applicationContext.register(EmptyComponent.class));
    Object component = applicationContext.get(EmptyComponent.class);
    assertInstanceOf(EmptyComponent.class, component);
  }

  @ParameterizedTest
  @MethodSource("provideImplementations")
  void shouldGetAlwaysTheSameSingleton(ApplicationContext applicationContext) {
    assertDoesNotThrow(
        () ->
            applicationContext.register(
                EmptyComponent.class, EmptyComponent.class, EmptyComponent.class));
    EmptyComponent emptyComponent1 = applicationContext.get(EmptyComponent.class);
    EmptyComponent emptyComponent2 = applicationContext.get(EmptyComponent.class);
    EmptyComponent emptyComponent3 = applicationContext.get(EmptyComponent.class);

    assertNotNull(emptyComponent1);
    assertNotNull(emptyComponent2);
    assertNotNull(emptyComponent3);
    assertEquals(emptyComponent1, emptyComponent2);
    assertEquals(emptyComponent1, emptyComponent3);
    assertEquals(emptyComponent2, emptyComponent3);
  }

  @ParameterizedTest
  @MethodSource("provideImplementations")
  void shouldGetNullIfComponentIsNotTracked(ApplicationContext applicationContext) {
    assertDoesNotThrow(() -> applicationContext.register(EmptyComponent.class));
    ComplexComponent complexComponent = applicationContext.get(ComplexComponent.class);
    assertNull(complexComponent);
  }

  @ParameterizedTest
  @MethodSource("provideImplementations")
  void shouldRunPostProcessors(ApplicationContext applicationContext) {
    assertDoesNotThrow(
        () ->
            applicationContext.register(
                ComplexComponent.class,
                SimpleComponent.class,
                EmptyComponent.class,
                SimplePostProcessor.class));
    ComplexComponent complexComponent = applicationContext.get(ComplexComponent.class);
    assertEquals("Hello from Postprocessor", complexComponent.getComponentName());
  }

  @ParameterizedTest
  @MethodSource("provideImplementations")
  void shouldRunPostConstructProcessor(ApplicationContext applicationContext) {
    assertDoesNotThrow(
        () ->
            applicationContext.register(
                ComplexComponent.class,
                SimpleComponent.class,
                EmptyComponent.class,
                SimplePostProcessor.class,
                PostConstructPostProcessor.class));
    ComplexComponent complexComponent = applicationContext.get(ComplexComponent.class);
    assertEquals("Hello from PostConstruct", complexComponent.getComponentDescription());
  }

  @ParameterizedTest
  @MethodSource("provideImplementations")
  void shouldFindDirectCircularDependency(ApplicationContext applicationContext) {
    assertThrows(
        CircularDependencyException.class,
        () -> applicationContext.register(CircularDependentComponent1.class));
  }

  @ParameterizedTest
  @MethodSource("provideImplementations")
  void shouldFindIndirectCircularDependency(ApplicationContext applicationContext) {
    assertThrows(
        CircularDependencyException.class,
        () ->
            applicationContext.register(
                CircularDependentComponent2.class, CircularDependentComponent3.class));
  }

  @ParameterizedTest
  @MethodSource("provideImplementations")
  void shouldTryToFindImplementationOfInterfaceWhenDependingOnInterface(
      ApplicationContext applicationContext) {
    assertDoesNotThrow(
        () ->
            applicationContext.register(
                ServiceInterface.class,
                ServiceInterfaceImpl.class,
                InterfaceDependentComponent.class));
    InterfaceDependentComponent interfaceDependentComponent =
        applicationContext.get(InterfaceDependentComponent.class);
    interfaceDependentComponent.run();
    assertEquals(10, interfaceDependentComponent.check);
  }

  @ParameterizedTest
  @MethodSource("provideImplementations")
  void shouldThrowIfNoImplementationForDependencyExists(ApplicationContext applicationContext) {
    assertThrows(
        NoImplementationFoundException.class,
        () ->
            applicationContext.register(ServiceInterface.class, InterfaceDependentComponent.class));
  }

  @ParameterizedTest
  @MethodSource("provideImplementations")
  void shouldFindDependenciesOfInterfaceImplementationsCorrectly(
      ApplicationContext applicationContext) {
    assertDoesNotThrow(
        () ->
            applicationContext.register(
                ServiceInterface.class,
                InterfaceDependentComponent.class,
                ServiceInterfaceWithDependenciesImpl.class,
                EmptyComponent.class));
    InterfaceDependentComponent interfaceDependentComponent =
        applicationContext.get(InterfaceDependentComponent.class);
    interfaceDependentComponent.run();
    assertEquals(20, interfaceDependentComponent.check);
  }

  @ParameterizedTest
  @MethodSource("provideImplementations")
  void shouldUseCorrectImplementation(ApplicationContext applicationContext) {
    assertDoesNotThrow(
        () ->
            applicationContext.register(
                ServiceInterface.class,
                EmptyComponent.class,
                InterfaceDependentComponentWithUse.class,
                ServiceInterfaceImpl.class,
                ServiceInterfaceWithDependenciesImpl.class));

    InterfaceDependentComponentWithUse interfaceDependentComponentWithUse =
        applicationContext.get(InterfaceDependentComponentWithUse.class);
    interfaceDependentComponentWithUse.run();
    assertEquals(20, interfaceDependentComponentWithUse.check);
  }
}
