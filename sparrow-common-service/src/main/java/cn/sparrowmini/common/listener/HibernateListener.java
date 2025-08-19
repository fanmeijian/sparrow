package cn.sparrowmini.common.listener;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.*;
import org.hibernate.internal.SessionFactoryImpl;
import org.springframework.stereotype.Service;

import java.util.List;


@RequiredArgsConstructor
@Service
public class HibernateListener {
	private final EntityManagerFactory entityManagerFactory;
	private final List<PreUpdateEventListener> preUpdateEventListeners;
	private final List<PostInsertEventListener> postInsertEventListeners;
	private final List<PostUpdateEventListener> postUpdateEventListeners;
	private final List<PreDeleteEventListener> preDeleteEventListeners;


	@PostConstruct
	private void init() {
		SessionFactoryImpl sessionFactory = entityManagerFactory.unwrap(SessionFactoryImpl.class);
		EventListenerRegistry registry = sessionFactory.getServiceRegistry().getService(EventListenerRegistry.class);
//		registry.getEventListenerGroup(EventType.PRE_INSERT).prependListener(insertEventListenerClass);
//		registry.getEventListenerGroup(EventType.POST_COMMIT_INSERT).prependListener(indexEventListenerClass);
        assert registry != null;
        registry.getEventListenerGroup(EventType.PRE_UPDATE).appendListeners(preUpdateEventListeners.toArray(new PreUpdateEventListener[0]));
		registry.getEventListenerGroup(EventType.POST_INSERT).appendListeners(postInsertEventListeners.toArray(new PostInsertEventListener[0]));
		registry.getEventListenerGroup(EventType.POST_UPDATE).appendListeners(postUpdateEventListeners.toArray(new PostUpdateEventListener[0]));
		registry.getEventListenerGroup(EventType.PRE_DELETE).appendListeners(preDeleteEventListeners.toArray(new PreDeleteEventListener[0]));
//		registry.getEventListenerGroup(EventType.POST_COMMIT_DELETE).prependListener(deleteLogEventListener);
//		registry.getEventListenerGroup(EventType.PRE_LOAD).prependListener(readEventListener);
	}
}
