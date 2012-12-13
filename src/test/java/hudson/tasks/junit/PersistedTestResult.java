package hudson.tasks.junit;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Kohsuke Kawaguchi
 */
@Entity(name="testResult")
public class PersistedTestResult {
    @Id
    @Column
    public int buildNumber;

    @OneToMany(mappedBy="parent")
    public List<PersistedSuiteResult> suites = new ArrayList<PersistedSuiteResult>();


}
