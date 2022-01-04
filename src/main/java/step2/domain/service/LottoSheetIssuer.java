package step2.domain.service;

import step2.domain.LottoConfig;
import step2.domain.LottoSheetWithId;

public interface LottoSheetIssuer<T extends LottoSheetWithId> {
    int PRICE = 1000;
    T issueLottoSheet(LottoConfig lottoConfig);
}
