# Eclipse Tradista Interception Framework

The Eclipse Tradista Interception Framework is a declarative, annotation-driven system built on top of Jakarta EE Interceptors. It provides a clean and consistent way to handle cross-cutting concerns such as authorization, data segregation, existence validation, and exception handling across the Eclipse Tradista service layer.

## Overview

Instead of manual checks in service beans, developers can use standard and custom annotations to enforce business rules. This approach:
- Reduces boilerplate code.
- Ensures consistency across modules.
- Makes business logic more readable.
- Simplifies testing and maintenance.

---

## 1. Product Scope Validation

The Product Scope framework restricts access to service methods based on the product types allowed for the current user's organization.

### Annotation: `@ProductScope`
Can be applied to a class (all methods) or a specific method.

**Attributes:**
- `value`: The product type (e.g., `"Equity"`, `"Bond"`). If empty, the framework attempts to detect the product type dynamically from the method parameters.
- `mode`: Configures when validation occurs.
  - `ProductScopeMode.ALWAYS` (default): Product scope validation is systematically performed on every method call.
  - `ProductScopeMode.ON_CREATION`: Validation is only performed when creating a new business object (i.e. any `TradistaObject` in the parameters with an `id == 0`). If the object is already saved in the database (`id != 0`), validation is bypassed.

### Interceptor: `TradistaProductScopeHandlerInterceptor`
This interceptor automatically verifies if the requested product type is authorized for the caller.

**Example Usage:**

```java
@ProductScope(value = "Equity", mode = ProductScopeMode.ON_CREATION)
public long saveEquity(Equity equity) { ... }
```

---

## 2. Data Segregation

Tradista enforces data segregation by Processing Organization (PO). Users can only see or modify data belonging to their own PO or global data.

### Interceptor: `TradistaSegregationHandlerInterceptor`
This interceptor is registered globally and automatically handles:
1. **Pre-filtering**: Checks that input objects (implementing `Segregable`) belong to the caller's PO.
2. **Post-filtering**: Filters return values (objects or collections) to ensure the caller only receives data they are authorized to see.

### Automatic Detection
Any parameter implementing the `Segregable` interface is automatically checked without requiring extra annotations.

### Annotation: `@CheckProcessingOrg`
Used for parameters that represent a PO but don't implement `Segregable` (e.g., a `String` short name or a `Long` ID).

**Example:**

```java
public List<Book> getBooksByPO(@CheckProcessingOrg String poName) { ... }
```

### Annotation: `@ProtectGlobal`
Applied to service methods to protect global objects (where Processing Org is `null`) from being created, updated, or deleted by sectorized/non-global users.

If a user belonging to a specific Processing Org calls an EJB method annotated with `@ProtectGlobal` with an input object that has no Processing Org (i.e., is global), the validation fails with a business exception preventing the operation.

**Example:**

```java
@ProtectGlobal
public long savePricingParameter(@CheckPricingParameterAccess PricingParameter param) { ... }
```

---

## 3. Extensible Access Checks

The framework provides a standardized way to verify the accessibility of referenced entities before proceeding with an operation.

### Meta-Annotation: `@AccessCheckedBy`
Links a parameter annotation to a specific `AccessChecker` implementation.

### Interface: `AccessChecker`
Developers must implement this interface to define how a specific entity type should be validated.

```java
public interface AccessChecker {
    void check(Object value, StringBuilder errMsg) throws TradistaBusinessException;
}
```

### How to add a new Access Check

1. **Create the Checker Implementation**:

  ```java
   public class LegalEntityAccessChecker implements AccessChecker {
       @Override
       public void check(Object value, StringBuilder errMsg) {
           // Validation logic here...
       }
   }
   ```

2. **Define the Annotation**:

  ```java
   @AccessCheckedBy(LegalEntityAccessChecker.class)
   @Target(ElementType.PARAMETER)
   @Retention(RetentionPolicy.RUNTIME)
   public @interface CheckLegalEntityAccess {}
   ```

3. **Use it in a Service**:

  ```java
   public void updateTrade(Trade trade, @CheckLegalEntityAccess String counterpartyName) { ... }
   ```

---

## 4. Exception Handling

### Interceptor: `TradistaExceptionHandlerInterceptor`
Ensures that exceptions are properly logged and transformed into meaningful business exceptions for the client. It standardizes the error reporting across the EJB layer.

---

## 5. Configuration

Interceptors are registered in the `ejb-jar.xml` file of the corresponding EJB module to ensure they are applied consistently.

**Example `ejb-jar.xml` configuration:**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<ejb-jar xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns="https://jakarta.ee/xml/ns/jakartaee"
	xsi:schemaLocation="https://jakarta.ee/xml/ns/jakartaee https://jakarta.ee/xml/ns/jakartaee/ejb-jar_4_0.xsd"
	version="4.0">
	
	<module-name>core-ejb</module-name>
	<display-name>core-ejb</display-name>
	
	<interceptors>
		<interceptor>
			<interceptor-class>org.eclipse.tradista.core.common.service.TradistaSegregationHandlerInterceptor</interceptor-class>
		</interceptor>
		<interceptor>
			<interceptor-class>org.eclipse.tradista.core.common.service.TradistaExceptionHandlerInterceptor</interceptor-class>
		</interceptor>
		<interceptor>
			<interceptor-class>org.eclipse.tradista.core.trade.service.TradistaProductScopeHandlerInterceptor</interceptor-class>
		</interceptor>
	</interceptors>
	
	<assembly-descriptor>
		<interceptor-binding>
			<ejb-name>*</ejb-name>
			<interceptor-class>org.eclipse.tradista.core.common.service.TradistaSegregationHandlerInterceptor</interceptor-class>
		</interceptor-binding>
		<interceptor-binding>
			<ejb-name>*</ejb-name>
			<interceptor-class>org.eclipse.tradista.core.common.service.TradistaExceptionHandlerInterceptor</interceptor-class>
		</interceptor-binding>
		<interceptor-binding>
			<ejb-name>*</ejb-name>
			<interceptor-class>org.eclipse.tradista.core.trade.service.TradistaProductScopeHandlerInterceptor</interceptor-class>
		</interceptor-binding>
	</assembly-descriptor>
</ejb-jar>
```

---

## Best Practices for Developers

- **Use Declarative Validation**: Prefer annotations over manual `if` checks in Service Beans.
- **Implement `Segregable`**: Ensure new domain objects that belong to a Processing Org implement the `Segregable` interface to benefit from auto-segregation.
- **Leverage Access Checkers**: Use the access check framework for any external reference (Book, Currency, Legal Entity, etc.) to keep your services clean and robust.
