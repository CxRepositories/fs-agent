import org.apache.rat.api.RatException;
import org.junit.Test;
import org.whitesource.fs.DependencyInfoFactory;

import javax.xml.transform.TransformerConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.Set;

/**
 * Test class for creadur-rat.
 *
 * @author tom.shapira
 */
public class DependencyInfoFactoryTest {

    @Test
    public void testRat() throws InterruptedException, RatException, TransformerConfigurationException, IOException {
        DependencyInfoFactory factory = new DependencyInfoFactory();
        Set<String> licenses = factory.scanLicenses(new File("C:\\WhiteSource\\Support\\Overture\\uip-clock-arch.c"));
//        Set<String> licenses = factory.scanLicenses(new File("C:\\WhiteSource\\Support\\File System Agent\\aes.h"));
        for (String license : licenses) {
            System.out.println(license);
        }
    }
}
