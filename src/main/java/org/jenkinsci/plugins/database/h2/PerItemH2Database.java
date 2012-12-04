package org.jenkinsci.plugins.database.h2;

import hudson.Extension;
import hudson.model.TopLevelItem;
import java.sql.SQLException;
import java.util.Map;
import java.util.WeakHashMap;
import javax.sql.DataSource;
import org.h2.Driver;
import org.jenkinsci.plugins.database.BasicDataSource2;
import org.jenkinsci.plugins.database.PerItemDatabase;
import org.jenkinsci.plugins.database.PerItemDatabaseDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;

public class PerItemH2Database extends PerItemDatabase  {
    
    private transient Map<TopLevelItem,DataSource> sources;

    @DataBoundConstructor public PerItemH2Database() {}

    @Override public DataSource getDataSource(TopLevelItem item) throws SQLException {
        if (sources == null) {
            sources = new WeakHashMap<TopLevelItem,DataSource>();
        }
        DataSource source = sources.get(item);
        if (source == null) {
            BasicDataSource2 fac = new BasicDataSource2();
            fac.setDriverClass(Driver.class);
            fac.setUrl("jdbc:h2:" + item.getRootDir().toURI() + "data");
            source = fac.createDataSource();
            sources.put(item, source); // XXX consider closing and discarding after some timeout
        }
        return source;
    }

    @Extension public static class DescriptorImpl extends PerItemDatabaseDescriptor {

        @Override public String getDisplayName() {
            return "Embedded local database (H2)";
        }

    }

}
