package com.comeeatme.batch.address;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lingala.zip4j.ZipFile;
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
import java.nio.charset.Charset;

@RequiredArgsConstructor
@Configuration
@Slf4j
public class AddressCodeFileUnzipJobConfig {

    private static final String JOB_NAME = "AddressCodeFileUnzipJob";

    private static final String STEP_NAME = "AddressCodeFileUnzipStep";

    private final JobBuilderFactory jobBuilderFactory;

    private final StepBuilderFactory stepBuilderFactory;

    private final AddressCodeFileConstant fileConst;

    @Bean
    public Job addressCodeFileUnzipJob() {
        return jobBuilderFactory.get(JOB_NAME)
                .start(addressCodeFileUnzipStep())
                .incrementer(new RunIdIncrementer())
                .build();
    }

    @Bean
    @JobScope
    public Step addressCodeFileUnzipStep() {
        return stepBuilderFactory.get(STEP_NAME)
                .tasklet((contribution, chunkContext) -> {
                    File zipFile = new File(fileConst.getDir(), fileConst.getZipName());

                    log.info("법정동 코드 데이터 unzip 시작. zipFile={}", zipFile);
                    try (ZipFile zip = new ZipFile(zipFile)){
                        zip.setCharset(Charset.forName("EUC-KR"));
                        zip.extractAll(fileConst.getDir().getAbsolutePath());
                    }
                    log.info("법정동 코드 데이터 unzip 완료. zipFile={}", zipFile);

                    return RepeatStatus.FINISHED;
                }).build();
    }
}
