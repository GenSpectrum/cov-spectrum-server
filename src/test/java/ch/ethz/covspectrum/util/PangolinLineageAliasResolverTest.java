package ch.ethz.covspectrum.util;

import ch.ethz.covspectrum.entity.core.PangolinLineageAlias;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.springframework.test.util.AssertionErrors.assertEquals;


public class PangolinLineageAliasResolverTest {

    @Test
    void testFindAlias() {
        PangolinLineageAliasResolver resolver = new PangolinLineageAliasResolver(new ArrayList<>() {{
            add(new PangolinLineageAlias("C", "B.1.1.1"));
            add(new PangolinLineageAlias("D", "B.1.1.25"));
            add(new PangolinLineageAlias("G", "B.1.258.2"));
            add(new PangolinLineageAlias("K", "B.1.1.277"));
            add(new PangolinLineageAlias("L", "B.1.1.10"));
            add(new PangolinLineageAlias("M", "B.1.1.294"));
            add(new PangolinLineageAlias("N", "B.1.1.33"));
            add(new PangolinLineageAlias("P", "B.1.1.28"));
            add(new PangolinLineageAlias("Q", "B.1.1.7"));
            add(new PangolinLineageAlias("R", "B.1.1.316"));
            add(new PangolinLineageAlias("S", "B.1.1.217"));
            add(new PangolinLineageAlias("U", "B.1.177.60"));
            add(new PangolinLineageAlias("V", "B.1.177.54"));
            add(new PangolinLineageAlias("W", "B.1.177.53"));
            add(new PangolinLineageAlias("Y", "B.1.177.52"));
            add(new PangolinLineageAlias("Z", "B.1.177.50"));
            add(new PangolinLineageAlias("AA", "B.1.177.15"));
            add(new PangolinLineageAlias("AB", "B.1.160.16"));
            add(new PangolinLineageAlias("AC", "B.1.1.405"));
            add(new PangolinLineageAlias("AD", "B.1.1.315"));
            add(new PangolinLineageAlias("AE", "B.1.1.306"));
            add(new PangolinLineageAlias("AF", "B.1.1.305"));
            add(new PangolinLineageAlias("AG", "B.1.1.297"));
            add(new PangolinLineageAlias("AH", "B.1.1.241"));
            add(new PangolinLineageAlias("AJ", "B.1.1.240"));
            add(new PangolinLineageAlias("AK", "B.1.1.232"));
            add(new PangolinLineageAlias("AL", "B.1.1.231"));
            add(new PangolinLineageAlias("AM", "B.1.1.216"));
            add(new PangolinLineageAlias("AN", "B.1.1.200"));
            add(new PangolinLineageAlias("AP", "B.1.1.70"));
            add(new PangolinLineageAlias("AQ", "B.1.1.39"));
            add(new PangolinLineageAlias("AS", "B.1.1.317"));
            add(new PangolinLineageAlias("AT", "B.1.1.370"));
            add(new PangolinLineageAlias("AU", "B.1.466.2"));
            add(new PangolinLineageAlias("AV", "B.1.1.482"));
            add(new PangolinLineageAlias("AW", "B.1.1.464"));
            add(new PangolinLineageAlias("AY", "B.1.617.2"));
        }});
        compare(resolver, "B.1.1.1.2", new HashSet<>() {{
            add("C.2");
        }});
        compare(resolver, "B.1.1.1", new HashSet<>() {{
            add("C");
        }});
        compare(resolver, "B.1.1.1*", new HashSet<>() {{
            add("C.*");
        }});
        compare(resolver, "B.1.1.1.*", new HashSet<>() {{
            add("C.*");
        }});
        compare(resolver, "B.1.617*", new HashSet<>() {{
            add("AY.*");
        }});
        compare(resolver, "B.1.617.2.1", new HashSet<>() {{
            add("AY.1");
        }});
        compare(resolver, "B.1.617.2.1*", new HashSet<>() {{
            add("AY.1.*");
        }});
        compare(resolver, "B.1.1*", new HashSet<>() {{
            add("AC.*");
            add("D.*");
            add("AD.*");
            add("K.*");
            add("M.*");
            add("L.*");
            add("N.*");
            add("Q.*");
            add("P.*");
            add("S.*");
            add("R.*");
            add("AW.*");
            add("AV.*");
            add("AS.*");
            add("AT.*");
            add("AQ.*");
            add("AP.*");
            add("AM.*");
            add("AN.*");
            add("AK.*");
            add("AL.*");
            add("AJ.*");
            add("AG.*");
            add("AH.*");
            add("C.*");
            add("AE.*");
            add("AF.*");
        }});
        compare(resolver, "B.1.1.12*", new HashSet<>());
    }

    private void compare(PangolinLineageAliasResolver resolver, String testInput, Set<String> expected) {
        List<String> resolved = resolver.findAlias(testInput);
        assertEquals("Resolve " + testInput + " - correct content", expected, new HashSet<>(resolved));
        assertEquals("Resolve " + testInput + " - no duplicates", expected.size(), resolved.size());
    }

}
