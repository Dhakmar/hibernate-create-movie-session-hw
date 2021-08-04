package mate.academy.dao.impl;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import mate.academy.dao.MovieSessionDao;
import mate.academy.exception.DataProcessingException;
import mate.academy.lib.Dao;
import mate.academy.model.MovieSession;
import mate.academy.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

@Dao
public class MovieSessionDaoImpl implements MovieSessionDao {
    @Override
    public MovieSession add(MovieSession movieSession) {
        Transaction transaction = null;
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();
            session.save(movieSession);
            transaction.commit();
            return movieSession;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new DataProcessingException("Can't insert movie session " + movieSession, e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public Optional<MovieSession> get(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return Optional.ofNullable(session.createQuery("FROM MovieSession ms "
                    + "LEFT JOIN FETCH ms.movie"
                    + " LEFT JOIN FETCH ms.cinemaHall "
                    + "WHERE ms.id = :id", MovieSession.class)
                    .setParameter("id", id)
                    .getSingleResult());
        } catch (Exception e) {
            throw new DataProcessingException("Can't get a movie session by id: " + id, e);
        }
    }

    @Override
    public List<MovieSession> findAvailableSessions(Long movieId, LocalDate date) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM MovieSession ms "
                    + "LEFT JOIN FETCH ms.movie m "
                    + "LEFT JOIN FETCH ms.cinemaHall "
                    + "WHERE m.id = :id "
                    + "AND DATE(ms.showTime) = :date", MovieSession.class)
                    .setParameter("id", movieId)
                    .setParameter("date", Date.valueOf(date))
                    .getResultList();
        } catch (Exception e) {
            throw new DataProcessingException("Can't get available movie sessions from data base", e);
        }
    }
}