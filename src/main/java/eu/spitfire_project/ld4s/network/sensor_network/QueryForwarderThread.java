package eu.spitfire_project.ld4s.network.sensor_network;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.ws4d.coap.connection.BasicCoapChannelManager;
import org.ws4d.coap.interfaces.CoapChannelManager;
import org.ws4d.coap.interfaces.CoapClient;
import org.ws4d.coap.interfaces.CoapClientChannel;
import org.ws4d.coap.interfaces.CoapRequest;
import org.ws4d.coap.interfaces.CoapResponse;
import org.ws4d.coap.messages.CoapRequestCode;

import eu.spitfire_project.ld4s.network.NetworkAddress;

public class QueryForwarderThread implements Runnable{
	
	private NetworkAddress netAddr = null;
		
	private CoapChannelManager channelManager = null;
	
	private CoapClient coapClient = null;
	
	private CoapRequest request = null;
	
	private CoapClientChannel channel = null;
	
	private String resourceToQueryPath = null;
	
	private CoapRequestCode code = null;
	
	private int port = -1;
	
	private int millisecs = -1;
	
	public QueryForwarderThread(CoapRequestCode code, String queryUri, NetworkAddress naddr, int port,
			int ms){
		this.code = code;
		this.resourceToQueryPath = queryUri;
		this.netAddr = naddr;
		this.port = port;
		this.millisecs = ms;
	}

	@Override
	public void run() {
		this.coapClient = this.createNewCoapClient();
		channelConnect();
		while(true) {
			try {
				this.sendRequest();
				Thread.currentThread();
				Thread.sleep(millisecs);  // to check for new values every X milliseconds
			} catch (InterruptedException e) {
				e.printStackTrace();
				throw new RuntimeException("Thread issues. " + e);
			}
		}
	}
	
	private void sendRequest(){
		this.request = channel.createRequest(true, code);
		this.request.setUriPath(resourceToQueryPath);
		channel.sendMessage(request);
	}
	
	private void channelConnect() {
		/** For now, I only consider one random gateway address. */
		String gateWay = netAddr.getGatewayAddresses().getFirst();
		
		this.channelManager = BasicCoapChannelManager.getInstance();
		this.channelManager.setMessageId(1);
		try {
			this.channel = this.channelManager.connect(this.coapClient, 
																InetAddress.getByName(gateWay + ":" + netAddr.getLocalAddress()),
																this.port);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			throw new RuntimeException("Unknown host! " + e);
		}
	}
	
	private CoapClient createNewCoapClient() {
		
		CoapClient coapClient = new CoapClient() {
			
			@Override
			public void onResponse(CoapClientChannel channel, CoapResponse response) {
				System.out.println(" ");
				System.out.println("Response from sensor at " + System.currentTimeMillis()/1000);
				
				String payload = new String(response.getPayload());
				String phrase = payload;
				String[] tokens = phrase.split(":");

				System.out.println(tokens[2]+" is "+tokens[3]);
				System.out.println(" ");
				
//				writeResultToFile(payload);
			}
			
			

			@Override
			public void onConnectionFailed(CoapClientChannel channel,
					boolean notReachable, boolean resetByServer) {
				throw new RuntimeException("Connection failed.");
				
			}

		};
		return coapClient;
	}

}
