package com.techjar.jfos2;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import org.teleal.cling.UpnpService;
import org.teleal.cling.UpnpServiceImpl;
import org.teleal.cling.support.igd.PortMappingListener;
import org.teleal.cling.support.model.PortMapping;

/**
 *
 * @author Techjar
 */
public class UPnPManager {
    private InetAddress localHost;
    private Map<Integer, UpnpService> upnpServices;
    
    
    public UPnPManager() {
        try {
            this.localHost = InetAddress.getLocalHost();
        }
        catch(Exception ex) {
            ex.printStackTrace();
            System.exit(0);
        }
        
        upnpServices = new HashMap<>();
    }
    
    public boolean start(int port) {
        if(upnpServices.containsKey(port)) return false;
        upnpServices.put(port, new UpnpServiceImpl(new PortMappingListener(new PortMapping(Constants.PORT, localHost.getHostAddress(), PortMapping.Protocol.TCP)))).getControlPoint().search();
        return true;
    }
    
    public boolean shutdown(int port) {
        if(!upnpServices.containsKey(port)) return false;
        upnpServices.remove(port).shutdown();
        return true;
    }
    
    public void shutdownAll() {
        for (UpnpService upnp : upnpServices.values())
            upnp.shutdown();
        upnpServices.clear();
    }
    
    public UpnpService get(int port) {
        return upnpServices.get(port);
    }
}
