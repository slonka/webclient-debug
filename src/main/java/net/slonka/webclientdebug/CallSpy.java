package net.slonka.webclientdebug;

import javassist.*;

import java.io.ByteArrayInputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

public class CallSpy implements ClassFileTransformer {

    @Override
    public byte[] transform(//region other parameters
                            ClassLoader loader,
                            String className,
                            Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain,
                            //endregion
                            byte[] classfileBuffer) throws IllegalClassFormatException {

        ClassPool cp = ClassPool.getDefault();
        cp.appendClassPath(new LoaderClassPath(Thread.currentThread().getContextClassLoader()));

        cp.importPackage("net.slonka.webclientdebug");

        //region filter agent classes
        // we do not want to profile ourselves
        if (className.startsWith("net/slonka/webclientdebug") && !className.startsWith("net/slonka/webclientdebug/Main")) {
            return null;
        }
        //endregion


        //region filter out non-application classes
        // Application filter. Can be externalized into a property file.
        // For instance, profilers use blacklist/whitelist to configure this kind of filters
        if (!className.startsWith("org/springframework/") && !className.startsWith("net/slonka/webclientdebug/Main")) {
            return classfileBuffer;
        }
        //endregion

        try {
            CtClass ct = cp.makeClass(new ByteArrayInputStream(classfileBuffer));

            CtMethod[] declaredMethods = ct.getDeclaredMethods();
            for (CtMethod method : declaredMethods) {
                if (isInstrumentable(method)) {
                    method.insertBefore(" { " +
                            "Timer.start();" +
                            "System.out.println(\"" + method.getLongName() + "\");" +
                            "}");
                    method.insertAfter("{ " +
                            "Timer.stop();" +
                            "System.out.print(\"" + method.getLongName() + "\");" +
                            "Timer.log();" +
                            "}", true);
                }
            }

            return ct.toBytecode();
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return classfileBuffer;
    }

    private boolean isInstrumentable(CtMethod method) {
        return !Modifier.isNative(method.getModifiers())
                && !Modifier.isAbstract(method.getModifiers())
                && !Modifier.isInterface(method.getModifiers());
    }
}