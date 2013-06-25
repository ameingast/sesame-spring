# Sesame Spring

[![Build Status](https://api.travis-ci.org/ameingast/sesame-spring.png)](https://travis-ci.org/ameingast/sesame-spring)

Sesame Spring provides Spring integration for the OpenRDF/Sesame library.

## Spring Transactions
The library provides a simple PlatformTransactionManager with thread-local scope for Sesame.

## Examples

### Creating a transaction manager for a single repository

You should use this approach, if you only deal with a single repository.

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
<bean id="sesameConnectionFactory" class="org.openrdf.spring.RepositoryConnectionFactory">
    <constructor-arg ref="sesameRepository"/>
</bean>

<bean id="transactionManager" class="org.openrdf.spring.SesameTransactionManager">
    <constructor-arg ref="sesameConnectionFactory"/>
</bean>
```

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
        TupleQuery tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL,
            "SELECT ?s ?p ?o WHERE { ?s ?p ?o . }");

        // Fetch the result
        TupleQueryResult result = tupleQuery.evaluate();

        // work with the result ...
    }
}
```

The transaction manager determines whether to roll-back the transaction in case of an exception or simply to commit
the changes and close the connection once finished.


### Creating a transaction manager for a repository handled by a repository manager

If you access repositories through a sesame repository-manager, you can use the RepositoryManagerConnectionFactory
and supply a repository-id either through the Spring configuration, or exchange it at run-time.

```xml
<context:annotation-config/>
<context:component-scan base-package="org.openrdf.spring"/>
<tx:annotation-driven transaction-manager="transactionManager"/>

<bean id="sesameConnectionFactory" class="org.openrdf.spring.RepositoryManagerConnectionFactory">
    <constructor-arg ref="repositoryManager"/>
    <property name="repositoryId" value="test-id"/>
</bean>

<bean id="repositoryManager" class="org.openrdf.repository.manager.LocalRepositoryManager"
      init-method="initialize" destroy-method="shutDown">
    <constructor-arg>
        <bean class="java.io.File">
            <constructor-arg value="/tmp"/>
        </bean>
    </constructor-arg>
</bean>

<bean id="transactionManager" class="org.openrdf.spring.SesameTransactionManager">
    <constructor-arg ref="repositoryManagerConnectionFactory"/>
</bean>
```

### Creating many transaction managers for multiple repositories handled by a repository manager

In the same fashion you can define many transaction managers for multiple repositories in the application-context and
access the repository through them:

```xml
<context:annotation-config/>
<context:component-scan base-package="org.openrdf.spring"/>
<tx:annotation-driven transaction-manager="test"/>
<tx:annotation-driven transaction-manager="data"/>

<bean id="repositoryManager" class="org.openrdf.repository.manager.LocalRepositoryManager"
      init-method="initialize" destroy-method="shutDown">
    <constructor-arg>
        <bean class="java.io.File">
            <constructor-arg value="/tmp"/>
        </bean>
    </constructor-arg>
</bean>

<bean name="testConnectionFactory" class="org.openrdf.spring.RepositoryManagerConnectionFactory">
    <constructor-arg name="repositoryManager" ref="repositoryManager"/>
    <constructor-arg name="repositoryId" value="test"/>
</bean>

<bean name="dataConnectionFactory" class="org.openrdf.spring.RepositoryManagerConnectionFactory">
    <constructor-arg name="repositoryManager" ref="repositoryManager"/>
    <constructor-arg name="repositoryId" value="data"/>
</bean>

<bean id="test" class="org.openrdf.spring.SesameTransactionManager">
    <constructor-arg ref="testConnectionFactory"/>
</bean>

<bean id="data" class="org.openrdf.spring.SesameTransactionManager">
    <constructor-arg ref="dataConnectionFactory"/>
</bean>
```

```java
public class TransactionTest {
    // Retrieve the connection factory to access the repository
    @Autowired
    private SesameConnectionFactory testConnectionFactory;

    @Autowired
    private SesameConnectionFactory dataConnectionFactory;

    @Transactional("test")
    public void doSomethingTransactionalWithTheTestRepository() throws Exception {
        // Acquire the connection
        RepositoryConnection connection = testConnectionFactory.getConnection();

        // Create a tuple query
        TupleQuery tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL,
            "SELECT ?s ?p ?o WHERE { ?s ?p ?o . }");

        // Fetch the result
        TupleQueryResult result = tupleQuery.evaluate();

        // work with the result ...
    }

    @Transactional("data")
    public void doSomethingTransactionalWithTheDataRepository() throws Exception {
        // Acquire the connection
        RepositoryConnection connection = dataConnectionFactory.getConnection();

        // Create a tuple query
        TupleQuery tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL,
            "SELECT ?s ?p ?o WHERE { ?s ?p ?o . }");

        // Fetch the result
        TupleQueryResult result = tupleQuery.evaluate();

        // work with the result ...
    }
}
```