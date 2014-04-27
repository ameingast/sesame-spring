package org.openrdf.spring;

import org.openrdf.repository.RepositoryConnection;

import static org.springframework.transaction.TransactionDefinition.*;

/**
 * <p>{@link SesameTransactionObject} holds the transaction state which consists of: </p>
 * <p/>
 * <ul>
 * <li>{@link RepositoryConnection}</li>
 * <li>Name</li>
 * <li>Transaction re-use marker</li>
 * <li>Rollback marker</li>
 * <li>Timeout marker</li>
 * <li>Isolation level marker</li>
 * <li>Propagation behavior marker</li>
 * <li>Read only marker</li>
 * </ul>
 *
 * @author ameingast@gmail.com
 * @see org.springframework.transaction.TransactionDefinition
 */
class SesameTransactionObject {
    private final RepositoryConnection repositoryConnection;

    private String name = "";

    private boolean existing = false;

    private boolean rollbackOnly = false;

    private int timeout = TIMEOUT_DEFAULT;

    private int isolationLevel = ISOLATION_DEFAULT;

    private int propagationBehavior = PROPAGATION_REQUIRED;

    private boolean readOnly = false;

    public SesameTransactionObject(RepositoryConnection repositoryConnection) {
        this.repositoryConnection = repositoryConnection;
    }

    @Override
    public String toString() {
        return "SesameTransactionObject{" +
                "repositoryConnection=" + repositoryConnection +
                ", name='" + name + '\'' +
                ", existing=" + existing +
                ", rollbackOnly=" + rollbackOnly +
                ", timeout=" + timeout +
                ", isolationLevel=" + isolationLevel +
                ", propagationBehavior=" + propagationBehavior +
                ", readOnly=" + readOnly +
                '}';
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

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getIsolationLevel() {
        return isolationLevel;
    }

    public void setIsolationLevel(int isolationLevel) {
        this.isolationLevel = isolationLevel;
    }

    public int getPropagationBehavior() {
        return propagationBehavior;
    }

    public void setPropagationBehavior(int propagationBehavior) {
        this.propagationBehavior = propagationBehavior;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
