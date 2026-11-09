#!/bin/bash
# ==============================================================================
# Script User Data para AWS EC2 (Se ejecuta automáticamente al iniciar la instancia)
# Asignatura: Desarrollo Cloud Native (CDY2204) - Duoc UC
# ==============================================================================

# Variables (reemplazar con tus valores antes de lanzar o pasar como User Data)
EFS_FS_ID="fs-031e5e5ca691d5661"
AWS_REGION="us-east-1"
DOCKER_IMAGE="ticketflow-service:latest"
S3_BUCKET_NAME="ticketflow-storage-duoc-vjvl"

exec > >(tee /var/log/user-data.log|logger -t user-data -s 2>/dev/console) 2>&1
echo "=== INICIANDO PROVISIÓN AUTOMÁTICA EN AWS EC2 ==="

# 1. Actualizar paquetes e instalar dependencias NFS y Docker
apt-get update -y
apt-get install -y nfs-common curl ca-certificates

# 2. Instalar Docker Engine
install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | gpg --dearmor -o /etc/apt/keyrings/docker.gpg
chmod a+r /etc/apt/keyrings/docker.gpg

echo \
  "deb [arch="$(dpkg --print-architecture)" signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
  "$(. /etc/os-release && echo "$VERSION_CODENAME")" stable" | \
  tee /etc/apt/sources.list.d/docker.list > /dev/null

apt-get update -y
apt-get install -y docker-ce docker-ce-cli containerd.io
systemctl enable docker
systemctl start docker
usermod -aG docker ubuntu

# 3. Crear y montar Amazon EFS
mkdir -p /mnt/efs/tickets
chmod 777 /mnt/efs/tickets

# Intentar montaje NFS de EFS
EFS_DNS="${EFS_FS_ID}.efs.${AWS_REGION}.amazonaws.com"
echo "Montando EFS: ${EFS_DNS}"
mount -t nfs4 -o nfsvers=4.1,rsize=1048576,wsize=1048576,hard,timeo=600,retrans=2,noresvport "${EFS_DNS}:/" /mnt/efs/tickets || {
    echo "No se pudo montar EFS directamente por DNS, asegurando directorio local."
}

# Persistir montaje en fstab
echo "${EFS_DNS}:/ /mnt/efs/tickets nfs4 defaults,_netdev 0 0" >> /etc/fstab

echo "=== USER DATA COMPLETADO EXITOSAMENTE ==="
