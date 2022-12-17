.. _upgrade-notes-dropwizard-4_0_x:

##################################
Upgrade Notes for Dropwizard 4.0.x
##################################

Transition to Jakarta EE
========================
For now, all previously released Dropwizard versions used Java/Jakarta EE dependencies under the ``javax`` namespace.
Dropwizard 4.0.x will now transition to Jakarta EE 9 components and therefore utilize the new ``jakarta`` namespace for many components.

That basically means that most of the imports of existing applications using Dropwizard 3.0.x will have to be changed from the ``javax`` to the ``jakarta`` namespace.
However, other components still use the ``javax`` namespace, so a simple search and replace could break other imports.

Jakarta EE compatibility
------------------------
As stated above, Dropwizard 4.0.x will transition to Jakarta EE 9 components. This means, Dropwizard will now try to be consistent with one specific EE version.
Therefore Dropwizard 4.0.x will stay on components of Jakarta EE 9 and the transition to components of Jakarta EE 10 will be postponed to an other release series.
Since the EE version bump will probably introduce breaking changes, the Jakarta EE 10 components will be most likely integrated in Dropwizard 5.0.x and not in a 4.1.x release.

Features of Dropwizard 4.0.x
============================
Dropwizard 4.0.x will include all features of Dropwizard 3.0.x as we plan to keep the versions (mostly) in sync.
But since the Jakarta EE library development now continues on the ``jakarta`` namespace, new features may be available only for Dropwizard 4.0.x.
Dropwizard 4.0.x may therefore include additional features that are not included in Dropwizard 3.0.x.
One such example is the upgrade to Hibernate 6.

Hibernate 6
===========
Hibernate 5.6 provides compatible implementations for JPA 2.2 and for Jakarta Persistence 3.0.
But Hibernate 6.0 and 6.1 still provide compatible implementations for Jakarta Persistence 3.0, so we upgraded to Hibernate 6.1 in Dropwizard 4.0.x.

This introduces the following changes:

 - removal of ``Criteria``: all methods taking ``Criteria`` instances as parameters are removed since it isn't supported by Hibernate 6 any more
 - removal of the restriction for ``Serializable`` keys: Hibernate reworked its type system and now every ``Object`` can be a key. Therefore parameters are changed from ``Serializable`` to ``Object``
 - removal of ``AvailableSettings.USE_NEW_ID_GENERATOR_MAPPINGS``: Dropwizard already used the default value and now this property isn't set any more

If any other aspects from Hibernate are used in an application rather than those provided by Dropwizard, there might be an additional migration cost.
Please follow the `Hibernate 6 migration guide <https://github.com/hibernate/hibernate-orm/blob/6.0/migration-guide.adoc>`_, if you encounter any problems.

Hibernate 6.0 has already reached its end-of-life, so Dropwizard 4.0.x includes Hibernate 6.1.
You may need to follow the `Hibernate 6.1 migration guide <https://github.com/hibernate/hibernate-orm/blob/6.1/migration-guide.adoc>`_ as well.
