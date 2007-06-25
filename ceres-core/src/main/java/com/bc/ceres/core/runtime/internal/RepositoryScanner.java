package com.bc.ceres.core.runtime.internal;

import com.bc.ceres.core.Assert;
import com.bc.ceres.core.ProgressMonitor;
import com.bc.ceres.core.SubProgressMonitor;
import com.bc.ceres.core.CoreException;
import com.bc.ceres.core.CanceledException;
import com.bc.ceres.core.runtime.Constants;
import com.bc.ceres.core.runtime.Module;
import com.bc.ceres.core.runtime.ProxyConfig;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class RepositoryScanner {

    private Logger logger;
    private URL url;
    private ProxyConfig proxyConfig;

    public RepositoryScanner(Logger logger, URL url, ProxyConfig proxyConfig) {
        Assert.notNull(logger, "logger");
        Assert.notNull(url, "url");
        Assert.notNull(proxyConfig, "proxyConfig");
        this.logger = logger;
        this.url = url;
        this.proxyConfig = proxyConfig;
    }

    public Module[] scan(ProgressMonitor progressMonitor) throws CoreException {
        try {
            Assert.notNull(progressMonitor, "progressMonitor");
            return scanImpl(progressMonitor);
        } catch (IOException e) {
            throw new CoreException(e);
        }
    }

    private Module[] scanImpl(ProgressMonitor progressMonitor) throws IOException, CanceledException {
        List<Module> repositoryModules = new ArrayList<Module>(64);
        progressMonitor.beginTask("Retrieving module information from repository", 100);
        progressMonitor.setSubTaskName("Connecting...");
        final URLConnection urlConnection = proxyConfig != null ? proxyConfig.openConnection(url) : url.openConnection();
        progressMonitor.worked(10);
        final InputStream inputStream = urlConnection.getInputStream();
        final InputStreamReader reader = new InputStreamReader(inputStream);
        try {
            String[] hrefs;
            progressMonitor.setSubTaskName("Scanning...");
            if ("text/plain".equals(urlConnection.getContentType())) {
                hrefs = parseTextPlain(reader);
            } else {
                hrefs = parseTextHtml(reader);
            }
            progressMonitor.worked(10);
            if (hrefs.length > 0) {
                collectModules(hrefs, repositoryModules, new SubProgressMonitor(progressMonitor, 80));
            } else {
                progressMonitor.worked(80);
            }
        } finally {
            reader.close();
            progressMonitor.done();
        }
        return repositoryModules.toArray(new Module[repositoryModules.size()]);
    }

    private void collectModules(String[] hrefs, List<Module> repositoryModules, ProgressMonitor pm) throws CanceledException {
        pm.beginTask("Loading modules...", hrefs.length);
        try {
            for (String href : hrefs) {
                if (pm.isCanceled()) {
                    throw new CanceledException();
                }
                collectModule(href, repositoryModules);
                pm.worked(1);
            }
        } finally {
            pm.done();
        }
    }

    private void collectModule(String href, List<Module> repositoryModules) {
        if (href.startsWith("http:")) {
            return;
        }
        String moduleName = href;
        if (moduleName.startsWith("/")) {
            moduleName = moduleName.substring(1);
        }
        if (moduleName.endsWith("/")) {
            moduleName = moduleName.substring(0, moduleName.length() - 1);
        }

        String urlString = url.toExternalForm();
        if (!urlString.endsWith("/")) {
            urlString += "/";
        }
        String urlBase = urlString + moduleName + '/';
        final ModuleImpl module;
        try {
            URL manifestUrl = new URL(urlBase + Constants.MODULE_MANIFEST_NAME );
            module = new ModuleReader(logger).readFromManifest(manifestUrl);
        } catch (CoreException e) {
            logger.warning(String.format("Repository entry [%s] is invalid: %s", moduleName, e.getMessage()));
            return;
        } catch (MalformedURLException e) {
            logger.warning(String.format("Repository entry [%s] is invalid: %s", moduleName, e.getMessage()));
            return;
        }

        Module repositoryModule = createRepositoryModule(module, urlBase, moduleName + ".jar");
        if (repositoryModule == null) {
            repositoryModule = createRepositoryModule(module, urlBase, moduleName + ".zip");
        }
        if (repositoryModule == null) {
            logger.warning(String.format("Repository entry [%s] is invalid, no archive found.", moduleName));
            return;
        }

        logger.info(String.format("Repository entry [%s] found.", moduleName));
        repositoryModules.add(repositoryModule);
    }

    private static Module createRepositoryModule(ModuleImpl module, String urlBase, String archiveName) {
        URL archiveUrl;
        URLConnection urlConnection;
        try {
            archiveUrl = new URL(urlBase + archiveName);
            urlConnection = archiveUrl.openConnection();
            urlConnection.connect();
        } catch (IOException e) {
            return null;
        }
        int contentLength = urlConnection.getContentLength();
        long lastModified = urlConnection.getLastModified();

        URL aboutUrl = null;
        InputStream inputStream = null;
        try {
            aboutUrl = new URL(urlBase + "about.html");
            inputStream = aboutUrl.openStream();
        } catch (IOException e) {
            aboutUrl = null;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }

        module.setLocation(archiveUrl);
        module.setContentLength(contentLength);
        module.setLastModified(lastModified);
        module.setAboutUrl(aboutUrl != null ? aboutUrl.toExternalForm() : null);
        return module;
    }

    private static String[] parseTextHtml(InputStreamReader reader) throws IOException {
        return new HrefParser(reader).parse();
    }

    private static String[] parseTextPlain(InputStreamReader reader) throws IOException {
        StreamTokenizer st = new StreamTokenizer(reader);
        st.resetSyntax();
        st.whitespaceChars(0, 32);
        st.wordChars(33, 255);
        st.eolIsSignificant(false);
        ArrayList<String> list = new ArrayList<String>(32);
        while (true) {
            int type = st.nextToken();
            if (type == StreamTokenizer.TT_EOF) {
                break;
            }
            list.add(st.sval);
        }
        return list.toArray(new String[list.size()]);
    }
}