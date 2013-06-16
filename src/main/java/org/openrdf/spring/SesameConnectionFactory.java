package org.openrdf.spring;

import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

public interface SesameConnectionFactory {
    RepositoryConnection getConnection();

    void closeConnection();

    SesameTransactionObject createTransaction() throws RepositoryException;

    void endTransaction(boolean rollback) throws RepositoryException;

    SesameTransactionObject getLocalTransactionObject();
}
