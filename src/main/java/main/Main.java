package main;
import com.sun.net.httpserver.HttpServer;
import entity.*;
import HttpHandeler.*;
import org.hibernate.SessionFactory;
import org.hibernate.Session;
import org.hibernate.cfg.Configuration;
import java.io.IOException;
import java.net.InetSocketAddress;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
            server.createContext("/restaurant", new RestaurantHandler());
            server.createContext("/User", new UserHandeler());

            server.start();
        }catch (IOException e){
            System.out.println("Error: " + e.getMessage());
        }

        SessionFactory factory = new Configuration().configure("hibernate.cfg.xml").addAnnotatedClass(User.class).buildSessionFactory();
        Session session = factory.getCurrentSession();
        session.beginTransaction();

        session.getTransaction().commit();
        session.close();

    }
}