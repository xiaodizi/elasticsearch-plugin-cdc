package com.elasticsearch.cdc;


import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.io.stream.NamedWriteableRegistry;
import org.elasticsearch.common.settings.ClusterSettings;
import org.elasticsearch.common.settings.Setting;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.env.Environment;
import org.elasticsearch.env.NodeEnvironment;
import org.elasticsearch.index.IndexModule;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.repositories.RepositoriesService;
import org.elasticsearch.script.ScriptService;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.watcher.ResourceWatcherService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

public class CDCPlugin extends Plugin {

    List<Setting<?>> settings = new ArrayList<>();


    private final Setting<String> kafkaCdcNodes = Setting.simpleString(PluginSettings.KAFKA_CDC_NODES, "", Setting.Property.NodeScope, Setting.Property.Dynamic);

    private final Setting<String> kafkaCdcTopic = Setting.simpleString(PluginSettings.KAFKA_CDC_TOPIC, "", Setting.Property.NodeScope, Setting.Property.Dynamic);


    public CDCPlugin() {

        settings.add(kafkaCdcNodes);

        settings.add(kafkaCdcTopic);
    }

    @Override
    public List<Setting<?>> getSettings() {
        return settings;
    }

    @Override
    public void onIndexModule(IndexModule indexModule) {
        final CDCListener cdcListener = new CDCListener(indexModule);
        indexModule.addIndexOperationListener(cdcListener);
    }

    @Override
    public Collection<Object> createComponents(Client client, ClusterService clusterService, ThreadPool threadPool, ResourceWatcherService resourceWatcherService, ScriptService scriptService, NamedXContentRegistry xContentRegistry, Environment environment, NodeEnvironment nodeEnvironment, NamedWriteableRegistry namedWriteableRegistry, IndexNameExpressionResolver indexNameExpressionResolver, Supplier<RepositoriesService> repositoriesServiceSupplier) {
        return super.createComponents(client, clusterService, threadPool, resourceWatcherService, scriptService, xContentRegistry, environment, nodeEnvironment, namedWriteableRegistry, indexNameExpressionResolver, repositoriesServiceSupplier);
    }
}