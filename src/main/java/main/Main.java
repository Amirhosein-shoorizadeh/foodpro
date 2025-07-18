package main;

import com.sun.net.httpserver.HttpServer;
import dao.UserDao;
import entity.*;
import HttpHandeler.*;
import org.hibernate.SessionFactory;
import org.hibernate.Session;
import org.hibernate.cfg.Configuration;
import org.mindrot.jbcrypt.BCrypt;
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
//        String password = "mew1384";
//        String name = "amir1384";
//        String phone = "13841384";
//        String email = "13841384@gmail.com";
//        String Address = "amir1384";
//        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
//        String profileImageBase64 = "amir1384";
//        Bankinfo bankinfo = new Bankinfo();
//        Admin admin = new Admin(hashedPassword, name, phone, email, profileImageBase64, bankinfo, Address);
        try {
//            UserDao.save(admin);
            TokenCleanupService.start();
            HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
            server.createContext("/restaurant", new RestaurantHandler());
            server.createContext("/auth", new UserHandeler());
            server.createContext("/vendors", new VendorHandler());
            server.createContext("/items", new ItemHandler());
            server.createContext("/admin", new adminHandler());
            server.createContext("/transactions",new OrderHandler());
            server.createContext("/wallet",new OrderHandler());
            server.createContext("/payment",new OrderHandler());
            server.createContext("/orders",new OrderHandler());
            server.start();
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }

        TokenCleanupService.shutdown();
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        session.getTransaction().commit();
        session.close();

    }
}