package com.duoc.ticketflow.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app")
public class StorageProperties {

    private Storage storage = new Storage();
    private Aws aws = new Aws();

    public Storage getStorage() {
        return storage;
    }

    public void setStorage(Storage storage) {
        this.storage = storage;
    }

    public Aws getAws() {
        return aws;
    }

    public void setAws(Aws aws) {
        this.aws = aws;
    }

    public static class Storage {
        private Efs efs = new Efs();

        public Efs getEfs() {
            return efs;
        }

        public void setEfs(Efs efs) {
            this.efs = efs;
        }

        public static class Efs {
            private String mountPath = "./storage/efs/tickets";
            private boolean cleanOnS3Upload = false;

            public String getMountPath() {
                return mountPath;
            }

            public void setMountPath(String mountPath) {
                this.mountPath = mountPath;
            }

            public boolean isCleanOnS3Upload() {
                return cleanOnS3Upload;
            }

            public void setCleanOnS3Upload(boolean cleanOnS3Upload) {
                this.cleanOnS3Upload = cleanOnS3Upload;
            }
        }
    }

    public static class Aws {
        private String region = "us-east-1";
        private S3 s3 = new S3();

        public String getRegion() {
            return region;
        }

        public void setRegion(String region) {
            this.region = region;
        }

        public S3 getS3() {
            return s3;
        }

        public void setS3(S3 s3) {
            this.s3 = s3;
        }

        public static class S3 {
            private String bucketName = "ticketflow-storage-cloudnative";
            private String endpointUrl = "";
            private String accessKeyId = "test";
            private String secretAccessKey = "test";
            private int presignedUrlExpirationMinutes = 15;

            public String getBucketName() {
                return bucketName;
            }

            public void setBucketName(String bucketName) {
                this.bucketName = bucketName;
            }

            public String getEndpointUrl() {
                return endpointUrl;
            }

            public void setEndpointUrl(String endpointUrl) {
                this.endpointUrl = endpointUrl;
            }

            public String getAccessKeyId() {
                return accessKeyId;
            }

            public void setAccessKeyId(String accessKeyId) {
                this.accessKeyId = accessKeyId;
            }

            public String getSecretAccessKey() {
                return secretAccessKey;
            }

            public void setSecretAccessKey(String secretAccessKey) {
                this.secretAccessKey = secretAccessKey;
            }

            public int getPresignedUrlExpirationMinutes() {
                return presignedUrlExpirationMinutes;
            }

            public void setPresignedUrlExpirationMinutes(int presignedUrlExpirationMinutes) {
                this.presignedUrlExpirationMinutes = presignedUrlExpirationMinutes;
            }
        }
    }
}
