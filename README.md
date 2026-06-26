# ConstruPlan - Sistema de Gestión de Planillas y Tareo en Construcción Civil

Este documento sirve como la guía técnica definitiva del proyecto **ConstruPlan**..
Lenguaje de Programación II

---

## 1. Propósito de Negocio (El "Porqué" del Proyecto)
En el sector de construcción civil, la gestión de la mano de obra presenta particularidades:
* **Fuerza laboral híbrida:** Existen ingenieros y personal administrativo que requieren acceso total al sistema web (perfiles de oficina y campo), pero también obreros/operarios (peones, oficiales) que no interactúan de forma directa con la tecnología.
* **Tareo diario y control de producción:** Los operarios son asignados a tareas específicas diariamente. Su salario y rendimiento dependen de la modalidad de trabajo:
  * **Jornal:** Pago basado en las horas del día laboradas.
  * **Tarea:** Pago asociado al cumplimiento de un volumen de avance específico (destajo).

**ConstruPlan** unifica el control de personal, proyectos, asignaciones diarias, metas operativas y acceso al sistema en una solución web integrada.

---

## 2. Arquitectura de Software y Módulos
El proyecto está construido sobre **Spring Boot 3.x** utilizando un patrón arquitectónico por capas combinado con una organización modular.

```
src/main/java/com/construplan/
├── admin/                 # Gestión administrativa de proyectos y recursos globales
│   ├── controller/        # Controladores web del panel de proyectos
│   ├── model/entity/      # Entidades persistentes (Proyecto)
│   ├── repository/        # Repositorios JPA para base de datos
│   └── service/           # Lógica transaccional de proyectos
│
├── campo/                 # Planificación y tareo en obra
│   ├── controller/        # Flujos de asignación, tareas y metas
│   ├── model/entity/      # Entidades operativas (Tarea, Meta, AsignacionTarea, Modalidad)
│   ├── repository/        # Repositorios para persistir asignaciones
│   └── service/           # Lógica de asignación y cálculo de avance
│
├── empleado/              # Administración del personal de obra
│   ├── controller/        # Dashboard interactivo del empleado
│   └── model/entity/      # Entidad Empleado
│
├── config/                # Configuraciones transversales (Spring Security, JPA, Jasper)
├── controller/            # Controladores transversales (Login, Auth global)
├── model/                 # Entidades globales del sistema
│   ├── dto/               # Data Transfer Objects
│   └── entity/            # Entidades comunes (Usuario, Rol)
│
├── repository/            # Repositorios comunes (Usuario, Empleado)
├── service/               # Servicios transversales (UsuarioDetailsService, EmpleadoService)
└── util/                  # Clases utilitarias comunes
```

---

## 3. Modelo de Datos y Relaciones Críticas

Para comprender y modificar la base de datos sin romper el modelo físico:

```mermaid
classDiagram
    class Usuario {
        +Integer id
        +String username
        +String password
        +Rol rol
        +boolean activo
        +LocalDateTime fechaCreacion
    }
    class Empleado {
        +Integer idEmpleado
        +Usuario usuario
        +String nombres
        +String apellidos
        +String dni
        +String categoria
        +boolean activo
    }
    class Proyecto {
        +int idProyecto
        +String nombre
        +String descripcion
        +boolean activo
    }
    class Tarea {
        +int idTarea
        +String nombre
        +String descripcion
        +String unidadMedida
    }
    class Meta {
        +int idMeta
        +Tarea tarea
        +BigDecimal cantidad
        +BigDecimal horasEquivalentes
        +boolean esLibre
    }
    class AsignacionTarea {
        +int idAsignacion
        +Empleado empleado
        +Usuario asignador
        +Proyecto proyecto
        +Meta meta
        +LocalDate fecha
        +Modalidad modalidad
        +LocalTime horaMetaCompletada
    }

    Usuario "1" --? "1" Empleado : relación_opcional (one-to-one)
    Meta "*" --> "1" Tarea : pertenece_a (many-to-one)
    AsignacionTarea "*" --> "1" Empleado : asignado_a (many-to-one)
    AsignacionTarea "*" --> "1" Usuario : asignado_por (many-to-one)
    AsignacionTarea "*" --> "1" Proyecto : pertenece_a (many-to-one)
    AsignacionTarea "*" --> "1" Meta : tiene_como_objetivo (many-to-one)
```

