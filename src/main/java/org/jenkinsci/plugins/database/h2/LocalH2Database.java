package org.jenkinsci.plugins.database.h2;

import hudson.Extension;
import hudson.init.InitMilestone;
import hudson.init.Initializer;
import hudson.util.FormValidation;
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;
import org.h2.Driver;
import org.jenkinsci.plugins.database.BasicDataSource2;
import org.jenkinsci.plugins.database.Database;
import org.jenkinsci.plugins.database.DatabaseDescriptor;
import org.jenkinsci.plugins.database.GlobalDatabaseConfiguration;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.sql.DataSource;
import java.io.File;
import java.sql.SQLException;

/**
 * Embedded (aka local) H2 database.
 * <p/>
 * This stores database in a local file.
 *
 * @author Kohsuke Kawaguchi
 */
public class LocalH2Database extends Database {
    private final File path;
    private final boolean autoServer;

    private transient DataSource source;

    @DataBoundConstructor
    public LocalH2Database(File path, boolean autoServer) {
        this.path = path;
        this.autoServer = autoServer;
    }

    public File getPath() {
        return path;
    }

    @Override
    public synchronized DataSource getDataSource() throws SQLException {
        if (source == null) {
            BasicDataSource2 fac = new BasicDataSource2();
            fac.setDriverClass(Driver.class);
            // because different database in the same folder doesn't share anything, there's no point in confusing
            // the users by asking two things (path+database) when one (path) is suffice.
            // http://www.h2database.com/html/faq.html#database_files
            String pathU = path.toURI().toString();
            String url = "jdbc:h2:" + pathU + (pathU.endsWith("/") ? "" : "/") + "data";
            if (getAutoServer()) {
                url += ";AUTO_SERVER=true";
            }
            fac.setUrl(url);
            source = fac.createDataSource();
        }
        return source;
    }

    public boolean getAutoServer() {
        return this.autoServer;
    }

    @Extension
    public static class DescriptorImpl extends DatabaseDescriptor {
        @Override
        public String getDisplayName() {
            return "Embedded local database (H2)";
        }

        public FormValidation doCheckPath(@QueryParameter String value) {
            Jenkins.getInstance().checkPermission(Jenkins.ADMINISTER);

            if (value.length() == 0)
                return FormValidation.ok(); // no value entered yet

            if (new File(value, "data.h2.db").exists())
                return FormValidation.ok("This database already exists.");
            else if (new File(value).isFile())
                return FormValidation.error("%s is a file; must be a directory.", value);
            else
                return FormValidation.ok("This database doesn't exist yet. It will be created.");
        }
    }

    @Initializer(after = InitMilestone.PLUGINS_STARTED)
    public static void setDefaultGlobalDatabase() {
        Jenkins j = Jenkins.getInstance();
        GlobalDatabaseConfiguration gdc = j.getExtensionList(GlobalConfiguration.class).get(GlobalDatabaseConfiguration.class);
        if (gdc != null) {// being defensive
            if (gdc.getDatabase() == null)
                gdc.setDatabase(new LocalH2Database(new File(j.getRootDir(), "global"), false));
        }
    }
}
