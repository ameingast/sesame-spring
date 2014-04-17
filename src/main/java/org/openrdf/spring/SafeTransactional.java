package org.openrdf.spring;

import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>{@link org.openrdf.spring.SafeTransactional} provides an annotation similar to
 * {@link org.springframework.transaction.annotation.Transactional}.</p>
 * <p>In contrast to {@link org.springframework.transaction.annotation.Transactional},
 * {@link org.openrdf.spring.SafeTransactional} will also trigger a transaction <b>roll-back</b>
 * on checked exceptions.</p>
 *
 * @author ameingast@gmail.com
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Transactional(rollbackFor = Exception.class)
public @interface SafeTransactional {
}