### Relaciones que una IA debe respetar al programar:
1. **`Usuario <-> Empleado` (Opcional - `@OneToOne`):** Un `Empleado` puede no tener un `Usuario` asignado si solo trabaja en campo sin acceder al sistema. Sin embargo, un usuario con el rol `EMPLEADO` **siempre** debe tener un `Empleado` asociado. 
2. **`AsignacionTarea` (Entidad Central):** Actúa como el tareo diario de la obra. Enlaza un trabajador (`Empleado`), el supervisor que lo asignó (`Usuario` asignador), el frente de trabajo (`Proyecto`), y lo que debe hacer (`Meta`).
3. **`Meta -> Tarea` (`@ManyToOne`):** Las metas traducen una tarea genérica a valores específicos de rendimiento (ej: Tarea "Vaciado de concreto", Meta "15 metros cúbicos, equivalente a 8 horas").

---

## 4. Flujo de Seguridad y Autorización
La seguridad se configura en [SecurityConfig.java](file:///src/main/java/com/construplan/config/SecurityConfig.java). 

* **Autenticación:** Es manejada por Spring Security mediante el proveedor de base de datos (`DaoAuthenticationProvider`), utilizando credenciales encriptadas de la tabla `usuario`.
* **Roles y Redirección (Enum `Rol`):** Cada rol define su URL de inicio (dashboard) y su autoridad correspondiente:
  * `ADMIN` (`ROLE_ADMIN`) -> `/admin/dashboard`
  * `OFICINA` (`ROLE_OFICINA`) -> `/oficina/dashboard`
  * `CAMPO` (`ROLE_CAMPO`) -> `/campo/dashboard`
  * `EMPLEADO` (`ROLE_EMPLEADO`) -> `/empleado/dashboard`
* **Protección CSRF:** Activada por defecto en formularios POST (`th:action` inyecta automáticamente el token de seguridad).

---

## 5. Directrices de Programación para Modificaciones (Guía de IA)

Si eres una Inteligencia Artificial encargada de agregar features o corregir bugs, debes seguir estrictamente estas pautas para mantener la armonía del diseño:

### A. Política de Idioma
* **Código Fuente (variables, nombres de funciones, clases, campos JPA):** **Únicamente en Inglés** (ej. `id`, `username`, `createdAt`, `registerEmployee`).
* **Comentarios y Explicaciones de código:** **Únicamente en Español**. No comentes el "qué" hace el código (eso se debe deducir del código limpio), explica el **"porqué"** se decidió implementar esa lógica.

### B. Buenas Prácticas de Código Limpio
* **Retorno Temprano (Early Return):** Evita anidaciones profundas de bloques `if/else`. Si un parámetro es inválido o no se cumple una condición inicial, sal del método inmediatamente usando guards.
* **Inmutabilidad:** Evita mutar objetos directamente en servicios. En su lugar, usa constructores limpios o el patrón Builder provisto por Lombok.
* **Transaccionalidad (`@Transactional`):** Asegura que cualquier método en las clases de servicio que realice múltiples operaciones de escritura (creación, edición, eliminación) esté anotado con `@Transactional` de Spring para garantizar la atomicidad.
* **Nombres Auto-documentados:** No uses abreviaciones confusas. Prefiere `employeeRepository` sobre `empRepo`, o `AsignacionTarea` sobre `AsigTarea`.

### C. Trabajo con Vistas (Thymeleaf)
* **Datos dinámicos de sesión:** Para mostrar el usuario actual en la interfaz, utiliza la variable de seguridad `${#authentication.name}` en lugar de inyectar variables duplicadas desde los controladores.
* **Seguridad en Formularios:** Cualquier formulario de actualización o creación vía POST debe incluir el token CSRF para evitar denegación de acceso en el servidor:
  ```html
  <input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}"/>
  ```
* **Nombres de atributos en el Modelo:** Al interactuar con el modelo de Thymeleaf, valida que el nombre del atributo coincida con los campos de la entidad JPA (por ejemplo, utilizar `${u.id}` en lugar de `${u.idUsuario}`).

---

## 6. Configuración de Desarrollo y Despliegue
* **Base de datos local:** MySQL en puerto por defecto (`3306`) con el esquema `construplan`.
* **Esquema automático:** La propiedad `spring.jpa.hibernate.ddl-auto` está configurada en `validate`. **Importante:** Cualquier cambio en las entidades JPA requiere que la base de datos MySQL local sea actualizada previamente mediante scripts SQL (DDL) antes de arrancar el servidor web.
* **Ejecución del proyecto:**
  ```bash
  ./mvnw spring-boot:run
  ```
  El servidor iniciará localmente en el puerto `8080` ([http://localhost:8080](http://localhost:8080)).
