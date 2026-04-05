package flandretw.bopomofo.translator;

import net.minecraft.text.HoverEvent;

public class Test {
    public static void test() {
        for (java.lang.reflect.Method m : HoverEvent.class.getDeclaredMethods()) {
            System.out.println("METHOD: " + m.toString());
        }
        for (java.lang.reflect.Constructor<?> c : HoverEvent.class.getDeclaredConstructors()) {
            System.out.println("CONSTRUCTOR: " + c.toString());
        }
        for (Class<?> c : HoverEvent.class.getDeclaredClasses()) {
            System.out.println("CLASS: " + c.toString());
        }
        throw new RuntimeException("DONE INTROSPECTION");
    }
}
