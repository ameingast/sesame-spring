package org.openrdf.spring;

import org.springframework.transaction.TransactionException;

/**
 * @author ameingast@gmail.com
 */
class SesameTransactionException extends TransactionException {
    public SesameTransactionException(Exception e) {
        super(e.getMessage(), e);
    }

    public SesameTransactionException(String s) {
        super(s);
    }
}
