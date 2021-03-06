package com.shopizer.utils;



//import org.elasticsearch.client.Client;
//import org.elasticsearch.client.transport.TransportClient;
//import org.elasticsearch.common.settings.ImmutableSettings;
//import org.elasticsearch.common.settings.Settings;
//import org.elasticsearch.common.transport.InetSocketTransportAddress;
//import org.elasticsearch.node.Node;

import static org.elasticsearch.node.NodeBuilder.*;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.node.Node;



/**
 * Singleton
 * @author Carl Samson
 *
 */
public class SearchClient {
	
		
	private Client client = null;
	private Node node = null;
	private boolean init = false;
	ServerConfiguration serverConfiguration = null;
	


	public ServerConfiguration getServerConfiguration() {
		return serverConfiguration;
	}

	public void setServerConfiguration(ServerConfiguration serverConfiguration) {
		this.serverConfiguration = serverConfiguration;
	}

	public Client getClient() {
		if(!init) {
			initClient();
		}
		return client;
	}

	public SearchClient() {
		
		
	}

	
	
	public void stopClient() {
		
		if(node!=null) {
			node.close();
		}
	}
	
	private synchronized void initClient() {
		

			
			if(client==null) {
				
				try {
					/**
					Config config = Config.getInstance();
					Configuration configuration = config.getConfiguration();
					
					
					
					if(configuration.getString("search.client.mode")!=null && configuration.getString("search.client.mode").equalsIgnoreCase("remote")) {
						//remote
						Settings s = ImmutableSettings.settingsBuilder().put("cluster.name", configuration.getString("search.client.cluster.name")).build();
						client = new TransportClient(s).addTransportAddress(new InetSocketTransportAddress(configuration.getString("search.client.remote.cluster.host","localhost"), configuration.getInt("search.client.remote.cluster.port",9300)));
	
					} else {
					
						//local (does not set cluster name ...)
						//nodeBuilder().settings().put(s);
						Node node = nodeBuilder().clusterName(configuration.getString("search.client.cluster.name")).local(true).node(); 
						client = node.client();
					}
					**/
					//ServerConfiguration config = (ServerConfiguration)BeanUtil.getBean("serverConfiguration");
					if(serverConfiguration.getMode().equalsIgnoreCase("remote")) {
						Settings s = ImmutableSettings.settingsBuilder().put("cluster.name", serverConfiguration.getClusterName()).build();
						client = new TransportClient(s).addTransportAddress(new InetSocketTransportAddress(serverConfiguration.getClusterHost(), serverConfiguration.getClusterPort()));
					} else {
						
						 //Node node = nodeBuilder().node();
						 //client = node.client();

						Node node = nodeBuilder().clusterName(serverConfiguration.getClusterName()).local(true).node(); 
						//Node node = nodeBuilder().local(false).node();
						//Node node = nodeBuilder().clusterName(serverConfiguration.getClusterName()).client(true).node();
						client = node.client();
					}
					
				} catch (Exception e) {
					e.printStackTrace();
				}

			}

	}
	


}
