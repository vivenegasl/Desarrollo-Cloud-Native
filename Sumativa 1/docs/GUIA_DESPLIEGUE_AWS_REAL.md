# Guía Definitiva de Aprovisionamiento y Despliegue en AWS Real
**Asignatura:** Desarrollo Cloud Native (CDY2204) - Duoc UC  
**Evaluación:** Sumativa Semana 3 - *"Construyendo y desplegando una solución Cloud Native"*  
**Meta:** Aprovisionar Amazon S3, Amazon EFS y Amazon EC2 en tu cuenta de AWS y conectar el pipeline CI/CD de GitHub Actions.

---

## 📌 Contexto y Modalidades de Acceso en Duoc UC

En Duoc UC el acceso a la nube generalmente se entrega a través de:
1. **AWS Academy Learner Lab (Vocareum):**
   - Región fija: **`us-east-1` (N. Virginia)**.
   - Rol IAM preexistente: **`LabRole`** (¡fundamental para que EC2 acceda a S3 y EFS automáticamente sin necesidad de credenciales hardcodeadas!).
2. **Cuenta de AWS directa / IAM User:**
   - Cuentas con usuario IAM y permisos concedidos por el docente.

Esta guía cubre ambos escenarios paso a paso.

---

## 🛠️ Paso 1: Crear el Bucket en Amazon S3

1. Ingresa a la **Consola de AWS** y busca el servicio **S3**.
2. Haz clic en el botón naranja **"Create bucket"** (Crear bucket).
3. Configura:
   - **Bucket name:** Un nombre único a nivel mundial, por ejemplo: `ticketflow-storage-duoc-<tus_iniciales>` (solo minúsculas, números y guiones).
   - **AWS Region:** `us-east-1` (N. Virginia).
   - **Object Ownership:** *ACLs disabled (recommended)*.
   - **Block Public Access settings for this bucket:** Mantén activada la casilla **"Block all public access"** (Bloquear todo el acceso público).
     > *Nota de arquitectura:* Nuestro microservicio accede a S3 autenticado mediante AWS SDK y entrega los archivos al cliente mediante HTTP Streaming o URLs prefirmadas (Pre-signed URLs), cumpliendo con las mejores prácticas de seguridad de AWS.
   - **Bucket Versioning:** *Enable* (opcional pero recomendado para el Criterio 3 de actualización).
   - **Default encryption:** *Server-side encryption with Amazon S3 managed keys (SSE-S3)*.
4. Haz clic en **"Create bucket"**.
5. **Anota el nombre exacto de tu bucket**, lo necesitarás más adelante.

---

## 🛡️ Paso 2: Crear los Security Groups (Grupos de Seguridad)

Ve al servicio **EC2 > Security Groups** y crea dos grupos de seguridad:

### 2.1 Grupo de Seguridad para la Instancia EC2 (`sg_ticketflow_ec2`)
- **Security group name:** `sg_ticketflow_ec2`
- **Description:** Permitir trafico SSH y API TicketFlow
- **VPC:** Tu VPC por defecto
- **Inbound rules (Reglas de entrada):**
  1. **SSH:** Type: `SSH` | Port: `22` | Source: `Anywhere-IPv4` (`0.0.0.0/0`)
  2. **API Port 8080:** Type: `Custom TCP` | Port: `8080` | Source: `Anywhere-IPv4` (`0.0.0.0/0`)
- Clic en **Create security group**.

### 2.2 Grupo de Seguridad para Amazon EFS (`sg_ticketflow_efs`)
- **Security group name:** `sg_ticketflow_efs`
- **Description:** Permitir trafico NFS desde la instancia EC2
- **VPC:** Tu VPC por defecto
- **Inbound rules (Reglas de entrada):**
  1. **NFS:** Type: `NFS` | Port: `2049` | Source: Selecciona el grupo `sg_ticketflow_ec2` creado en el paso anterior (o el CIDR de la VPC `172.31.0.0/16`).
- Clic en **Create security group**.

---

## 📂 Paso 3: Crear el Sistema de Archivos Amazon EFS (Criterio 1)

1. En la consola de AWS, busca el servicio **EFS** (Elastic File System).
2. Haz clic en **"Create file system"** (Crear sistema de archivos).
3. En la ventana emergente, haz clic en **"Customize"** (Personalizar):
   - **Name:** `ticketflow-efs`
   - **File system type:** *Regional*
   - Clic en **Next**.
