package com.comeeatme.batch.restaurant.update;


import com.comeeatme.batch.restaurant.*;
import com.comeeatme.batch.restaurant.exception.NoAddressException;
import com.comeeatme.batch.restaurant.exception.NotFoundAddressCodeException;
import com.comeeatme.batch.restaurant.init.LocalDataRestaurantEntityBuildProcessor;
import com.comeeatme.batch.restaurant.RestaurantSkipListener;
import com.comeeatme.batch.service.exception.RequestFrequentInShortException;
import com.comeeatme.batch.service.juso.JusoService;
import com.comeeatme.domain.restaurant.LocalData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.DefaultJobParametersValidator;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.skip.AlwaysSkipItemSkipPolicy;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.batch.item.support.builder.CompositeItemProcessorBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.retry.backoff.FixedBackOffPolicy;

import javax.persistence.EntityManagerFactory;
import java.io.File;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class RestaurantUpdateJobConfig {

    private static final String JOB_NAME = "RestaurantUpdateJob";

    private static final String STEP_NAME = "RestaurantUpdateStep";

    private static final int CHUNK_SIZE = 100;

    private static final String[] CSV_HEADERS = {
            "번호",	"개방자치단체코드", "관리번호", "개방서비스ID", "데이터갱신구분", "데이터갱신일자",
            "개방서비스명", "사업장명", "지번우편번호", "지번주소", "도로명우편번호", "도로명주소",
            "소재지면적", "인허가일자", "인허가취소일자", "폐업일자", "휴업시작일자", "휴업종료일자",
            "재개업일자", "영업상태코드", "영업상태명", "상세영업상태코드", "상세영업상태명",
            "좌표정보(X)", "좌표정보(Y)", "최종수정일자", "업태구분명", "전화번호"
    };

    private final JobBuilderFactory jobBuilderFactory;

    private final StepBuilderFactory stepBuilderFactory;

    private final RestaurantFileConstant fileConst;

    private final AddressCodeFinder addressCodeFinder;

    private final JusoService jusoService;

    private final EntityManagerFactory emf;

    @Bean
    public Job restaurantUpdateJob() {
        return jobBuilderFactory.get(JOB_NAME)
                .start(restaurantUpdateStep())
                .incrementer(new RunIdIncrementer())
                .validator(new DefaultJobParametersValidator(
                        new String[] {"serviceId", "date"},
                        new String[] {}
                ))
                .build();
    }

    @Bean
    @JobScope
    public Step restaurantUpdateStep() {
        FixedBackOffPolicy fixedBackOffPolicy = new FixedBackOffPolicy();
        fixedBackOffPolicy.setBackOffPeriod(2000);

        return stepBuilderFactory.get(STEP_NAME)
                .<LocalDataRestaurantDto, LocalData>chunk(CHUNK_SIZE)
                .reader(restaurantUpdateFileItemReader(null, null))
                .processor(restaurantUpdateProcessor())
                .writer(restaurantUpdateWriter())

                .faultTolerant()
                .skip(FlatFileParseException.class)
                .skip(NoAddressException.class)
                .skip(NotFoundAddressCodeException.class)
                .skipPolicy(new AlwaysSkipItemSkipPolicy())

                .retry(RequestFrequentInShortException.class)
                .backOffPolicy(fixedBackOffPolicy)
                .retryLimit(1000)

                .listener(new RestaurantSkipListener())
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemReader<LocalDataRestaurantDto> restaurantUpdateFileItemReader(
            @Value("#{jobParameters[serviceId]}") String serviceId, @Value("#{jobParameters[date]}") String date) {
        FileSystemResource resource = new FileSystemResource(new File(
                fileConst.getUpdateDir(), fileConst.getUpdateFileName(serviceId, date)));
        return new FlatFileItemReaderBuilder<LocalDataRestaurantDto>()
                .name("restaurantUpdateFileItemReader")
                .resource(resource)
                .fieldSetMapper(new RestaurantUpdateCsvFieldSetMapper())
                .encoding("UTF-8")
                .linesToSkip(1)
                .delimited().delimiter(DelimitedLineTokenizer.DELIMITER_COMMA)
                .names(CSV_HEADERS)
                .build();
    }

    @Bean
    @StepScope
    public CompositeItemProcessor<LocalDataRestaurantDto, LocalData> restaurantUpdateProcessor() {
        return new CompositeItemProcessorBuilder<LocalDataRestaurantDto, LocalData>()
                .delegates(
                        new RestaurantTrimProcessor(),
                        new RestaurantAddressSearchProcessor(jusoService),
                        new LocalDataRestaurantEntityBuildProcessor(addressCodeFinder)
                ).build();
    }

    @Bean
    @StepScope
    public JpaItemWriter<LocalData> restaurantUpdateWriter() {
        return new JpaItemWriterBuilder<LocalData>()
                .entityManagerFactory(emf)
                .usePersist(false)
                .build();
    }
}
