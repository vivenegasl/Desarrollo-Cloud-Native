#!/bin/bash
echo "Inicializando bucket S3 en LocalStack..."
awslocal s3 mb s3://ticketflow-storage-duoc-vjvl
echo "Bucket ticketflow-storage-duoc-vjvl creado exitosamente."
