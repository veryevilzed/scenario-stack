package ru.veryevilzed.tools;

import com.squareup.javapoet.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.processing.*;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

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
        return checkScenarioAnnotatedElement(roundEnv);
    }

    boolean checkScenarioAnnotatedElement(RoundEnvironment roundEnv) {
        Set<? extends Element> entityAnnotated =
                roundEnv.getElementsAnnotatedWith(scenarioType.element);

        List<ExecutableScenarioElement> executableScenarioElements = new ArrayList<>();

        TypeSpec.Builder serviceBuilder = TypeSpec.classBuilder("ScenarioServiceImpl")
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(ScenarioService.class)
                .addAnnotation(Service.class);

        for (TypeElement typeElement : ElementFilter.typesIn(entityAnnotated)) {
            if (typeElement.getKind().isInterface())
                try {
                    ExecutableScenarioElement ee = buildImplementation(typeElement);
                    executableScenarioElements.add(ee);
                    serviceBuilder.addField(
                            FieldSpec.builder(
                                    TypeName.get(typeElement.asType()), StringUtils.uncapitalize(ee.implementationName))
                                    .addAnnotation(Autowired.class)
                                    .build()
                            );
                }catch (IOException e){
                    messager.printMessage(Diagnostic.Kind.ERROR, e.getMessage());
                }
        }

        MethodSpec.Builder method = MethodSpec.methodBuilder("execute")
                .addException(RuntimeException.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(Object.class)
                .addParameter(String.class, "name")
                .addParameter(String.class, "method")
                .addParameter(Object.class, "context");


        for(ExecutableScenarioElement executableScenarioElement : executableScenarioElements) {
            if (!executableScenarioElement.scenarioAnnotation.name().equals("")) {

                for(Map.Entry<String, String> name : executableScenarioElement.methodNames.entrySet()) {
                    method.addCode("if (name == $S && method == $S) return $L.$L(($T)context);\n",
                            executableScenarioElement.scenarioAnnotation.name(),
                            name.getValue().equals("") ? name.getKey() : name.getValue(),
                            StringUtils.uncapitalize(executableScenarioElement.implementationName),
                            name.getKey(),
                            executableScenarioElement.context.type
                    );
                }
            }
        }

        method.addCode("return context;\n");
        serviceBuilder.addMethod(method.build());

        if (executableScenarioElements.size() == 0)
            return false;

        JavaFile javaFile = JavaFile.builder("ru.veryevilzed.tools", serviceBuilder.build())
                .build();
        try {
            javaFile.writeTo(filer);
        }catch (IOException e){
            messager.printMessage(Diagnostic.Kind.ERROR, e.getMessage());
        }
        return true;
    }

    /**
     * Создадим имплементацию
     * @param typeElement
     */
    ExecutableScenarioElement buildImplementation(TypeElement typeElement) throws IOException {
        //TODO Найдем все методы ScenarioMethod

        ExecutableScenarioElement executableScenarioElement = new ExecutableScenarioElement();
        executableScenarioElement.scenarioType = typeElement;
        executableScenarioElement.scenarioAnnotation = typeElement.getAnnotation(Scenario.class);
        executableScenarioElement.context = getType(executableScenarioElement.scenarioAnnotation.context());


        Map<ExecutableElement, ScenarioMethod> scenarioMethodMap = new HashMap<ExecutableElement, ScenarioMethod>();
        for(ExecutableElement executableElement : ElementFilter.methodsIn(typeElement.getEnclosedElements())) {
            ScenarioMethod scenarioMethod = executableElement.getAnnotation(ScenarioMethod.class);
            if (scenarioMethod == null)
                continue;
            scenarioMethodMap.put(executableElement, scenarioMethod);

        }
        executableScenarioElement.implementationName = typeElement.getSimpleName() + "Impl";
        TypeSpec.Builder serviceBuilder = TypeSpec.classBuilder(executableScenarioElement.implementationName)
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(TypeName.get(typeElement.asType()))
                .addAnnotation(Service.class);

        List<MethodSpec> methods = buildMethods(scenarioMethodMap, executableScenarioElement);

        executableScenarioElement.autowiredItems.forEach(i -> serviceBuilder.addField(i.createField()));
        methods.forEach(serviceBuilder::addMethod);
        JavaFile javaFile = JavaFile.builder("ru.veryevilzed.tools", serviceBuilder.build())
                .build();

        javaFile.writeTo(filer);
        return executableScenarioElement;
    }

    List<MethodSpec> buildMethods(Map<ExecutableElement, ScenarioMethod> scenarioMethodMap, ExecutableScenarioElement executableScenarioElement) {

        List<MethodSpec> res = new ArrayList<>();

        for(Map.Entry<ExecutableElement, ScenarioMethod> scenarioMethod : scenarioMethodMap.entrySet()) {

            executableScenarioElement.methodNames.put(
                    scenarioMethod.getKey().getSimpleName().toString(),
                    scenarioMethod.getValue().name()
                    );

            MethodSpec.Builder method = MethodSpec.methodBuilder(scenarioMethod.getKey().getSimpleName().toString());
            method.addModifiers(Modifier.PUBLIC);
            method.returns(ClassName.get(executableScenarioElement.context.element.asType()));
            method.addException(RuntimeException.class);
            // Посмотрим на параметры
            List<String> args = new ArrayList<>();

            for(VariableElement te : scenarioMethod.getKey().getParameters()){
                if (te.asType() == executableScenarioElement.context.element.asType()) {
                    method.addParameter(ParameterSpec.builder(ClassName.get(te.asType()), "context").build());
                    args.add("context");
                }
            }

            for(ScenarioTarget target : scenarioMethod.getValue().targets()){
                ExecutableScenarioElement.AutowiredItems autowiredItem = null;
                if (!target.autowired().equals("")){
                    autowiredItem = new ExecutableScenarioElement.AutowiredItems();
                    autowiredItem.autowiredName = target.autowiredName();
                    autowiredItem.autowired = getType(target.autowired());
                    if (!target.qualifier().equals(""))
                        autowiredItem.qualifier = getType(target.autowired());
                    autowiredItem.build();
                    executableScenarioElement.autowiredItems.add(autowiredItem);
                }

                String m = target.method();
                if (autowiredItem != null)
                    m = autowiredItem.autowiredName + "." + m;
                List<String> _args = new ArrayList<>(args);
                Collections.addAll(_args, target.params());
                method.addCode("$L($L);\n", m, _args.stream().collect(Collectors.joining(", ")));
            }

            method.addCode("return $L;\n", "context");
            res.add(method.build());
        }
        return res;
    }

}


