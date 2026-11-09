# Duoc UC ONLINE

**Semana 3**  
**Desarrollo Cloud Native (CDY2204)**  
**Instrucciones Específicas**

---

## Descripción de la actividad

En esta tercera semana deberán realizar una actividad sumativa de manera grupal llamada **"Construyendo y desplegando una solución Cloud Native"**, donde deberán desarrollar una aplicación a partir de un caso planteado y luego desplegarla utilizando CI/CD en la nube, en la cual deberán integrar el uso de lo aprendido en las asignaturas anteriores de Desarrollo Backend para incluir despliegue continuo en el aplicativo.

---

## Instrucciones específicas

Deberán aprovisionar y configurar componentes cloud, para integrarlos a los microservicios con Spring Boot que den solución al siguiente requerimiento:

### Caso:
Una startup desea implementar un **Sistema de Gestión de Reservas de Eventos con Tickets**. El sistema deberá cumplir con los siguientes requisitos:

### Requisitos:

1. **EFS para almacenamiento temporal:** 
   - Los tickets personalizados se generan y almacenan en el EFS asociado al microservicio.

2. **Subida automática a AWS S3:** 
   - Los tickets se guardan en AWS S3 por evento y usuario (ejemplo, `/evento123/usuarioX/ticket001.pdf`).

3. **Endpoints REST:**
   - Generar tickets.
   - Subir tickets a S3.
   - Descargar tickets por usuario o evento.
   - Modificar detalles del ticket.
   - Eliminar tickets específicos.
   - Reportar estadísticas de ventas y reservas por evento.

4. **Despliegue automatizado:**
   - Imagen Docker generada y publicada en Docker Hub al realizar un push a la rama principal.
   - Despliegue automático en una instancia EC2 utilizando GitHub Actions.

---

## Presentación 

Para la presentación de su proyecto deberán usar la herramienta de **Teams**, mostrando el correcto funcionamiento de sus microservicios, ejecución y pruebas mediante **POSTMAN**. 

Es importante destacar que deben mostrar en tiempo real todas las funcionalidades requeridas del desarrollo donde se cumplan todos los requerimientos solicitados del caso. 

---

## Grabación 

Para esto deberás generar una reunión en Teams con tu compañera (o), y hacer la presentación de lo realizado en la actividad. 

Para acceder a Teams de videoconferencias descarga la guía de instalación de office si aún no lo has hecho. Esta guía está en la introducción de la semana. 

Puedes revisar el siguiente link para conocer cómo gestionar una reunión con tu equipo en Teams:  
[https://support.microsoft.com/es-es/office/unirse-a-la-reuni%C3%B3n-de-teams-078e9868-f1aa-4414-8bb9-ee88e9236ee4](https://support.microsoft.com/es-es/office/unirse-a-la-reuni%C3%B3n-de-teams-078e9868-f1aa-4414-8bb9-ee88e9236ee4)

Luego pega el link de la grabación donde se indica, para que tu docente pueda verlo.

---

> **Importante**  
> Las temáticas desarrolladas deben cumplir con las indicaciones entregadas por el o la docente de la asignatura.   
> Consulta al docente ante dudas.

---

## Entregables

Ten en cuenta los siguientes entregables al momento de realizar tu evaluación:

- Video de la presentación.
- Link con el repositorio del proyecto.
- Zip o rar con la documentación.

*Comparte los links del repositorio en Git en el AVA al momento de adjuntar el archivo comprimido (.rar o .zip).*

---

*Reservados todos los derechos Fundación Instituto Profesional Duoc UC. No se permite copiar, reproducir, reeditar, descargar, publicar, emitir, difundir, de forma total o parcial la presente obra, ni su incorporación a un sistema informático, ni su transmisión en cualquier forma o por cualquier medio (electrónico, mecánico, fotocopia, grabación u otros) sin autorización previa y por escrito de Fundación Instituto Profesional Duoc UC. La infracción de dichos derechos puede constituir un delito contra la propiedad intelectual.*