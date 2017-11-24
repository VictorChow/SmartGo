package pers.victor.smartgo;

import com.google.common.io.Closer;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

/**
 * Created by Victor on 22/11/2017. (ง •̀_•́)ง
 */

class SmartPathFile {
    private static final String RES_PATH = "META-INF/services/pers.victor.smartgo.SmartPathInjector";
    private static Map<Element, String> elements = new HashMap<>();

    static void addElement(Element element, String path) {
        elements.put(element, path);
    }

    static void createSmartPath(Filer filer, ProcessingEnvironment env) throws Exception {
        for (Map.Entry<Element, String> entry : elements.entrySet()) {
            TypeElement typeElement = (TypeElement) entry.getKey();
            String packageName = env.getElementUtils().getPackageOf(typeElement).getQualifiedName().toString();
            String pathValue = entry.getValue();
            String className = typeElement.getSimpleName() + "_SmartPath";
            String targetFullClass = typeElement.getQualifiedName().toString();
            MethodSpec goPath = MethodSpec.methodBuilder("goPath")
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(ParameterSpec.builder(SmartPathEntity.class, "entity").build())
                    .returns(boolean.class)
                    .addCode("if (!entity.path.contentEquals($S)) {\n" +
                            "  return false;\n" +
                            "}\n", pathValue)
                    .addStatement("$T context = (Context) entity.context", ClassName.bestGuess("android.content.Context"))
                    .addStatement("$T intent = (Intent) entity.intent", ClassName.bestGuess("android.content.Intent"))
                    .addStatement("intent.setClass(context, $T.class)", ClassName.bestGuess(targetFullClass))
                    .addCode("if (entity.isForResult) {\n" +
                                    "  if (!(context instanceof $T)) {\n" +
                                    "    throw new $T(\"非Activity的Context，不能startActivityForResult\");\n" +
                                    "  }\n" +
                                    "  ((Activity) context).startActivityForResult(intent, entity.requestCode);\n" +
                                    "} else {\n" +
                                    "  context.startActivity(intent);\n" +
                                    "}\n",
                            ClassName.bestGuess("android.app.Activity"),
                            ClassName.bestGuess("java.lang.IllegalArgumentException"))
                    .addCode("if (entity.isTransition) {\n" +
                            "  if (!(context instanceof Activity)) {\n" +
                            "    throw new IllegalArgumentException(\"非Activity的Context，不能overridePendingTransition\");\n" +
                            "  }\n" +
                            "  ((Activity) context).overridePendingTransition(entity.enterAnim, entity.exitAnim);\n" +
                            "}\n")
                    .addStatement("return true")
                    .build();
            MethodSpec getOriginClass = MethodSpec.methodBuilder("getOriginClass")
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(String.class, "path")
                    .returns(String.class)
                    .addCode("if (path.contentEquals($S)) {\n" +
                            "  return $S;\n" +
                            "}\n", pathValue, typeElement.getQualifiedName())
                    .addStatement("return null")
                    .build();
            TypeSpec type = TypeSpec.classBuilder(className)
                    .addSuperinterface(SmartPathInjector.class)
                    .addModifiers(Modifier.PUBLIC)
                    .addMethod(goPath)
                    .addMethod(getOriginClass)
                    .build();
            JavaFile file = JavaFile.builder(packageName, type).build();
            file.writeTo(filer);
        }
    }

    static void createResources(Filer filer) throws Exception {
        Set<String> services = new HashSet<>();
        for (Map.Entry<Element, String> entry : elements.entrySet()) {
            TypeElement typeElement = (TypeElement) entry.getKey();
            String className = typeElement.getQualifiedName() + "_SmartPath";
            services.add(className);
        }
        try {
            FileObject existingFile = filer.getResource(StandardLocation.CLASS_OUTPUT, "", RES_PATH);
            InputStream inputStream = existingFile.openInputStream();
            services.addAll(readServiceFile(inputStream));
            inputStream.close();
        } catch (Exception ignored) {

        }
        FileObject fileObject = filer.createResource(StandardLocation.CLASS_OUTPUT, "", RES_PATH);
        OutputStream outputStream = fileObject.openOutputStream();
        writeServiceFile(services, outputStream);
        outputStream.close();
    }

    private static Set<String> readServiceFile(InputStream input) throws IOException {
        HashSet<String> serviceClasses = new HashSet<>();
        Closer closer = Closer.create();
        try {
            BufferedReader r = closer.register(new BufferedReader(new InputStreamReader(input, Charset.forName("UTF-8"))));
            String line;
            while ((line = r.readLine()) != null) {
                int commentStart = line.indexOf('#');
                if (commentStart >= 0) {
                    line = line.substring(0, commentStart);
                }
                line = line.trim();
                if (!line.isEmpty()) {
                    serviceClasses.add(line);
                }
            }
            return serviceClasses;
        } catch (Throwable t) {
            throw closer.rethrow(t);
        } finally {
            closer.close();
        }
    }

    static void writeServiceFile(Set<String> services, OutputStream output) throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output, Charset.forName("UTF-8")));
        for (String service : services) {
            writer.write(service);
            writer.newLine();
        }
        writer.flush();
    }
}
