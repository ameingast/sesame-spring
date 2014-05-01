package org.openrdf.spring;

import org.junit.Assert;
import org.junit.Ignore;
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

@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/repositoryTestContext.xml")
public abstract class BaseTest {
    @Autowired
    protected SesameConnectionFactory repositoryConnectionFactory;

    @Autowired
    protected SesameConnectionFactory repositoryManagerConnectionFactory;

    protected void assertDataPresent() throws Exception {
        RepositoryConnection connection = repositoryManagerConnectionFactory.getConnection();
        final TupleQuery tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, "SELECT ?s ?o WHERE { ?s <http://example.com/b> ?o . }");
        TupleQueryResult result = tupleQuery.evaluate();

        SesameResultHandlers.withTupleQueryResult(result, new SesameResultHandlers.TupleQueryResultHandler() {
            @Override
            public void handle(TupleQueryResult tupleQueryResult) throws Exception {
                Assert.assertTrue(tupleQueryResult.hasNext());

                BindingSet bindingSet = tupleQueryResult.next();

                Assert.assertEquals("http://example.com/a", bindingSet.getBinding("s").getValue().stringValue());
                Assert.assertEquals("http://example.com/c", bindingSet.getBinding("o").getValue().stringValue());
            }
        });
    }

    protected void addData() throws RepositoryException {
        ValueFactory f = ValueFactoryImpl.getInstance();
        URI a = f.createURI("http://example.com/a");
        URI b = f.createURI("http://example.com/b");
        URI c = f.createURI("http://example.com/c");

        RepositoryConnection connection = repositoryManagerConnectionFactory.getConnection();
        connection.add(a, b, c);
    }
}
