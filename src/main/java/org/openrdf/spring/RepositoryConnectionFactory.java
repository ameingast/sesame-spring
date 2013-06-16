package org.openrdf.spring;

import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.transaction.TransactionSystemException;

/**
 * <p>{@link RepositoryConnectionFactory} handles connections to the corresponding {@link Repository} and manages
 * the transaction state (represented by {@link SesameTransactionObject}).</p>
 * <p/>
 * <p>This class provides methods to access <i>transactional</i> connections from the outside and is typically
 * the only class that library users interact with.</p>
 *
 * @author ameingast@gmail.com
 */
public class RepositoryConnectionFactory implements DisposableBean, SesameConnectionFactory {
    private final ThreadLocal<SesameTransactionObject> localTransactionObject;

    private Repository repository;

    public RepositoryConnectionFactory(Repository repository) {
        this.repository = repository;
        localTransactionObject = new ThreadLocal<SesameTransactionObject>();
    }

    /**
     * <p>Retrieves the connection for the current transaction. This method may be called at any time as long as a
     * transaction is active and will return the same connection-handle to the repository in the same
     * transaction context (which is thread-local).</p>
     *
     * @return the {@link RepositoryConnection}
     * @throws SesameTransactionException if
     *                                    <ul>
     *                                    <li>No transaction is active</li>
     *                                    </ul>
     */
    @Override
    public RepositoryConnection getConnection() {
        SesameTransactionObject sesameTransactionObject = localTransactionObject.get();

        if (sesameTransactionObject == null) {
            throw new SesameTransactionException("No transaction active");
        }

        RepositoryConnection repositoryConnection = sesameTransactionObject.getRepositoryConnection();

        try {
            if (!repositoryConnection.isOpen()) {
                throw new SesameTransactionException("Connection closed during transaction");
            }
        } catch (RepositoryException e) {
            throw new SesameTransactionException(e);
        }

        return repositoryConnection;
    }

    /**
     * <p>Closes the connection and cleans up the (thread-local) state for the current transaction.</p>
     * <p/>
     * <p>This method should not be called manually, since the connection is managed by the
     * {@link SesameTransactionManager}.</p>
     * <p></p>
     *
     * @throws SesameTransactionException if
     *                                    <ul>
     *                                    <li>No transaction is active</li>
     *                                    <li>The connection could not be closed</li>
     *                                    </ul>
     */
    @Override
    public void closeConnection() {
        SesameTransactionObject sesameTransactionObject = null;
        RepositoryConnection repositoryConnection = null;

        try {
            sesameTransactionObject = localTransactionObject.get();

            if (sesameTransactionObject == null) {
                throw new SesameTransactionException("No transaction active");
            }

            repositoryConnection = sesameTransactionObject.getRepositoryConnection();

            try {
                if (!repositoryConnection.isOpen()) {
                    throw new SesameTransactionException("Connection closed during transaction");
                }
            } catch (RepositoryException e) {
                throw new SesameTransactionException(e);
            }
        } finally {
            if (sesameTransactionObject != null && repositoryConnection != null) {
                try {
                    repositoryConnection.close();
                } catch (RepositoryException e) {
                    throw new SesameTransactionException(e);
                }

                localTransactionObject.remove();
            }
        }
    }

    /**
     * <p>Shuts down the {@link Repository} if it was initialized before.</p>
     *
     * @throws Exception {@see Repository#shutDown}
     */
    @Override
    public void destroy() throws Exception {
        if (repository != null && repository.isInitialized()) {
            try {
                repository.shutDown();
            } finally {
                repository = null;
            }
        }
    }

    /**
     * <p>Creates a new {@link SesameTransactionObject}, connects the created object
     * to the corresponding {@link Repository} and disables auto-commit on the connection.</p>
     * <p/>
     * <p>This method should only be called by {@link SesameTransactionManager}.</p>
     *
     * @return the created transaction object representing the transaction state.
     * @throws RepositoryException {@see Repository#getConnection}
     */
    @Override
    public SesameTransactionObject createTransaction() throws RepositoryException {
        RepositoryConnection connection = repository.getConnection();
        connection.setAutoCommit(false);

        SesameTransactionObject sesameTransactionObject = new SesameTransactionObject(connection);
        localTransactionObject.set(sesameTransactionObject);

        return sesameTransactionObject;
    }

    /**
     * <p>Ends the active transaction by either rolling-back or committing the changes to the {@link Repository}
     * depending on the rollback-flag.</p>
     * <p/>
     * <p>This method should only be called by {@link SesameTransactionManager}.</p>
     *
     * @param rollback if <code>true</code> the current transaction is rolled back, if <code>false</code> the pending
     *                 changes on the connection are committed to the {@link Repository}.
     * @throws RepositoryException        if
     *                                    <ul>
     *                                    <li>The changes could not be rolled back</li>
     *                                    <li>The changes could not be committed</li>
     *                                    </ul>
     * @throws SesameTransactionException if
     *                                    <ul>
     *                                    <li>No transaction is active</li>
     *                                    <li>The connection could not be closed</li>
     *                                    </ul>
     */
    @Override
    public void endTransaction(boolean rollback) throws RepositoryException {
        SesameTransactionObject sesameTransactionObject = localTransactionObject.get();

        if (sesameTransactionObject == null) {
            throw new TransactionSystemException("No transaction active");
        }

        RepositoryConnection repositoryConnection = sesameTransactionObject.getRepositoryConnection();

        if (!repositoryConnection.isOpen()) {
            throw new SesameTransactionException("Connection closed during transaction");
        }

        if (rollback) {
            repositoryConnection.rollback();
        } else {
            repositoryConnection.commit();
        }
    }

    /**
     * <p>Retrieves the current transaction state.</p>
     * <p/>
     * <p>This method should only be called by {@link SesameTransactionManager}.</p>
     *
     * @return The current transaction state in form of a {@link SesameTransactionObject}.
     */
    @Override
    public SesameTransactionObject getLocalTransactionObject() {
        return localTransactionObject.get();
    }

    @Override
    public String toString() {
        return "RepositoryConnectionFactory{" +
                "repository=" + repository +
                ", localTransactionObject=" + localTransactionObject +
                '}';
    }
}
