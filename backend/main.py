from fastapi import FastAPI, HTTPException, Query, BackgroundTasks
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import Optional
import mysql.connector
from datetime import datetime, date, timedelta
import random
import os
import smtplib
from email.mime.text import MIMEText
from email.mime.multipart import MIMEMultipart

# ==================== CONFIG ====================
GMAIL_USER = os.environ.get("GMAIL_USER", "noreplymonte2@gmail.com")
GMAIL_PASS = os.environ.get("GMAIL_PASS", "")

DB_CONFIG = {
    "host": os.environ.get("MYSQLHOST", "junction.proxy.rlwy.net"),
    "port": int(os.environ.get("MYSQLPORT", "16661")),
    "user": os.environ.get("MYSQLUSER", "root"),
    "password": os.environ.get("MYSQL_ROOT_PASSWORD", ""),
    "database": os.environ.get("MYSQLDATABASE", "railway"),
}

app = FastAPI()
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


def conectar():
    return mysql.connector.connect(**DB_CONFIG)


# ==================== MODELOS ====================
class LoginRequest(BaseModel):
    email: str
    password: str


class RegistroClienteRequest(BaseModel):
    nombre: str
    apellido_paterno: str
    apellido_materno: Optional[str] = None
    email: str
    password: str
    curp: Optional[str] = None
    telefono: Optional[str] = None
    direccion: Optional[str] = None
    no_identificacion: Optional[str] = None


class VerificarEmailRequest(BaseModel):
    email: str
    codigo: str


class RecuperacionRequest(BaseModel):
    email: str
    codigo: str
    nueva_password: str


class PrestamoRequest(BaseModel):
    id_cliente: int
    monto: float
    plazo_meses: int


class AprobarPrestamoRequest(BaseModel):
    id_prestamo: int
    accion: str  # aprobar | rechazar
    id_empleado: int  # requerido por robustez/integridad


class CrearEmpleadoRequest(BaseModel):
    nombre: str
    apellido_paterno: str
    apellido_materno: Optional[str] = None
    email: str
    password: str
    telefono: Optional[str] = None


class RegistrarPagoRequest(BaseModel):
    id_pago: int
    id_empleado: int


class ConfiguracionRequest(BaseModel):
    valor: str


# ==================== EMAIL ====================
def enviar_email(destinatario: str, asunto: str, html: str):
    try:
        msg = MIMEMultipart("alternative")
        msg["Subject"] = asunto
        msg["From"] = f"Monte de Piedad <{GMAIL_USER}>"
        msg["To"] = destinatario
        msg.attach(MIMEText(html, "html", "utf-8"))

        with smtplib.SMTP_SSL("smtp.gmail.com", 465) as server:
            server.login(GMAIL_USER, GMAIL_PASS.replace(" ", ""))
            server.sendmail(GMAIL_USER, destinatario, msg.as_string())
        print(f"✅ Email enviado a {destinatario}")
    except Exception as e:
        print(f"❌ Error enviando email a {destinatario}: {e}")


def email_bienvenida(destinatario: str, nombre: str):
    html = f"<h2>¡Bienvenido, {nombre}!</h2><p>Tu cuenta ha sido creada exitosamente.</p>"
    enviar_email(destinatario, f"🎉 ¡Bienvenido a Monte de Piedad, {nombre}!", html)


def email_codigo_recuperacion(destinatario: str, codigo: str, nombre: str = "Usuario"):
    html = f"<h2>Recuperación de contraseña</h2><p>Hola {nombre}, tu código es: <b>{codigo}</b></p>"
    enviar_email(destinatario, f"🔐 Código de Recuperación: {codigo}", html)


# ==================== ENDPOINTS BASE ====================
@app.get("/")
def root():
    return {"app": "Monte SIN Piedad API", "version": "2.1", "status": "✅ Operativo"}


@app.post("/login")
def login_unificado(request: LoginRequest):
    db = conectar()
    cursor = db.cursor(dictionary=True)
    try:
        cursor.execute(
            """
            SELECT id_usuario, nombre, apellido_paterno, apellido_materno,
                   email, rol, activo, email_verificado, curp, telefono
            FROM usuarios
            WHERE email = %s AND password = %s
            """,
            (request.email, request.password),
        )
        usuario = cursor.fetchone()
        if not usuario:
            raise HTTPException(status_code=401, detail="Credenciales incorrectas")
        if not usuario.get("activo", True):
            raise HTTPException(status_code=403, detail="Cuenta desactivada")
        if not usuario.get("email_verificado", False):
            raise HTTPException(status_code=403, detail="Email no verificado. Revisa tu correo.")

        return {
            "status": "success",
            "message": "Login exitoso",
            "usuario": {
                "id_usuario": usuario["id_usuario"],
                "nombre": usuario["nombre"],
                "apellido_paterno": usuario.get("apellido_paterno", ""),
                "apellido_materno": usuario.get("apellido_materno", ""),
                "email": usuario["email"],
                "rol": usuario["rol"],
                "curp": usuario.get("curp"),
                "telefono": usuario.get("telefono"),
                "email_verificado": bool(usuario.get("email_verificado", False)),
            },
        }
    finally:
        cursor.close()
        db.close()


