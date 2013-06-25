package org.openrdf.spring;

import org.openrdf.query.TupleQueryResult;

public class SesameResultHandlers {
    public static void withTupleQueryResult(TupleQueryResult tupleQueryResult,
                                            TupleQueryResultHandler tupleQueryResultHandler) throws Exception {
        try {
            tupleQueryResultHandler.handle(tupleQueryResult);
        } finally {
            tupleQueryResult.close();
        }
    }

    public static interface TupleQueryResultHandler {
        void handle(TupleQueryResult tupleQueryResult) throws Exception;
    }
}
