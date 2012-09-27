package com.yammer.dropwizard.jpa.jersey;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceUnit;
import javax.ws.rs.ext.Provider;

import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;
import com.yammer.dropwizard.jpa.PersistenceUnitRepository;

@Provider
public class EntityManagerFactoryInjectableProvider implements InjectableProvider<PersistenceUnit, Type> {

	private final Map<String, EntityManagerFactory> EMFs = new ConcurrentHashMap<String, EntityManagerFactory>();
	private PersistenceUnitRepository repository;
	
	public EntityManagerFactoryInjectableProvider() {
		repository = new PersistenceUnitRepository();
	}

	@Override
	public ComponentScope getScope() {
		return ComponentScope.Singleton;
	}

	@Override
	public Injectable<EntityManagerFactory> getInjectable(ComponentContext ic, PersistenceUnit a, Type type) {
		if(type.equals(EntityManagerFactory.class)) {
			String unitName = a.unitName();
			if(unitName == null && !repository.hasMultiplePersistenceUnits()) {
				unitName = repository.getDefaultPersistenceUnit();
			}
			if(!EMFs.containsKey(unitName)) {
				EMFs.put(unitName, getEntityManagerFactory(unitName));
			}
			return new EMFInjectable(EMFs.get(unitName));
		}
		return null;
	}
	
	protected EntityManagerFactory getEntityManagerFactory(String unitName) {
		return Persistence.createEntityManagerFactory(unitName);
	}
	
	public static final class EMFInjectable implements Injectable<EntityManagerFactory> {
		private EntityManagerFactory emf;

		EMFInjectable(EntityManagerFactory emf) {
			this.emf = emf;
		}

		@Override
		public EntityManagerFactory getValue() {
			return emf;
		}
	}
}
