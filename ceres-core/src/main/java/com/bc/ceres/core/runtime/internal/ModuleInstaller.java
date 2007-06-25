package com.bc.ceres.core.runtime.internal;

import com.bc.ceres.core.Assert;
import com.bc.ceres.core.CoreException;
import com.bc.ceres.core.ProgressMonitor;
import com.bc.ceres.core.SubProgressMonitor;
import com.bc.ceres.core.runtime.ProxyConfig;
import com.bc.ceres.core.runtime.ModuleState;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.text.MessageFormat;
import java.util.logging.Logger;

/**
 * An uninstaller for modules.
 */
public class ModuleInstaller {

    public static final String INSTALL_INFO_XML = "install-info.xml";

    private Logger logger;

    public ModuleInstaller(Logger logger) {
        Assert.notNull(logger, "logger");
        this.logger = logger;
    }

    public ModuleImpl installModule(URL url, ProxyConfig proxyConfig, File modulesDir, ProgressMonitor pm) throws CoreException {
        Assert.notNull(url, "url");
        Assert.notNull(proxyConfig, "proxyConfig");
        Assert.notNull(modulesDir, "modulesDir");
        Assert.notNull(pm, "pm");

        pm.beginTask("Installing module", 100);

        logger.info(MessageFormat.format("Installing [{0}] in [{1}]...", url, modulesDir));

        try {
            String fileName = FileHelper.getFileName(url);
            File tempFile = new File(modulesDir, fileName + ".incomplete");
            File targetFile = new File(modulesDir, fileName);

            try {
                logger.info(MessageFormat.format("Downloading [{0}] to [{1}]...", url, tempFile.getName()));
                pm.setSubTaskName(MessageFormat.format("Downloading [{0}]", fileName));
                URLConnection urlConnection = proxyConfig.openConnection(url);
                FileHelper.copy(urlConnection, tempFile, new SubProgressMonitor(pm, 90));

                logger.info(MessageFormat.format("Copying [{0}] to [{1}]...", tempFile, fileName));
                pm.setSubTaskName(MessageFormat.format("Copying [{0}]", fileName));
                FileHelper.copy(tempFile, targetFile, new SubProgressMonitor(pm, 10));
            } finally {
                if (!tempFile.delete()) {
                    logger.warning(MessageFormat.format("Failed to delete file [{0}], reason unknown.", tempFile));
                }
            }

            ModuleReader moduleReader = new ModuleReader(logger);
            ModuleImpl module = moduleReader.readFromLocation(targetFile);
            module.setState(ModuleState.INSTALLED);
            return module;
        } catch (CoreException e) {
            e.printStackTrace();
            throw e;
        } catch (IOException e) {
            e.printStackTrace();
            throw new CoreException("Failed to install module [" + url + "]: " + e.getMessage(), e);
        } finally {
            pm.done();
        }
    }
}