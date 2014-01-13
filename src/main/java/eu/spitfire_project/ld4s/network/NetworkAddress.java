package eu.spitfire_project.ld4s.network;

import java.util.LinkedList;

public class NetworkAddress {

	private LinkedList<String> gatewayAddresses = null;

	private String localAddress = null;

	public NetworkAddress(String gatewayAddress, String localAddress){
		this((LinkedList<String>)null, localAddress);
		addGatewayAddress(gatewayAddress);
	}
	
	public NetworkAddress(){
		this((LinkedList<String>)null, null);	
	}
	
	public NetworkAddress(LinkedList<String> gatewayAddresses, String localAddress){
		if (gatewayAddresses != null){
			this.setGatewayAddresses(gatewayAddresses);
		}else{
			this.setGatewayAddresses(new LinkedList<String>());
		}
		this.setLocalAddress(localAddress);
	}

	

	public LinkedList<String> getGatewayAddresses() {
		return gatewayAddresses;
	}

	public void setGatewayAddresses(LinkedList<String> gatewayAddresses) {
		this.gatewayAddresses = gatewayAddresses;
	}
	
	public void addGatewayAddress(String gatewayAddress) {
		this.gatewayAddresses.add(gatewayAddress);
	}

	public String getLocalAddress() {
		return localAddress;
	}

	public void setLocalAddress(String localAddress) {
		this.localAddress = localAddress;
	}

}
