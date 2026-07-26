package com.luadministra.config;

import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;

@Component
public class JmDnsRegistrar {

    private static final Logger log = LoggerFactory.getLogger(JmDnsRegistrar.class);
    static final String SERVICE_TYPE = "_http._tcp.local.";
    static final String SERVICE_NAME = "LuAdministra";

    private JmDNS jmDNS;

    @Value("${server.port:8080}")
    private int port;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        try {
            InetAddress addr = findSiteLocalAddress();
            if (addr == null) {
                log.warn("No se encontró una interfaz de red local. mDNS no disponible.");
                return;
            }
            jmDNS = JmDNS.create(addr);
            ServiceInfo info = ServiceInfo.create(SERVICE_TYPE, SERVICE_NAME, port, "path=/");
            jmDNS.registerService(info);
            log.info("mDNS: {} registrado como {} en {}", SERVICE_NAME, SERVICE_NAME + ".local", addr.getHostAddress());
        } catch (Exception e) {
            log.warn("No se pudo iniciar mDNS: {}", e.toString());
        }
    }

    @PreDestroy
    public void onDestroy() {
        if (jmDNS != null) {
            jmDNS.unregisterAllServices();
            try {
                jmDNS.close();
            } catch (Exception e) {
                log.warn("Error cerrando mDNS: {}", e.toString());
            }
        }
    }

    private static InetAddress findSiteLocalAddress() {
        try {
            var interfaces = NetworkInterface.networkInterfaces().toList();
            for (var ni : interfaces) {
                if (!ni.isUp() || ni.isLoopback() || ni.isVirtual()) continue;
                var addresses = Collections.list(ni.getInetAddresses());
                for (var addr : addresses) {
                    if (addr.isSiteLocalAddress()) return addr;
                }
            }
        } catch (Exception e) {
            log.warn("Error explorando interfaces de red: {}", e.toString());
        }
        return null;
    }
}