4. En **Network** (Configuración de red):
   - Asegúrate de seleccionar la **VPC por defecto**.
   - En la sección **Mount targets** (Destinos de montaje), para cada Zona de Disponibilidad (Subnet), elimina el Security Group por defecto (`default`) y selecciona únicamente **`sg_ticketflow_efs`**.
5. Clic en **Next** > **Next** > **Create**.
6. Una vez creado, copia el **File System ID** (por ejemplo: `fs-0123456789abcdef0`).

---

## 💻 Paso 4: Lanzar la Instancia AWS EC2 (Criterio 6)

1. En la consola de AWS, ve a **EC2 > Instances > Launch instances**.
2. **Name:** `ticketflow-server`
3. **Application and OS Images:**
   - Selecciona **Ubuntu** (Ubuntu Server 24.04 LTS o 22.04 LTS, 64-bit x86).
4. **Instance type:**
   - Selecciona **`t2.micro`** o **`t3.micro`** (o `t3.small` si tu Learner Lab lo permite).
5. **Key pair (login):**
   - Selecciona un par de claves existente o crea uno nuevo (`ticketflow-key.pem`).
   - Guarda el archivo `.pem` en tu computador en un lugar seguro (ejemplo: `C:\Users\vlind\.ssh\ticketflow-key.pem`).
6. **Network settings:**
   - Haz clic en **Edit**.
   - **Auto-assign public IP:** *Enable* (Habilitado).
   - **Firewall (security groups):** Selecciona *Select existing security group* y marca **`sg_ticketflow_ec2`**.
7. **Configure storage:**
   - 15 GiB o 20 GiB gp3 (estándar).
8. **Advanced details (Detalles avanzados):**
   - **IAM instance profile:**
     - Si estás en **AWS Academy Learner Lab**, selecciona **`LabRole`**. Esto le otorga a la instancia permisos nativos para S3 y EFS sin requerir tokens ni claves manuales.
9. Haz clic en **"Launch instance"** (Lanzar instancia).
10. Una vez que la instancia pase a estado **Running**, copia su **Public IPv4 address** (IP pública).

---

## 🔗 Paso 5: Conectar a la Instancia y Montar Amazon EFS

Abre una terminal de PowerShell en tu equipo y conéctate por SSH a tu instancia:

```powershell
# Ajusta la ruta a tu archivo .pem y la IP de tu EC2
ssh -i "C:\ruta\a\tu-clave.pem" ubuntu@<IP_PUBLICA_EC2>
```

Una vez dentro de la terminal de Ubuntu en EC2, tienes dos formas de configurarlo:

### Opción A (Recomendada - 1 solo comando desde tu GitHub oficial):
Descarga y ejecuta directamente el script que ya está alojado en tu repositorio de GitHub. Este script **limpia automáticamente cualquier intento previo fallido**, instala Docker, utilidades NFS y monta tu EFS real (`fs-031e5e5ca691d5661`):

```bash
curl -fsSL "https://raw.githubusercontent.com/vivenegasl/Desarrollo-Cloud-Native/main/Sumativa%201/scripts/setup_ec2_efs.sh" | bash
```

---

### Opción B (Comandos manuales paso a paso si prefieres ver cada línea):

#### 1. Revertir y limpiar el intento anterior:
```bash
# Desmontar cualquier proceso previo y limpiar fstab
sudo umount -f -l /mnt/efs/tickets 2>/dev/null || true
sudo sed -i '/efs/d' /etc/fstab
```

#### 2. Instalar utilidades y montar tu EFS real:
```bash
# Instalar paquetes
sudo apt-get update -y
sudo apt-get install -y nfs-common docker.io curl
sudo systemctl enable docker
sudo systemctl start docker
sudo usermod -aG docker ubuntu

# Crear carpeta de montaje
sudo mkdir -p /mnt/efs/tickets
sudo chmod 777 /mnt/efs/tickets

# Montar tu EFS real (ID: fs-031e5e5ca691d5661)
sudo mount -t nfs4 -o nfsvers=4.1,rsize=1048576,wsize=1048576,hard,timeo=600,retrans=2,noresvport fs-031e5e5ca691d5661.efs.us-east-1.amazonaws.com:/ /mnt/efs/tickets

# Dejar el montaje persistente tras reinicios
echo "fs-031e5e5ca691d5661.efs.us-east-1.amazonaws.com:/ /mnt/efs/tickets nfs4 defaults,_netdev 0 0" | sudo tee -a /etc/fstab

# Verificar montaje
df -h /mnt/efs/tickets
```
*(Si `df -h /mnt/efs/tickets` muestra `fs-031e5e5ca691d5661.efs.us-east-1.amazonaws.com:/`, ¡el montaje fue 100% exitoso!)*

