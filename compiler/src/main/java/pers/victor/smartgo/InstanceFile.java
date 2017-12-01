package pers.victor.smartgo;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

/**
 * Created by Victor on 01/12/2017. (ง •̀_•́)ง
 */

class InstanceFile {
    private static final String path = "META-INF/services/pers.victor.smartgo.InstanceInjector";
    private static final String suffix = "_Instance";
    private static final Map<Element, String> elements = new HashMap<>();

    static void addElement(Element element, String path) {
        elements.put(element, path);
    }

    static void createInstance(Filer filer, ProcessingEnvironment env) throws Exception {
        for (Map.Entry<Element, String> entry : elements.entrySet()) {
            TypeElement typeElement = (TypeElement) entry.getKey();
            String packageName = env.getElementUtils().getPackageOf(typeElement).getQualifiedName().toString();
            String pathValue = entry.getValue();
            String className = typeElement.getSimpleName() + suffix;
            String targetFullClass = typeElement.getQualifiedName().toString();
            MethodSpec newInstance = MethodSpec.methodBuilder("newInstance")
                    .addAnnotation(Override.class)
                    .addAnnotation(AnnotationSpec.builder(SuppressWarnings.class).addMember("value", "$S", "unchecked").build())
                    .addModifiers(Modifier.PUBLIC)
                    .addTypeVariable(TypeVariableName.get("T"))
                    .returns(TypeVariableName.get("T"))
                    .addStatement("return (T) new $T()", ClassName.bestGuess(targetFullClass))
                    .build();
            MethodSpec getPath = MethodSpec.methodBuilder("getPath")
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(String.class)
                    .addStatement("return $S", pathValue)
                    .build();
            TypeSpec type = TypeSpec.classBuilder(className)
                    .addSuperinterface(InstanceInjector.class)
                    .addModifiers(Modifier.PUBLIC)
                    .addMethod(newInstance)
                    .addMethod(getPath)
                    .build();
            JavaFile.builder(packageName, type).build().writeTo(filer);
        }
    }

    static void createResources(Filer filer) throws Exception {
        ResourceUtil.createResources(filer, elements, path, suffix);
    }

}
