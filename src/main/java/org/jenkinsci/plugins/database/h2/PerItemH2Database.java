package org.jenkinsci.plugins.database.h2;

import hudson.Extension;
import hudson.init.InitMilestone;
import hudson.init.Initializer;
import hudson.model.TopLevelItem;
import java.sql.SQLException;
import java.util.Map;
import java.util.WeakHashMap;
import javax.sql.DataSource;

import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;
import org.h2.Driver;
import org.jenkinsci.plugins.database.BasicDataSource2;
import org.jenkinsci.plugins.database.PerItemDatabase;
import org.jenkinsci.plugins.database.PerItemDatabaseConfiguration;
import org.jenkinsci.plugins.database.PerItemDatabaseDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;

public class PerItemH2Database extends PerItemDatabase  {
    private final boolean autoServer;

    // XXX should also discard entry if ItemListener.onDeleted/Renamed
    private transient Map<TopLevelItem,DataSource> sources;

    @DataBoundConstructor public PerItemH2Database(boolean autoServer) {
        this.autoServer = autoServer;
    }

    @Override public DataSource getDataSource(TopLevelItem item) throws SQLException {
        if (sources == null) {
            sources = new WeakHashMap<TopLevelItem,DataSource>();
        }
        DataSource source = sources.get(item);
        if (source == null) {
            BasicDataSource2 fac = new BasicDataSource2();
            fac.setDriverClass(Driver.class);
            fac.setUrl(appendUrlParameters("jdbc:h2:" + item.getRootDir().toURI() + "data"));
            source = fac.createDataSource();
            sources.put(item, source); // XXX consider closing and discarding after some timeout
        }
        return source;
    }

    private String appendUrlParameters(String url) {
        if (getAutoServer()) {
            url += ";AUTO_SERVER=true";
        }
        return url;
    }

    public boolean getAutoServer() {
        return this.autoServer;
    }

    @Extension public static class DescriptorImpl extends PerItemDatabaseDescriptor {

        @Override public String getDisplayName() {
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
