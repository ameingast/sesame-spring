package org.openrdf.spring;

import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.TupleQueryResult;

public class SesameResultHandlers {
    public static interface TupleQueryResultHandler {
        void handle(TupleQueryResult tupleQueryResult) throws Exception;
    }

    public static interface GraphQueryResultHandler {
        void handle(GraphQueryResult graphQueryResult) throws Exception;
    }

    public static void withTupleQueryResult(TupleQueryResult tupleQueryResult,
                                            TupleQueryResultHandler tupleQueryResultHandler) throws Exception {
        try {
            tupleQueryResultHandler.handle(tupleQueryResult);
        } finally {
            tupleQueryResult.close();
        }
    }

    public static void withGraphQueryResult(GraphQueryResult graphQueryResult,
                                            GraphQueryResultHandler graphQueryResultHandler) throws Exception {
        try {
            graphQueryResultHandler.handle(graphQueryResult);
        } finally {
            graphQueryResult.close();
        }
    }
}
