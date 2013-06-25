package org.openrdf.spring;

import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.manager.RepositoryManager;
import org.springframework.beans.factory.DisposableBean;

/**
 * <p>{@link RepositoryManagerConnectionFactory} handles connections to a single {@link Repository} managed by a
 * {@link RepositoryManager}. It also manages transaction state (represented by {@link  SesameTransactionObject}).</p>
 * <p/>
 * <p>A {@link RepositoryManager} can hold multiple {@link Repository}s. To identify the {@link Repository} to
 * which connections will be opened, a <i>repository-id</i> has to be provided as a constructor argument.</p>
 * <p/>
 *
 * @author ameingast@gmail.com
 * @see RepositoryConnectionFactory
 */
public class RepositoryManagerConnectionFactory implements SesameConnectionFactory, DisposableBean {
    private final RepositoryManager repositoryManager;

    private final String repositoryId;

    private RepositoryConnectionFactory repositoryConnectionFactory;

    /**
     * <p>Creates a new {@link RepositoryManagerConnectionFactory} for the {@link Repository} identified by the
     * provided <code>repositoryId</code> in the {@ink RepositoryManager} <code>repositoryManager</code>.</p>
     *
     * @param repositoryManager The {@link RepositoryManager} that holds the {@link Repository} to which connections
     *                          will be opened.
     * @param repositoryId      The id of the {@link Repository} which is used by the {@link RepositoryManager} to
     *                          identify the {@link Repository} to which connections will be opened.
     */
    public RepositoryManagerConnectionFactory(RepositoryManager repositoryManager, String repositoryId) {
        this.repositoryManager = repositoryManager;
        this.repositoryId = repositoryId;
    }

    /**
     * @inheritDoc
     */
    @Override
    public RepositoryConnection getConnection() {
        initializeRepository();
        return repositoryConnectionFactory.getConnection();
    }

    /**
     * @inheritDoc
     */
    @Override
    public void closeConnection() {
        initializeRepository();
        repositoryConnectionFactory.closeConnection();
    }

    /**
     * @inheritDoc
     */
    @Override
    public SesameTransactionObject createTransaction() throws RepositoryException {
        initializeRepository();
        return repositoryConnectionFactory.createTransaction();
    }

    /**
     * @inheritDoc
     */
    @Override
    public void endTransaction(boolean rollback) throws RepositoryException {
        initializeRepository();
        repositoryConnectionFactory.endTransaction(rollback);
    }

    /**
     * @inheritDoc
     */
    @Override
    public SesameTransactionObject getLocalTransactionObject() {
        initializeRepository();
        return repositoryConnectionFactory.getLocalTransactionObject();
    }

    private void initializeRepository() {
        if (repositoryConnectionFactory != null) {
            return;
        }

        try {
            Repository repository = repositoryManager.getRepository(repositoryId);
            if (repository == null) {
                throw new SesameTransactionException("No such repository: " + repositoryId);
            }
            repositoryConnectionFactory = new RepositoryConnectionFactory(repository);
        } catch (RepositoryConfigException e) {
            throw new SesameTransactionException(e);
        } catch (RepositoryException e) {
            throw new SesameTransactionException(e);
        }
    }

    /**
     * <p>Shuts down the associated {@link Repository} if it was initialized before.</p>
     *
     * @throws Exception {@see Repository#shutDown}
     */
    @Override
    public void destroy() throws Exception {
        if (repositoryConnectionFactory != null) {
            repositoryConnectionFactory.destroy();
        }
    }

    @Override
    public String toString() {
        return "RepositoryManagerConnectionFactory{" +
                "repositoryManager=" + repositoryManager +
                ", repositoryId='" + repositoryId + '\'' +
                ", repositoryConnectionFactory=" + repositoryConnectionFactory +
                '}';
    }
}
