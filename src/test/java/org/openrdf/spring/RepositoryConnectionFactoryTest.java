package org.openrdf.spring;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/repositoryTestContext.xml")
public class RepositoryConnectionFactoryTest {
    @Autowired
    protected SesameConnectionFactory repositoryConnectionFactory;

    @Test(expected = SesameTransactionException.class)
    public void testFactoryDoesNotCreateConnection() throws RepositoryException {
        repositoryConnectionFactory.getConnection();
    }

    @Test
    @Transactional
    public void testTransactionCreatesConnection() throws RepositoryException {
        RepositoryConnection currentConnection = repositoryConnectionFactory.getConnection();
        Assert.assertNotNull(currentConnection);
    }

    @Test
    @Transactional
    public void testTransactionDisablesAutoCommit() throws RepositoryException {
        RepositoryConnection connection = repositoryConnectionFactory.getConnection();

        Assert.assertTrue(connection.isActive());
    }

    @Test
    @Transactional
    public void testWrapTransactions() {
        RepositoryConnection connection = repositoryConnectionFactory.getConnection();

        for (RepositoryConnection repositoryConnection : Arrays.asList(transactionScope(), transactionScopeWithoutAnnotation())) {
            Assert.assertTrue(connection == repositoryConnection);
        }
    }

    @Transactional
    protected RepositoryConnection transactionScope() {
        return repositoryConnectionFactory.getConnection();
    }

    protected RepositoryConnection transactionScopeWithoutAnnotation() {
        return repositoryConnectionFactory.getConnection();
    }

    @Test
    @Transactional
    public void testWriteData() throws Exception {
        addData();
        assertDataPresent();
    }

    protected void assertDataPresent() throws Exception {
        RepositoryConnection connection = repositoryConnectionFactory.getConnection();
        final TupleQuery tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, "SELECT ?s ?p ?o WHERE { ?s ?p ?o . }");
        TupleQueryResult result = tupleQuery.evaluate();

        SesameResultHandlers.withTupleQueryResult(result, new SesameResultHandlers.TupleQueryResultHandler() {
            @Override
            public void handle(TupleQueryResult tupleQueryResult) throws Exception {
                Assert.assertTrue(tupleQueryResult.hasNext());

                BindingSet bindingSet = tupleQueryResult.next();

                Assert.assertEquals("http://example.com/a", bindingSet.getBinding("s").getValue().stringValue());
                Assert.assertEquals("http://example.com/b", bindingSet.getBinding("p").getValue().stringValue());
                Assert.assertEquals("http://example.com/c", bindingSet.getBinding("o").getValue().stringValue());
            }
        });
    }

    protected void addData() throws RepositoryException {
        ValueFactory f = ValueFactoryImpl.getInstance();
        URI a = f.createURI("http://example.com/a");
        URI b = f.createURI("http://example.com/b");
        URI c = f.createURI("http://example.com/c");

        RepositoryConnection connection = repositoryConnectionFactory.getConnection();
        connection.add(a, b, c);
    }
}
