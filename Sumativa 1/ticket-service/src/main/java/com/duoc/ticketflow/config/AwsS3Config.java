package com.duoc.ticketflow.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Configuration
public class AwsS3Config {

    private static final Logger log = LoggerFactory.getLogger(AwsS3Config.class);

    private final StorageProperties properties;

    public AwsS3Config(StorageProperties properties) {
        this.properties = properties;
    }

    private AwsCredentialsProvider resolveCredentialsProvider() {
        String accessKey = properties.getAws().getS3().getAccessKeyId();
        String secretKey = properties.getAws().getS3().getSecretAccessKey();
        String endpointUrl = properties.getAws().getS3().getEndpointUrl();

        // Si se proveen credenciales estáticas y no son valores dummy en AWS real
        if (StringUtils.hasText(accessKey) && StringUtils.hasText(secretKey) 
                && !accessKey.equalsIgnoreCase("default") 
                && (!accessKey.equalsIgnoreCase("test") || StringUtils.hasText(endpointUrl))) {
            return StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey));
        }
        // En AWS EC2 (incluyendo AWS Academy LabRole), utiliza la cadena de credenciales por defecto (IMDSv2)
        log.info("Utilizando DefaultCredentialsProvider (IAM Instance Profile / LabRole / AWS CLI / Env)");
        return DefaultCredentialsProvider.create();
    }

    @Bean
    public S3Client s3Client() {
        String regionStr = properties.getAws().getRegion();
        String endpointUrl = properties.getAws().getS3().getEndpointUrl();

        Region region = Region.of(StringUtils.hasText(regionStr) ? regionStr : "us-east-1");
        AwsCredentialsProvider credentialsProvider = resolveCredentialsProvider();

        S3ClientBuilder builder = S3Client.builder()
                .region(region)
                .credentialsProvider(credentialsProvider);

        if (StringUtils.hasText(endpointUrl)) {
            log.info("Inicializando S3Client con Endpoint personalizado (LocalStack / Mock): {}", endpointUrl);
            builder.endpointOverride(URI.create(endpointUrl))
                   .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build());
        } else {
            log.info("Inicializando S3Client estándar para AWS Cloud en región: {}", region.id());
        }

        return builder.build();
    }

    @Bean
    public S3Presigner s3Presigner() {
        String regionStr = properties.getAws().getRegion();
        String endpointUrl = properties.getAws().getS3().getEndpointUrl();

        Region region = Region.of(StringUtils.hasText(regionStr) ? regionStr : "us-east-1");
        AwsCredentialsProvider credentialsProvider = resolveCredentialsProvider();

        S3Presigner.Builder builder = S3Presigner.builder()
                .region(region)
                .credentialsProvider(credentialsProvider);

        if (StringUtils.hasText(endpointUrl)) {
            builder.endpointOverride(URI.create(endpointUrl))
                   .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build());
        }

        return builder.build();
    }
}
