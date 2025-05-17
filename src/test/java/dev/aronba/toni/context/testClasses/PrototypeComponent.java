package dev.aronba.toni.context.testClasses;

import dev.aronba.toni.context.Component;
import dev.aronba.toni.context.Lifetime;

@Component(Lifetime.PROTOTYPE)
public class PrototypeComponent {
    private static int classNumber = 0;
    public int id;

    public PrototypeComponent(){
        classNumber++;
        id = classNumber;
    }
}
