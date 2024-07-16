package org.cardanofoundation.lob.app.support.orm;

import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.AliasFor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import java.lang.annotation.*;

@Async
@Transactional(
        propagation = Propagation.REQUIRES_NEW,
        isolation = Isolation.SERIALIZABLE
)
@TransactionalEventListener
@Documented
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface StrictApplicationModuleListener {
    @AliasFor(
            annotation = Transactional.class,
            attribute = "readOnly"
    )
    boolean readOnlyTransaction() default false;

    @AliasFor(
            annotation = EventListener.class,
            attribute = "id"
    )
    String id() default "";
}