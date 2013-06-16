package org.openrdf.spring;

import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.manager.RepositoryManager;

/**
 * <p>{@link RepositoryManagerConnectionFactory} handles connections to a {@link RepositoryManager} and manages
 * the transaction state (represented by {@link SesameTransactionObject}).</p>
 * <p/>
 * <p>The {@link RepositoryManager}'s repository-id is set via the property
 * {@link RepositoryManagerConnectionFactory#setLocalRepositoryId(String)}/
 * {@link org.openrdf.spring.RepositoryManagerConnectionFactory#getLocalRepositoryId()}.
 * <p/>
 * </p>It supports a single repository-id per thread; i.e. you can use the {@link RepositoryManagerConnectionFactory}
 * with different @{link Repository}s in different threads.
 * <p/>
 * <p>This class provides methods to access <i>transactional</i> connections from the outside and is typically
 * the only class that library users interact with.</p>
 *
 * @author ameingast@gmail.com
 */
public class RepositoryManagerConnectionFactory implements SesameConnectionFactory {
    private final RepositoryManager repositoryManager;

    private ThreadLocal<String> localRepositoryId;

    private ThreadLocal<RepositoryConnectionFactory> localConnectionFactory;

    public RepositoryManagerConnectionFactory(RepositoryManager repositoryManager) {
        this.repositoryManager = repositoryManager;
        localRepositoryId = new ThreadLocal<String>();
        localConnectionFactory = new ThreadLocal<RepositoryConnectionFactory>();
    }

    @Override
    public RepositoryConnection getConnection() {
        initializeLocalConnectionFactory();

        return localConnectionFactory.get().getConnection();
    }

    private void initializeLocalConnectionFactory() {
        if (localConnectionFactory.get() == null) {
            if (localRepositoryId.get() == null || localRepositoryId.get().isEmpty()) {
                throw new RuntimeException("Local repository-id has not been initialized");
            }

            try {
                Repository repository = repositoryManager.getRepository(localRepositoryId.get());
                RepositoryConnectionFactory repositoryConnectionFactory = new RepositoryConnectionFactory(repository);

                localConnectionFactory.set(repositoryConnectionFactory);
            } catch (RepositoryException e) {
                throw new RuntimeException(e);
            } catch (RepositoryConfigException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void closeConnection() {
        try {
            initializeLocalConnectionFactory();

            localConnectionFactory.get().closeConnection();
        } finally {
            localConnectionFactory.remove();
        }
    }

    @Override
    public SesameTransactionObject createTransaction() throws RepositoryException {
        initializeLocalConnectionFactory();

        return localConnectionFactory.get().createTransaction();
    }

    @Override
    public void endTransaction(boolean rollback) throws RepositoryException {
        initializeLocalConnectionFactory();

        localConnectionFactory.get().endTransaction(rollback);
    }

    @Override
    public SesameTransactionObject getLocalTransactionObject() {
        initializeLocalConnectionFactory();

        return localConnectionFactory.get().getLocalTransactionObject();
    }

    public String getLocalRepositoryId() {
        return localRepositoryId.get();
    }

    public void setLocalRepositoryId(String repositoryId) {
        this.localRepositoryId.set(repositoryId);
    }

    @Override
    public String toString() {
        return "RepositoryManagerConnectionFactory{" +
                "repositoryManager=" + repositoryManager +
                ", repositoryId='" + localRepositoryId + '\'' +
                ", connectionFactory=" + localConnectionFactory +
                '}';
    }
}
