package delivery;

import camp.nextstep.edu.missionutils.test.NsTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static camp.nextstep.edu.missionutils.test.Assertions.assertRandomNumberInRangeTest;
import static camp.nextstep.edu.missionutils.test.Assertions.assertSimpleTest;
import static org.assertj.core.api.Assertions.assertThat;

class ApplicationTest extends NsTest {

    @Nested
    @DisplayName("메뉴 선택 테스트")
    class MenuSelectTest {

        @Test
        @DisplayName("메뉴판 출력")
        void 메뉴판_출력() {
            assertSimpleTest(() -> {
                run("PZ001", "1", "N", "N", "1", "N", "N", "Y");
                assertThat(output()).contains(" 피자");
                assertThat(output()).contains("페퍼로니 피자");
                assertThat(output()).contains("15,000원");
                assertThat(output()).contains(" 사이드");
                assertThat(output()).contains(" 음료");
            });
        }

        @Test
        @DisplayName("존재하지 않는 메뉴 코드")
        void 존재하지_않는_메뉴() {
            assertSimpleTest(() -> {
                run("XX999", "PZ001", "1", "N", "N", "1", "N", "N", "Y");
                assertThat(output()).contains("[ERROR] 존재하지 않는 메뉴입니다");
            });
        }
    }

    @Nested
    @DisplayName("옵션 선택 테스트")
    class OptionSelectTest {

        @Test
        @DisplayName("피자 사이즈 선택")
        void 피자_사이즈_선택() {
            assertSimpleTest(() -> {
                run("PZ001", "2", "N", "N", "1", "N", "N", "Y");
                assertThat(output()).contains("페퍼로니 피자 (L)");
                assertThat(output()).contains("18,000원"); // 15000 + 3000
            });
        }

        @Test
        @DisplayName("토핑 추가")
        void 토핑_추가() {
            assertSimpleTest(() -> {
                run("PZ001", "1", "Y", "1,3", "N", "1", "N", "N", "Y");
                assertThat(output()).contains("치즈 추가");
                assertThat(output()).contains("베이컨 추가");
                assertThat(output()).contains("18,500원"); // 15000 + 1500 + 2000
            });
        }

        @Test
        @DisplayName("사이드 주문 - 옵션 없음")
        void 사이드_주문() {
            assertSimpleTest(() -> {
                run("SD001", "2", "N", "N", "Y");
                assertThat(output()).contains("치즈스틱");
                assertThat(output()).contains("10,000원");
            });
        }
    }

    @Nested
    @DisplayName("추가 주문 테스트")
    class AdditionalOrderTest {

        @Test
        @DisplayName("추가 주문 후 장바구니 합산")
        void 추가_주문() {
            assertSimpleTest(() -> {
                // 피자 주문 후 음료 추가 주문
                run("PZ001", "1", "N", "N", "1", // 피자 M사이즈 1개 = 15000
                        "Y", "DR001", "1", // 추가 주문: 콜라 1개 = 2500
                        "N", "N", "Y");
                assertThat(output()).contains("페퍼로니 피자");
                assertThat(output()).contains("콜라 1.25L");
                assertThat(output()).contains("17,500원"); // 15000 + 2500
            });
        }

        @Test
        @DisplayName("같은 메뉴 같은 옵션 수량 합산")
        void 같은_메뉴_수량_합산() {
            assertSimpleTest(() -> {
                run("SD001", "2", // 치즈스틱 2개
                        "Y", "SD001", "3", // 치즈스틱 3개 추가
                        "N", "N", "Y");
                assertThat(output()).contains("25,000원"); // 5000 * 5
            });
        }
    }

    @Nested
    @DisplayName("쿠폰 적용 테스트")
    class CouponTest {

        @Test
        @DisplayName("정액 할인 쿠폰")
        void 정액_할인_쿠폰() {
            assertSimpleTest(() -> {
                run("PZ001", "1", "N", "N", "1", "N", // 피자 15000원
                        "1", // 첫 주문 3000원 할인 쿠폰
                        "Y");
                assertThat(output()).contains("-3,000원");
                assertThat(output()).contains("12,000원"); // 15000 - 3000
            });
        }

        @Test
        @DisplayName("정률 할인 쿠폰")
        void 정률_할인_쿠폰() {
            assertSimpleTest(() -> {
                run("PZ001", "2", "N", "N", "2", "N", // 피자 L사이즈 2개 = 36000원
                        "2", // 10% 할인 쿠폰
                        "Y");
                assertThat(output()).contains("-3,600원"); // 36000 * 10% = 3600
            });
        }

