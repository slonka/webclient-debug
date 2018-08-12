package com.zeroturnaround.callspy;

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
          System.out.println("Instrumenting: " + method.getName());
          method.insertBefore("System.out.println(\"started method " + method.getName() + "\");");
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