# ==================== PASO B (timeout) ====================
@app.post("/registrar_cliente")
def registrar_cliente(request: RegistroClienteRequest, background_tasks: BackgroundTasks):
    db = conectar()
    cursor = db.cursor()
    try:
        cursor.execute("SELECT id_usuario FROM usuarios WHERE email = %s", (request.email,))
        if cursor.fetchone():
            raise HTTPException(status_code=400, detail="El email ya está registrado")

        if request.curp:
            cursor.execute("SELECT id_usuario FROM usuarios WHERE curp = %s", (request.curp,))
            if cursor.fetchone():
                raise HTTPException(status_code=400, detail="El CURP ya está registrado")

        codigo_verificacion = str(random.randint(100000, 999999))
        cursor.execute(
            """
            INSERT INTO usuarios
            (nombre, apellido_paterno, apellido_materno, email, password, rol,
             curp, telefono, direccion, no_identificacion, activo,
             email_verificado, codigo_verificacion, fecha_codigo_verificacion)
            VALUES (%s, %s, %s, %s, %s, 'Cliente', %s, %s, %s, %s, TRUE, TRUE, %s, NOW())
            """,
            (
                request.nombre,
                request.apellido_paterno,
                request.apellido_materno,
                request.email,
                request.password,
                request.curp,
                request.telefono,
                request.direccion,
                request.no_identificacion,
                codigo_verificacion,
            ),
        )
        db.commit()
        id_cliente = cursor.lastrowid

        # Asíncrono: no bloquea respuesta HTTP
        background_tasks.add_task(email_bienvenida, request.email, request.nombre)

        return {
            "status": "success",
            "message": "Registro exitoso. Ya puedes iniciar sesión.",
            "requiere_verificacion": False,
            "id_cliente": id_cliente,
        }
    except HTTPException:
        raise
    except Exception as e:
        db.rollback()
        raise HTTPException(status_code=500, detail=str(e))
    finally:
        cursor.close()
        db.close()


@app.post("/solicitar_codigo")
def solicitar_codigo_recuperacion(email: str = Query(...), background_tasks: BackgroundTasks = None):
    db = conectar()
    cursor = db.cursor(dictionary=True)
    try:
        cursor.execute("SELECT id_usuario, nombre FROM usuarios WHERE email = %s", (email,))
        usuario = cursor.fetchone()
        if not usuario:
            raise HTTPException(status_code=404, detail="El correo no está registrado")

        codigo = str(random.randint(100000, 999999))
        cursor.execute(
            "UPDATE usuarios SET codigo_recuperacion = %s, fecha_codigo = NOW() WHERE email = %s",
            (codigo, email),
        )
        db.commit()

        # Asíncrono: no bloquea respuesta HTTP
        if background_tasks:
            background_tasks.add_task(email_codigo_recuperacion, email, codigo, usuario["nombre"])

        return {"status": "success", "message": f"Código enviado a {email}"}
    except HTTPException:
        raise
    except Exception as e:
        db.rollback()
        raise HTTPException(status_code=500, detail=str(e))
    finally:
        cursor.close()
        db.close()


