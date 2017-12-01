package pers.victor.smartgo;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

/**
 * Created by Victor on 22/11/2017. (ง •̀_•́)ง
 */

class SmartPathFile {
    private static final String RES_PATH = "META-INF/services/pers.victor.smartgo.SmartPathInjector";
    private static final String suffix = "_SmartPath";
    private static final Map<Element, String> elements = new HashMap<>();

    static void addElement(Element element, String path) {
        elements.put(element, path);
    }

    static void createSmartPath(Filer filer, ProcessingEnvironment env) throws Exception {
        for (Map.Entry<Element, String> entry : elements.entrySet()) {
            TypeElement typeElement = (TypeElement) entry.getKey();
            String packageName = env.getElementUtils().getPackageOf(typeElement).getQualifiedName().toString();
            String pathValue = entry.getValue();
            String className = typeElement.getSimpleName() + suffix;
            String targetFullClass = typeElement.getQualifiedName().toString();
            MethodSpec goActivity = MethodSpec.methodBuilder("goActivity")
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(ParameterSpec.builder(SmartPathEntity.class, "entity").build())
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
                    .build();
            MethodSpec getPath = MethodSpec.methodBuilder("getPath")
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(String.class)
                    .addStatement("return $S", pathValue)
                    .build();
            TypeSpec type = TypeSpec.classBuilder(className)
                    .addSuperinterface(SmartPathInjector.class)
                    .addModifiers(Modifier.PUBLIC)
                    .addMethod(goActivity)
                    .addMethod(getPath)
                    .build();
            JavaFile.builder(packageName, type).build().writeTo(filer);
        }
    }

    static void createResources(Filer filer) throws Exception {
        ResourceUtil.createResources(filer, elements, RES_PATH, suffix);
    }
}
