package ma.hibernate.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import ma.hibernate.model.Phone;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

public class PhoneDaoImpl extends AbstractDao implements PhoneDao {
    public PhoneDaoImpl(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    @Override
    public Phone create(Phone phone) {
        Session session = null;
        Transaction transaction = null;
        try {
            session = factory.openSession();
            transaction = session.beginTransaction();
            session.persist(phone);
            transaction.commit();
            return phone;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Can't create phone in DB " + phone, e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public List<Phone> findAll(Map<String, String[]> params) {
        try (Session session = factory.openSession()) {
            CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
            CriteriaQuery<Phone> phoneQuery = criteriaBuilder.createQuery(Phone.class);
            Root<Phone> root = phoneQuery.from(Phone.class);
            List<Predicate> andClause = new ArrayList<>();
            for (Map.Entry<String, String[]> entry : params.entrySet()) {
                List<Predicate> orClause = new ArrayList<>();
                for (String param : entry.getValue()) {
                    orClause.add(criteriaBuilder.equal(root.get(entry.getKey()), param));
                }
                andClause.add(criteriaBuilder.or(orClause.toArray(new Predicate[0])));
            }
            Predicate finalCondition = criteriaBuilder.and(andClause.toArray(new Predicate[0]));
            phoneQuery.select(root).where(finalCondition);
            return session.createQuery(phoneQuery).getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Can't get all phones from DB", e);
        }
    }
}
