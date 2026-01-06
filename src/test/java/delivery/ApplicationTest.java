package delivery;

import camp.nextstep.edu.missionutils.test.NsTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static camp.nextstep.edu.missionutils.test.Assertions.assertRandomNumberInRangeTest;
import static camp.nextstep.edu.missionutils.test.Assertions.assertSimpleTest;
import static org.assertj.core.api.Assertions.assertThat;

class ApplicationTest extends NsTest {

    @Override
    protected void runMain() {
        Application.main(new String[]{});
    }

    @Nested
    @DisplayName("메뉴 조회 테스트")
    class MenuListTest {

        @Test
        @DisplayName("전체 메뉴 조회")
        void 전체_메뉴_조회() {
            assertSimpleTest(() -> {
                run("1", "Q");
                assertThat(output()).contains("== 피자 ==");
                assertThat(output()).contains("페퍼로니 피자");
                assertThat(output()).contains("15,000원");
                assertThat(output()).contains("== 사이드 ==");
                assertThat(output()).contains("치즈스틱");
                assertThat(output()).contains("== 음료 ==");
                assertThat(output()).contains("콜라 1.25L");
            });
        }
    }

    @Nested
    @DisplayName("메뉴 주문 테스트")
    class OrderMenuTest {

        @Test
        @DisplayName("피자 주문 - 필수 옵션 선택")
        void 피자_주문_필수옵션() {
            assertSimpleTest(() -> {
                run("2", "PZ001", "2", "N", "N", "1", "Q");
                assertThat(output()).contains("장바구니에 추가되었습니다");
                assertThat(output()).contains("페퍼로니 피자 (L)");
                assertThat(output()).contains("18,000원");
            });
        }

        @Test
        @DisplayName("피자 주문 - 토핑 추가")
        void 피자_주문_토핑추가() {
            assertSimpleTest(() -> {
                run("2", "PZ001", "1", "Y", "1,2", "N", "1", "Q");
                assertThat(output()).contains("장바구니에 추가되었습니다");
                assertThat(output()).contains("치즈 추가");
                assertThat(output()).contains("페퍼로니 추가");
                assertThat(output()).contains("18,500원"); // 15000 + 1500 + 2000
            });
        }

        @Test
        @DisplayName("사이드 주문 - 필수 옵션 없음")
        void 사이드_주문() {
            assertSimpleTest(() -> {
                run("2", "SD001", "2", "Q");
                assertThat(output()).contains("장바구니에 추가되었습니다");
                assertThat(output()).contains("치즈스틱");
                assertThat(output()).contains("10,000원");
            });
        }

        @Test
        @DisplayName("존재하지 않는 메뉴")
        void 존재하지_않는_메뉴() {
            assertSimpleTest(() -> {
                run("2", "XX999", "PZ001", "1", "N", "N", "1", "Q");
                assertThat(output()).contains("[ERROR] 존재하지 않는 메뉴입니다");
            });
        }

        @Test
        @DisplayName("잘못된 수량")
        void 잘못된_수량() {
            assertSimpleTest(() -> {
                run("2", "SD001", "0", "1", "Q");
                assertThat(output()).contains("[ERROR] 수량은 1 이상의 숫자여야 합니다");
            });
        }
    }

    @Nested
    @DisplayName("장바구니 테스트")
    class CartTest {

        @Test
        @DisplayName("장바구니 조회")
        void 장바구니_조회() {
            assertSimpleTest(() -> {
                run("2", "SD001", "2",
                        "3", "3",
                        "Q");
                assertThat(output()).contains("치즈스틱");
                assertThat(output()).contains("10,000원");
                assertThat(output()).contains("총 주문 금액");
            });
        }

        @Test
        @DisplayName("빈 장바구니")
        void 빈_장바구니() {
            assertSimpleTest(() -> {
                run("3", "Q");
                assertThat(output()).contains("장바구니가 비어있습니다");
            });
        }

        @Test
        @DisplayName("항목 삭제")
        void 항목_삭제() {
            assertSimpleTest(() -> {
                run("2", "SD001", "2",
                        "3", "1", "1",
                        "Q");
                assertThat(output()).contains("삭제되었습니다");
            });
        }

