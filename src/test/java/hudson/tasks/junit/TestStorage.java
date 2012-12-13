package hudson.tasks.junit;

import com.thoughtworks.xstream.XStream;
import hudson.util.HeapSpaceStringConverter;
import hudson.util.XStream2;
import org.h2.Driver;
import org.h2.tools.Console;
import org.jenkinsci.plugins.database.BasicDataSource2;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceProvider;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.ServiceLoader;

/**
 * @author Kohsuke Kawaguchi
 */
public class TestStorage {
    public static void main(String[] args) throws Exception {

        BasicDataSource2 ds = new BasicDataSource2();
        ds.setDriverClass(Driver.class);
        String url = "jdbc:h2:/tank/h2-testdata/fill";
        ds.setUrl(url);

        EntityManagerFactory emf = createEntityManagerFactory(ds,
                PersistedTestResult.class,PersistedSuiteResult.class,PersistedCaseResult.class);

        populate(emf);

        Console.main(new String[]{"-url",url});
    }

    private static void populate(EntityManagerFactory emf) throws FileNotFoundException {
        final XStream XSTREAM = createXStream();

        TestResult tr = (TestResult)XSTREAM.fromXML(new FileInputStream("src/test/resources/junitResult.xml"));
        for (int i=1000; i<2000; i++) {
            EntityManager em = emf.createEntityManager();
            em.getTransaction().begin();

            PersistedTestResult ptr = new PersistedTestResult();
            ptr.buildNumber = i;
            for (SuiteResult sr : tr.getSuites()) {
                PersistedSuiteResult psr = PersistedSuiteResult.from(sr);
                for (PersistedCaseResult cr : psr.cases)
                    em.persist(cr);
                em.persist(psr);
                ptr.suites.add(psr);
            }
            em.persist(ptr);

            em.getTransaction().commit();
            System.out.print('.');
        }
    }

    /**
     * For reading back junitResult.xml
     */
    private static XStream createXStream() {
        final XStream XSTREAM = new XStream2();
        XSTREAM.alias("result",TestResult.class);
        XSTREAM.alias("suite",SuiteResult.class);
        XSTREAM.alias("case",CaseResult.class);
        XSTREAM.registerConverter(new HeapSpaceStringConverter(),100);
        return XSTREAM;
    }

    private static EntityManagerFactory createEntityManagerFactory(BasicDataSource2 ds, Class... classes) {
        EntityManagerFactory emf=null;
        for (PersistenceProvider pp : ServiceLoader.load(PersistenceProvider.class)) {
            emf = pp.createContainerEntityManagerFactory(new PersistenceProviderImpl(ds,
                    Arrays.asList(classes), TestStorage.class.getClassLoader()), null);
            break;
        }
        return emf;
    }
}
