package com.iota.iribenchmark;

import com.iota.iri.controllers.TransactionViewModel;
import com.iota.iri.hash.SpongeFactory;
import com.iota.iri.model.Hash;
import com.iota.iri.utils.Converter;
import org.apache.commons.lang3.StringUtils;

public class TransactionTestUtils {


    public static TransactionViewModel createTransactionWithTrytes(String trytes) {
        String expandedTrytes  = expandTrytes(trytes);
        byte[] trits = Converter.allocatingTritsFromTrytes(expandedTrytes);
        return new TransactionViewModel(trits, Hash.calculate(SpongeFactory.Mode.CURLP81, trits));
    }

    private static String expandTrytes(String trytes) {
        return trytes + StringUtils.repeat('9', TransactionViewModel.TRYTES_SIZE - trytes.length());
    }
}
