package org.openrdf.spring;

import org.junit.Assert;
import org.junit.Test;
import org.openrdf.IsolationLevels;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

public class RepositoryManagerConnectionFactoryTest extends BaseTest {
    @Test(expected = SesameTransactionException.class)
    public void testFactoryDoesNotCreateConnection() throws RepositoryException {
        repositoryManagerConnectionFactory.getConnection();
    }

    @Test
    @Transactional("repositoryTransactionManager")
    public void testTransactionCreatesConnection() throws RepositoryException {
        RepositoryConnection currentConnection = repositoryManagerConnectionFactory.getConnection();

        Assert.assertNotNull(currentConnection);
    }

    @Test
    @Transactional("repositoryTransactionManager")
    public void testTransactionDisablesAutoCommit() throws RepositoryException {
        RepositoryConnection connection = repositoryManagerConnectionFactory.getConnection();

        Assert.assertTrue(connection.isActive());
    }

    @Test
    @Transactional("repositoryTransactionManager")
    public void testWrapTransactions() {
        RepositoryConnection connection = repositoryManagerConnectionFactory.getConnection();

        for (RepositoryConnection repositoryConnection : Arrays.asList(transactionScope(), transactionScopeWithoutAnnotation())) {
            Assert.assertTrue(connection == repositoryConnection);
        }
    }

    @Transactional
    private RepositoryConnection transactionScope() {
        return repositoryManagerConnectionFactory.getConnection();
    }

    private RepositoryConnection transactionScopeWithoutAnnotation() {
        return repositoryManagerConnectionFactory.getConnection();
    }

    @Test
    @Transactional("repositoryTransactionManager")
    public void testWriteData() throws Exception {
        addData(repositoryManagerConnectionFactory);
        assertDataPresent(repositoryManagerConnectionFactory);
    }


    @Test
    @Transactional(value = "transactionManager", isolation = Isolation.SERIALIZABLE)
    public void testTransactionWithSerializableIsolationLevel() {
        RepositoryConnection connection = repositoryConnectionFactory.getConnection();

        Assert.assertEquals(IsolationLevels.SERIALIZABLE, connection.getIsolationLevel());
    }

    @Test
    @Transactional(value = "transactionManager", isolation = Isolation.READ_COMMITTED)
    public void testTransactionWithReadCommittedIsolationLevel() {
        RepositoryConnection connection = repositoryConnectionFactory.getConnection();

        Assert.assertEquals(IsolationLevels.READ_COMMITTED, connection.getIsolationLevel());
    }
}
