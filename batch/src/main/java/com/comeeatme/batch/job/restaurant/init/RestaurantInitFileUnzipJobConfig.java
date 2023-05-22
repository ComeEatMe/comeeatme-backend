package com.comeeatme.batch.job.restaurant.init;


import com.comeeatme.batch.job.restaurant.RestaurantFileConstant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lingala.zip4j.ZipFile;
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
import java.nio.charset.Charset;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class RestaurantInitFileUnzipJobConfig {

    private static final String JOB_NAME = "RestaurantInitFileUnzipJob";

    private static final String STEP_NAME = "RestaurantInitFileUnzipStep";

    private final JobBuilderFactory jobBuilderFactory;

    private final StepBuilderFactory stepBuilderFactory;

    private final RestaurantFileConstant fileConst;

    @Bean
    public Job restaurantInitFileUnzipJob() {
        return jobBuilderFactory.get(JOB_NAME)
                .start(restaurantInitFileUnzipStep(null))
                .incrementer(new RunIdIncrementer())
                .validator(new DefaultJobParametersValidator(
                        new String[] {"serviceId"},
                        new String[] {}
                ))
                .build();
    }

    @Bean
    @JobScope
    public Step restaurantInitFileUnzipStep(@Value("#{jobParameters[serviceId]}") String serviceId) {
        return stepBuilderFactory.get(STEP_NAME)
                .tasklet((contribution, chunkContext) -> {
                    File zipFile = new File(fileConst.getInitDir(), fileConst.getInitZipName(serviceId));

                    log.info("음식점(" + serviceId + ") 초기화 데이터 unzip 시작. zipFile={}", zipFile);
                    try (ZipFile zip = new ZipFile(zipFile)){
                        zip.setCharset(Charset.forName("EUC-KR"));
                        zip.extractAll(fileConst.getInitDir().getAbsolutePath());
                    }
                    log.info("음식점(" + serviceId + ") 초기화 데이터 unzip 완료. zipFile={}", zipFile);

                    return RepeatStatus.FINISHED;
                }).build();
    }
}
