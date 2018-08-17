package com.zeroturnaround.callspy;

import javassist.*;

import java.io.ByteArrayInputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.List;

public class CallSpy implements ClassFileTransformer {
    public static final List<String> interestingMethods = Arrays.asList(new String[]{
            "org.springframework.http.HttpMethod.values()",
            "org.springframework.util.CollectionUtils.unmodifiableMultiValueMap(org.springframework.util.MultiValueMap)",
            "org.springframework.web.reactive.function.client.ClientRequest.create(org.springframework.http.HttpMethod,java.net.URI)",
            "org.springframework.web.reactive.function.client.DefaultClientResponse.bodyToMono(java.lang.Class)",
            "org.springframework.core.ResolvableType.forClassWithGenerics(java.lang.Class,java.lang.Class[])"
    });

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

        cp.importPackage("com.zeroturnaround.callspy");

        //region filter agent classes
        // we do not want to profile ourselves
        if (className.startsWith("com/zeroturnaround/callspy") && !className.startsWith("com/zeroturnaround/callspy/Main")) {
            return null;
        }
        //endregion


        //region filter out non-application classes
        // Application filter. Can be externalized into a property file.
        // For instance, profilers use blacklist/whitelist to configure this kind of filters
        if (!className.startsWith("org/springframework/") && !className.startsWith("com/zeroturnaround/callspy/Main")) {
            return classfileBuffer;
        }
        //endregion

        // Print out all instrumented classes loaded
//    System.out.println("class name: " + className);

        try {
            CtClass ct = cp.makeClass(new ByteArrayInputStream(classfileBuffer));

            CtMethod[] declaredMethods = ct.getDeclaredMethods();
            for (CtMethod method : declaredMethods) {
                //region instrument method

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

//                    if (interestingMethods.contains(method.getLongName())) {
//                        method.insertBefore("System.out.println(\"> " + method.getLongName() + " s: \" + System.nanoTime() / 1000000 " + ");");
//                        method.insertAfter("System.out.println(\"< " + method.getLongName() + " e: \" + System.nanoTime() / 1000000 " + ");");
//                    }
                }
                //endregion
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