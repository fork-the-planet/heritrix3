package org.archive.modules.deciderules;


import java.util.ArrayList;
import java.util.List;

import org.archive.url.URIException;
import org.archive.modules.CrawlURI;
import org.archive.modules.extractor.LinkContext;
import org.archive.net.UURI;
import org.archive.net.UURIFactory;
import org.archive.state.ModuleTestBase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ViaSurtPrefixedDecideRuleTest extends ModuleTestBase {

    @Test
    public void testNoVia() throws Exception {
        ViaSurtPrefixedDecideRule dr = new ViaSurtPrefixedDecideRule();
        List<String> surtPrefixes = new ArrayList<String>();
        surtPrefixes.add("http://(org,archive,");
        dr.setSurtPrefixes(surtPrefixes);
        CrawlURI testUri = createTestUri("http://example.com");
        
        assertFalse(dr.evaluate(testUri));
    }
    @Test
    public void testNoSurts() throws Exception {
        ViaSurtPrefixedDecideRule dr = new ViaSurtPrefixedDecideRule();
        List<String> surtPrefixes = new ArrayList<String>();
        dr.setSurtPrefixes(surtPrefixes);
        CrawlURI testUri = createTestUri("http://example.com");

        assertFalse(dr.evaluate(testUri));
    }
    @Test
    public void testNullSurts() throws Exception {
        ViaSurtPrefixedDecideRule dr = new ViaSurtPrefixedDecideRule();
        dr.setSurtPrefixes(null);
        CrawlURI testUri = createTestUri("http://example.com");

        assertFalse(dr.evaluate(testUri));
    }
    @Test
    public void testPositiveSingleSurt() throws Exception {
        ViaSurtPrefixedDecideRule dr = new ViaSurtPrefixedDecideRule();
        List<String> surtPrefixes = new ArrayList<String>();
        surtPrefixes.add("http://(org,archive,");
        dr.setSurtPrefixes(surtPrefixes);
        CrawlURI testUri = createTestUri("http://example.com","http://archive.org");

        assertTrue(dr.evaluate(testUri));
    }
    @Test
    public void testNegativeSingleSurt() throws Exception {
        ViaSurtPrefixedDecideRule dr = new ViaSurtPrefixedDecideRule();
        List<String> surtPrefixes = new ArrayList<String>();
        surtPrefixes.add("http://(org,archive,");
        dr.setSurtPrefixes(surtPrefixes);
        CrawlURI testUri = createTestUri("http://example.com","http://google.com");

        assertFalse(dr.evaluate(testUri));
    }
    @Test
    public void testPositiveMultipleSurts() throws Exception {
        ViaSurtPrefixedDecideRule dr = new ViaSurtPrefixedDecideRule();
        List<String> surtPrefixes = new ArrayList<String>();
        surtPrefixes.add("http://(org,archive,");
        surtPrefixes.add("http://(com,test,");
        surtPrefixes.add("http://(com,google,");
        dr.setSurtPrefixes(surtPrefixes);
        CrawlURI testUri = createTestUri("http://example.com","http://google.com");

        assertTrue(dr.evaluate(testUri));
    }
    @Test
    public void testPositiveMultipleSurts2() throws Exception {
        ViaSurtPrefixedDecideRule dr = new ViaSurtPrefixedDecideRule();
        List<String> surtPrefixes = new ArrayList<String>();
        surtPrefixes.add("http://(org,archive,");
        surtPrefixes.add("http://(com,google,");
        surtPrefixes.add("http://(com,test,");
        dr.setSurtPrefixes(surtPrefixes);
        CrawlURI testUri = createTestUri("http://example.com","http://google.com");

        assertTrue(dr.evaluate(testUri));
    }
    @Test
    public void testNegativeMultipleSurts() throws Exception {
        ViaSurtPrefixedDecideRule dr = new ViaSurtPrefixedDecideRule();
        List<String> surtPrefixes = new ArrayList<String>();
        surtPrefixes.add("http://(org,archive,");
        surtPrefixes.add("http://(com,test,");
        surtPrefixes.add("http://(com,google,");
        dr.setSurtPrefixes(surtPrefixes);
        CrawlURI testUri = createTestUri("http://example.com","http://negativeexample.com");

        assertFalse(dr.evaluate(testUri));
    }
    
    
    private CrawlURI createTestUri(String urlStr) throws URIException {
        UURI testUuri = UURIFactory.getInstance(urlStr);
        CrawlURI testUri = new CrawlURI(testUuri, null, null, LinkContext.NAVLINK_MISC);

        return testUri;
    }
    private CrawlURI createTestUri(String urlStr, String via) throws URIException {
        
        UURI testViaUuri = UURIFactory.getInstance(via);
        CrawlURI testUri = createTestUri(urlStr);
        testUri.setVia(testViaUuri);

        return testUri;
    }
}
