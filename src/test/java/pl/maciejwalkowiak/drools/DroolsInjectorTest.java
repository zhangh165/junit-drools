package pl.maciejwalkowiak.drools;

import org.junit.Test;
import pl.maciejwalkowiak.drools.annotations.DroolsFiles;

import java.util.List;

import static com.googlecode.catchexception.CatchException.catchException;
import static com.googlecode.catchexception.CatchException.caughtException;
import static org.fest.assertions.api.Assertions.assertThat;

public class DroolsInjectorTest {
    @Test
    public void should_throw_exception_for_null_class() throws Exception {
        catchException(new DroolsInjector()).initDrools(null);

        assertThat((Exception)caughtException())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("null");
    }

    @Test
    public void should_throw_exception_for_file_not_found() throws Exception {
        NotExistingFileTestClass testClass = new NotExistingFileTestClass();

        catchException(new DroolsInjector()).initDrools(testClass);

        assertThat((Exception)caughtException()).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("not found");
    }

    @Test
    public void should_throw_exception_for_dsl_file_not_found() throws Exception {
        NotExistingDslFileTestClass testClass = new NotExistingDslFileTestClass();

        catchException(new DroolsInjector()).initDrools(testClass);

        assertThat((Exception)caughtException()).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("not found");
    }

    @Test
    public void foo() throws Exception {
        RulesWithErrorsTestClass testClass = new RulesWithErrorsTestClass();

        catchException(new DroolsInjector()).initDrools(testClass);

        assertThat((Exception)caughtException()).isInstanceOf(IllegalStateException.class).hasMessageContaining("errors in DRL files");
    }

    @DroolsFiles(value = "foo.drl", location = "/")
    private static class NotExistingFileTestClass {

    }

    @DroolsFiles(value = "rules-with-errors.drl", location = "/drl/", dsl = "foo.dsl")
    private static class NotExistingDslFileTestClass {

    }

    @DroolsFiles(value = "rules-with-errors.drl", location = "/drl/")
    private static class RulesWithErrorsTestClass {

    }
}
