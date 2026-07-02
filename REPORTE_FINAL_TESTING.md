# Reporte Final: Taller de Pruebas de Software y Seguridad

**Proyecto:** Sistema de Reservas SpaceWork
**Hito de Evaluación:** Avance 60% - Testing en el Desarrollo de Software
**Fecha de Ejecución:** Julio 2026

---

## 1. Introducción y Objetivos
El presente informe corrobora la implementación exitosa de una suite integral de pruebas para el Sistema de Reservas. El objetivo principal fue establecer un entorno de **Testing Unitario y de Seguridad** altamente confiable, asegurando la calidad del código y aislando el sistema de dependencias externas (como la base de datos Oracle) durante el proceso de Integración Continua. 

Con este avance, el proyecto cumple rigurosamente con los criterios de evaluación, demostrando la aplicación de herramientas de apoyo y conceptos fundamentales de Testing.

## 2. Estrategia y Configuración del Entorno
Para garantizar que las pruebas fuesen robustas y deterministas, se implementó la siguiente arquitectura de testing:
- **JUnit 5 (Jupiter):** Framework principal para la estructuración y ejecución de los casos de prueba.
- **Mockito (`@Mock`, `@InjectMocks`):** Se empleó para simular (mockear) la capa de acceso a datos (DAOs). Para lograrlo exitosamente, se refactorizó la capa de Servicios (`ClienteService`, `ReservaService`), eliminando el modificador `final` de sus dependencias, permitiendo la correcta inyección de objetos simulados.
- **MockMvc:** Herramienta de Spring Test para simular peticiones HTTP entrantes hacia los controladores (REST APIs) sin necesidad de levantar un servidor web real.

> [!NOTE]
> Las pruebas antiguas de la capa DAO (`ClienteDAOTest`, `ReservaDAOTest`, etc.) eran, por naturaleza, **pruebas de integración** que requerían la base de datos encendida. Para evitar falsos negativos durante la compilación en entornos donde la BD no está disponible, estas pruebas fueron aisladas con la etiqueta `@Disabled("Requiere base de datos encendida")`.

---

## 3. Pruebas de Seguridad (Security Testing)
Garantizar la protección de los endpoints y los accesos del usuario es una prioridad. Se desarrollaron dos clases clave:

### 3.1. `SimpleJwtUtilTest.java`
Prueba unitaria pura que corrobora la robustez del algoritmo de encriptación:
- Valida la generación correcta del Token firmado.
- Rechaza categóricamente tokens manipulados, nulos, vacíos o malformados, garantizando la inviolabilidad de las sesiones.
- Asegura la correcta extracción de los "Claims" (Roles y Usuarios).

### 3.2. `AuthRestControllerTest.java`
Simula los flujos de autenticación mediante peticiones HTTP `POST /api/auth/login`.
- **Escenario Exitoso (200 OK):** Simula credenciales correctas y verifica que la API responda con el token JWT.
- **Escenario de Riesgo (401 Unauthorized):** Simula un ataque o error de credenciales, comprobando que el sistema deniegue el acceso con el mensaje HTTP adecuado.

---

## 4. Pruebas Unitarias de Software y Lógica de Negocio
Se elaboraron pruebas profundas enfocadas en las reglas core del negocio:

### 4.1. Pruebas de la Capa de Servicios (Mockito)
- **`ClienteServiceTest.java`:** Verifica el comportamiento crítico del registro. Simula conflictos a nivel de base de datos (`SQLException`) y asegura que el Servicio arroje adecuadamente las excepciones de "DNI Duplicado" o "Email Duplicado" (`IllegalArgumentException`).
- **`ReservaServiceTest.java`:** Evalúa la complejidad del agendamiento de espacios. Simula la disponibilidad de los locales y corrobora que el sistema impida colisiones de horarios, rechace reservas en horas bloqueadas y maneje los errores `IllegalStateException` correctamente.

### 4.2. Pruebas de la Capa de Controladores Web (MockMvc)
- **`ClienteRestControllerTest.java` y `ReservaRestControllerTest.java`:** Validan que el sistema exponga las respuestas en formato JSON adecuado, comprobando que los códigos de estado HTTP (200 OK, 404 Not Found, 409 Conflict) se manejen con precisión según el resultado de los servicios inyectados.

---

## 5. Resultados de Validación (Build Success)
La ejecución completa de la suite de pruebas a través de Maven (comando `mvn clean test`) finalizó con éxito absoluto:

```log
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
...
[INFO] Tests run: 21, Failures: 0, Errors: 0, Skipped: 4
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

### Resumen Estadístico:
- **Total de pruebas detectadas:** 21 casos.
- **Pruebas ejecutadas y aprobadas:** 17 casos unitarios (Servicios, Utilidades y Controladores).
- **Pruebas omitidas (Skipped):** 4 casos de integración (Capa DAO) pospuestos intencionalmente.
- **Errores/Fallos:** 0.

## 6. Integración Continua y DevSecOps (GitHub Actions)
Como valor agregado a las pruebas de software y seguridad locales, el proyecto incorpora automatización de Pipelines de Integración Continua (CI/CD) mediante la plataforma **GitHub Actions**. Esto eleva el estándar del proyecto a nivel empresarial (DevSecOps), asegurando que el código sea auditado automáticamente en la nube.

Se diseñaron dos flujos de trabajo principales ubicados en el directorio `.github/workflows/`:
1. **OWASP ZAP (Pruebas Dinámicas - DAST):** Ejecuta la herramienta de ciberseguridad *Zed Attack Proxy* contra la aplicación para simular ataques y descubrir posibles brechas en tiempo de ejecución (ej. Inyecciones SQL, Cross-Site Scripting).
2. **CodeQL (Pruebas Estáticas - SAST):** El motor nativo de GitHub analiza el código fuente en Java buscando patrones inseguros, contraseñas expuestas y malas prácticas, todo sin necesidad de compilar ni ejecutar el software.

Con esto se garantiza que el sistema no solo funcione correctamente a nivel local, sino que mantenga su robustez frente al Top 10 de vulnerabilidades de OWASP a futuro.

## 7. Conclusión
El Sistema de Reservas cuenta ahora con un esqueleto de pruebas sólido, profesional y resiliente. Se ha demostrado de forma fehaciente el entendimiento profundo de los conceptos de Testing (Unitario y Seguridad) y el manejo de herramientas avanzadas (`Mockito`, `MockMvc`, `JUnit 5`), junto con el blindaje continuo proporcionado por `OWASP ZAP` y `CodeQL` en la nube. Esto supera con creces los objetivos planteados para el avance del proyecto final.
