package eu.spitfire_project.ld4s.network.sensor_network;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;

import org.ws4d.coap.connection.BasicCoapChannelManager;
import org.ws4d.coap.interfaces.CoapChannelManager;
import org.ws4d.coap.interfaces.CoapClient;
import org.ws4d.coap.interfaces.CoapClientChannel;
import org.ws4d.coap.interfaces.CoapRequest;
import org.ws4d.coap.interfaces.CoapResponse;
import org.ws4d.coap.messages.CoapRequestCode;

import eu.spitfire_project.ld4s.network.NetworkAddress;

public class QueryForwarder extends Thread {

	public final static int SUCCESS = 1;
	public final static int FAIL = -1;
	public final static int RUNNING = 0;
	int _status = 0;

	private NetworkAddress netAddr = null;

	private CoapChannelManager channelManager = null;

	private CoapClient coapClient = null;

	private CoapRequest request = null;

	private CoapClientChannel channel = null;

	private String resourceToQueryPath = null;

	private CoapRequestCode code = null;

	private int port = -1;

	private String latestReading = null;


	public QueryForwarder(CoapRequestCode code, String queryUri, NetworkAddress naddr, int port){
		this.code = code;
		this.resourceToQueryPath = queryUri;
		this.netAddr = naddr;
		this.port = port;
		this._status = RUNNING;
	}

	public int status() {
		return _status;
	}


	/**
	 * Get the latest reading from the previously specified CoAP connection
	 * @return
	 */
	@Override
	public void run() {
		this.coapClient = this.createNewCoapClient();

		LinkedList<String> list = netAddr.getGatewayAddresses();
		if (list == null){
			return;
		}
		/** For now, I only consider one random gateway address. */
		String gateWay = list.getFirst();

		this.channelManager = BasicCoapChannelManager.getInstance();
		this.channelManager.setMessageId(1);
		try {
			InetAddress inetaddr = InetAddress.getByName(gateWay + ":" + netAddr.getLocalAddress());
			this.channel = this.channelManager.connect(this.coapClient, inetaddr, this.port);
			System.out.println("Connected to host "+gateWay + ":" + netAddr.getLocalAddress()+
					" with IP "+inetaddr+" on port "+this.port);
		} catch (UnknownHostException e) {
			_status = FAIL;
			e.printStackTrace();
			throw new RuntimeException("Unknown host! " + e);
		}


		this.request = channel.createRequest(true, code);
		this.request.setUriPath(resourceToQueryPath);
		System.out.println("Request created with code "+code+ " addressing "+resourceToQueryPath);
		System.out.println("Now sending the request...");
		channel.sendMessage(request);

		System.out.println("Request successfully sent!");
		
		while (_status == RUNNING){
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}


	private CoapClient createNewCoapClient() {

		CoapClient coapClient = new CoapClient() {

			@Override
			public void onResponse(CoapClientChannel channel, CoapResponse response) {
				System.out.println("Response successfully received! :) \n");
				System.out.println("Response from sensor at " + System.currentTimeMillis()/1000);

				String payload = new String(response.getPayload());
				String phrase = payload;
				String[] tokens = phrase.split(":");
				if (tokens.length == 0 || tokens.length < 3){
					System.err.println("Empty Payload received :(");
					_status = FAIL;
				}else{
					latestReading = tokens[2];

					System.out.println(tokens[0] + " reading from " + tokens[1] + " = " + latestReading+"\n");

					_status = SUCCESS;
				}
//				synchronized(this){
//					notifyAll();
//				}

				//				writeResultToFile(payload);
			}



			@Override
			public void onConnectionFailed(CoapClientChannel channel,
					boolean notReachable, boolean resetByServer) {
				_status = FAIL;
				System.err.println("Connection failed. :'(");
//				synchronized (this){
//					this.notifyAll();
//				}
				//				throw new RuntimeException("Connection failed.");

			}

		};
		return coapClient;
	}
	
	public String getLatestReading(){
		return this.latestReading;
	}

}
