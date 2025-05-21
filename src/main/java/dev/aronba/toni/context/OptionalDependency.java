package dev.aronba.toni.context;


public class OptionalDependency extends Dependency {

    private final TypeReference<?> typeReference;

    private OptionalDependency(TypeReference typeReference) {
        super(typeReference.getType().getClass());
        this.typeReference = typeReference;
    }

    public static OptionalDependency of(TypeReference typeReference){
        return new OptionalDependency(typeReference);
    }

}
