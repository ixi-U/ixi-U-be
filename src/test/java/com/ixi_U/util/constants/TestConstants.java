package com.ixi_U.util.constants;

import com.ixi_U.plan.dto.request.SavePlanRequest;
import com.ixi_U.plan.entity.PlanState;
import com.ixi_U.plan.entity.PlanType;

import java.util.List;

public class TestConstants {

    public static final String CHATBOT_WELCOME_MESSAGE = """
            안녕하세요, 고객님! 어떤 요금제를 찾고 계실까요?
            관심있는 혜택 또는 원하는 조건을 말씀해주시면 최적의 요금제를 안내해드리겠습니다!
            예시) "넷플릭스 있는 요금제 중 가장 싼 요금제가 뭐야?", "데이터 10기가 이상인 요금제 알려줘"
            """;

    public static SavePlanRequest createDefault() {
        return new SavePlanRequest(
                "너겟 1111111111111",
                PlanState.ABLE,
                100,
                150,
                100,
                500,
                69000,
                PlanType.FIVE_G_LTE,
                "사용시 주의 사항",
                400,
                7,
                30,
                false,
                0,
                null,
                1,
                List.of("temp19", "temp20", "temp17", "temp18"),
                List.of("temp22", "temp21", "temp23", "temp24", "temp26")
        );
    }
}