# ==================== PASO C + D (admin/empleado robustez) ====================
@app.post("/admin/aprobar_prestamo")
def procesar_prestamo(request: AprobarPrestamoRequest):
    if request.accion not in ["aprobar", "rechazar"]:
        raise HTTPException(status_code=400, detail="Acción inválida. Usa 'aprobar' o 'rechazar'")
    if request.id_empleado <= 0:
        raise HTTPException(status_code=400, detail="id_empleado es obligatorio y debe ser válido")

    db = conectar()
    cursor = db.cursor(dictionary=True)
    try:
        cursor.execute("SELECT * FROM prestamos WHERE id_prestamo = %s", (request.id_prestamo,))
        prestamo = cursor.fetchone()
        if not prestamo:
            raise HTTPException(status_code=404, detail="Préstamo no encontrado")
        if prestamo["estado"] != "PENDIENTE":
            raise HTTPException(status_code=400, detail="El préstamo no está en estado PENDIENTE")

        if request.accion == "aprobar":
            capital = float(prestamo["monto_total"])
            plazo = int(prestamo["plazo_meses"])
            tasa = float(prestamo["tasa_interes"])
            cuota = capital * (tasa * (1 + tasa) ** plazo) / ((1 + tasa) ** plazo - 1)
            saldo = round(cuota * plazo, 2)
            hoy = date.today()

            cursor.execute(
                """
                UPDATE prestamos
                SET estado='ACTIVO', saldo_pendiente=%s, fecha_aprobacion=NOW(), id_aprobador=%s
                WHERE id_prestamo=%s
                """,
                (saldo, request.id_empleado, request.id_prestamo),
            )

            for i in range(1, plazo + 1):
                fecha_venc = hoy + timedelta(days=30 * i)
                cursor.execute(
                    """
                    INSERT INTO pagos (id_prestamo, numero_pago, fecha_vencimiento, monto, estado)
                    VALUES (%s, %s, %s, %s, 'pendiente')
                    """,
                    (request.id_prestamo, i, fecha_venc, round(cuota, 2)),
                )

            db.commit()
            return {"status": "success", "message": f"Préstamo aprobado. Se generaron {plazo} pagos."}

        cursor.execute(
            "UPDATE prestamos SET estado='RECHAZADO', id_aprobador=%s WHERE id_prestamo=%s",
            (request.id_empleado, request.id_prestamo),
        )
        db.commit()
        return {"status": "success", "message": "Préstamo rechazado."}
    except HTTPException:
        raise
    except Exception as e:
        db.rollback()
        raise HTTPException(status_code=500, detail=str(e))
    finally:
        cursor.close()
        db.close()


@app.get("/admin/estadisticas")
def obtener_estadisticas():
    """
    Campos EXACTOS esperados por Android:
    total_clientes, prestamos_activos, capital_otorgado, saldo_pendiente, monto_recuperado
    """
    db = conectar()
    cursor = db.cursor(dictionary=True)
    try:
        cursor.execute("SELECT COUNT(*) AS total FROM usuarios WHERE rol='Cliente' AND activo=TRUE")
        total_clientes = int(cursor.fetchone()["total"])

        cursor.execute("SELECT COUNT(*) AS total FROM prestamos WHERE estado='ACTIVO'")
        prestamos_activos = int(cursor.fetchone()["total"])

        cursor.execute("SELECT COALESCE(SUM(monto_total),0) AS total FROM prestamos")
        capital_otorgado = float(cursor.fetchone()["total"])

        cursor.execute("SELECT COALESCE(SUM(saldo_pendiente),0) AS total FROM prestamos WHERE estado IN ('ACTIVO','MOROSO')")
        saldo_pendiente = float(cursor.fetchone()["total"])

        cursor.execute("SELECT COALESCE(SUM(monto_total - saldo_pendiente),0) AS total FROM prestamos")
        monto_recuperado = float(cursor.fetchone()["total"])

        return {
            "status": "success",
            "total_clientes": total_clientes,
            "prestamos_activos": prestamos_activos,
            "capital_otorgado": capital_otorgado,
            "saldo_pendiente": saldo_pendiente,
            "monto_recuperado": monto_recuperado,
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
    finally:
        cursor.close()
        db.close()


@app.get("/empleado/pagos_pendientes")
def obtener_pagos_pendientes():
    """
    Campos EXACTOS esperados por Android PagoPendiente:
    id_pago, numero_pago, monto, fecha_vencimiento, id_prestamo,
    monto_total, saldo_pendiente, nombre_cliente, telefono
    """
    db = conectar()
    cursor = db.cursor(dictionary=True)
    try:
        cursor.execute(
            """
            SELECT
                g.id_pago,
                g.numero_pago,
                g.monto,
                g.fecha_vencimiento,
                p.id_prestamo,
                p.monto_total,
                p.saldo_pendiente,
                CONCAT(u.nombre, ' ', u.apellido_paterno) AS nombre_cliente,
                u.telefono
            FROM pagos g
            JOIN prestamos p ON g.id_prestamo = p.id_prestamo
            JOIN usuarios u ON p.id_cliente = u.id_usuario
            WHERE g.estado = 'pendiente' AND p.estado IN ('ACTIVO', 'MOROSO')
            ORDER BY g.fecha_vencimiento ASC
            """
        )
        pagos = cursor.fetchall()
        for p in pagos:
            if p.get("fecha_vencimiento") and hasattr(p["fecha_vencimiento"], "isoformat"):
                p["fecha_vencimiento"] = p["fecha_vencimiento"].isoformat()
            p["monto"] = float(p.get("monto") or 0)
            p["monto_total"] = float(p.get("monto_total") or 0)
            p["saldo_pendiente"] = float(p.get("saldo_pendiente") or 0)
        return pagos
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
    finally:
        cursor.close()
        db.close()


if __name__ == "__main__":
    import uvicorn

    uvicorn.run(app, host="0.0.0.0", port=8000)
