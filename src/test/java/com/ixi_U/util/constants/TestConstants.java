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

    public static SavePlanRequest createSavePlanRequest() {
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

    public static String createDescription(){
        return "월 69000원 요금제인 너겟은 100GB의 데이터 제공으로 고속 인터넷 사용이 가능하다. 통화와 문자는 무제한으로 제공된다. 주요 혜택으로는 디즈니+와 티빙 구독 서비스가 포함되며, 삼성팩과 애플디바이스팩을 통해 다양한 디바이스를 추가로 제공한다. 삼성팩 선택 시 갤럭시워치7, 갤럭시버즈3, 갤럭시버즈3프로 또는 갤럭시워치7과 갤럭시버즈3의 조합 중 선택 가능하다. 애플디바이스팩 선택 시 에어팟4, 애플워치 SE2, 아이패드 11세대 등 다양한 애플 기기 제공 옵션이 있다. 멀티팩을 통해 지니뮤직 앱, 바이브 앱, 아이들나라 스탠다드+러닝, 밀리의 서재와 같은 음악 및 독서 관련 구독 서비스 이용이 가능하다. 추가 혜택으로 태블릿/스마트기기 월정액 할인, 로밍 혜택 프로모션, 피싱/해킹 안심서비스 무료 이용 프로모션, 멤버십 VIP 프로모션이 제공된다. 이 요금제는 데이터 사용이 많은 고객과 다양한 디바이스 및 콘텐츠를 원하는 고객에게 적합하다. 5G 네트워크 지원으로 고품질 인터넷 이용 가능하다.";
    }

    public static String createBundledBenefitsSingleBenefitNameText(){
        return "디즈니+, 티빙, 갤럭시버즈3, 갤럭시워치7+버즈3, 갤럭시버즈3프로, 갤럭시워치7, 아이패드 11세대, 애플어치SE2 44mm, 에어팟4 액티브 노이즈 캔슬링, 에어팟4, 애플워치SE2 40mm, 아이들나라 스탠다드+러닝, 밀리의 서재, 지니뮤직 앱 음악감상, 바이브 앱 음악감상, 지니뮤직 앱 음악감상, 아이들나라 스탠다드+러닝, 밀리의 서재, 바이브 앱 음악감상";
    }

    public static String createSingleBenefitNamesText(){
        return "태블릿/스마트기기 월정액 할인, 로밍 혜택 프로모션, 피싱/해킹 안심서비스 무료 이용 프로모션, 멤버십 VIP 프로모션";
    }

    public static String createBundledBenefitNamesText(){
        return "디즈니+티빙, 삼성팩, 애플디바이스팩, 멀티팩, 멀티팩";
    }

    public static String createSingleBenefitBenefitTypeText(){
        return "DISCOUNT, DISCOUNT, DISCOUNT, DISCOUNT";
    }

    public static String  createPlanId(){
        return "0235cc16-6d6a-43f6-ada2-23c97fedb846";
    }

    public static int createBundledBenefitCount(){
        return 5;
    }

    public static int createSingleBenefitCount(){
        return 4;
    }
}