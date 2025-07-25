package com.ixi_U.user.exception;

import com.ixi_U.common.exception.BaseException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ReviewedException implements BaseException {

    REVIEW_ALREADY_EXIST(HttpStatus.BAD_REQUEST, "이미 요금제에 대해 리뷰를 작성하였습니다"),
    REVIEW_NOT_FOUND(HttpStatus.BAD_REQUEST,"리뷰를 찾을 수 없습니다"),
    REVIEW_NOT_OWNER(HttpStatus.BAD_REQUEST,"해당 리뷰를 작성하지 않은 분은 수정,삭제를 진행할 수 없습니다"),
    REVIEW_REPORT_ONLY_OTHER(HttpStatus.BAD_REQUEST,"리뷰 신고 기능은 자신에게 사용할 수 없습니다"),
    REVIEW_FIND_FORBIDDEN_WORD(HttpStatus.BAD_REQUEST,"리뷰 내용에서 비속어가 발견되었습니다");

    private final HttpStatus httpStatus;
    private final String message;

    @Override
    public String getExceptionName() {

        return this.name();
    }
}
