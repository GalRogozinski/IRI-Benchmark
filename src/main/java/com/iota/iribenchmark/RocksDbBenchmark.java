package com.iota.iribenchmark;

import com.iota.iri.conf.BaseIotaConfig;
import com.iota.iri.controllers.TransactionViewModel;
import com.iota.iri.model.Hash;
import com.iota.iri.model.Transaction;
import com.iota.iri.storage.PersistenceProvider;
import com.iota.iri.storage.Tangle;
import com.iota.iri.storage.rocksDB.RocksDBPersistenceProvider;
import org.openjdk.jmh.annotations.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class RocksDbBenchmark {
    private static final File dbFolder = new File("db-bench");
    private static final File logFolder = new File("db-log-bench");

    @State(Scope.Benchmark)
    public static class PersistState {
        public static final int NUM_TXS = 1000;
        Tangle tangle;
        List<TransactionViewModel> transactions = new ArrayList<>(NUM_TXS);


        @Setup(Level.Trial)
        public void setup() throws Exception {
            System.out.println("-----------------------trial setup--------------------------------");
            boolean mkdirs = dbFolder.mkdirs();
            System.out.println("mkdirs success: " + mkdirs );
            logFolder.mkdirs();
            PersistenceProvider dbProvider = new RocksDBPersistenceProvider(dbFolder.getPath(), logFolder.getPath(),
                    BaseIotaConfig.Defaults.DB_CACHE_SIZE);
            dbProvider.init();
            tangle = new Tangle();
            tangle.addPersistenceProvider(dbProvider);
            String trytes = "";
            for (int i = 0; i < NUM_TXS; i++) {
                trytes = nextWord(trytes);
                TransactionViewModel tvm = TransactionTestUtils.createTransactionWithTrytes(trytes);
                transactions.add(tvm);
            }
        }

        @TearDown(Level.Trial)
        public void teardown() throws Exception {
            System.out.println("-----------------------trial teardown--------------------------------");
            tangle.shutdown();
            dbFolder.delete();
            logFolder.delete();
        }

        @TearDown(Level.Iteration)
        public void clearDb() throws Exception {
            System.out.println("-----------------------iteration teardown--------------------------------");
            tangle.clearColumn(Transaction.class);
            tangle.clearMetadata(Transaction.class);
        }

        private String nextWord(String trytes) {
            if ("".equals(trytes)) {
                return "A";
            }
            trytes = trytes.toUpperCase();
            char[] chars = trytes.toCharArray();
            for (int i = chars.length -1; i>=0; --i) {
                if (chars[i] != 'Z') {
                    ++chars[i];
                    return new String(chars);
                }
            }
            return trytes + 'A';
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Fork(value = 1, warmups = 1)
    @Warmup(iterations = 5)
    @Measurement(iterations = 5)
    public void persistOneByOne(PersistState state) throws Exception {
        for (TransactionViewModel tvm : state.transactions) {
            Hash hash = tvm.getHash();
            tvm.store(state.tangle);
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Fork(value = 1, warmups = 1)
    @Warmup(iterations = 5)
    @Measurement(iterations = 5)
    public void deleteOneByOne(PersistState state) throws Exception {
        for (TransactionViewModel tvm : state.transactions) {
            Hash hash = tvm.getHash();
            tvm.store(state.tangle);
        }
    }

}


