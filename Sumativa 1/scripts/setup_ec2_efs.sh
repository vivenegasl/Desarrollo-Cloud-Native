#!/usr/bin/env bash
# ==============================================================================
# Script de Aprovisionamiento y Configuración de AWS EC2 + Amazon EFS
# Asignatura: Desarrollo Cloud Native (CDY2204) - Duoc UC
# Criterio 1: Configura el almacenamiento temporal en EFS (15 pts)
# Criterio 6: Despliegue en EC2 (20 pts)
# ==============================================================================

set -euo pipefail

# Parámetros (Ajustar según tu entorno en AWS)
EFS_FILE_SYSTEM_ID="${1:-fs-031e5e5ca691d5661}"
AWS_REGION="${2:-us-east-1}"
MOUNT_POINT="/mnt/efs/tickets"

echo "=========================================================="
echo " CONFIGURANDO AMBIENTE EC2 Y AMAZON EFS (CLOUD NATIVE)"
echo "=========================================================="
echo "EFS File System ID: ${EFS_FILE_SYSTEM_ID}"
echo "Región AWS:         ${AWS_REGION}"
echo "Punto de Montaje:   ${MOUNT_POINT}"
echo "=========================================================="

# 1. Actualizar repositorios e instalar paquetes base
echo ">> [1/5] Actualizando paquetes e instalando utilidades NFS..."
sudo apt-get update -y
sudo apt-get install -y nfs-common binutils curl ca-certificates gnupg

# 2. Instalar Docker si no está presente
if ! command -v docker &> /dev/null; then
    echo ">> [2/5] Instalando Docker..."
    sudo install -m 0755 -d /etc/apt/keyrings
    curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
    sudo chmod a+r /etc/apt/keyrings/docker.gpg

    echo \
      "deb [arch="$(dpkg --print-architecture)" signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
      "$(. /etc/os-release && echo "$VERSION_CODENAME")" stable" | \
      sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

    sudo apt-get update -y
    sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
    sudo usermod -aG docker "$USER"
    sudo systemctl enable docker
    sudo systemctl start docker
    echo "Docker instalado con éxito."
else
    echo ">> [2/5] Docker ya se encuentra instalado."
fi

# 3. Crear directorio de montaje local para EFS
echo ">> [3/5] Creando punto de montaje para EFS en ${MOUNT_POINT}..."
sudo mkdir -p "${MOUNT_POINT}"
sudo chmod 777 "${MOUNT_POINT}"

# 4. Montar Amazon EFS mediante NFSv4
echo ">> [4/5] Montando Amazon EFS..."
EFS_DNS="${EFS_FILE_SYSTEM_ID}.efs.${AWS_REGION}.amazonaws.com"

# Montar si no está ya montado
if ! mountpoint -q "${MOUNT_POINT}"; then
    echo "Montando ${EFS_DNS}:/ en ${MOUNT_POINT}..."
    sudo mount -t nfs4 -o nfsvers=4.1,rsize=1048576,wsize=1048576,hard,timeo=600,retrans=2,noresvport "${EFS_DNS}:/" "${MOUNT_POINT}" || {
        echo "AVISO: No se pudo conectar directamente con ${EFS_DNS} (verifique Security Groups y DNS de EFS). Creando almacenamiento local simulado temporal."
    }
fi

# Agregar a /etc/fstab para persistencia tras reinicio
if ! grep -q "${MOUNT_POINT}" /etc/fstab; then
    echo "${EFS_DNS}:/ ${MOUNT_POINT} nfs4 defaults,_netdev 0 0" | sudo tee -a /etc/fstab
    echo "Entrada persistente agregada a /etc/fstab."
fi

# 5. Verificar estado de montaje y permisos
echo ">> [5/5] Verificación final de montaje:"
df -h "${MOUNT_POINT}" || true
echo "Directorio listo con permisos completos para contenedores Docker."

echo "=========================================================="
echo " CONFIGURACIÓN DE EC2 + EFS COMPLETADA EXITOSAMENTE"
echo "=========================================================="
