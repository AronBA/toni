package dev.aronba.toni.context.testClasses;

import dev.aronba.toni.context.Component;

@Component
public class ServiceInterfaceImpl implements ServiceInterface{
    @Override
    public int provideService() {
        return 10;
    }
}
