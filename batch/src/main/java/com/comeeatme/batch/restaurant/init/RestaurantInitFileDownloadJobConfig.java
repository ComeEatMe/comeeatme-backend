package com.comeeatme.batch.restaurant.init;


import com.comeeatme.batch.restaurant.RestaurantFileConstant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.DefaultJobParametersValidator;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class RestaurantInitFileDownloadJobConfig {

    private static final String JOB_NAME = "RestaurantInitFileDownloadJob";

    private static final String STEP_NAME = "RestaurantInitFileDownloadStep";

    private final JobBuilderFactory jobBuilderFactory;

    private final StepBuilderFactory stepBuilderFactory;

    private final RestaurantFileConstant fileConstant;

    @Bean
    public Job restaurantInitFileDownloadJob() {
        return jobBuilderFactory.get(JOB_NAME)
                .start(restaurantInitFileDownloadStep(null))
                .incrementer(new RunIdIncrementer())
                .validator(new DefaultJobParametersValidator(
                        new String[] {"serviceId"},
                        new String[] {}
                ))
                .build();
    }

    @Bean
    @JobScope
    public Step restaurantInitFileDownloadStep(@Value("#{jobParameters[serviceId]}") String serviceId) {
        return stepBuilderFactory.get(STEP_NAME)
                .tasklet((contribution, chunkContext) -> {
                    String downloadUrl = "https://www.localdata.go.kr/datafile/each/" + serviceId + "_CSV.zip";
                    String zipName = fileConstant.getInitZipName(serviceId);
                    Path zipPath = new File(fileConstant.getInitDir(), zipName).toPath();

                    log.info("음식점(" + serviceId + ") 초기화 데이터 zip 파일 다운로드 시작 path={}, downloadUrl={}",
                            zipPath, downloadUrl);
                    try (InputStream in = new URL(downloadUrl).openStream()) {
                        Files.copy(in, zipPath);
                    }
                    log.info("음식점(" + serviceId + ") 초기화 데이터 zip 파일 다운로드 완료 path={}", zipPath);

                    return RepeatStatus.FINISHED;
                }).build();
    }
}
