package org.jenkinsci.plugins.database.h2;

import hudson.model.FreeStyleProject;
import org.jenkinsci.plugins.database.PerItemDatabaseConfiguration;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@WithJenkins
class PerItemH2DatabaseTest {

    @Test
    void basics(JenkinsRule j) throws Exception {
        FreeStyleProject p = j.jenkins.createProject(FreeStyleProject.class, "somejob");
        DataSource ds = PerItemDatabaseConfiguration.find().getDataSource(p);

        try (Connection con = ds.getConnection();
             Statement st = con.createStatement()) {

            st.execute("create table FOO (a int, b int )");
            st.execute("insert into FOO values (1,2)");

            try (ResultSet r = st.executeQuery("select b from FOO where a=1")) {
                r.next();
                assertThat(r.getInt(1), is(2));
            }
        }
    }

}
