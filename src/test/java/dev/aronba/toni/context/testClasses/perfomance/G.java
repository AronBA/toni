package dev.aronba.toni.context.testClasses.perfomance;

import dev.aronba.toni.context.Component;

@Component
public class G {
    public G(F f,E e,D d,C c,B b, A a){
        try {
            Thread.sleep(100);
        } catch (InterruptedException ignore) {}
    }

    public G(Q q,E e,D d,C c,B b, A a){
        try {
            Thread.sleep(100);
        } catch (InterruptedException ignore) {}
    }

    public G(Q q,R r,D d,C c,B b, A a){
        try {
            Thread.sleep(100);
        } catch (InterruptedException ignore) {}
    }
}
