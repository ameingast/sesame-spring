# Sesame Spring

Sesame Spring provides Spring integration for the Openrdf/Sesame library.

## Spring Transactions
The library provides a simple PlatformTransactionManager with thread-local scope for Sesame.

## Usage

Wiring up a simple in-memory repository to the transaction manager:

```xml
<context:annotation-config/>
<context:component-scan base-package="your.package.here"/>
<tx:annotation-driven transaction-manager="transactionManager"/>

<!-- Sesame repository configuration -->
<bean id="sesameRepository" class="org.openrdf.repository.sail.SailRepository" init-method="initialize">
    <constructor-arg ref="memoryStore"/>
</bean>

<bean id="memoryStore" class="org.openrdf.sail.memory.MemoryStore">
    <property name="persist" value="false"/>
</bean>

<!-- Transaction manager configuration -->
<bean id="sesameConnectionFactory" class="org.openrdf.spring.SesameConnectionFactory">
    <constructor-arg ref="sesameRepository"/>
</bean>

<bean id="transactionManager" class="org.openrdf.spring.SesameTransactionManager">
    <constructor-arg ref="sesameConnectionFactory"/>
</bean>
```

Transactions are now executed like this:

```java
public class TransactionTest {
    // Retrieve the connection factory to access the repository
    @Autowired
    private SesameConnectionFactory sesameConnectionFactory;

    @Transactional
    public void doSomethingTransactional() throws Exception {
        // Acquire the connection
        RepositoryConnection connection = sesameConnectionFactory.getConnection();

        // Create a tuple query
        connection.prepareTupleQuery(QueryLanguage.SPARQL, "SELECT ?s ?p ?o WHERE { ?s ?p ?o . }");

        // Fetch the result
        TupleQueryResult result = tupleQuery.evaluate();

        // work with the result ...
    }
}
```

The transaction manager determines whether to roll-back the transaction in case of an exception or simply to commit
the changes and close the connection once finished.