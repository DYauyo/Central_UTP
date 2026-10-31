# RestoControl — Central

Sistema web de pedidos e inventarios para restaurante (TERMAIN S.A.C.).

**Stack:** Java 17 · Jakarta EE 10 (Servlets, JSP/JSTL, JSF 4 + CDI) · MySQL 8 · OpenPDF · proyecto NetBeans (Ant)

## Módulos

- Login con roles: Administrador (1), Cajero (2), Mesero (3), Cocina (4), Almacén (5), Gerente (6)
- Mesas y pedidos (salón, delivery, para llevar, reservas)
- Cocina (JSF) y Caja (JSF) con comprobante en PDF
- Productos, categorías, menú e insumos (inventario)
- Usuarios
- Panel y reportes en PDF
- Idiomas: español, inglés, italiano y portugués

## Estructura

```
src/java/
  bean/         Beans JSF (CDI): pedidos, caja, cocina, idioma
  config/       Conexión a MySQL (ConexionBD.java)
  controlador/  Servlets y filtros (seguridad, encoding)
  dao/          Acceso a datos (JDBC)
  modelo/       Clases del dominio
  util/         Generación de PDF (comprobantes y reportes)
  i18n/         Textos por idioma
web/
  login.jsp, dashboard.jsp
  jsf/          Pantallas JSF: pedidos, caja, cocina
  vistas/       Pantallas JSP por módulo
  assets/       CSS, fuentes e imágenes
lib/            Librerías (MySQL Connector, JSTL, OpenPDF)
scripts/        Scripts de arranque (Windows)
```

## Requisitos

- JDK 17
- MySQL 8 (o MariaDB de XAMPP) con la base de datos `sistema_restaurante2`
- GlassFish 7 **o** Apache Tomcat 10.1

## Base de datos

La conexión está en `src/java/config/ConexionBD.java`:

| Parámetro | Valor |
|---|---|
| Host | `localhost:3306` |
| Base de datos | `sistema_restaurante2` |
| Usuario | `root` |

Cambia usuario y contraseña ahí si tu MySQL usa otros.

## Ejecutar en GlassFish 7 (NetBeans)

1. Abrir la carpeta del proyecto en NetBeans y registrar GlassFish 7 como servidor.
2. *Clean and Build* y luego *Run* (F6).
3. Abrir `http://localhost:8080/RestoControl/login.jsp` (el puerto puede variar).

Atajo en Windows: `levantar.bat` (inicia MySQL y GlassFish y despliega `dist/RestoControl.war`).

## Ejecutar en Apache Tomcat 10.1

Tomcat no incluye JSF ni CDI, así que el WAR para Tomcat lleva además
**Mojarra 4.0** (`org.glassfish:jakarta.faces`) y **Weld 5.1** (`org.jboss.weld.servlet:weld-servlet-shaded`).
Se descargan de Maven Central en `lib/tomcat/` y no afectan el build de NetBeans/GlassFish.

**Automático (Windows):** doble clic en `levantar_tomcat.bat`.
Descarga las librerías si faltan, compila, arma `dist/RestoControl_tomcat.war`, lo copia a `webapps`,
inicia Tomcat y abre `http://localhost:8080/RestoControl/login.jsp`.
El detalle queda en `scripts/levantar_tomcat.log`.

**Manual:**
1. Ejecutar `scripts/descargar_libs_tomcat.bat` (o copiar los dos `.jar` en `lib/tomcat/`).
2. Compilar `src/java` con el classpath `TOMCAT/lib/*`, `lib/*` y `lib/tomcat/*` y armar el WAR con:
   - el contenido de `web/` (sin `WEB-INF/glassfish-web.xml`)
   - las clases y `i18n/*.properties` en `WEB-INF/classes`
   - `lib/*.jar` y `lib/tomcat/*.jar` en `WEB-INF/lib`
   - `WEB-INF/beans.xml` (CDI, `bean-discovery-mode="annotated"`)
   - `META-INF/context.xml` con el recurso `BeanManager` de Weld
3. Copiar el WAR como `RestoControl.war` en `TOMCAT/webapps` y ejecutar `TOMCAT/bin/startup.bat`.
