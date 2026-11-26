# Eirmena

App Android para acompañar el ciclo menstrual, **sin internet, gratis y de código abierto**.

Eirmena une a dos diosas: **Eir**, la diosa nórdica de la sanación, y **Mena**, la diosa romana de la menstruación.

## Principios

- **Sin internet.** La app no declara el permiso `INTERNET` (y lo quita del manifiesto si alguna librería lo pidiera). No hay cuentas, servidores, publicidad ni analíticas.
- **Los datos son de la persona.** Se guardan en un solo archivo cifrado con AES-256-GCM y una clave del Android Keystore. `allowBackup` está desactivado: Android no los copia a la nube.
- **Respaldo bajo su control.** Ajustes → Exportar respaldo genera un `.json` cifrado con contraseña (PBKDF2-HMAC-SHA256 + AES-256-GCM). Se importa desde Ajustes o desde la bienvenida, y se puede combinar con los datos actuales o reemplazarlos.
- **Gratis para siempre.** Sin fines de lucro y sin versión paga.
- **No es un dispositivo médico.** Las fechas son estimaciones; no es un método anticonceptivo ni diagnostica.

## Qué trae

- **Hoy:** anillo del ciclo, fase con su explicación hormonal, próximo período con rango o retraso, anticonceptivo del día, alertas, bienestar por fase y el minijuego ¿Sabías que…?
- **Registro diario:** sangrado, 21 síntomas, ánimo, energía, sueño, deseo, sexo y protección, anticoncepción de emergencia, temperatura basal, flujo, tests de ovulación y de embarazo, notas.
- **Calendario:** período anotado y estimado, ventana fértil y ovulación (ocultas con métodos que frenan la ovulación).
- **Ciclos:** promedio, regularidad, historial, estadísticas (barras, síntomas frecuentes, mapas de calor por fase) e informe para la consulta en PDF o Markdown.
- **Etapas de vida:** ciclo, buscar embarazo (detección de ovulación por temperatura "3 sobre 6" o test de LH), embarazo semana a semana con controles y vacunas, perimenopausia y menopausia, primeros ciclos.
- **Anticonceptivos:** pastilla (21+7, placebo, 24+4, continua), parche, anillo, inyección, implante y DIU, con recordatorios.
- **Avisos locales:** período, ventana fértil, pastilla con repaso, cambios de método, registro diario y semana de embarazo; discretos por defecto.
- **Aprender:** gráfico interactivo de hormonas, asistente de retraso, señales para consultar (criterios FIGO), bienestar por fase y 23 artículos offline con fuentes.
- **Privacidad:** PIN, huella o rostro, ocultar en recientes, bloqueo a los 30 s, respaldo cifrado.

## Compilar

```
./gradlew testDebugUnitTest assembleRelease
```

La variante de depuración usa el paquete `com.byfooglesframes.eirmena.debug`, así se instala aparte sin pisar la app real. Para generar un informe PDF de ejemplo en un teléfono conectado: `./gradlew connectedDebugAndroidTest` (prueba `InformePdfTest`).

Para firmar el release hace falta `firma/firma.properties`, que no está en el repositorio. Sin ese archivo, el APK se firma con la clave de depuración.

## Licencia

GPL-3.0 (ver `LICENSE`). Cualquiera puede leer, usar y modificar el código; las versiones modificadas tienen que seguir siendo libres bajo la misma licencia.

Tipografías: Comfortaa (con nombre reservado "Comfortaa") y Outfit, sin modificar, bajo la SIL Open Font License 1.1. Los textos completos de las tres licencias están en `app/src/main/assets/licencias/` y se pueden leer desde la app, en Ajustes › Acerca de Eirmena › Licencias.

---

Desarrollado por **ByFooglesFrames**.
