@echo off
REM ==============================================================================
REM Script para Ejecución Local de TicketFlow (Spring Boot + Java 21)
REM ==============================================================================
echo ==============================================================
echo INICIANDO TICKETFLOW CLOUD NATIVE MICROSERVICE (MODO LOCAL)
echo ==============================================================
echo Java Version:
java -version
echo.
echo Compilando y ejecutando microservicio...
cd "%~dp0..\ticket-service"
"C:\Program Files\Apache NetBeans\java\maven\bin\mvn.cmd" spring-boot:run
pause
