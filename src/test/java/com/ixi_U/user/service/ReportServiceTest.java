package com.ixi_U.user.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.ixi_U.common.exception.GeneralException;
import com.ixi_U.user.entity.Reported;
import com.ixi_U.user.entity.User;
import com.ixi_U.user.exception.ReviewedException;
import com.ixi_U.user.exception.UserException;
import com.ixi_U.user.repository.ReportedRepository;
import com.ixi_U.user.repository.UserRepository;
import java.util.Optional;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReportedRepository reportedRepository;

    @InjectMocks
    private ReportService reportService;

    @DisplayName("리뷰 신고 시 ")
    @Nested
    class CreateReport {

        final String userId = "userId";
        final long reviewId = 1L;

        @DisplayName("성공한다")
        @Test
        public void success() {
            // given
            User user = Mockito.mock(User.class);
            given(user.getId()).willReturn(userId);

            given(userRepository.findOwnerByReviewedId(reviewId)).willReturn(Optional.of(user));
            given(userRepository.findById(userId)).willReturn(Optional.of(user));

            // when
            reportService.createReport(userId, reviewId);

            // then
            verify(reportedRepository.save(any(Reported.class)), times(1));
        }

        @DisplayName("존재하지 않는 리뷰일 경우, 예외가 발생한다")
        @Test
        public void reviewNotFound() {
            // given

            // when

            // then
            assertThatThrownBy(() -> reportService.createReport(userId, reviewId))
                    .isInstanceOf(GeneralException.class)
                    .extracting("exception")
                    .isEqualTo(ReviewedException.REVIEW_NOT_FOUND);
        }

        @DisplayName("존재하지 않는 사용자일 경우, 예외가 발생한다")
        @Test
        public void userNotFound() {
            // given
            User user = mock(User.class);

            given(userRepository.findOwnerByReviewedId(reviewId)).willReturn(Optional.of(user));

            // when

            // then
            assertThatThrownBy(() -> reportService.createReport(userId, reviewId))
                    .isInstanceOf(GeneralException.class)
                    .extracting("exception")
                    .isEqualTo(UserException.USER_NOT_FOUND);
        }

        @DisplayName("자신의 리뷰를 신고하는 경우, 예외가 발생한다")
        @Test
        public void reviewNotOwnedByUser() {
            // given
            User user = mock(User.class);
            given(user.getId()).willReturn(userId);

            User reviewOwner = mock(User.class);
            String reviewOwnerId = "reviewOwnerId";
            given(reviewOwner.getId()).willReturn(reviewOwnerId);

            // when

            // then
            assertThatThrownBy(() -> reportService.createReport(userId, reviewId))
                    .isInstanceOf(GeneralException.class)
                    .extracting("exception")
                    .isEqualTo(ReviewedException.REVIEW_REPORT_ONLY_OTHER);
        }
    }
}