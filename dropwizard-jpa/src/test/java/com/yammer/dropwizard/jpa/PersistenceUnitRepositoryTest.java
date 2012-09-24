package com.yammer.dropwizard.jpa;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class PersistenceUnitRepositoryTest {

	@Test
	public void testSingleUnitPersistenceConfig() {
		PersistenceUnitRepository repo = new PersistenceUnitRepository("single-unit-persistence.xml");
		assertThat(repo.hasMultiplePersistenceUnits(), is(false));
		assertThat(repo.isPersistenceUnitAvailable("sample"), is(true));
		assertThat(repo.isPersistenceUnitAvailable("bad-sample"), is(false));
	}

	@Test
	public void testMultipleUnitPersistenceConfig() {
		PersistenceUnitRepository repo = new PersistenceUnitRepository("multiple-unit-persistence.xml");
		assertThat(repo.hasMultiplePersistenceUnits(), is(true));
		assertThat(repo.isPersistenceUnitAvailable("sample-1"), is(true));
		assertThat(repo.isPersistenceUnitAvailable("sample-2"), is(true));
		assertThat(repo.isPersistenceUnitAvailable("sample-3"), is(true));
		assertThat(repo.isPersistenceUnitAvailable("bad-sample"), is(false));
	}
}