---

## 🚀 Paso 6: Configurar GitHub Secrets y Desplegar con CI/CD

El repositorio ya está configurado en: **`https://github.com/vivenegasl/Desarrollo-Cloud-Native.git`**  
La Sumativa 1 se encuentra encapsulada dentro de la carpeta `Sumativa 1/` para permitir que las futuras evaluaciones convivan en el mismo repositorio.

Para que el pipeline de GitHub Actions despliegue automáticamente en tu instancia EC2 cada vez que hagas `git push`:

1. Crea un repositorio en **GitHub** y sube el código del proyecto.
2. En GitHub, ve a **Settings > Secrets and variables > Actions > New repository secret**.
3. Agrega los siguientes 5 secretos indispensables:

| Nombre del Secret | Valor | Ejemplo |
| :--- | :--- | :--- |
| **`DOCKERHUB_USERNAME`** | Tu usuario de Docker Hub | `victorlind` |
| **`DOCKERHUB_TOKEN`** | Token de acceso generado en Docker Hub (*Account Settings > Personal access tokens*) | `dckr_pat_xxxx` |
| **`EC2_HOST`** | IP pública de tu instancia EC2 | `54.210.12.34` |
| **`EC2_USERNAME`** | Usuario del sistema operativo | `ubuntu` |
| **`EC2_SSH_KEY`** | Contenido completo de tu archivo `.pem` (incluyendo `-----BEGIN RSA PRIVATE KEY-----`) | *(Pegar todo el texto del archivo .pem)* |
| **`AWS_S3_BUCKET`** | Nombre de tu bucket creado en S3 | `ticketflow-storage-duoc-vjvl` |
| **`AWS_REGION`** | Región de AWS | `us-east-1` |

*(Opcional: Si no utilizaste el `LabRole` en EC2, agrega también `AWS_ACCESS_KEY_ID` y `AWS_SECRET_ACCESS_KEY`).*

4. Haz un commit y push a la rama `main`:
   ```powershell
   git add .
   git commit -m "feat: configurar despliegue en AWS real con EFS y S3"
   git push origin main
   ```
5. Ve a la pestaña **Actions** en tu repositorio de GitHub y observa el pipeline ejecutarse en verde:
   - ✅ Maven Build & Tests
   - ✅ Docker Build & Push a Docker Hub
   - ✅ Despliegue SSH en AWS EC2 con volumen EFS montado

---

## 📬 Paso 7: Probar la API en AWS Real con Postman

1. Abre **Postman**.
2. Selecciona el entorno **`TicketFlow AWS EC2 Producción`**.
3. En la variable `baseUrl`, reemplaza `<TU_IP_PUBLICA_EC2>` por la IP pública real de tu instancia:
   - Ejemplo: `http://54.210.12.34:8080`
4. Ejecuta el request `01 - Health Check (Actuator)`:
   - Debe responder:
     ```json
     {
       "status": "UP"
     }
     ```
5. Ejecuta el request `03 - Generar Ticket (EFS + S3 Automático)`.
6. Ve a tu consola de **AWS S3** en el navegador y abre tu bucket:
   - ¡Verás la carpeta creada exactamente según la pauta: `/evento123/usuarioX/TCK-XXXXX.pdf`!
7. Conéctate a tu EC2 por SSH y ejecuta:
   ```bash
   ls -la /mnt/efs/tickets/evento123/usuarioX/
   ```
   - ¡Constatarás el archivo físico temporal residiendo en Amazon EFS!

---

## 🎯 Conclusión para tu Video de Presentación

Al realizar la presentación en Microsoft Teams mostrando la instancia EC2 real, el bucket S3 en la consola de AWS y el montaje EFS en la terminal, obtendrás el **100% de la calificación (CL - Completamente Logrado)** destacando por encima del estándar.
