package ut.com.isroot.stash.plugin;

import com.isroot.stash.plugin.YaccPerson;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Sean Ford
 * @since 2017-01-25
 */
public class YaccPersonTest {
    @Test
    public void testConstructor_parseNormalIdent() {
        YaccPerson yaccPerson = new YaccPerson("First Last <email@address.com>");

        assertThat(yaccPerson.getName()).isEqualTo("First Last");
        assertThat(yaccPerson.getEmailAddress()).isEqualTo("email@address.com");
    }

    @Test
    public void testConstructor_noEmail() {
        YaccPerson yaccPerson = new YaccPerson("First Last <>");

        assertThat(yaccPerson.getName()).isEqualTo("First Last");
        assertThat(yaccPerson.getEmailAddress()).isEqualTo("");
    }

    @Test
    public void testConstructor_noName() {
        YaccPerson yaccPerson = new YaccPerson("<email@address.com>");

        assertThat(yaccPerson.getName()).isEqualTo("");
        assertThat(yaccPerson.getEmailAddress()).isEqualTo("email@address.com");
    }

    @Test
    public void testConstructor_singleName() {
        YaccPerson yaccPerson = new YaccPerson("username <email@address.com>");

        assertThat(yaccPerson.getName()).isEqualTo("username");
        assertThat(yaccPerson.getEmailAddress()).isEqualTo("email@address.com");
    }
}
