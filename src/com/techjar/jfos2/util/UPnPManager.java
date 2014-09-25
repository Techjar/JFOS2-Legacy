package com.techjar.jfos2.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import lombok.SneakyThrows;
import org.teleal.cling.UpnpService;
import org.teleal.cling.UpnpServiceImpl;
import org.teleal.cling.support.igd.PortMappingListener;
import org.teleal.cling.support.model.PortMapping;
import org.teleal.cling.support.model.PortMapping.Protocol;

/**
 *
 * @author Techjar
 */
public class UPnPManager {
    public static final UPnPManager INSTANCE = new UPnPManager();
    private final InetAddress localHost;
    private final Map<Integer, UpnpService> upnpServices;
    
    @SneakyThrows(UnknownHostException.class)
    private UPnPManager() {
        this.localHost = InetAddress.getLocalHost();
        upnpServices = new HashMap<>();
    }
    
    public boolean start(int port, Protocol protocol) {
        if(upnpServices.containsKey(port)) return false;
        upnpServices.put(port, new UpnpServiceImpl(new PortMappingListener(new PortMapping(port, localHost.getHostAddress(), protocol)))).getControlPoint().search();
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
    
    protected UpnpService get(int port) {
        return upnpServices.get(port);
    }
}
