package pers.victor.smartgo;

import com.google.auto.service.AutoService;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

/**
 * Created by Victor on 2017/2/3. (ง •̀_•́)ง
 */

@AutoService(Processor.class)
@SupportedOptions("module")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes({"pers.victor.smartgo.IntentExtra", "pers.victor.smartgo.Path", "pers.victor.smartgo.Instance"})
public class SmartGoProcessor extends AbstractProcessor {
    static String packageName = "smartgo.module.";
    private static ProcessingEnvironment proEnv;

    private static void log(String text) {
        proEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, text);
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        proEnv = processingEnvironment;
        packageName += processingEnv.getOptions().get("module").toLowerCase().replaceAll("[^a-zA-Z0-9]", "");
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        try {
            createSmartGo(roundEnvironment);
            createSmartPath(roundEnvironment);
            createInstance(roundEnvironment);
        } catch (Exception e) {
            if (!e.getMessage().contains("Attempt to recreate a file for type")) {
                log(e.getMessage());
            }
        }
        return false;
    }

    private void createSmartGo(RoundEnvironment roundEnvironment) throws Exception {
        for (Element element : roundEnvironment.getElementsAnnotatedWith(IntentExtra.class)) {
            SmartGoFile.getEachVariableElement(element, processingEnv);
        }
        SmartGoFile.createSmartGo(processingEnv.getFiler());
        SmartGoFile.createInjectors(processingEnv.getFiler());
    }

    private void createSmartPath(RoundEnvironment roundEnvironment) throws Exception {
        for (Element element : roundEnvironment.getElementsAnnotatedWith(Path.class)) {
            SmartPathFile.addElement(element, element.getAnnotation(Path.class).value());
        }
        SmartPathFile.createSmartPath(processingEnv.getFiler(), processingEnv);
        SmartPathFile.createResources(processingEnv.getFiler());
    }

    private void createInstance(RoundEnvironment roundEnvironment) throws Exception {
        for (Element element : roundEnvironment.getElementsAnnotatedWith(Instance.class)) {
            InstanceFile.addElement(element, element.getAnnotation(Instance.class).value());
        }
        InstanceFile.createInstance(processingEnv.getFiler(), processingEnv);
        InstanceFile.createResources(processingEnv.getFiler());
    }
}
