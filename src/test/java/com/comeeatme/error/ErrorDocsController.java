package com.comeeatme.error;

import com.comeeatme.error.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/docs/error")
public class ErrorDocsController {

    @GetMapping("/codes")
    public ResponseEntity<Map<ErrorCode, ErrorCodeDto>> errorCodes() {
        Map<ErrorCode, ErrorCodeDto> errorCodeToDto = Arrays.stream(ErrorCode.values())
                .collect(Collectors.toMap(Function.identity(), ErrorCodeDto::of));
        return ResponseEntity.ok(errorCodeToDto);
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    static class ErrorCodeDto {

        private HttpStatus status;

        private int statusValue;

        private String message;

        public static ErrorCodeDto of(ErrorCode errorCode) {
            HttpStatus status = HttpStatus.valueOf(errorCode.getStatus());
            return new ErrorCodeDto(
                    status, status.value(), errorCode.getMessage()
            );
        }

        public ErrorCodeDto(HttpStatus status, int statusValue, String message) {
            this.status = status;
            this.statusValue = statusValue;
            this.message = message;
        }
    }
}
