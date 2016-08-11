package ru.veryevilzed.tools;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.StringUtils;

import javax.lang.model.element.TypeElement;
import java.util.*;

/**
 * Created by zed on 11.08.16.
 */
public class ExecutableScenarioElement {

    public Scenario scenarioAnnotation;
    public TypeElement scenarioType;
    public String implementationName;
    public ElementTypePair incoming;
    public ElementTypePair context;

    public Map<String, String> methodNames = new HashMap<>();

    public static class AutowiredItems {
        public ElementTypePair autowired;
        public String autowiredName;
        public ElementTypePair qualifier;

        public void build() {
            this.autowiredName = autowiredName == null || autowiredName.equals("") ? StringUtils.uncapitalize(autowired.element.getSimpleName().toString()) : StringUtils.uncapitalize(autowiredName);
        }

        public FieldSpec createField() {
            FieldSpec.Builder res = FieldSpec.builder(ClassName.get(autowired.element), autowiredName)
                    .addAnnotation(Autowired.class);
            if (qualifier != null)
                res.addAnnotation(AnnotationSpec.builder(Qualifier.class)
                .addMember("value", "$T", qualifier.element.asType()).build());
            return res.build();
        }

        @Override
        public boolean equals(Object obj) {
            return obj.getClass() == AutowiredItems.class && ((AutowiredItems) obj).autowiredName.equals(this.autowiredName);
        }

        @Override
        public int hashCode() {
            return autowiredName.hashCode();
        }
    }

    Set<AutowiredItems> autowiredItems = new HashSet<>();


}
