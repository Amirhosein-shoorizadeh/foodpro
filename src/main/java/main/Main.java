package main;

import com.sun.net.httpserver.HttpServer;
import entity.*;
import HttpHandeler.*;
import org.hibernate.SessionFactory;
import org.hibernate.Session;
import org.hibernate.cfg.Configuration;
import service.TokenCleanupService;
import util.HibernateUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        try {
            TokenCleanupService.start();
            HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
            server.createContext("/restaurant", new RestaurantHandler());
            server.createContext("/auth", new UserHandeler());
            server.createContext("/vendors", new VendorHandler());
            server.createContext("/items",new ItemHandler());
            server.start();
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }

        TokenCleanupService.shutdown();
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        session.getTransaction().commit();
        session.close();

    }
}