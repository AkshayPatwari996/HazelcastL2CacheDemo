
package com.example;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;

public class Main {

    public static void main(String[] args) {

        EntityManagerFactory emf =
                Persistence.createEntityManagerFactory("hazelcast-demo");

        try {
            SessionFactory sf = emf.unwrap(SessionFactory.class);
            Statistics stats = sf.getStatistics();

            // Make sure employee 101 exists
            EntityManager setupEm = emf.createEntityManager();

            try {
                setupEm.getTransaction().begin();

                Employee employee = setupEm.find(Employee.class, 101);

                if (employee == null) {
                    setupEm.persist(
                            new Employee(101, "Rahul", 60000.0)
                    );
                }

                setupEm.getTransaction().commit();
            } finally {
                setupEm.close();
            }

            // Clear the L2 cache for this employee
            sf.getCache().evictEntityData(Employee.class, 101);
            stats.clear();

            // Read 1: EntityManager 1
            EntityManager em1 = emf.createEntityManager();

            try {
                Employee e1 = em1.find(Employee.class, 101);
                System.out.println("Read 1: " + e1);
            } finally {
                em1.close();
            }

            // Read 2: EntityManager 2
            EntityManager em2 = emf.createEntityManager();

            try {
                Employee e2 = em2.find(Employee.class, 101);
                System.out.println("Read 2: " + e2);
            } finally {
                em2.close();
            }

            // Read 3: EntityManager 3
            EntityManager em3 = emf.createEntityManager();

            try {
                Employee e3 = em3.find(Employee.class, 101);
                System.out.println("Read 3: " + e3);
            } finally {
                em3.close();
            }

            // Display cache statistics
            System.out.println("\n--- L2 Cache Statistics ---");
            System.out.println("Hits: "
                    + stats.getSecondLevelCacheHitCount());
            System.out.println("Misses: "
                    + stats.getSecondLevelCacheMissCount());
            System.out.println("Puts: "
                    + stats.getSecondLevelCachePutCount());

        } finally {
            emf.close();
        }
    }
}
