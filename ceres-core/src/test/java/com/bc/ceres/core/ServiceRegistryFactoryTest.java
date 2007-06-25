package com.bc.ceres.core;

import junit.framework.TestCase;

import java.util.Set;
import java.util.HashSet;

public class ServiceRegistryFactoryTest extends TestCase {


    public void testGetServiceRegistry() {
        ServiceRegistryFactory instance = ServiceRegistryFactory.getInstance();
        assertNotNull(instance);

        ServiceRegistry<A> serviceRegistryA = instance.getServiceRegistry(A.class);
        ListenerA listenerA = new ListenerA();
        serviceRegistryA.addListener(listenerA);

        ServiceRegistry<B> serviceRegistryB = instance.getServiceRegistry(B.class);
        ListenerB listenerB = new ListenerB();
        serviceRegistryB.addListener(listenerB);

        A a1 = new A("foo") {};
        A a2 = new A("grunt") {};
        B b1 = new B("bar") {};
        B b2 = new B("baz") {};

        serviceRegistryA.addService(a1);
        serviceRegistryA.addService(a2);
        serviceRegistryB.addService(b1);
        serviceRegistryB.addService(b2);

        assertSame(a1, serviceRegistryA.getService(a1.getClass().getName()));
        assertSame(a2, serviceRegistryA.getService(a2.getClass().getName()));
        assertSame(b1, serviceRegistryB.getService(b1.getClass().getName()));
        assertSame(b2, serviceRegistryB.getService(b2.getClass().getName()));

        assertTrue(listenerA.services.contains(a1));
        assertTrue(listenerA.services.contains(a2));
        assertTrue(listenerB.services.contains(b1));
        assertTrue(listenerB.services.contains(b2));

        Set<A> servicesA = serviceRegistryA.getServices();
        for (A serviceA : servicesA) {
           serviceRegistryA.removeService(serviceA);
        }

        Set<B> servicesB = serviceRegistryB.getServices();
        for (B serviceB : servicesB) {
           serviceRegistryB.removeService(serviceB);
        }

        assertFalse(listenerA.services.contains(a1));
        assertFalse(listenerA.services.contains(a2));
        assertFalse(listenerB.services.contains(b1));
        assertFalse(listenerB.services.contains(b2));
    }

    class S {
        String id;

        public S(String id) {
            this.id = id;
        }

        public String toString() {
            return id;
        }
    }

    class A extends S {
        public A(String id) {
            super(id);
        }
    }

    class B extends S {
        public B(String id) {
            super(id);
        }
    }


    private class ListenerBase {
        public Set<Object> services = new HashSet<Object>(4);

        public void serviceAdded(Object service) {
            services.add(service);
        }

        public void serviceRemoved(Object service) {
            services.remove(service);
        }
    }

    private class ListenerA extends ListenerBase implements ServiceRegistryListener<A> {
        public void serviceAdded(ServiceRegistry<A> registry, A service) {
            serviceAdded(service);
        }

        public void serviceRemoved(ServiceRegistry<A> registry, A service) {
            serviceRemoved(service);
        }
    }

    private class ListenerB extends ListenerBase implements ServiceRegistryListener<B> {
        public void serviceAdded(ServiceRegistry<B> registry, B service) {
            serviceAdded(service);
        }

        public void serviceRemoved(ServiceRegistry<B> registry, B service) {
            serviceRemoved(service);
        }
    }
}