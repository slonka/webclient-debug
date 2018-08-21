package net.slonka.webclientdebug;
import java.util.Stack;

@SuppressWarnings("unused")
public class Timer {
    private static Stack<Double> stack = new Stack<>();
    static int indent = 0;

    public static void start() {
        stack.push((double) System.nanoTime());
        indent++;
        System.out.print(String.format("%" + indent + "s >", ""));
    }

    public static void stop() {
        System.out.print(String.format("%" + indent + "s <", ""));
        indent--;
    }

    public static void log() {
        System.out.println(String.format(" - took %.2f ms", ((System.nanoTime() - stack.pop()) / 1_000_000)));
    }
}