        @Test
        @DisplayName("수량 변경")
        void 수량_변경() {
            assertSimpleTest(() -> {
                run("2", "SD001", "2",
                        "3", "2", "1", "5",
                        "Q");
                assertThat(output()).contains("5개로 변경되었습니다");
            });
        }

        @Test
        @DisplayName("같은 메뉴 같은 옵션 수량 합산")
        void 같은_메뉴_수량_합산() {
            assertSimpleTest(() -> {
                run("2", "SD001", "2",
                        "2", "SD001", "3",
                        "3", "3",
                        "Q");
                assertThat(output()).contains("5,000원 × 5개");
            });
        }
    }

    @Nested
    @DisplayName("쿠폰 적용 테스트")
    class CouponTest {

        @Test
        @DisplayName("정액 할인 쿠폰 적용")
        void 정액_쿠폰_적용() {
            assertSimpleTest(() -> {
                run("2", "PZ001", "1", "N", "N", "1", // 15,000원 피자 주문
                        "4", "1", // 첫 주문 3000원 할인 쿠폰
                        "Q");
                assertThat(output()).contains("첫 주문 3000원 할인이 적용되었습니다");
                assertThat(output()).contains("3,000원");
            });
        }

        @Test
        @DisplayName("정률 할인 쿠폰 적용")
        void 정률_쿠폰_적용() {
            assertSimpleTest(() -> {
                run("2", "PZ001", "2", "N", "N", "2", // 36,000원 (L사이즈 18,000 × 2)
                        "4", "2", // 10% 할인 쿠폰
                        "Q");
                assertThat(output()).contains("10% 할인 쿠폰이 적용되었습니다");
            });
        }

        @Test
        @DisplayName("쿠폰 조건 미충족")
        void 쿠폰_조건_미충족() {
            assertSimpleTest(() -> {
                run("2", "SD001", "2", // 10,000원
                        "4", "1", // 최소 15,000원 필요
                        "Q");
                assertThat(output()).contains("[ERROR] 최소 주문 금액");
            });
        }

        @Test
        @DisplayName("쿠폰 교체")
        void 쿠폰_교체() {
            assertSimpleTest(() -> {
                run("2", "PZ001", "1", "N", "N", "2", // 18,000원 (L사이즈)
                        "4", "1", // 첫 주문 3000원 할인 적용
                        "4", "2", // 10% 할인으로 교체
                        "Q");
                assertThat(output()).contains("기존 쿠폰");
                assertThat(output()).contains("해제되고");
            });
        }
    }

    @Nested
    @DisplayName("주문 완료 테스트")
    class OrderCompleteTest {

        @Test
        @DisplayName("정상 주문 완료")
        void 정상_주문_완료() {
            assertRandomNumberInRangeTest(
                    () -> {
                        run("2", "PZ001", "1", "N", "N", "1",
                                "5", "Y");
                        assertThat(output()).contains("주문이 완료되었습니다");
                        assertThat(output()).contains("예상 도착 시간");
                    },
                    40 // 랜덤 값 40분
            );
        }

        @Test
        @DisplayName("최소 주문 금액 미달")
        void 최소_주문_금액_미달() {
            assertSimpleTest(() -> {
                run("2", "DR001", "1", // 2,500원
                        "5",
                        "Q");
                assertThat(output()).contains("[ERROR] 최소 주문 금액(12,000원)을 충족하지 않습니다");
            });
        }

        @Test
        @DisplayName("장바구니 비어있음")
        void 장바구니_비어있음() {
            assertSimpleTest(() -> {
                run("5", "Q");
                assertThat(output()).contains("[ERROR] 장바구니가 비어있습니다");
            });
        }

        @Test
        @DisplayName("배달비 무료 - 금액 충족")
        void 배달비_무료_금액충족() {
            assertRandomNumberInRangeTest(
                    () -> {
                        run("2", "PZ001", "2", "N", "N", "2", // L사이즈 18,000 × 2 = 36,000원
                                "5", "Y");
                        assertThat(output()).contains("배달비");
                        assertThat(output()).contains("0원");
                    },
                    40
            );
        }

