package com.comeeatme.batch.address;

import com.comeeatme.domain.address.AddressCode;
import com.comeeatme.domain.address.repository.AddressCodeRepository;
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
import org.springframework.core.io.FileSystemResource;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Configuration
@Slf4j
public class AddressCodeInitJobConfig {

    private static final String JOB_NAME = "AddressCodeInitJob";

    private static final String STEP_NAME = "AddressCodeInitStep";

    private static final String ROOT_NAME = "root";

    private static final String ROOT_CODE = "0".repeat(AddressCode.TOTAL_CODE_LEN);

    private static final int ROOT_DEPTH = 0;

    private final JobBuilderFactory jobBuilderFactory;

    private final StepBuilderFactory stepBuilderFactory;

    private final AddressCodeFileConstant fileConst;

    private final AddressCodeRepository addressCodeRepository;

    @Bean
    public Job addressCodeInitJob() {
        return jobBuilderFactory.get(JOB_NAME)
                .start(addressCodeInitStep())
                .incrementer(new RunIdIncrementer())
                .build();
    }

    @Bean
    @JobScope
    public Step addressCodeInitStep() {
        return stepBuilderFactory.get(STEP_NAME)
                .tasklet((contribution, chunkContext) -> {
                    List<AddressCode> addressCodes = getAddressCodes();
                    addressCodeRepository.saveAll(addressCodes);
                    return RepeatStatus.FINISHED;
                }).build();
    }

    private List<AddressCode> getAddressCodes() {
        List<AddressCode> addressCodes = new ArrayList<>();
        AddressCode root = AddressCode.builder()
                .code(ROOT_CODE)
                .name(ROOT_NAME)
                .fullName(ROOT_NAME)
                .depth(ROOT_DEPTH)
                .parentCode(addressCodeRepository.getReferenceById(ROOT_CODE))
                .terminal(false)
                .build();
        addressCodes.add(root);

        List<AddressCodeDto> dtos = getAddressCodeDtosFiltered();
        for (AddressCodeDto sidoAddressCodeDto : getSidoAddressCodeDtos(dtos)) {
            List<AddressCode> subAddressCodes = getAddressCodesFromSido(sidoAddressCodeDto, dtos);
            addressCodes.addAll(subAddressCodes);
        }

        return addressCodes;
    }

    private List<AddressCodeDto> getAddressCodeDtosFiltered() {
        Set<String> notUsedAddresses = Set.of(
                "경기도 수원시",
                "경기도 성남시",
                "경기도 안양시",
                "경기도 안산시",
                "경기도 고양시",
                "경기도 용인시",
                "충청북도 청주시",
                "충청남도 천안시",
                "전라북도 전주시",
                "경상북도 포항시",
                "경상남도 창원시"
        );
        return getAddressCodeDtosFromFile()
                .stream()
                .filter(dto -> !dto.getDeleted().equals("폐지"))
                .filter(dto -> !notUsedAddresses.contains(dto.getAddress()))
                .filter(dto -> !isRiCode(dto.getCode()))
                .collect(Collectors.toList());
    }

    private List<AddressCodeDto> getAddressCodeDtosFromFile() {
        List<AddressCodeDto> addressCodes = new ArrayList<>();
        FileSystemResource file = new FileSystemResource(new File(fileConst.getDir(), fileConst.getTxtName()));


        try (BufferedReader bf = new BufferedReader(new InputStreamReader(
                file.getInputStream(), Charset.forName("EUC-KR")))) {
            String line = bf.readLine();
            while ((line = bf.readLine()) != null) {
                String[] values = line.split("\t");
                String code = values[0].trim();
                String address = values[1].replaceAll("\\s+", " ").trim();
                String deleted = values[2].trim();
                addressCodes.add(new AddressCodeDto(code, address, deleted));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return addressCodes;
    }

    private boolean isRiCode(String code) {
        return !code.endsWith("0".repeat(AddressCode.RI_CODE_LEN));
    }

    private List<AddressCodeDto> getSidoAddressCodeDtos(List<AddressCodeDto> dtos) {
        return dtos.stream()
                .filter(dto -> dto.getAddress().equals(dto.getAddress().split(" ")[0]))
                .collect(Collectors.toList());
    }

    private List<AddressCode> getAddressCodesFromSido(AddressCodeDto sidoDto, List<AddressCodeDto> dtos) {
        String codePrefix = getCodePrefix(sidoDto.getCode());
        List<AddressCodeDto> subAddressCodeDtos = dtos.stream()
                .filter(dto -> dto.getCode().startsWith(codePrefix))
                .sorted(Comparator.comparing(AddressCodeDto::getCode))
                .collect(Collectors.toList());

        List<AddressCode> result = new ArrayList<>();
        AddressCode sido = AddressCode.builder()
                .code(sidoDto.getCode())
                .parentCode(addressCodeRepository.getReferenceById(ROOT_CODE))
                .name(sidoDto.getAddress())
                .fullName(sidoDto.getAddress())
                .depth(1)
                .terminal(false)
                .build();
        result.add(sido);

        Map<String, String> codeToFullname = new HashMap<>();
        codeToFullname.put(sido.getCode(), sido.getFullName());
        Map<String, Integer> codeToDepth = new HashMap<>();
        codeToDepth.put(sido.getCode(), sido.getDepth());

        for (int i = 1; i < subAddressCodeDtos.size(); i++) {
            AddressCodeDto dto = subAddressCodeDtos.get(i);
            String parentCode = getParentCode(dto.getCode());
            String parentFullname = codeToFullname.get(parentCode);
            String name = dto.getAddress().substring(parentFullname.length()).trim();
            int depth = codeToDepth.get(parentCode) + 1;

            AddressCode addressCode = AddressCode.builder()
                    .code(dto.getCode())
                    .parentCode(addressCodeRepository.getReferenceById(parentCode))
                    .name(name)
                    .fullName(dto.getAddress())
                    .depth(depth)
                    .terminal(isCodeTerminal(dto.getCode()))
                    .build();

            result.add(addressCode);
            codeToFullname.put(addressCode.getCode(), addressCode.getFullName());
            codeToDepth.put(addressCode.getCode(), addressCode.getDepth());
        }

        return result;
    }

    private String getCodePrefix(String code) {
        StringBuilder sb = new StringBuilder();
        for (String token : getCodeTokens(code)) {
            if (token.matches("0+")) {
                break;
            }
            sb.append(token);
        }
        return sb.toString();
    }

    private String getParentCode(String code) {
        StringBuilder sb = new StringBuilder();
        List<String> tokens = getCodeTokens(code);
        for (int i = 0; i < tokens.size() - 1; i++) {
            String curToken = tokens.get(i);
            String nextToken = tokens.get(i + 1);
            if (nextToken.matches("0+")) {
                break;
            }
            sb.append(curToken);
        }

        return sb + "0".repeat(AddressCode.TOTAL_CODE_LEN - sb.length());
    }

    private boolean isCodeTerminal(String code) {
        List<String> tokens = getCodeTokens(code);
        for (int i = 0; i < tokens.size() - 1; i++) {
            if (tokens.get(i).matches("0+")) {
                return false;
            }
        }
        return true;
    }

    private List<String> getCodeTokens(String code) {
        List<String> tokens = new ArrayList<>();
        int accLen = 0;
        for (int codeLen : AddressCode.CODE_LENS) {
            String token = code.substring(accLen, accLen + codeLen);
            tokens.add(token);
            accLen += codeLen;
        }

        return tokens;
    }

}
