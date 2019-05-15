.. _upgrade-notes-dropwizard-1_1_x:

##################################
Upgrade Notes for Dropwizard 1.1.x
##################################

Due to :pr:`1851`, users must now add mockito as a test dependency

.. code-block:: xml

   <dependency>
       <groupId>org.mockito</groupId>
       <artifactId>mockito-core</artifactId>
       <version>2.7.6</version>
       <scope>test</scope>
   </dependency>

Else become susceptible to the following error:

::

   java.lang.NoClassDefFoundError: org/mockito/Mockito

--------------

Due to :pr:`1695`, ``Cli`` no longer allows exceptions to propagate, (which is a net positive),
but I did have to rewrite my tests to no longer trap for exceptions but examine stderr.

--------------

If you used the Hibernate integration, you need to upgrade your data access code to Hibernate 5.2.7 from 5.1.0.
Please see the discussion of the change in :pr:`1871`.
Please also check out the `Hibernate 5.2 Migration Guide <https://github.com/hibernate/hibernate-orm/wiki/Migration-Guide---5.2>`__.
