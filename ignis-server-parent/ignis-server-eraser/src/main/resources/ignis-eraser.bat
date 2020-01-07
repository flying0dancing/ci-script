SET DIR=%~dp0

SET IGNIS_ERASER_HOME=%DIR%

SET JAR_FILE=ignis-server-eraser-exec.jar
SET JAVA_OPTS=-Xmx512m -Dspring.config.location=%IGNIS_ERASER_HOME%application.properties

SET ignisEraserScript=java -jar %JAR_FILE% %JAVA_OPTS%

echo "Starting Ignis Eraser..."
%ignisEraserScript%
