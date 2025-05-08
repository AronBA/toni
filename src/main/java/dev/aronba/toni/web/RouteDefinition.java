package dev.aronba.toni.web;

import java.lang.reflect.Method;

public record RouteDefinition(
    String path, String httpMethod, Method responseHandler, Object controllerInstance) {}
