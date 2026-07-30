# User Guide: Ratings Management (Ticket 321)

This document describes the concepts and management of **Ratings** (financial credit ratings) associated with financial products (Bonds, Equities, etc.) within Eclipse Tradista.

---

## 1. Key Concepts

### A. Rating Agency
A rating agency is an entity (e.g., Standard & Poor's, Moody's, Fitch) responsible for assigning risk assessment grades to financial assets or issuers.

### B. Rating
A rating is a code or score (e.g., AAA, AA+, B-) representing the risk level of a financial product. Each rating is linked to a specific rating agency.

### C. Ratable
The `Ratable` interface represents any financial product that can be evaluated and rated by a rating agency. Currently, the following securities implement this interface and can be assigned ratings:
* **Bonds**
* **Equities**

### D. Rating Assignment
This defines the relationship established between a financial product (`Ratable`), a specific `Rating` from an agency, and a validity period defined by:
* A mandatory start date (**Valid From**)
* An optional end date (**Valid To**)

---

## 2. User Interface & Features

### A. Managing Agencies and Ratings (Web & GUI)
Administrators can configure and manage the repository of agencies and their associated ratings via the dedicated Rating Agency management screen.
* **Creating an Agency**: Allows adding a new agency (e.g., "Fitch").
* **Adding Ratings**: Allows entering valid rating codes for that agency (e.g., "AAA", "AA", "A") along with an optional description.
* **Deletion**: 
  * Physical deletion of a rating if it is not currently in use.
  * Logical deletion (*Soft Delete*) of an agency to prevent new assignments while preserving historical data.

### B. Assigning Ratings to Securities (Bond / Equity)
When entering or editing a Bond or an Equity, a **Ratings** tab is available in the product definition screen:

1. **Associate a New Rating**:
   * Select the agency and the desired rating from the dropdown list.
   * Specify the mandatory start date (**Valid From**).
   * Optionally specify an end date (**Valid To**).
   * Click **Add** to save and link the rating to the security.

2. **View History**:
   * The tab displays a summary table of all ratings assigned to the product over time.

3. **Remove a Rating**:
   * Select the assignment in the table and click **Remove**.
