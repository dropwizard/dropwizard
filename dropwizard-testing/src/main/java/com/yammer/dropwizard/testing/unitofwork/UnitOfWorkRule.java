package com.yammer.dropwizard.testing.unitofwork;
import org.hibernate.SessionFactory;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

/**
 * Add to your JUnit4 test:
 * @Rule
 * public UnitOfWorkRule unitOfWorkRule = new UnitOfWorkRule("conf/myproject-configuration-test.json", ... entity classes);
 *
 * and test your DAO layer under transaction.
 * By default all updates will be rollback.
 * If commit needed - use new UnitOfWorkRule(...).commitDefault();
 */

public class UnitOfWorkRule extends TestWatcher {

    UnitOfWorkHelper unitOfWorkHelper = new UnitOfWorkHelper();
    boolean commitDefault = false;

    public UnitOfWorkRule(String config, Class<?>... entities){
        try {
            unitOfWorkHelper.initDB(config, entities);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public UnitOfWorkRule commitDefault(){
        commitDefault = true;
        return this;
    }

    @Override
    protected void starting(Description description) {
        unitOfWorkHelper.startSession();
    }

    @Override
    protected void finished(Description description) {
        if(commitDefault)
            unitOfWorkHelper.commitAndCloseSession();
        else
            unitOfWorkHelper.rollbackAndCloseSession();
    }

    public SessionFactory getSessionFactory() {
        return unitOfWorkHelper.getSessionFactory();
    }
}