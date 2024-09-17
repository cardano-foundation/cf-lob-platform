package org.cardanofoundation.lob.app.accounting_reporting_core.service.internal;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.annotations.LOBVersionSinkRelevant;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.annotations.LOBVersionSourceRelevant;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.Set;

@AutoService(Processor.class)
@SupportedAnnotationTypes({
        "org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.annotations.LOBVersionSourceRelevant",
        "org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.annotations.LOBVersionSinkRelevant"
})
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class LOBVersionProcessor extends AbstractProcessor {

    private Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        messager = processingEnv.getMessager();
        messager.printMessage(Diagnostic.Kind.NOTE, "Initializing LOBVersionProcessor...");
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        messager.printMessage(Diagnostic.Kind.NOTE, "Generating class based on: LOBVersionSourceRelevant and LOBVersionSinkRelevant annotations");

        // Collect all elements (fields) annotated with @LOBVersionSourceRelevant and @LOBVersionSinkRelevant
        Set<? extends Element> sourceElements = roundEnv.getElementsAnnotatedWith(LOBVersionSourceRelevant.class);
        Set<? extends Element> sinkElements = roundEnv.getElementsAnnotatedWith(LOBVersionSinkRelevant.class);

        // Generate code to compute hash
        if (!sourceElements.isEmpty()) {
            generateHashMethod(sourceElements, "computeSourceHash", "LOBVersionSourceRelevant");
        }

        if (!sinkElements.isEmpty()) {
            generateHashMethod(sinkElements, "computeSinkHash", "LOBVersionSinkRelevant");
        }

        return true; // annotations are claimed by this processor
    }

    private void generateHashMethod(Set<? extends Element> elements, String methodName, String annotationType) {
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(methodName + "Generator")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(String.class)
                .addParameter(Object.class, "obj")
                .addStatement("StringBuilder builder = new StringBuilder()");

        for (Element element : elements) {
            String fieldName = element.getSimpleName().toString();
            methodBuilder.addStatement("builder.append(obj.getClass().getDeclaredField($S).get(obj))", fieldName);
        }

        methodBuilder.addStatement("return org.cardanofoundation.lob.app.support.crypto.SHA3.digestAsHex(builder.toString())");

        classBuilder.addMethod(methodBuilder.build());

        JavaFile javaFile = JavaFile.builder("org.cardanofoundation.lob.app.accounting_reporting_core.generated", classBuilder.build())
                .build();

        try {
            javaFile.writeTo(processingEnv.getFiler());
        } catch (IOException e) {
            messager.printMessage(Diagnostic.Kind.ERROR, e.toString());
        }
    }
}