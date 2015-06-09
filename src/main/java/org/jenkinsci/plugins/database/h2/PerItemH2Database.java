package org.jenkinsci.plugins.database.h2;

import hudson.Extension;
import hudson.init.InitMilestone;
import hudson.init.Initializer;
import hudson.model.TopLevelItem;

import java.io.File;
import java.sql.SQLException;
import java.util.Map;
import java.util.WeakHashMap;
import javax.sql.DataSource;

import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;
import org.h2.Driver;
import org.jenkinsci.plugins.database.*;
import org.kohsuke.stapler.DataBoundConstructor;

public class PerItemH2Database extends PerItemDatabase {

    // XXX should also discard entry if ItemListener.onDeleted/Renamed
    private transient Map<TopLevelItem, DataSource> sources;

    private final boolean autoServer;

    @DataBoundConstructor
    public PerItemH2Database(boolean autoServer) {
        this.autoServer = autoServer;
    }

    public boolean getAutoServer() {
        return this.autoServer;
    }

    @Override
    public DataSource getDataSource(TopLevelItem item) throws SQLException {
        if (sources == null) {
            sources = new WeakHashMap<TopLevelItem, DataSource>();
        }
        DataSource source = sources.get(item);
        if (source == null) {
            BasicDataSource2 fac = new BasicDataSource2();
            fac.setDriverClass(Driver.class);
            String url = "jdbc:h2:" + item.getRootDir().toURI() + "data";
            if (this.autoServer) {
                url += ";AUTO_SERVER=true";
            }
            fac.setUrl(url);
            source = fac.createDataSource();
            sources.put(item, source); // XXX consider closing and discarding after some timeout
        }
        return source;
    }

    @Extension
    public static class DescriptorImpl extends PerItemDatabaseDescriptor {

        @Override
        public String getDisplayName() {
            return "Embedded local database (H2)";
        }

    }

    @Initializer(after = InitMilestone.PLUGINS_STARTED)
    public static void setDefaultPerItemDatabase() {
        Jenkins j = Jenkins.getInstance();
        PerItemDatabaseConfiguration pidbc = j.getExtensionList(GlobalConfiguration.class).get(PerItemDatabaseConfiguration.class);
        if (pidbc != null) {// being defensive
            if (pidbc.getDatabase() == null)
                pidbc.setDatabase(new PerItemH2Database(false));
        }
    }

}
