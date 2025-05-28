package dev.aronba.toni.context;

import static org.junit.jupiter.api.Assertions.*;

import dev.aronba.toni.context.core.ApplicationContext;
import dev.aronba.toni.context.core.BasicApplicationContext;
import dev.aronba.toni.context.exception.CircularDependencyException;
import dev.aronba.toni.context.exception.NoImplementationFoundException;
import dev.aronba.toni.context.processor.PostConstructPostProcessor;
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
    assertDoesNotThrow(() -> applicationContext.registerComponents(EmptyComponent.class));
    EmptyComponent emptyComponent = applicationContext.getComponent(EmptyComponent.class);
    assertNotNull(emptyComponent);
  }

  @ParameterizedTest
  @MethodSource("provideImplementations")
  void shouldRegisterMultipleComponents(ApplicationContext applicationContext) {
    assertDoesNotThrow(
        () -> applicationContext.registerComponents(EmptyComponent.class, SimpleComponent.class));
    EmptyComponent emptyComponent = applicationContext.getComponent(EmptyComponent.class);
    SimpleComponent simpleComponent = applicationContext.getComponent(SimpleComponent.class);
    assertNotNull(emptyComponent);
    assertNotNull(simpleComponent);
  }

  @ParameterizedTest
  @MethodSource("provideImplementations")
  void shouldRegisterComponentWithOneDependency(ApplicationContext applicationContext) {
    assertDoesNotThrow(
        () -> applicationContext.registerComponents(EmptyComponent.class, SimpleComponent.class));
    SimpleComponent simpleComponent = applicationContext.getComponent(SimpleComponent.class);
    assertNotNull(simpleComponent);
    assertNotNull(simpleComponent.getEmptyComponent());
  }

  @ParameterizedTest
  @MethodSource("provideImplementations")
  void shouldRegisterComponentWithMultipleDependencies(ApplicationContext applicationContext) {
    assertDoesNotThrow(
        () ->
            applicationContext.registerComponents(
                EmptyComponent.class, SimpleComponent.class, ComplexComponent.class));
    ComplexComponent complexComponent = applicationContext.getComponent(ComplexComponent.class);
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
            applicationContext.registerComponents(
                ComplexComponent.class, EmptyComponent.class, SimpleComponent.class));
    ComplexComponent complexComponent = applicationContext.getComponent(ComplexComponent.class);
    assertNotNull(complexComponent);
  }

  @ParameterizedTest
  @MethodSource("provideImplementations")
  void shouldHandleMultipleConstructors(ApplicationContext applicationContext) {

    assertDoesNotThrow(
        () ->
            applicationContext.registerComponents(
                SimpleComponent.class, ComplexComponent.class, EmptyComponent.class));
    ComplexComponent complexComponent = applicationContext.getComponent(ComplexComponent.class);

    assertNotNull(complexComponent.getSimpleComponent());
    assertNotNull(complexComponent);
    assertNotNull(complexComponent.getEmptyComponent());
    assertNull(complexComponent.getComponentDescription());
    assertNull(complexComponent.getComponentName());
  }

  @ParameterizedTest
  @MethodSource("provideImplementations")
  void shouldGetCorrectInstance(ApplicationContext applicationContext) {
    assertDoesNotThrow(() -> applicationContext.registerComponents(EmptyComponent.class));
    Object component = applicationContext.getComponent(EmptyComponent.class);
    assertInstanceOf(EmptyComponent.class, component);
  }

  @ParameterizedTest
  @MethodSource("provideImplementations")
  void shouldGetAlwaysTheSameSingleton(ApplicationContext applicationContext) {
    assertDoesNotThrow(
        () ->
            applicationContext.registerComponents(
                EmptyComponent.class, EmptyComponent.class, EmptyComponent.class));
    EmptyComponent emptyComponent1 = applicationContext.getComponent(EmptyComponent.class);
    EmptyComponent emptyComponent2 = applicationContext.getComponent(EmptyComponent.class);
    EmptyComponent emptyComponent3 = applicationContext.getComponent(EmptyComponent.class);

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
    assertDoesNotThrow(() -> applicationContext.registerComponents(EmptyComponent.class));
    ComplexComponent complexComponent = applicationContext.getComponent(ComplexComponent.class);
    assertNull(complexComponent);
  }

  @ParameterizedTest
  @MethodSource("provideImplementations")
  void shouldRunPostProcessors(ApplicationContext applicationContext) {
    assertDoesNotThrow(
        () ->
            applicationContext.registerComponents(
                ComplexComponent.class,
                SimpleComponent.class,
                EmptyComponent.class,
                SimplePostProcessor.class));
    ComplexComponent complexComponent = applicationContext.getComponent(ComplexComponent.class);
    assertEquals("Hello from Postprocessor", complexComponent.getComponentName());
  }

  @ParameterizedTest
  @MethodSource("provideImplementations")
  void shouldRunPostConstructProcessor(ApplicationContext applicationContext) {
    assertDoesNotThrow(
        () ->
            applicationContext.registerComponents(
                ComplexComponent.class,
                SimpleComponent.class,
                EmptyComponent.class,
                SimplePostProcessor.class,
                PostConstructPostProcessor.class));
    ComplexComponent complexComponent = applicationContext.getComponent(ComplexComponent.class);
    assertEquals("Hello from PostConstruct", complexComponent.getComponentDescription());
  }

  @ParameterizedTest
  @MethodSource("provideImplementations")
  void shouldFindDirectCircularDependency(ApplicationContext applicationContext) {
    assertThrows(
        CircularDependencyException.class,
        () -> applicationContext.registerComponents(CircularDependentComponent1.class));
  }

  @ParameterizedTest
  @MethodSource("provideImplementations")
  void shouldFindIndirectCircularDependency(ApplicationContext applicationContext) {
    assertThrows(
        CircularDependencyException.class,
        () ->
            applicationContext.registerComponents(
                CircularDependentComponent2.class, CircularDependentComponent3.class));
  }

  @ParameterizedTest
  @MethodSource("provideImplementations")
  void shouldTryToFindImplementationOfInterfaceWhenDependingOnInterface(
      ApplicationContext applicationContext) {
    assertDoesNotThrow(
        () ->
            applicationContext.registerComponents(
                ServiceInterface.class,
                ServiceInterfaceImpl.class,
                InterfaceDependentComponent.class));
    InterfaceDependentComponent interfaceDependentComponent =
        applicationContext.getComponent(InterfaceDependentComponent.class);
    interfaceDependentComponent.run();
    assertEquals(10, interfaceDependentComponent.check);
  }

  @ParameterizedTest
  @MethodSource("provideImplementations")
  void shouldThrowIfNoImplementationForDependencyExists(ApplicationContext applicationContext) {
    assertThrows(
        NoImplementationFoundException.class,
        () ->
            applicationContext.registerComponents(ServiceInterface.class, InterfaceDependentComponent.class));
  }

  @ParameterizedTest
  @MethodSource("provideImplementations")
  void shouldFindDependenciesOfInterfaceImplementationsCorrectly(
      ApplicationContext applicationContext) {
    assertDoesNotThrow(
        () ->
            applicationContext.registerComponents(
                ServiceInterface.class,
                InterfaceDependentComponent.class,
                ServiceInterfaceWithDependenciesImpl.class,
                EmptyComponent.class));
    InterfaceDependentComponent interfaceDependentComponent =
        applicationContext.getComponent(InterfaceDependentComponent.class);
    interfaceDependentComponent.run();
    assertEquals(20, interfaceDependentComponent.check);
  }

  @ParameterizedTest
  @MethodSource("provideImplementations")
  void shouldUseCorrectImplementation(ApplicationContext applicationContext) {
    assertDoesNotThrow(
        () ->
            applicationContext.registerComponents(
                ServiceInterface.class,
                EmptyComponent.class,
                InterfaceDependentComponentWithUse.class,
                ServiceInterfaceImpl.class,
                ServiceInterfaceWithDependenciesImpl.class));

    InterfaceDependentComponentWithUse interfaceDependentComponentWithUse =
        applicationContext.getComponent(InterfaceDependentComponentWithUse.class);
    interfaceDependentComponentWithUse.run();
    assertEquals(20, interfaceDependentComponentWithUse.check);
  }

  @ParameterizedTest
  @MethodSource("provideImplementations")
  void shouldCreateNewInstanceWhenPrototype(ApplicationContext applicationContext) {
    assertDoesNotThrow(() -> applicationContext.registerComponents(PrototypeComponent.class));

    PrototypeComponent prototypeComponent = applicationContext.getComponent(PrototypeComponent.class);

    PrototypeComponent prototypeComponent2 = applicationContext.getComponent(PrototypeComponent.class);

    assertNotNull(prototypeComponent);
    assertNotNull(prototypeComponent2);
    assertNotEquals(prototypeComponent, prototypeComponent2);
    assertNotEquals(prototypeComponent.id, prototypeComponent2.id);
  }

  @ParameterizedTest
  @MethodSource("provideImplementations")
  void shouldInstantiateConstructorsWithNoParams(ApplicationContext applicationContext) {
    assertDoesNotThrow(() -> applicationContext.registerComponents(EmptyConstructorComponent.class));

    EmptyConstructorComponent emptyConstructorComponent =
        applicationContext.getComponent(EmptyConstructorComponent.class);
    assertNotNull(emptyConstructorComponent);
  }

  @ParameterizedTest
  @MethodSource("provideImplementations")
  void shouldResolveOptionalDependencies(ApplicationContext applicationContext) {
    assertDoesNotThrow(
        () ->
            applicationContext.registerComponents(
                OptionalDependenciesComponent.class, SimpleComponent.class, EmptyComponent.class));
    OptionalDependenciesComponent optionalDependenciesComponent =
        applicationContext.getComponent(OptionalDependenciesComponent.class);
    assertNotNull(optionalDependenciesComponent);
    assertNotNull(optionalDependenciesComponent.simpleComponent);
    assertTrue(optionalDependenciesComponent.simpleComponent.isPresent());
    assertInstanceOf(SimpleComponent.class,optionalDependenciesComponent.simpleComponent.get());
  }

  @ParameterizedTest
  @MethodSource("provideImplementations")
  void shouldResolveOptionalDependenciesIfMissing(ApplicationContext applicationContext) {
    assertDoesNotThrow(
        () ->
            applicationContext.registerComponents(
                OptionalDependenciesComponent.class));
    OptionalDependenciesComponent optionalDependenciesComponent =
        applicationContext.getComponent(OptionalDependenciesComponent.class);
    assertNotNull(optionalDependenciesComponent);
    assertNotNull(optionalDependenciesComponent.simpleComponent);
    assertTrue(optionalDependenciesComponent.simpleComponent.isEmpty());
  }

  @ParameterizedTest
  @MethodSource("provideImplementations")
  void shouldResolveOptionalInterfaceDependencies(ApplicationContext applicationContext) {}
}
