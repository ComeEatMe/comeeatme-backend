package com.comeeatme.batch.restaurant.update;


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
public class RestaurantUpdateFileDownloadJobConfig {

    private static final String JOB_NAME = "RestaurantUpdateFileDownloadJob";

    private static final String STEP_NAME = "RestaurantUpdateFileDownloadStep";

    private final JobBuilderFactory jobBuilderFactory;

    private final StepBuilderFactory stepBuilderFactory;

    private final RestaurantFileConstant fileConstant;

    @Bean
    public Job restaurantUpdateFileDownloadJob() {
        return jobBuilderFactory.get(JOB_NAME)
                .start(restaurantUpdateFileDownloadStep(null, null))
                .incrementer(new RunIdIncrementer())
                .validator(new DefaultJobParametersValidator(
                        new String[] {"serviceId", "date"},
                        new String[] {}
                ))
                .build();
    }

    @Bean
    @JobScope
    public Step restaurantUpdateFileDownloadStep(
            @Value("#{jobParameters[serviceId]}") String serviceId, @Value("#{jobParameters[date]}") String date) {
        return stepBuilderFactory.get(STEP_NAME)
                .tasklet((contribution, chunkContext) -> {
                    String downloadUrl = "http://www.localdata.go.kr/platform/rest/TO0/openDataApi?" +
                            "authKey=" + fileConstant.getLocalDataAuthKey()
                            + "&opnSvcId=" + serviceId
                            + "&lastModTsBgn=" + date + "&lastModTsEnd=" + date + "&resultType=csv&resultFileYn=y";
                    String updateFileName = fileConstant.getUpdateFileName(serviceId, date);
                    Path path = new File(fileConstant.getUpdateDir(), updateFileName).toPath();

                    log.info("음식점(" + serviceId + ", " + date + ") 업데이트 데이터 파일 다운로드 시작 " +
                                    "path={}, downloadUrl={}", path, downloadUrl);
                    try (InputStream in = new URL(downloadUrl).openStream()) {
                        Files.copy(in, path);
                    }
                    log.info("음식점(" + serviceId + ", " + date + ") 업데이트 데이터 파일 다운로드 완료 " +
                            "path={}", path);

                    return RepeatStatus.FINISHED;
                }).build();
    }
}
