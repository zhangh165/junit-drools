package pl.maciejwalkowiak.drools;

import org.drools.compiler.builder.impl.KnowledgeBuilderImpl;
import org.drools.compiler.compiler.DroolsError;
import org.drools.compiler.compiler.DroolsParserException;
import org.drools.compiler.compiler.PackageBuilderErrors;
import org.drools.core.io.impl.FileSystemResource;
import org.drools.core.util.StringUtils;
import org.kie.api.io.Resource;
import org.kie.api.runtime.KieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.maciejwalkowiak.drools.annotations.DroolsFiles;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * Initializes Drools knowledge base and {@link KieSession} and injects them to test class
 *
 * @author Maciej Walkowiak
 */
public class DroolsInjector {
    private static final Logger LOG = LoggerFactory.getLogger(DroolsInjector.class);
    private final KnowledgeBuilderImpl builder = new KnowledgeBuilderImpl();

    public void initDrools(Object testClass) throws Exception {
        if (testClass == null) {
            throw new IllegalArgumentException("Test class cannot be null");
        }

        LOG.info("Initializing Drools objects for test class: {}", testClass.getClass());

        DroolsAnnotationProcessor annotationProcessor = new DroolsAnnotationProcessor(testClass);
        DroolsFiles droolsFiles = annotationProcessor.getDroolsFiles();

        DroolsSession droolsSession = 
          initKnowledgeBase(droolsFiles.location(),
                  droolsFiles.dsl(),
                  Arrays.asList(droolsFiles.value()));

        annotationProcessor.setDroolsSession(droolsSession);
    }

    private DroolsSession initKnowledgeBase(String droolsLocation,
                                            String dslFileName,
                                            Iterable<String> drlFileNames) throws Exception {

        LOG.info("Initializing knowledge base for drl files located in {} with names: {}", droolsLocation, drlFileNames);

        loadRuleFiles(droolsLocation,dslFileName,drlFileNames);

        checkErrorsExistsInKnowledgeBase();

        KieSession session = builder.newKieBase().newKieSession();

        return new DroolsSessionImpl(session);
    }

    private void loadRuleFiles(String droolsLocation,
                               String dslFileName,
                               Iterable<String> drlFileNames) throws IOException, DroolsParserException {
        for (String fileName : drlFileNames) {
            builder.addPackageFromDrl(loadDroolFile(droolsLocation, fileName));
        }

        if(!StringUtils.isEmpty(dslFileName)){
            builder.addPackageFromDslr(loadDslFile(droolsLocation,dslFileName));
        }
    }

    private InputStreamReader loadDroolFile(String droolsLocation, String filename) {
        InputStream stream = getClass().getResourceAsStream(droolsLocation + filename);

        if (stream == null) {
            throw new IllegalArgumentException("File not found in location: " + droolsLocation + filename);
        }
        return new InputStreamReader(stream);
    }

    private Resource loadDslFile(String droolLocation,String fileName){
        Path dslFilePath = Paths.get(droolLocation+File.separator+fileName);
        if(!Files.exists(dslFilePath)){
            throw new IllegalArgumentException("File not found in location: "+dslFilePath);
        }

        return new FileSystemResource(dslFilePath.toString());
    }

    private void checkErrorsExistsInKnowledgeBase(){
        PackageBuilderErrors errors = builder.getErrors();

        // Make sure that there are no errors in knowledge base
        if (errors.getErrors().length > 0) {
            LOG.error("Errors during loading DRL files");

            for (DroolsError error : errors.getErrors()) {
                LOG.error("Error: {}", error.getMessage());
            }

            throw new IllegalStateException("There are errors in DRL files");
        }
    }
}
