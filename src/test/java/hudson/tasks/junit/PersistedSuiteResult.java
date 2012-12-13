package hudson.tasks.junit;

import org.hibernate.annotations.Index;
import org.hibernate.annotations.Table;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Kohsuke Kawaguchi
 */
@Entity(name="suite")
public class PersistedSuiteResult {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    @Column
    private int columnId;

    @Column
    public String file;
    @Column @Index(name="index_name")
    public String name;
    @Column @Lob
    public String stdout;
    @Column @Lob
    public String stderr;
    @Column
    public float duration;

    /**
     * The 'timestamp' attribute of  the test suite.
     * AFAICT, this is not a required attribute in XML, so the value may be null.
     */
    @Column
    public String timestamp;

    /** Optional ID attribute of a test suite. E.g., Eclipse plug-ins tests always have the name 'tests' but a different id. **/
    @Column
    public String id;

    @ManyToOne
    @JoinColumn
    public PersistedTestResult parent;

    @OneToMany(mappedBy="parent")
    public List<PersistedCaseResult> cases = new ArrayList<PersistedCaseResult>();

    public static PersistedSuiteResult from(SuiteResult sr) {
        PersistedSuiteResult r = new PersistedSuiteResult();
        r.file = sr.getFile();
        r.name = sr.getName();
        r.stdout = sr.getStdout();
        r.stderr = sr.getStderr();
        r.duration = sr.getDuration();
        r.timestamp = sr.getTimestamp();
        r.id = sr.getId();

        for (CaseResult cr : sr.getCases()) {
            PersistedCaseResult pcr = PersistedCaseResult.from(cr);
            pcr.parent = r;
            r.cases.add(pcr);
        }

        return r;
    }
}
