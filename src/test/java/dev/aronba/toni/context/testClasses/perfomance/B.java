package dev.aronba.toni.context.testClasses.perfomance;



import dev.aronba.toni.context.Component;

@Component
public class B {
    public B(A a){
        try {
            Thread.sleep(100);
        } catch (InterruptedException ignore) {}
    }
}
