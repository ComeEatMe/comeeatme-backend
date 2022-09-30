package com.comeeatme.domain.images.store.impl;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.comeeatme.domain.images.store.ImageStore;
import com.comeeatme.error.exception.InvalidImageException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
@Primary
@Profile({"prod", "dev"})
@RequiredArgsConstructor
public class AwsS3ImageStore implements ImageStore {

    @Value("${cloud.aws.s3.bucket")
    private final String bucketName;

    private final AmazonS3Client s3Client;

    public String store(Resource image, String storedName) {
        try (InputStream input = image.getInputStream()) {
            String contentType = Files.probeContentType(Path.of(image.getFilename()));
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(image.contentLength());
            metadata.setContentType(contentType);
            s3Client.putObject(new PutObjectRequest(bucketName, storedName, input, metadata)
                    .withCannedAcl(CannedAccessControlList.PublicRead));
            return s3Client.getUrl(bucketName, storedName).toString();
        } catch (IOException e) {
            throw new InvalidImageException(e);
        }
    }
}
