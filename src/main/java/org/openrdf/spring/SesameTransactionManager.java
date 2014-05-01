package org.openrdf.spring;

import org.openrdf.IsolationLevel;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.Sail;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

import static org.openrdf.spring.IsolationLevelAdapter.adaptToRdfIsolation;

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
    protected void doBegin(Object transaction, TransactionDefinition transactionDefinition) throws TransactionException {
        SesameTransactionObject sesameTransactionObject = (SesameTransactionObject) transaction;

        sesameTransactionObject.setTimeout(transactionDefinition.getTimeout());
        sesameTransactionObject.setIsolationLevel(transactionDefinition.getIsolationLevel());
        sesameTransactionObject.setPropagationBehavior(transactionDefinition.getPropagationBehavior());
        sesameTransactionObject.setReadOnly(transactionDefinition.isReadOnly());
        sesameTransactionObject.setName(Thread.currentThread().getName() + " " + transactionDefinition.getName());

        setIsolationLevel(sesameTransactionObject, transactionDefinition);
    }

    private void setIsolationLevel(SesameTransactionObject sesameTransactionObject, TransactionDefinition transactionDefinition) {
        RepositoryConnection repositoryConnection = sesameTransactionObject.getRepositoryConnection();
        Repository repository = repositoryConnection.getRepository();

        if (repository instanceof SailRepository) {
            Sail sail = ((SailRepository) repository).getSail();
            IsolationLevel isolationLevel = adaptToRdfIsolation(sail, transactionDefinition.getIsolationLevel());

            repositoryConnection.setIsolationLevel(isolationLevel);
        }
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
