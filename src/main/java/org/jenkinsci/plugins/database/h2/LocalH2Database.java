package org.jenkinsci.plugins.database.h2;

import hudson.Extension;
import hudson.util.FormValidation;
import jenkins.model.Jenkins;
import org.h2.Driver;
import org.jenkinsci.plugins.database.BasicDataSource2;
import org.jenkinsci.plugins.database.Database;
import org.jenkinsci.plugins.database.DatabaseDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.sql.DataSource;
import java.io.File;
import java.sql.SQLException;

/**
 * Embedded (aka local) H2 database.
 *
 * This stores database in a local file.
 *
 * @author Kohsuke Kawaguchi
 */
public class LocalH2Database extends Database {
    private final File path;

    private transient DataSource source;

    @DataBoundConstructor
    public LocalH2Database(File path) {
        this.path = path;
    }

    public File getPath() {
        return path;
    }

    @Override
    public synchronized DataSource getDataSource() throws SQLException {
        if (source==null) {
            BasicDataSource2 fac = new BasicDataSource2();
            fac.setDriverClass(Driver.class);
            // because different database in the same folder doesn't share anything, there's no point in confusing
            // the users by asking two things (path+database) when one (path) is suffice.
            fac.setUrl("jdbc:h2:file:"+path+"/data");
            source = fac.createDataSource();
        }
        return source;
    }

    @Extension
    public static class DescriptorImpl extends DatabaseDescriptor {
        @Override
        public String getDisplayName() {
            return "Embedded local database (H2)";
        }

        public FormValidation doCheckPath(@QueryParameter String value) {
            Jenkins.getInstance().checkPermission(Jenkins.ADMINISTER);

            if (value.length()==0)
                return FormValidation.ok(); // no value entered yet

            if (new File(value,"data.h2.db").exists())
                return FormValidation.ok("This database already exists.");
            else
                return FormValidation.ok("This database doesn't exist yet. It will be created.");
        }
    }
}
