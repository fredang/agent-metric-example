package com.chimpler.example.agentmetric;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import javassist.ByteArrayClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimedClassTransformer implements ClassFileTransformer {
	private final static Logger logger = LoggerFactory.getLogger(TimedClassTransformer.class);
	private ClassPool classPool;
	
	public TimedClassTransformer() {
		classPool = new ClassPool();
		classPool.appendSystemPath();
		try {
			classPool.appendPathList(System.getProperty("java.class.path"));
			
			// make sure that MetricReporter is loaded
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	 
    public byte[] transform(ClassLoader loader, String fullyQualifiedClassName, Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain, byte[] classBytes) throws IllegalClassFormatException {
    	String className = fullyQualifiedClassName.replace("/", ".");

		classPool.appendClassPath(new ByteArrayClassPath(className, classBytes));
		
		try {
			CtClass ctClass = classPool.get(className);
			if (ctClass.isFrozen()) {
				logger.debug("Skip class {}: is frozen", className);
				return null;
			}
			
			if (ctClass.isPrimitive() || ctClass.isArray() || ctClass.isAnnotation()
					|| ctClass.isEnum() || ctClass.isInterface()) {
				logger.debug("Skip class {}: not a class", className);
			} else {
				boolean isClassModified = false;
				for(CtMethod method: ctClass.getDeclaredMethods()) {
					// if method is annotated, add the code to measure the time
					if (method.hasAnnotation(Measured.class)) {
						if (method.getMethodInfo().getCodeAttribute() == null) {
							logger.debug("Skip method " + method.getLongName());
							continue;
						}
						logger.debug("Instrumenting method " + method.getLongName());
						method.addLocalVariable("__metricStartTime", CtClass.longType);
						method.insertBefore("__metricStartTime = System.currentTimeMillis();");
						String metricName = ctClass.getName() + "." + method.getName();
						method.insertAfter("com.chimpler.example.agentmetric.MetricReporter.reportTime(\"" + metricName + "\", System.currentTimeMillis() - __metricStartTime);");
						isClassModified = true;
					}
				}
				if (isClassModified) {
					return ctClass.toBytecode();
				}
			}
		} catch (Exception e) {
			logger.debug("Skip class {}: ", className, e.getMessage());
		}
		return classBytes;
    }
}