package org.cardanofoundation.lob.app.support.modulith;

import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.AliasFor;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;



@Transactional(
        propagation = Propagation.REQUIRES_NEW
)
@TransactionalEventListener
@Documented
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface SyncApplicationModuleListener {

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

    @AliasFor(
            annotation = EventListener.class,
            attribute = "condition"
    )
    String condition() default "";

}
