@echo off
echo Starting ERP System...
echo.

REM Change to the ERP System directory
cd /d "e:\ERPSystem"

echo Checking database file before startup...
dir *.db
echo.

echo Running application...
REM Run the JavaFX application with all required dependencies
java --module-path "javafx-sdk-21.0.1\lib" --add-modules javafx.controls,javafx.fxml -cp "target\classes;sqlite-jdbc.jar;slf4j-api.jar;slf4j-simple.jar" com.erpsystem.MainApplication

echo.
echo Checking database file after startup...
dir *.db
echo.
echo Application closed.
pause