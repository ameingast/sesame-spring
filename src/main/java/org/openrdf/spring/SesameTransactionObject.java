package org.openrdf.spring;

import org.openrdf.repository.RepositoryConnection;

/**
 * <p>{@link SesameTransactionObject} holds the transaction state which consists of: </p>
 * <p/>
 * <ul>
 * <li>{@link RepositoryConnection}</li>
 * <li>Transaction re-use marker</li>
 * <li>Rollback marker</li>
 * </ul>
 *
 * @author ameingast@gmail.com
 */
class SesameTransactionObject {
    private final RepositoryConnection repositoryConnection;

    private boolean existing = false;

    private boolean rollbackOnly = false;

    public SesameTransactionObject(RepositoryConnection repositoryConnection) {
        this.repositoryConnection = repositoryConnection;
    }

    public RepositoryConnection getRepositoryConnection() {
        return repositoryConnection;
    }

    public boolean isExisting() {
        return existing;
    }

    public void setExisting(boolean existing) {
        this.existing = existing;
    }

    public boolean isRollbackOnly() {
        return rollbackOnly;
    }

    public void setRollbackOnly(boolean rollbackOnly) {
        this.rollbackOnly = rollbackOnly;
    }

    @Override
    public String toString() {
        return "SesameTransactionObject{" +
                "repositoryConnection=" + repositoryConnection +
                ", existing=" + existing +
                ", rollbackOnly=" + rollbackOnly +
                '}';
    }
}
