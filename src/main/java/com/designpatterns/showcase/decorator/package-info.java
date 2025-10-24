/**
 * Decorator Pattern Implementation
 * <p>
 * This package demonstrates the Decorator design pattern for dynamically adding
 * functionality to objects by wrapping them in decorator classes.
 * <p>
 * Key components:
 * <ul>
 *   <li>{@link com.designpatterns.showcase.decorator.DataService} - Core service interface</li>
 *   <li>{@link com.designpatterns.showcase.decorator.SimpleDataService} - Base implementation</li>
 *   <li>{@link com.designpatterns.showcase.decorator.DataServiceDecorator} - Abstract decorator base class</li>
 *   <li>{@link com.designpatterns.showcase.decorator.LoggingDataServiceDecorator} - Adds logging</li>
 *   <li>{@link com.designpatterns.showcase.decorator.CachingDataServiceDecorator} - Adds caching</li>
 *   <li>{@link com.designpatterns.showcase.decorator.EncryptionDataServiceDecorator} - Adds encryption</li>
 *   <li>{@link com.designpatterns.showcase.decorator.FeatureToggleDataServiceDecorator} - Adds feature toggle</li>
 * </ul>
 * <p>
 * The implementation demonstrates:
 * <ul>
 *   <li>Decorator stacking with proper ordering</li>
 *   <li>Spring configuration with @Primary and @Qualifier</li>
 *   <li>Runtime behavior modification via configuration</li>
 *   <li>Cross-cutting concerns (logging, caching, security)</li>
 * </ul>
 *
 * @see com.designpatterns.showcase.decorator.DecoratorConfiguration
 * @see com.designpatterns.showcase.decorator.DecoratorDemoController
 */
package com.designpatterns.showcase.decorator;
