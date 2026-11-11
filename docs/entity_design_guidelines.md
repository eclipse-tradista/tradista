# Eclipse Tradista Entity Design and Validation Guidelines

This guide outlines the architectural conventions and standards for designing, persisting, and validating business entities across the Eclipse Tradista platform. Following these guidelines ensures consistency across all modules, client applications, and persistence layers.

---

## 1. Core Entity Hierarchy & Interfaces

### Base Class: `TradistaObject`
All domain entities in Eclipse Tradista must extend `TradistaObject` (`org.eclipse.tradista.core.common.model.TradistaObject`).

Key responsibilities and contract:
- **Identifier Lifecycle (`id`)**:
  - `id == 0` designates a transient object not yet persisted.
  - `id > 0` designates a persisted entity.
  - `setId(long)` is strictly reserved for the Data Access Layer (DAL) / persistence logic and should not be invoked manually in business flows.
- **Identity, Equality & HashCode**:
  - If both objects are persisted (`id > 0`), equality is evaluated by matching IDs (`id == other.id`).
  - If objects are transient or IDs are non-positive, equality and `hashCode` fallback to the entity's **business key** fields annotated with `@Id` (`org.eclipse.tradista.core.common.model.Id`).
- **Cloneability & Serialization**:
  - Implements `Serializable` and `Cloneable` to support distributed EJB communication, caching, and safe duplication across tiers.

### Data Segregation Interface: `Segregable`
Entities that belong to a specific Processing Organization (PO) must implement the `Segregable` interface (`org.eclipse.tradista.core.common.model.Segregable`).

- **Contract**: Exposes `LegalEntity getProcessingOrg()`.
- **Automatic Interception**:
  - The EJB tier automatically intercepts all methods receiving or returning `Segregable` objects (via `TradistaSegregationHandlerInterceptor`).
  - Guarantees multi-tenant data isolation: users can only view, create, modify, or delete entities belonging to their own Processing Organization (or global objects if authorized via `@ProtectGlobal`).

---

## 2. Database Schema and Naming Conventions

### Standard Name Field Length (50 Characters)
- **General Rule**: Across all domain entity tables, the descriptive business name column (`NAME`) must be standardized to `VARCHAR(50)`.
  - Applicable to entities such as `BOOK`, `CURRENCY`, `INDEX`, `VOLATILITY_SURFACE`, `PRICING_PARAMETER`, `EQUITY_OPTION_CONTRACT_SPECIFICATION`, `AGENT`, `MANDATE`, `GC_BASKET`, `ALLOCATION_CONFIGURATION`, etc.
- **Exceptions**:
  - Technical / ISO codes (such as `CURRENCY.ISO_CODE` with `VARCHAR(3)`) use dedicated short lengths.

---

## 3. Validation Layer Responsibility

All input validations must be enforced before reaching the transactional EJB layer:

- **Enforcement Layer**: Business rules and field constraints (such as name presence and length) must be verified in the corresponding **Business Delegate** or dedicated **Validator** class (e.g., `BookBusinessDelegate`, `PricingParameterValidator`, `DateRuleValidator`).
- **Standard Name Validation**:
  - The name must not be `null`, empty, or composed only of whitespace.
  - The name length must not exceed **50 characters**.
  - Any violation must throw a `TradistaBusinessException` with an informative error message (e.g., `"The name is mandatory."`, `"The name length cannot exceed 50 characters."`).
- **Defensive Design**: Database constraints (`VARCHAR(50)`, `NOT NULL`) serve as a safety net, but exceptions must always be caught and surfaced cleanly by the validation layer prior to database execution.
