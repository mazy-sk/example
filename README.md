### Príklad Java EE6
EJB - JMS - JAX-WS

Build a postup inštalácie platí len pre Linux OS.

### 1. Postup pre použitie arquillian-glassfish-managed maven profilu
#### Build, instalácia GlassFish servera, vytvorenie queue, štart servera a deploy aplikácie
```
mvn -Parquillian-glassfish-managed clean package
mvn -Parquillian-glassfish-managed org.glassfish.maven.plugin:maven-glassfish-plugin:deploy -N
```
Pre otestovanie viď. časť **Testovanie aplikácie**

Ďalšie príkazy
--------------
```
mvn -Parquillian-glassfish-managed org.glassfish.maven.plugin:maven-glassfish-plugin:start-domain -N
mvn -Parquillian-glassfish-managed org.glassfish.maven.plugin:maven-glassfish-plugin:stop-domain -N
mvn -Parquillian-glassfish-managed org.glassfish.maven.plugin:maven-glassfish-plugin:deploy -N
mvn -Parquillian-glassfish-managed org.glassfish.maven.plugin:maven-glassfish-plugin:undeploy -N
```
----

### 2. Postup pre použitie arquillian-glassfish-remote maven profilu

#### Inštalácia servera
1. Stiahnuť poslednú verziu Java EE6 z achvívu : [glassfish-3.1.2.2.zip](http://download.java.net/glassfish/3.1.2.2/release/glassfish-3.1.2.2.zip)
2. Po rozbalení zip archívu treba nastaviť premenné prostredia
```
export GLASSFISH_HOME=/data/work/development/tools/glassfish-3.1.2.2
export PATH=$PATH:$GLASSFISH_HOME/bin
export EXAMPLE_PROJECT=/data/work/lumastec-ws/projects/example
```

3. Pri prvom sputení servera treba nastaviť heslo admina (user=admin, heslo=admin)
```
asadmin start-domain
```

#### Vytvorenie queue a connection factory
1. Nastaviť heslo admina v `$EXAMPLE_PROJECT/config/glassfish-remote/passwordfile` ak je iné ako "admin"
2. Spustiť
```
asadmin --user=admin --passwordfile=$EXAMPLE_PROJECT/config/glassfish-remote/passwordfile \
add-resources $EXAMPLE_PROJECT/config/glassfish-remote/glassfish-resources.xml
```

3. Vytvorené resources možno skontrolovať cez http://localhost:4848/ alebo cez asadmin
```
asadmin --user=admin --passwordfile=$EXAMPLE_PROJECT/config/glassfish-remote/passwordfile list-jms-resources
jms/ExampleQueue
jms/ConnectionFactory
```

#### Build a deploy aplikácie
```
mvn clean package -Parquillian-glassfish-remote
mvn -Parquillian-glassfish-remote org.glassfish.maven.plugin:maven-glassfish-plugin:deploy -N
```

V prípade, že počas deployovania došlo k chybe, treba pozreť do server.log
`$GLASSFISH_HOME/glassfish/domains/domain1/logs/server.log`

Ďalšie príkazy
--------------
```
mvn -Parquillian-glassfish-remote org.glassfish.maven.plugin:maven-glassfish-plugin:start-domain -N
mvn -Parquillian-glassfish-remote org.glassfish.maven.plugin:maven-glassfish-plugin:stop-domain -N
mvn -Parquillian-glassfish-remote org.glassfish.maven.plugin:maven-glassfish-plugin:deploy -N
mvn -Parquillian-glassfish-remote org.glassfish.maven.plugin:maven-glassfish-plugin:undeploy -N
```
----

#### Testovanie aplikácie
1. V `$EXAMPLE_PROJECT` je projekt pre SoapUI `example-soapui-project.xml`
2. Správy možno posielať cez Tester GlassFishu na `http://localhost:8080/example-ws/MessageService?Tester`
3. Správy možno posielať cez HermesJMS

#### Testovacie prípady
1. Request `Hello world`, Response `world`
2. Request `Something`, Response `ERROR: Valid payload is only "Hello world"`
3. Request `MapMessage`, Response 
```
Received message of wrong type: class com.sun.messaging.jms.ra.DirectMapPacket.
Allowed are only text messages!
```

pozn. Správa `MapMessage` spôsobí, že klient pošle správu typu `javax.jms.MapMessage`.
Implementovaný Message driven bean spracováva len správy `javax.jms.TextMessage`.
Temto prípad simuluje odoslanie správy inej ako TextMessage cez HermesJMS.

