package ru.veryevilzed.tools;

import javax.annotation.processing.*;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;
import java.util.Set;

/**
 * Created by zed on 10.08.16.
 */
@SupportedAnnotationTypes({ScenarioServiceAnnotationProcessor.S_TYPE})
public class ScenarioServiceAnnotationProcessor extends AbstractProcessor {

    final static String S_TYPE = "ru.veryevilzed.tools.Scenario";


    Filer filer;
    Messager messager;
    private ElementTypePair scenarioType;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.filer = processingEnv.getFiler();
        this.messager = processingEnv.getMessager();
        this.scenarioType = getType(S_TYPE);
    }

    private Types typeUtils() {
        return processingEnv.getTypeUtils();
    }

    private ElementTypePair getType(String className) {
        TypeElement typeElement = processingEnv.getElementUtils().getTypeElement(className);
        DeclaredType declaredType = typeUtils().getDeclaredType(typeElement);
        return new ElementTypePair(typeElement, declaredType);
    }

    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        checkScenarioAnnotatedElement(roundEnv);
        return false;
    }

    void checkScenarioAnnotatedElement(RoundEnvironment roundEnv) {
        Set<? extends Element> entityAnnotated =
                roundEnv.getElementsAnnotatedWith(scenarioType.element);

        //TODO: Получить список классов,
        //TODO: создать класс/сервис
        //TODO: Выстроить список сервисов
        for (TypeElement typeElement : ElementFilter.typesIn(entityAnnotated)) {

        }
    }

}


