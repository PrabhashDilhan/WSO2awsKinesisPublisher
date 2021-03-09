# FLUIDRA

### Estructura del proyecto:

\HazelcastMediator (Nombre del mediador):

	\pom.xml (pom padre)
	\src\main\java (carpeta que contiene el código java del mediador)
	\src\main\resources (carpeta que contiene los recursos que va a necesitar el mediador)

### Dependencias

Para el correcto funcionamiento este mediador, es necesario añadir al script de arranque del ESB (integrator.bat o integrator.sh), los siguientes parámetros:
 
`-Dhazelcast.client.config="C:\Users\usuario\custom-hazelcast-client.xml" -Dhazelcast.ignoreXxeProtectionFailures="true"`
 
Donde en el primero de ellos, se pondrá la ruta del fichero de configuración --> custom-hazelcast-client.xml.

Además de esta configuración, este mediador debe contener en la carpeta "resources" la librería de Hazelcast (imdg-price-stocks-1.0.0.jar) 

### Importar el proyecto en eclipse

Para importar un proyecto, se debe importar como proyecto maven en eclipse mediante el pom padre (el que aparece en la carpeta principal).

### Generar el jar

Lo primero que debemos hacer el compilar el proyecto de Hazelcast (imdg-price-stocks) para generar el fichero jar imdg-price-stocks-1.0.0.jar que encontraremos en la carpeta ".target".
Copiaremos dicha librería en este mediador (HazelcastMediator) dentro de la carpeta "resources" que podemos ver en la estructura del proyecto.

Tras haber copiado la librería, vamos a compilar el mediador realizando `mvn clean install` sobre el pom padre (aÃ±adiendo el profile conveniente). 
Esto generarÃ¡ un jar dentro del proyecto en la carpeta ".target" con el siguiente nombre HazelcastMediator-1.0.0.jar.

Una vez generado el jar, será necesario añadir este fichero a la carpeta <wso2ei-6.6.0>/dropins y borrar el anterior si no es necesario mantenerlo.
 