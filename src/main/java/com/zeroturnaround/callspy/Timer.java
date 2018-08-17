package com.zeroturnaround.callspy;
import java.util.Stack;

@SuppressWarnings("unused")
public class Timer {
    private static Stack<Long> stack = new Stack<>();
    static int indent = 0;

    public static void start() {
        stack.push(System.nanoTime());
        indent++;
        System.out.print(String.format("%" + indent + "s >", ""));
    }

    public static void stop() {
        System.out.print(String.format("%" + indent + "s <", ""));
        indent--;
    }

    public static void log() {
        System.out.println(String.format(" - took %d ms", ((System.nanoTime() - stack.pop()) / 1_000_000)));
    }
}
