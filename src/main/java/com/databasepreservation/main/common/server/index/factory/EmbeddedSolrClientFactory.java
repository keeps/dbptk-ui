package com.databasepreservation.main.common.server.index.factory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.io.FileUtils;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.common.SolrException;
import org.apache.solr.core.CoreContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.databasepreservation.main.common.server.ViewerConfiguration;
import com.databasepreservation.main.common.server.index.utils.SolrUtils;

public class EmbeddedSolrClientFactory extends SolrClientFactory<EmbeddedSolrServer> {
    private static final Logger LOGGER = LoggerFactory.getLogger(EmbeddedSolrClientFactory.class);

    private final Path solrHome;

    protected EmbeddedSolrClientFactory() {
        this.solrHome = ViewerConfiguration.getInstance().getIndexPath();
        try {
            Files.createDirectories(this.solrHome);
        } catch (IOException e) {
            LOGGER.error("Could not create solr home at " + solrHome, e);
        }
    }

    protected EmbeddedSolrServer configureSolrClient() {
        LOGGER.info("Instantiating SOLR Embedded at {}", solrHome);

        System.setProperty("solr.data.dir", solrHome.toString());

        // create empty solr.xml
        try {
            Files.write(solrHome.resolve("solr.xml"), "<solr></solr>".getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            LOGGER.error("Could not create solr.xml under the SOLR Home", e);
        }

        return new EmbeddedSolrServer(solrHome, "test");
    }

    @Override
    protected Collection<String> getCollectionList() {
        return getSolrClient().getCoreContainer().getAllCoreNames();
    }

    @Override
    protected void waitForSolrToInitialize() {
        CoreContainer cc = getSolrClient().getCoreContainer();

        boolean allLoaded = true;
        do {
            for (String core : cc.getAllCoreNames()) {
                boolean loaded = cc.isLoaded(core);
                LOGGER.debug("Collection {} is loaded={}", core, loaded);
                allLoaded &= loaded;
            }
            if (!allLoaded) {
                LOGGER.info("Not all cores are loaded, trying again in 2s...");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    // do nothing
                }
            }
        } while (!allLoaded);
    }

    @Override
    protected boolean createCollection(String collection, Path configPath) {
        boolean ret;
        try {
            LOGGER.info("Creating SOLR collection {}", collection);

            Path collectionPath = solrHome.resolve(collection);
            FileUtils.copyDirectory(configPath.toFile(), collectionPath.resolve(SolrUtils.CONF).toFile());

            // Add core

            CoreContainer coreContainer = getSolrClient().getCoreContainer();
            coreContainer.create(collection, Collections.singletonMap("name", collection));

            LOGGER.info("SOLR collection {} is loaded=", collection, coreContainer.isLoaded(collection));

            ret = true;
        } catch (SolrException | IOException e) {
            LOGGER.error("Error creating collection {}", collection, e);
            ret = false;
        }
        return ret;
    }

}
