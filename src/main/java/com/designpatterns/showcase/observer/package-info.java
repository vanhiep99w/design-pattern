/**
 * Observer Pattern implementation using Spring's event-driven architecture.
 * 
 * <p>This package demonstrates the Observer pattern with:
 * <ul>
 *   <li>Domain events for orders (created, shipped, delivered) and user registration</li>
 *   <li>ApplicationEventPublisher for publishing events</li>
 *   <li>@EventListener for handling events synchronously</li>
 *   <li>@Async configuration for asynchronous event processing</li>
 *   <li>Event chaining for complex workflows</li>
 *   <li>Proper error handling and thread pool configuration</li>
 * </ul>
 * 
 * <p>Key components:
 * <ul>
 *   <li>{@code events/} - Domain event classes extending ApplicationEvent</li>
 *   <li>{@code listener/} - Event listeners with @EventListener and @Async</li>
 *   <li>{@code service/} - Services that publish events using ApplicationEventPublisher</li>
 *   <li>{@code config/} - Async configuration with custom thread pool</li>
 * </ul>
 * 
 * <p>Example usage:
 * <pre>
 * // Publishing an event
 * OrderCreatedEvent event = new OrderCreatedEvent(this, orderId, userId, amount);
 * eventPublisher.publishEvent(event);
 * 
 * // Handling an event synchronously
 * {@literal @}EventListener
 * public void handleOrderCreated(OrderCreatedEvent event) {
 *     // Process event
 * }
 * 
 * // Handling an event asynchronously
 * {@literal @}EventListener
 * {@literal @}Async("eventTaskExecutor")
 * public void sendEmail(OrderCreatedEvent event) {
 *     // Send email in background thread
 * }
 * </pre>
 * 
 * @see com.designpatterns.showcase.observer.events
 * @see com.designpatterns.showcase.observer.listener
 * @see com.designpatterns.showcase.observer.service
 * @see com.designpatterns.showcase.observer.config.AsyncEventConfig
 */
package com.designpatterns.showcase.observer;
