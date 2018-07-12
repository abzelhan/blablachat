/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kz.wg.utils;

import org.hibernate.HibernateException;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 *
 * @author Bakhyt
 */
public class PersistenseUtil {

    public static EntityManagerFactory emf;
    public static EntityManager em;
    public static int attempts = 0;
    public static long lastConnection = System.currentTimeMillis();
    public static long refreshTimeout = 1 * 60 * 1000;
    // Session factory initialization

    static {
        try {
            emf = Persistence.createEntityManagerFactory("DOMBIRA-PU");
            em = emf.createEntityManager();
        } catch (HibernateException ex) {
            throw new RuntimeException("Configuration problem: " + ex.getMessage(), ex);
        }
    }

    public static boolean refreshConnection(boolean force) {
        if ((System.currentTimeMillis() - lastConnection >= refreshTimeout)||!em.isOpen()||force) {
            try {
                System.out.println("Closing em and emf");
                em.close();
                //emf.close();
                System.out.println("Closed em and emf");
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("We have an exception on closing em and emf...Who cares, anyway!");
            }
            try {
                System.out.println("Opening em and emf");
                //emf = Persistence.createEntityManagerFactory("DOMBIRA-PU");
                em = emf.createEntityManager();
                System.out.println("Opened em and emf");
            } catch (Exception e) {
                e.printStackTrace();
                attempts++;
                if (attempts < 4) {
                    System.out.println("We have an error while refreshing connection...Rebinding... " + attempts);
                    return refreshConnection(true);
                } else {
                    System.out.println("Giving up...Blya...");
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean save(Object object) {
        getEm().getTransaction().begin();
        try {
            getEm().persist(object);
            getEm().getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
            getEm().getTransaction().rollback();
            return false;
        }
        getEm().flush();
        return true;
    }

    public static boolean delete(Object object) {
        getEm().getTransaction().begin();
        try {
            getEm().remove(object);
            getEm().getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
            getEm().getTransaction().rollback();
            return false;
        }
        getEm().flush();
        return true;
    }

    public static boolean update(Object object) {
        getEm().getTransaction().begin();
        try {
            getEm().refresh(object);
            getEm().getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
            getEm().getTransaction().rollback();
            return false;
        }
        getEm().flush();
        return true;
    }

    public static EntityManager getEm() {
        refreshConnection(false);
        return em;
    }

    public static EntityManagerFactory getEmf() {
        return emf;
    }
}
