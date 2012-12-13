package hudson.tasks.junit;

import org.hibernate.annotations.Index;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

/**
 * @author Kohsuke Kawaguchi
 */
@Entity(name="case")
public class PersistedCaseResult {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    @Column
    private int columnId;

    @Column
    public float duration;
    /**
     * In JUnit, a test is a method of a class. This field holds the fully qualified class name
     * that the test was in.
     */
    @Column
    @Index(name="index_className")
    public String className;
    /**
     * This field retains the method name.
     */
    @Column
    @Index(name="index_testName")
    public String testName;
    @Column
    public boolean skipped;
    @Column
    public String errorStackTrace;
    @Column
    public String errorDetails;
    @ManyToOne @JoinColumn
    public PersistedSuiteResult parent;

    @Column
    @Lob
    public String stdout,stderr;

    @Column
    public int failedSince;

    public static PersistedCaseResult from(CaseResult cr) {
        PersistedCaseResult r = new PersistedCaseResult();
        r.duration = cr.getDuration();
        r.className = cr.getClassName();
        r.testName = cr.getName();
        r.skipped = cr.isSkipped();
        r.errorStackTrace = cr.getErrorStackTrace();
        r.errorDetails = cr.getErrorDetails();
        // stdout stderr

        return r;
    }
}
