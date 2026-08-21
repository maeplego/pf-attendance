package com.pf.attendance.app.export;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class VendorCsvFormatsTest {
  @Test
  void splitsJapaneseDisplayName() {
    assertThat(VendorCsvFormats.splitJapaneseName("青木 陽")).containsExactly("青木", "陽");
    assertThat(VendorCsvFormats.splitJapaneseName("単名")).containsExactly("単名", "");
  }
}
