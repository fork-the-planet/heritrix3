/*
 *  This file is part of the Heritrix web crawler (crawler.archive.org).
 *
 *  Licensed to the Internet Archive (IA) by one or more individual 
 *  contributors. 
 *
 *  The IA licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.archive.modules.extractor;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.archive.url.URIException;
import org.archive.modules.CrawlURI;
import org.archive.net.UURI;
import org.archive.net.UURIFactory;


/**
 * An extractor for finding 'implied' URIs inside other URIs.  If the 
 * 'trigger' regex is matched, a new URI will be constructed from the
 * 'build' replacement pattern. 
 * 
 * Unlike most other extractors, this works on URIs discovered by 
 * previous extractors. Thus it should appear near the end of any 
 * set of extractors.
 *
 * Initially, only finds absolute HTTP(S) URIs in query-string or its 
 * parameters.
 *
 * TODO: extend to find URIs in path-info
 *
 * @author Gordon Mohr
 *
 **/

public class ExtractorImpliedURI extends Extractor {

    @SuppressWarnings("unused")
    private static final long serialVersionUID = 3L;

    private static Logger LOGGER =
        Logger.getLogger(ExtractorImpliedURI.class.getName());
   
    
    {
        setRegex(Pattern.compile("^(.*)$"));
    }
    public Pattern getRegex() {
        return (Pattern) kp.get("regex");
    }
    /**
     * Triggering regular expression. When a discovered URI matches this
     * pattern, the 'implied' URI will be built. The capturing groups of this
     * expression are available for the build replacement pattern.
     */
    public void setRegex(Pattern regex) {
        kp.put("regex",regex);
    }

    
    {
        setFormat("");
    }
    public String getFormat() {
        return (String) kp.get("format");
    }
    /**
     * Replacement pattern to build 'implied' URI, using captured groups of
     * trigger expression.
     */
    public void setFormat(String format) {
        kp.put("format",format);
    }

    {
        setRemoveTriggerUris(false);
    }
    public boolean getRemoveTriggerUris() {
        return (Boolean) kp.get("removeTriggerUris");
    }
    /**
     * If true, all URIs that match trigger regular expression are removed
     * from the list of extracted URIs. Default is false.
     */
    public void setRemoveTriggerUris(boolean remove) {
        kp.put("removeTriggerUris",remove);
    }
 
    /**
     * Constructor.
     */
    public ExtractorImpliedURI() {
    }

    
    @Override
    protected boolean shouldProcess(CrawlURI uri) {
        return true;
    }

    /**
     * Perform usual extraction on a CrawlURI
     * 
     * @param curi Crawl URI to process.
     */
    @Override
    public void extract(CrawlURI curi) {
        List<CrawlURI> links = new ArrayList<CrawlURI>(curi.getOutLinks());
        int max = links.size();
        for (int i = 0; i < max; i++) {
        	CrawlURI link = links.get(i);
            Pattern trigger = getRegex();
            String build = getFormat();
            CharSequence dest = link.getUURI();
            String implied = extractImplied(dest, trigger, build);
            if (implied != null) {
                try {
                    UURI target = UURIFactory.getInstance(implied);
                    LinkContext lc = LinkContext.INFERRED_MISC;
                    Hop hop = Hop.INFERRED;
                    addOutlink(curi, target, lc, hop);
                    numberOfLinksExtracted.incrementAndGet();

                    boolean removeTriggerURI = getRemoveTriggerUris();
                    // remove trigger URI from the outlinks if configured so.
                    if (removeTriggerURI) {
                       if (curi.getOutLinks().remove(link)) {
                               LOGGER.log(Level.FINE, link.getURI() + 
                                     " has been removed from " + 
                                     curi.getURI() + " outlinks list.");
                               numberOfLinksExtracted.decrementAndGet();
                       } else {
                               LOGGER.log(Level.FINE, "Failed to remove " + 
                                      link.getURI() + " from " + 
                                      curi.getURI() + " outlinks list.");
                       }
                    }
                } catch (URIException e) {
                    LOGGER.log(Level.FINE, "bad URI", e);
                }
            }
        }
    }
    
    /**
     * Utility method for extracting 'implied' URI given a source uri, 
     * trigger pattern, and build pattern. 
     * 
     * @param uri source to check for implied URI
     * @param trigger regex pattern which if matched implies another URI
     * @param build replacement pattern to build the implied URI
     * @return implied URI, or null if none
     */
    protected static String extractImplied(CharSequence uri, Pattern trigger, String build) {
        if (trigger == null) {
            return null;
        }
        Matcher m = trigger.matcher(uri);
        if(m.matches()) {
            String result = m.replaceFirst(build);
            return result; 
        }
        return null; 
    }
}
