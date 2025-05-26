# Toni Dependency Injection Framework

Toni is a lightweight Dependency Injection (DI) framework written in Java. It provides a simple and extensible way to manage object creation, dependency resolution, and lifecycle management in your applications.

## Features

- **Component Scanning**: Automatically detects and registers components annotated with `@Component` or `@Bean`.
- **Constructor Injection**: Supports dependency injection through constructors.
- **Post-Construction Hooks**: Executes methods annotated with `@PostConstruct` after object creation.
- **Custom Implementation Selection**: Allows selecting specific implementations for interfaces using the `@Use` annotation.
- **Optional Dependencies**: Supports optional dependencies using `Optional<T>`.
- **Post-Processors**: Enables custom processing of components using `@PostProcessor`.

## Missing Features

The framework is still under development and lacks some advanced features, such as:
- Scope management (e.g., prototype, request, session).
- Circular dependency handling.
- Lazy initialization.
- Aspect-Oriented Programming (AOP).
- Event handling and external configuration support.

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.8 or higher

### Installation

Clone the repository and build the project using Maven:

```bash
git clone <repository-url>
cd toni
mvn clean install
```

### Usage

1. Annotate your classes with `@Component` or `@Bean` to register them with the DI container.

```java
@Component
public class MyService {
  private final MyRepository repository;

  public MyService(MyRepository repository) {
    this.repository = repository;
  }
}
```

2. Use the `@Use` annotation to specify a preferred implementation for an interface.

```java
@Component
public class MyController {
  private final MyService service;

  public MyController(@Use(implementationName = "SpecialService") MyService service) {
    this.service = service;
  }
}
```

3. Start the application using the `ToniApplication` class.

```java
public class Main {
  public static void main(String[] args) throws Exception {
    ToniApplication.run(args);
  }
}
```

### Project Structure

- `src/main/java/dev/aronba/toni`: Core application classes.
- `src/main/java/dev/aronba/toni/context`: DI container implementation.
- `src/main/java/dev/aronba/toni/context/annotation`: Custom annotations for components and configuration.
- `src/main/java/dev/aronba/toni/context/internal`: Internal utilities for dependency resolution and instance creation.

### Example

Here is an example of a simple application using Toni:

```java
@Component
public class HelloWorldService {
  public String getMessage() {
    return "Hello, World!";
  }
}

@Component
public class HelloWorldController {
  private final HelloWorldService service;

  public HelloWorldController(HelloWorldService service) {
    this.service = service;
  }

  public void printMessage() {
    System.out.println(service.getMessage());
  }
}

public class Main {
  public static void main(String[] args) throws Exception {
    ToniApplication.run(args);
  }
}
```

## Contributing

Contributions are welcome! Feel free to open issues or submit pull requests to improve the framework.

## License

This project is licensed under the MIT License. See the `LICENSE` file for details.

## Acknowledgments

- Inspired by popular DI frameworks like Spring and Guice.
- Special thanks to the open-source community for their contributions and ideas.
