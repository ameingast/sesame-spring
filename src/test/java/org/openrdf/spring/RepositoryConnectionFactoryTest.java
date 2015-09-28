package org.openrdf.spring;

import org.junit.Assert;
import org.junit.Test;
import org.openrdf.IsolationLevel;
import org.openrdf.IsolationLevels;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

public class RepositoryConnectionFactoryTest extends BaseTest {
    @Test(expected = SesameTransactionException.class)
    public void testFactoryDoesNotCreateConnection() throws RepositoryException {
        repositoryConnectionFactory.getConnection();
    }

    @Test
    @Transactional("transactionManager")
    public void testEmptyTransaction() {
        Assert.assertEquals(0, 0);
    }

    @Test
    @Transactional("transactionManager")
    public void testTransactionCreatesConnection() throws RepositoryException {
        RepositoryConnection currentConnection = repositoryConnectionFactory.getConnection();

        Assert.assertNotNull(currentConnection);
    }

    @Test
    @Transactional("transactionManager")
    public void testTransactionDisablesAutoCommit() throws RepositoryException {
        RepositoryConnection connection = repositoryConnectionFactory.getConnection();

        Assert.assertTrue(connection.isActive());
    }

    @Test
    @Transactional("transactionManager")
    public void testWrapTransactions() {
        RepositoryConnection connection = repositoryConnectionFactory.getConnection();

        for (RepositoryConnection repositoryConnection : Arrays.asList(transactionScope(), transactionScopeWithoutAnnotation())) {
            Assert.assertTrue(connection == repositoryConnection);
        }
    }

    @Transactional("transactionManager")
    public RepositoryConnection transactionScope() {
        return repositoryConnectionFactory.getConnection();
    }

    public RepositoryConnection transactionScopeWithoutAnnotation() {
        return repositoryConnectionFactory.getConnection();
    }

    @Test
    @Transactional("transactionManager")
    public void testWriteData() throws Exception {
        addData(repositoryConnectionFactory);
        assertDataPresent(repositoryConnectionFactory);
    }

    @Test
    @Transactional(value = "transactionManager")
    public void testTransactionIsolationLevel() {
        RepositoryConnection connection = repositoryConnectionFactory.getConnection();
        SailRepository sailRepository = (SailRepository) connection.getRepository();
        IsolationLevel defaultIsolationLevel = sailRepository.getSail().getDefaultIsolationLevel();

        Assert.assertEquals(defaultIsolationLevel, connection.getIsolationLevel());
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
