package com.comeeatme.batch.job.restaurant.init;


import com.comeeatme.batch.job.restaurant.AddressCodeFinder;
import com.comeeatme.batch.job.restaurant.LocalDataRestaurantDto;
import com.comeeatme.batch.job.restaurant.RestaurantFileConstant;
import com.comeeatme.batch.job.restaurant.exception.NoAddressException;
import com.comeeatme.batch.job.restaurant.exception.NotFoundAddressCodeException;
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
public class RestaurantInitJobConfig {

    private static final String JOB_NAME = "RestaurantInitJob";

    private static final String STEP_NAME = "RestaurantInitStep";

    private static final int CHUNK_SIZE = 100;

    private static final String[] CSV_HEADERS = {
            "번호", "개방서비스명", "개방서비스아이디", "개방자치단체코드", "관리번호", "인허가일자",
            "인허가취소일자", "영업상태구분코드", "영업상태명", "상세영업상태코드", "상세영업상태명", "폐업일자",
            "휴업시작일자", "휴업종료일자", "재개업일자", "소재지전화", "소재지면적", "소재지우편번호",
            "소재지전체주소", "도로명전체주소", "도로명우편번호", "사업장명", "최종수정시점", "데이터갱신구분",
            "데이터갱신일자", "업태구분명", "좌표정보(x)", "좌표정보(y)", "위생업태명", "남성종사자수",
            "여성종사자수", "영업장주변구분명", "등급구분명", "급수시설구분명", "총직원수", "본사직원수",
            "공장사무직직원수", "공장판매직직원수", "공장생산직직원수", "건물소유구분명", "보증액", "월세액",
            "다중이용업소여부", "시설총규모", "전통업소지정번호", "전통업소주된음식", "홈페이지", ""
    };

    private final JobBuilderFactory jobBuilderFactory;

    private final StepBuilderFactory stepBuilderFactory;

    private final RestaurantFileConstant fileConst;

    private final AddressCodeFinder addressCodeFinder;

    private final JusoService jusoService;

    private final EntityManagerFactory emf;

    @Bean
    public Job restaurantInitJob() {
        return jobBuilderFactory.get(JOB_NAME)
                .start(restaurantInitStep(null))
                .incrementer(new RunIdIncrementer())
                .validator(new DefaultJobParametersValidator(
                        new String[] {"serviceId"},
                        new String[] {}
                ))
                .build();
    }

    @Bean
    @JobScope
    public Step restaurantInitStep(@Value("#{jobParameters[serviceId]}") String serviceId) {
        FixedBackOffPolicy fixedBackOffPolicy = new FixedBackOffPolicy();
        fixedBackOffPolicy.setBackOffPeriod(2000);
        return stepBuilderFactory.get(STEP_NAME)
                .<LocalDataRestaurantDto, LocalData>chunk(CHUNK_SIZE)
                .reader(restaurantInitFileItemReader(null))
                .processor(restaurantInitProcessor())
                .writer(restaurantInitWriter())

                .faultTolerant()
                .skip(FlatFileParseException.class)
                .skip(NoAddressException.class)
                .skip(NotFoundAddressCodeException.class)
                .skipPolicy(new AlwaysSkipItemSkipPolicy())

                .retry(RequestFrequentInShortException.class)
                .backOffPolicy(fixedBackOffPolicy)
                .retryLimit(100)

                .listener(new RestaurantInitSkipListener())
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemReader<LocalDataRestaurantDto> restaurantInitFileItemReader(
            @Value("#{jobParameters[serviceId]}") String serviceId) {
        return new FlatFileItemReaderBuilder<LocalDataRestaurantDto>()
                .name("restaurantInitFileItemReader")
                .resource(new FileSystemResource(new File(fileConst.getInitDir(), fileConst.getInitFileName(serviceId))))
                .fieldSetMapper(new RestaurantInitCsvFieldSetMapper())
                .encoding("EUC-KR")
                .linesToSkip(1)
                .delimited().delimiter(DelimitedLineTokenizer.DELIMITER_COMMA)
                .names(CSV_HEADERS)
                .build();
    }

    @Bean
    @StepScope
    public CompositeItemProcessor<LocalDataRestaurantDto, LocalData> restaurantInitProcessor() {
        return new CompositeItemProcessorBuilder<LocalDataRestaurantDto, LocalData>()
                .delegates(
                        new RestaurantSkipClosedProcessor(),
                        new RestaurantTrimProcessor(),
                        new RestaurantOldAddressProcessor(),
                        new RestaurantAddressSearchProcessor(jusoService),
                        new LocalDataRestaurantEntityBuildProcessor(addressCodeFinder)
                ).build();
    }

    @Bean
    @StepScope
    public JpaItemWriter<LocalData> restaurantInitWriter() {
        return new JpaItemWriterBuilder<LocalData>()
                .entityManagerFactory(emf)
                .usePersist(true)
                .build();
    }
}
