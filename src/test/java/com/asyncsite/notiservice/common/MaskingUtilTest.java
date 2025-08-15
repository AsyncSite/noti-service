package com.asyncsite.notiservice.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class MaskingUtilTest {

    @Test
    @DisplayName("이름 중간 마스킹 규칙 검증")
    void maskNameMiddle_rules() {
        assertThat(MaskingUtil.maskNameMiddle(null)).isNull();
        assertThat(MaskingUtil.maskNameMiddle("")).isEqualTo("");
        assertThat(MaskingUtil.maskNameMiddle("A")).isEqualTo("*");
        assertThat(MaskingUtil.maskNameMiddle("AB")).isEqualTo("A*");
        assertThat(MaskingUtil.maskNameMiddle("홍길동")).isEqualTo("홍*동");
        assertThat(MaskingUtil.maskNameMiddle("이순신")).isEqualTo("이*신");
        assertThat(MaskingUtil.maskNameMiddle(" John ")).isEqualTo("J**n");
    }

    @Test
    @DisplayName("variables에서 userName/recipientName/proposerName 마스킹")
    void maskVariablesForDisplay_onlyNameKeys() {
        Map<String, Object> vars = Map.of(
                "userName", "홍길동",
                "recipientName", "이순신",
                "proposerName", "김철수",
                "resetUrl", "https://example.com"
        );
        Map<String, Object> masked = MaskingUtil.maskVariablesForDisplay(vars);
        assertThat(masked.get("userName")).isEqualTo("홍*동");
        assertThat(masked.get("recipientName")).isEqualTo("이*신");
        assertThat(masked.get("proposerName")).isEqualTo("김*수");
        assertThat(masked.get("resetUrl")).isEqualTo("https://example.com");
    }
}
