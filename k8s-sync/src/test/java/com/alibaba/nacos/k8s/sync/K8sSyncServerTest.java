package com.alibaba.nacos.k8s.sync;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.naming.core.InstanceOperatorClientImpl;
import io.kubernetes.client.custom.IntOrString;
import io.kubernetes.client.openapi.models.V1EndpointAddress;
import io.kubernetes.client.openapi.models.V1EndpointSubset;
import io.kubernetes.client.openapi.models.V1Endpoints;
import io.kubernetes.client.openapi.models.V1ServicePort;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RunWith(MockitoJUnitRunner.class)
public class K8sSyncServerTest {
    
    @InjectMocks
    private K8sSyncServer k8sSyncServer;

    @Mock
    private InstanceOperatorClientImpl instanceOperatorClient;
    
    @Test
    public void testRegisterInstances() throws Exception {
        V1ServicePort servicePort = new V1ServicePort();
        servicePort.setPort(11);
        servicePort.setTargetPort(new IntOrString(12));
        List<V1ServicePort> servicePorts = new ArrayList<>();
        servicePorts.add(servicePort);
        Set<String> addIpSet = new HashSet<>(Arrays.asList("1.1.1.1", "2.2.2.2"));
        k8sSyncServer.registerInstances(addIpSet, "testNamespace", "testServiceName", servicePorts);
        
        Mockito.verify(instanceOperatorClient, Mockito.times(2)).registerInstance(Mockito.anyString(), Mockito.anyString(), Mockito.any());
    }
    
    @Test
    public void testUnregisterInstances() {
        List<Instance> oldInstanceList = new ArrayList<>();
        Instance instance1 = new Instance();
        instance1.setIp("1.1.1.1");
        Instance instance2 = new Instance();
        instance2.setIp("2.2.2.2");
        oldInstanceList.add(instance1);
        oldInstanceList.add(instance2);
        Set<String> deleteIpSet = new HashSet<>(Arrays.asList("1.1.1.1", "2.2.2.2"));
        k8sSyncServer.unregisterInstances(deleteIpSet, "testNamespace", "testServiceName", oldInstanceList);
    
        Mockito.verify(instanceOperatorClient, Mockito.times(2)).removeInstance(Mockito.anyString(), Mockito.anyString(), Mockito.any());
    }
    
    @Test
    public void testGetIpFromEndPoints() {
        V1EndpointAddress endpointAddress1 = new V1EndpointAddress();
        endpointAddress1.setIp("1.1.1.1");
        V1EndpointAddress endpointAddress2 = new V1EndpointAddress();
        endpointAddress2.setIp("2.2.2.2");
        V1EndpointSubset endpointSubset = new V1EndpointSubset();
        endpointSubset.addAddressesItem(endpointAddress1);
        endpointSubset.addAddressesItem(endpointAddress2);
        V1Endpoints endpoints = new V1Endpoints();
        endpoints.addSubsetsItem(endpointSubset);
        Assert.assertEquals(new HashSet<>(Arrays.asList("1.1.1.1", "2.2.2.2")), k8sSyncServer.getIpFromEndpoints(endpoints));
    }
    
    @Test
    public void testCompareServicePorts() {
        List<V1ServicePort> oldServicePorts = new ArrayList<>();
        V1ServicePort oldServicePort1 = new V1ServicePort();
        oldServicePort1.setPort(11);
        oldServicePorts.add(oldServicePort1);
        V1ServicePort oldServicePort2 = new V1ServicePort();
        oldServicePort1.setPort(12);
        oldServicePorts.add(oldServicePort2);
        List<V1ServicePort> newServicePorts = new ArrayList<>();
        V1ServicePort newServicePort1 = new V1ServicePort();
        newServicePort1.setPort(11);
        newServicePorts.add(newServicePort1);
        Assert.assertFalse(k8sSyncServer.compareServicePorts(oldServicePorts, newServicePorts));
    
        V1ServicePort newServicePort2 = new V1ServicePort();
        newServicePort1.setPort(12);
        newServicePorts.add(newServicePort2);
        Assert.assertTrue(k8sSyncServer.compareServicePorts(oldServicePorts, newServicePorts));
    
        V1ServicePort oldServicePort3 = new V1ServicePort();
        oldServicePort1.setPort(13);
        oldServicePorts.add(oldServicePort3);
        V1ServicePort newServicePort4 = new V1ServicePort();
        newServicePort1.setPort(14);
        newServicePorts.add(newServicePort4);
        Assert.assertFalse(k8sSyncServer.compareServicePorts(oldServicePorts, newServicePorts));
    }
    
}