        @Test
        @DisplayName("쿠폰 최소 금액 미달")
        void 쿠폰_최소금액_미달() {
            assertSimpleTest(() -> {
                run("SD001", "2", "N", // 치즈스틱 10000원
                        "1", // 3000원 쿠폰 (최소 15000원)
                        "N", // 다시 선택 - 미적용
                        "Y");
                assertThat(output()).contains("[ERROR] 최소 주문 금액");
            });
        }

        @Test
        @DisplayName("쿠폰 미적용")
        void 쿠폰_미적용() {
            assertSimpleTest(() -> {
                run("PZ001", "1", "N", "N", "1", "N", "N", "Y");
                assertThat(output()).contains("할인");
                assertThat(output()).contains("0원");
            });
        }
    }

    @Nested
    @DisplayName("배달비 계산 테스트")
    class DeliveryFeeTest {

        @Test
        @DisplayName("배달비 부과 - 20000원 미만")
        void 배달비_부과() {
            assertSimpleTest(() -> {
                run("PZ001", "1", "N", "N", "1", "N", "N", "Y"); // 15000원
                assertThat(output()).contains("배달비");
                assertThat(output()).contains("3,000원");
            });
        }

        @Test
        @DisplayName("배달비 무료 - 20000원 이상")
        void 배달비_무료_금액() {
            assertSimpleTest(() -> {
                run("PZ001", "2", "N", "N", "2", "N", "N", "Y"); // 36000원
                assertThat(output()).contains("배달비");
                assertThat(output()).contains("0원");
            });
        }

        @Test
        @DisplayName("배달비 무료 쿠폰")
        void 배달비_무료_쿠폰() {
            assertSimpleTest(() -> {
                run("PZ001", "1", "N", "N", "1", "N", // 15000원 (20000 미만)
                        "3", // 배달비 무료 쿠폰
                        "Y");
                assertThat(output()).contains("배달비 무료");
            });
        }
    }

    @Nested
    @DisplayName("주문 완료 테스트")
    class OrderCompleteTest {

        @Test
        @DisplayName("주문 확정")
        void 주문_확정() {
            assertRandomNumberInRangeTest(
                    () -> {
                        run("PZ001", "1", "N", "N", "1", "N", "N", "Y");
                        assertThat(output()).contains("주문이 완료되었습니다");
                        assertThat(output()).contains("예상 도착 시간");
                    },
                    35 // 랜덤값 35 → 30+35=65분 후
            );
        }

        @Test
        @DisplayName("주문 취소")
        void 주문_취소() {
            assertSimpleTest(() -> {
                run("PZ001", "1", "N", "N", "1", "N", "N", "N");
                assertThat(output()).contains("주문이 취소되었습니다");
            });
        }

        @Test
        @DisplayName("최소 주문 금액 미달")
        void 최소_주문_금액_미달() {
            assertSimpleTest(() -> {
                run("DR001", "1", "N", "N"); // 콜라 2500원
                assertThat(output()).contains("[ERROR] 최소 주문 금액(12,000원)을 충족하지 않습니다");
            });
        }
    }

    @Nested
    @DisplayName("전체 주문 흐름 테스트")
    class FullFlowTest {

        @Test
        @DisplayName("피자 + 사이드 + 음료 + 쿠폰 적용")
        void 전체_주문_흐름() {
            assertRandomNumberInRangeTest(
                    () -> {
                        run(
                                // 1. 피자 주문
                                "PZ001", "2", "Y", "1", "N", "1", // 페퍼로니 L + 치즈토핑 = 19500
                                // 2. 사이드 추가
                                "Y", "SD001", "1", // 치즈스틱 = 5000
                                // 3. 음료 추가
                                "Y", "DR001", "2", // 콜라 2개 = 5000
                                // 4. 추가 주문 종료
                                "N",
                                // 5. 쿠폰 적용 (10% 할인)
                                "2",
                                // 6. 주문 확정
                                "Y"
                        );
                        // 총: 19500 + 5000 + 5000 = 29500
                        // 할인: 2950 (10%)
                        // 배달비: 0 (20000 이상)
                        // 최종: 26550
                        assertThat(output()).contains("페퍼로니 피자 (L)");
                        assertThat(output()).contains("치즈스틱");
                        assertThat(output()).contains("콜라 1.25L");
                        assertThat(output()).contains("29,500원");
                        assertThat(output()).contains("-2,900원"); // 100원 단위 절삭
                        assertThat(output()).contains("주문이 완료되었습니다");
                    },
                    40
            );
        }
    }

    @Override
    protected void runMain() {
        Application.main(new String[]{});
    }
}