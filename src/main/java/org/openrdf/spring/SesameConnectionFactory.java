package org.openrdf.spring;

import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.springframework.beans.factory.DisposableBean;

/**
 * <p>{@link SesameConnectionFactory} handles connections to a single corresponding {@link org.openrdf.repository.Repository} and manages
 * the transaction state (represented by {@link SesameTransactionObject}).</p>
 *
 * @author ameingast@gmail.com
 */
public interface SesameConnectionFactory extends DisposableBean {
    /**
     * <p>Retrieves the connection for the current transaction. This method may be called at any time as long as a
     * transaction is active and it will return the same connection-handle to the repository in the current transaction
     * context (which is thread-local).</p>
     *
     * @return the {@link RepositoryConnection}
     *
     * @throws SesameTransactionException if
     *                                    <ul>
     *                                    <li>No transaction is active</li>
     *                                    <li>The connection was closed during the transaction</li>
     *                                    </ul>
     */
    RepositoryConnection getConnection();

    /**
     * <p>Closes the connection and cleans up the (thread-local) state for the current transaction.</p>
     * <p/>
     * <p>This method should <i>not</i> be called manually, but rather by the associated {@link
     * SesameTransactionManager} which handles the current transaction.</p>
     *
     * @throws SesameTransactionException if
     *                                    <ul>
     *                                    <li>No transaction is active</li>
     *                                    <li>The connection could not be closed</li>
     *                                    </ul>
     */
    void closeConnection();

    /**
     * <p>Creates a new {@link SesameTransactionObject}, connects the created object to the corresponding {@link
     * org.openrdf.repository.Repository} and disables auto-commit on the connection.</p>
     * <p/>
     * <p>This method should <i>not</i> be called manually, but rather by the associated {@link
     * SesameTransactionManager} which handles the current transaction.</p>
     *
     * @return the created transaction object representing the transaction state.
     *
     * @throws RepositoryException if the transaction object could not be created.
     */
    SesameTransactionObject createTransaction() throws RepositoryException;

    /**
     * <p>Ends the active transaction by either rolling-back or committing the changes to the associated
     * {@link org.openrdf.repository.Repository} depending on the rollback-flag.</p>
     * <p/>
     * <p>This method should <i>not</i> be called manually, but rather by the associated {@link
     * SesameTransactionManager} which handles the current transaction.</p>
     *
     * @param rollback if <code>true</code> the current transaction is rolled back, if <code>false</code> the pending
     *                 changes on the connection are committed to the {@link org.openrdf.repository.Repository}.
     *
     * @throws RepositoryException        if
     *                                    <ul>
     *                                    <li>The changes could not be rolled back</li>
     *                                    <li>The connection was closed during the transaction</li>
     *                                    <li>The changes could not be committed</li>
     *                                    </ul>
     * @throws SesameTransactionException if
     *                                    <ul>
     *                                    <li>No transaction is active</li>
     *                                    <li>The connection could not be closed</li>
     *                                    </ul>
     */
    void endTransaction(boolean rollback) throws RepositoryException;

    /**
     * <p>Retrieves the current (thread-local) transaction state.</p>
     * <p/>
     * <p>This method should <i>not</i> be called manually, but rather by the associated {@link
     * SesameTransactionManager} which handles the current transaction.</p>
     *
     * @return The current transaction state in form of a {@link SesameTransactionObject}.
     */
    SesameTransactionObject getLocalTransactionObject();
}
