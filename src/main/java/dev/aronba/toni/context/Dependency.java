package dev.aronba.toni.context;

public abstract class Dependency {
    private final Class<?> clazz;

    protected Dependency(Class<?> clazz){
        this.clazz = clazz;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public static Dependency of(Class<?> clazz){
        return new Dependency(clazz) {};
    }


}
