Feature: Product Catalog

  As a customer,
  I want to easily search, filter, and sort products in the catalog
  So that I can find what I need quickly.

  Sally is an online shopper

  Rule: Customers should be able to search for products by name
    Example: The one where Sally searches for an Adjustable Wrench
      Given Sally is on the home page
      When she searches for "Adjustable Wrench"
      Then the "Adjustable Wrench" product should be displayed

    Example: The one where Sally searches for a more general term
      Given Sally is on the home page
      When she searches for "saw"
      Then the following products should be displayed:
        | Wood Saw     |
        | Circular Saw |

    Example: The one where Sally searches for "Pliers" and we check the price
      Given Sally is on the home page
      When she searches for "pliers"
      Then the following products and their prices should be displayed:
        | Product            | Price  |
        | Combination Pliers | $14.15 |
        | Pliers             | $12.01 |
        | Long Nose Pliers   | $14.24 |
        | Slip Joint Pliers  | $9.17  |

  Rule: The one where Sally searches for a product that doesn't exist
    Example: The one where Sally searches for an Adjustable Wrench
      Given Sally is on the home page
      When she searches for "Superglue"
      Then no products should be displayed
      And the message "There are no products found." should be displayed

  Rule: Customers should be able to narrow downs their search by category
    Example: The one where Sally want to see "Hand Saws"
      Given Sally is on the home page
      When she searches for "saw"
      And she filters by "Hand Saw"
      Then the "Wood Saw" product should be displayed
      Then the following products and their prices should be displayed:
        | Product  | Price  |
        | Wood Saw | $12.18 |

  Rule: Customers should be able to sort products by various criteria
    Scenario Outline: Sally sorts by different criteria
      Given Sally is on the home page
      When she sorts by "<Sort>"
      Then the first product  displayed should be "<First Product>"
      Examples:
        | Sort               | First Product       |
        | Name (A - Z)       | Adjustable Wrench   |
        | Name (Z - A)       | Wood Saw            |
        | Price (High - Low) | Drawer Tool Cabinet |
        | Price (Low - High) | Washers             |
