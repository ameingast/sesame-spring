package org.openrdf.spring;

import org.openrdf.repository.RepositoryException;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

/**
 * <p>{@link SesameTransactionManager} manages the transaction lifecycle of a {@link SesameTransactionObject}.</p>
 * <p/>
 * <p>The transaction-manager works in co-operation with {@link SesameConnectionFactory}. The manager is operated by
 * Spring and steers transactions.</p>
 * <p>It creates and destroys the transaction-state which is held by the {@link SesameConnectionFactory}.</p>
 * <p/>
 * <p>When the transaction finishes, the changes are either committed or rolled back by Spring.</p>
 *
 * @author ameingast@gmail.com
 */
public class SesameTransactionManager extends AbstractPlatformTransactionManager {
    private final SesameConnectionFactory sesameConnectionFactory;

    /**
     * <p>Creates a new {@link SesameTransactionManager} for the provided {@link SesameConnectionFactory} which
     * handles connection-management to a single {@link org.openrdf.repository.Repository}.</p>
     *
     * @param sesameConnectionFactory The {@link SesameConnectionFactory} providing connections for the repository.
     */
    public SesameTransactionManager(SesameConnectionFactory sesameConnectionFactory) {
        this.sesameConnectionFactory = sesameConnectionFactory;
    }

    /**
     * {@see AbstractPlatformTransactionManager#doGetTransaction}
     */
    @Override
    protected Object doGetTransaction() throws TransactionException {
        SesameTransactionObject localTransactionObject = sesameConnectionFactory.getLocalTransactionObject();

        try {
            if (localTransactionObject == null) {
                localTransactionObject = sesameConnectionFactory.createTransaction();
            } else {
                localTransactionObject.setExisting(true);
            }

            return localTransactionObject;
        } catch (RepositoryException e) {
            throw new SesameTransactionException(e);
        }
    }

    /**
     * {@see AbstractPlatformTransactionManager#isExistingTransaction}
     */
    @Override
    protected boolean isExistingTransaction(Object transaction) throws TransactionException {
        SesameTransactionObject sesameTransactionObject = (SesameTransactionObject) transaction;

        return sesameTransactionObject.isExisting();
    }

    /**
     * {@see AbstractPlatformTransactionManager#doBegin}
     */
    @Override
    protected void doBegin(Object transaction, TransactionDefinition definition) throws TransactionException {

    }

    /**
     * {@see AbstractPlatformTransactionManager#doCommit}
     */
    @Override
    protected void doCommit(DefaultTransactionStatus status) throws TransactionException {
        SesameTransactionObject sesameTransactionObject = (SesameTransactionObject) status.getTransaction();

        try {
            sesameConnectionFactory.endTransaction(sesameTransactionObject.isRollbackOnly());
        } catch (RepositoryException e) {
            throw new TransactionSystemException(e.getMessage(), e);
        }
    }

    /**
     * {@see AbstractPlatformTransactionManager#doRollback}
     */
    @Override
    protected void doRollback(DefaultTransactionStatus status) throws TransactionException {
        try {
            sesameConnectionFactory.endTransaction(true);
        } catch (RepositoryException e) {
            throw new TransactionSystemException(e.getMessage(), e);
        }
    }

    /**
     * {@see AbstractPlatformTransactionManager#doSetRollbackOnly}
     */
    @Override
    public void doSetRollbackOnly(DefaultTransactionStatus status) throws TransactionException {
        SesameTransactionObject sesameTransactionObject = (SesameTransactionObject) status.getTransaction();

        sesameTransactionObject.setRollbackOnly(true);
    }

    /**
     * {@see AbstractPlatformTransactionManager#doCleanupAfterCompletion}
     */
    @Override
    public void doCleanupAfterCompletion(Object transaction) {
        sesameConnectionFactory.closeConnection();
    }

    @Override
    public String toString() {
        return "SesameTransactionManager{" +
                "sesameConnectionFactory=" + sesameConnectionFactory +
                '}';
    }
}
