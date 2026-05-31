# Configuracion de Correo - SpaceWork

El sistema envia correos automaticamente en 3 situaciones:
- Registro de cliente nuevo (envia sus credenciales de acceso)
- Pago completado (confirmacion con detalle del pago)
- Solicitud de evaluacion (enlace con token unico)

Para que funcione, cada persona debe configurar su propia cuenta Gmail.

---

## Paso 1 — Activar verificacion en dos pasos en Gmail

1. Ir a [myaccount.google.com](https://myaccount.google.com)
2. Seguridad → Verificacion en dos pasos
3. Activarla si no esta activada (es requisito para el paso siguiente)

---

## Paso 2 — Crear una contrasena de aplicacion

1. Ir a [myaccount.google.com/apppasswords](https://myaccount.google.com/apppasswords)
2. En "Seleccionar aplicacion" elegir **Correo**
3. En "Seleccionar dispositivo" elegir **Otro** y escribir `SpaceWork`
4. Clic en **Generar**
5. Google te dara una clave de 16 caracteres como esta: `abcd efgh ijkl mnop`
6. Copiarla — solo se muestra una vez

> La contrasena de aplicacion es diferente a tu contrasena de Gmail normal.
> Nunca uses tu contrasena real de Gmail en este archivo.

---

## Paso 3 — Editar mail.properties

Abrir el archivo:
```
src/main/resources/mail.properties
```

Reemplazar los valores:

```properties
smtp.user=TU_CORREO@gmail.com
smtp.password=xxxx xxxx xxxx xxxx
smtp.from=TU_CORREO@gmail.com
```

Por los tuyos:

```properties
smtp.user=miemail@gmail.com
smtp.password=abcd efgh ijkl mnop
smtp.from=miemail@gmail.com
```

No tocar el resto del archivo.

---

## Paso 4 — Verificar que funciona

Levantar el proyecto:
```bash
mvn spring-boot:run
```

Registrar un cliente nuevo desde el sistema. Si el correo llega a la bandeja de entrada del cliente, la configuracion es correcta.

Si no llega, revisar:
- Que la contrasena de aplicacion sea correcta (16 caracteres sin espacios extra)
- Que la verificacion en dos pasos este activa en la cuenta
- La carpeta de spam del destinatario

---

## Que hacer si Gmail bloquea el envio

Algunos correos corporativos o universitarios pueden tener restricciones.
En ese caso, se puede usar Outlook/Hotmail cambiando estos valores en `mail.properties`:

```properties
smtp.host=smtp.office365.com
smtp.port=587
smtp.user=TU_CORREO@hotmail.com
smtp.password=TU_CONTRASENA_NORMAL
smtp.from=TU_CORREO@hotmail.com
```

Con Outlook no se necesita contrasena de aplicacion — se usa la contrasena normal de la cuenta.

---

## Archivos involucrados

| Archivo | Que hace |
|---------|----------|
| `src/main/resources/mail.properties` | Credenciales y configuracion SMTP |
| `src/main/java/com/spacework/util/MailService.java` | Lee el archivo y conecta con el servidor |
| `src/main/java/com/spacework/util/EmailUtil.java` | Construye el HTML de cada tipo de correo |

---

## Nota importante

El archivo `mail.properties` contiene credenciales privadas.
Si el proyecto se sube a GitHub, agregar esta linea al `.gitignore`:

```
src/main/resources/mail.properties
```

Asi cada persona que clone el proyecto configura su propia cuenta.
