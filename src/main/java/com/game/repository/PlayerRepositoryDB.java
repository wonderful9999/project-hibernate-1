package com.game.repository;

import com.game.entity.Player;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

@Repository(value = "db")
public class PlayerRepositoryDB implements IPlayerRepository {

    private final SessionFactory sessionFactory;

    public PlayerRepositoryDB() {
        Properties properties = new Properties();
        properties.put(Environment.DRIVER, "com.p6spy.engine.spy.P6SpyDriver");
        properties.put(Environment.URL, "jdbc:p6spy:mysql://localhost:3306/rpg");
        properties.put(Environment.DIALECT, "org.hibernate.dialect.MySQL8Dialect");
        properties.put(Environment.USER, "root");
        properties.put(Environment.PASS, "1234");
        properties.put(Environment.SHOW_SQL, "true");
        properties.put(Environment.HBM2DDL_AUTO, "validate");

        sessionFactory = new Configuration()
                .addAnnotatedClass(Player.class)
                .setProperties(properties)
                .buildSessionFactory();
    }

    @Override
    public List<Player> getAll(int pageNumber, int pageSize) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            NativeQuery<Player> playerQuery = session.createNativeQuery("select * from player", Player.class);
            playerQuery.setFirstResult(pageNumber * pageSize);
            playerQuery.setMaxResults(pageSize);
            session.getTransaction().commit();
            return playerQuery.getResultList();
        }
    }


    @Override
    public int getAllCount() {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            Query<Long> query = session.createNamedQuery("player_getAllCount", Long.class);
            int result = Math.toIntExact(query.getSingleResult());
            session.getTransaction().commit();
            return result;
        }
    }

    @Override
    public Player save(Player player) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.persist(player);
            session.getTransaction().commit();
            return player;
        }
    }

    @Override
    public Player update(Player player) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            Player resultPlayer = (Player) session.merge(player);
            session.getTransaction().commit();
            return resultPlayer;
        }
    }

    @Override
    public Optional<Player> findById(long id) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            Player result = session.find(Player.class, id);
            session.getTransaction().commit();
            return Optional.of(result);
        }
    }

    @Override
    public void delete(Player player) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.delete(player);
            session.getTransaction().commit();
        }
    }

    @PreDestroy
    public void beforeStop() {
        sessionFactory.close();
    }
}