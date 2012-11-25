import hudson.Util;
import org.jenkinsci.plugins.database.h2.LocalH2Database;
import org.junit.Test;

import javax.sql.DataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

/**
 * @author Kohsuke Kawaguchi
 */
public class FooTest {
    @Test
    public void testFoo() throws Exception {
        File dir = File.createTempFile("h2db", ".test");
        try {
            dir.delete();
            dir.mkdirs();
            DataSource ds = new LocalH2Database(dir).getDataSource();
            Connection con = ds.getConnection();
            con.createStatement().execute("create table FOO (a int, b int )");
            con.createStatement().execute("insert into FOO values (1,2)");
            ResultSet r = con.createStatement().executeQuery("select b from FOO where a=1");
            r.next();
            assertThat(r.getInt(1), is(2));
            con.close();
        } finally {
            Util.deleteRecursive(dir);
        }
    }
}
