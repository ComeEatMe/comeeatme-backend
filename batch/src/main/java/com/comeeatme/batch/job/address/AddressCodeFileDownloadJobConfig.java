package com.comeeatme.batch.job.address;

import com.comeeatme.batch.property.FileProperty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

@RequiredArgsConstructor
@Configuration
@Slf4j
public class AddressCodeFileDownloadJobConfig {

    private static final String JOB_NAME = "AddressCodeFileDownloadJob";

    private static final String STEP_NAME = "AddressCodeFileDownloadStep";

    private final JobBuilderFactory jobBuilderFactory;

    private final StepBuilderFactory stepBuilderFactory;

    private final FileProperty fileProperty;

    @Bean
    public Job addressCodeFileDownloadJob() {
        return jobBuilderFactory.get(JOB_NAME)
                .start(addressCodeFileDownloadStep())
                .incrementer(new RunIdIncrementer())
                .build();
    }

    @Bean
    @JobScope
    public Step addressCodeFileDownloadStep() {
        return stepBuilderFactory.get(STEP_NAME)
                .tasklet((contribution, chunkContext) -> {
                    String downloadUrl = "https://www.code.go.kr/etc/codeFullDown.do?codeseId=법정동코드";
                    String zipName = "address_code.zip";
                    Path zipPath = new File(fileProperty.getAddressCodeDir(), zipName).toPath();

                    log.info("법정동 코드 데이터 zip 파일 다운로드 시작 name={}, path={}, downloadUrl={}",
                            zipName, zipPath, downloadUrl);
                    try (InputStream in = new URL(downloadUrl).openStream()) {
                        Files.copy(in, zipPath);
                    }
                    log.info("법정동 코드 데이터 zip 파일 다운로드 완료");

                    return RepeatStatus.FINISHED;
                }).build();
    }
}