        @Test
        @DisplayName("배달비 부과 - 금액 미달")
        void 배달비_부과() {
            assertRandomNumberInRangeTest(
                    () -> {
                        run("2", "PZ001", "1", "N", "N", "1", // M사이즈 15,000원
                                "5", "Y");
                        assertThat(output()).contains("배달비");
                        assertThat(output()).contains("3,000원");
                    },
                    40
            );
        }

        @Test
        @DisplayName("배달비 무료 쿠폰 적용")
        void 배달비_무료_쿠폰() {
            assertRandomNumberInRangeTest(
                    () -> {
                        run("2", "PZ001", "1", "N", "N", "1", // M사이즈 15,000원
                                "4", "3", // 배달비 무료 쿠폰
                                "5", "Y");
                        assertThat(output()).contains("배달비");
                        assertThat(output()).contains("0원");
                    },
                    40
            );
        }
    }

    @Nested
    @DisplayName("할인 계산 테스트")
    class DiscountCalculationTest {

        @Test
        @DisplayName("정액 할인 계산")
        void 정액_할인_계산() {
            assertRandomNumberInRangeTest(
                    () -> {
                        run("2", "PZ001", "1", "N", "N", "1", // 15,000원
                                "4", "1", // 3,000원 할인
                                "5", "Y");
                        assertThat(output()).contains("-3,000원");
                        assertThat(output()).contains("15,000원"); // 최종: 15000 - 3000 + 3000(배달비)
                    },
                    40
            );
        }

        @Test
        @DisplayName("정률 할인 계산 - 100원 단위 절삭")
        void 정률_할인_100원_절삭() {
            assertRandomNumberInRangeTest(
                    () -> {
                        run("2", "PZ001", "2", "N", "N", "1", // M사이즈 15,000 × 2 = 30,000원
                                "4", "2", // 10% 할인
                                "5", "Y");
                        // 30,000 × 10% = 3,000원 할인
                        assertThat(output()).contains("-3,000원");
                    },
                    40
            );
        }
    }

    @Nested
    @DisplayName("옵션 선택 테스트")
    class OptionTest {

        @Test
        @DisplayName("여러 토핑 선택")
        void 여러_토핑_선택() {
            assertSimpleTest(() -> {
                run("2", "PZ001", "1", "Y", "1,2,3", "N", "1", "Q");
                assertThat(output()).contains("치즈 추가");
                assertThat(output()).contains("페퍼로니 추가");
                assertThat(output()).contains("베이컨 추가");
                // 15,000 + 1,500 + 2,000 + 2,000 = 20,500원
                assertThat(output()).contains("20,500원");
            });
        }

        @Test
        @DisplayName("토핑 + 소스 함께 선택")
        void 토핑_소스_함께_선택() {
            assertSimpleTest(() -> {
                run("2", "PZ001", "1", "Y", "1", "Y", "1", "1", "Q");
                assertThat(output()).contains("치즈 추가");
                assertThat(output()).contains("핫소스 추가");
                // 15,000 + 1,500 + 500 = 17,000원
                assertThat(output()).contains("17,000원");
            });
        }

        @Test
        @DisplayName("잘못된 옵션 번호")
        void 잘못된_옵션_번호() {
            assertSimpleTest(() -> {
                run("2", "PZ001", "9", "1", "N", "N", "1", "Q");
                assertThat(output()).contains("[ERROR] 잘못된 옵션 번호입니다");
            });
        }
    }

    @Nested
    @DisplayName("입력 예외 테스트")
    class InputExceptionTest {

        @Test
        @DisplayName("잘못된 메뉴 선택")
        void 잘못된_메뉴_선택() {
            assertSimpleTest(() -> {
                run("X", "Q");
                assertThat(output()).contains("[ERROR] 잘못된 입력입니다");
            });
        }

        @Test
        @DisplayName("빈 값 입력")
        void 빈값_입력() {
            assertSimpleTest(() -> {
                run("2", "", "SD001", "1", "Q");
                assertThat(output()).contains("[ERROR]");
            });
        }
    }
}
