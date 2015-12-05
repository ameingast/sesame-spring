package org.openrdf.spring;

import org.openrdf.repository.config.RepositoryImplConfig;
import org.openrdf.repository.manager.RepositoryManager;

/**
 * @author ameingast@gmail.com
 */
public class RepositoryManagerConnectionFactory extends DynamicRepositoryManagerConnectionFactory {
    /**
     * <p>Creates a new {@link RepositoryManagerConnectionFactory} for the {@link org.openrdf.repository.Repository} identified by the
     * provided <code>repositoryId</code> in the {@link RepositoryManager} <code>repositoryManager</code>.</p>
     *
     * @param repositoryManager The {@link RepositoryManager} that holds the {@link org.openrdf.repository.Repository} to which connections
     *                          will be opened.
     * @param repositoryId      The id of the {@link org.openrdf.repository.Repository} which is used by the {@link RepositoryManager} to
     *                          identify the {@link org.openrdf.repository.Repository} to which connections will be opened.
     */
    public RepositoryManagerConnectionFactory(RepositoryManager repositoryManager, final String repositoryId) {
        super(repositoryManager, () -> repositoryId);
    }

    public RepositoryManagerConnectionFactory(RepositoryManager repositoryManager, RepositoryImplConfig repositoryImplConfig, final String repositoryId) {
        super(repositoryManager, repositoryImplConfig, () -> repositoryId);
    }
}
