# Ignis Eraser
Module housing the Ignis Eraser application, used for erasing configuration and data in the FCR Engine.

This application is intended for product development use only, dire consequences await those who use it in production.

<img src="https://i0.wp.com/farm3.static.flickr.com/2294/2034654905_f1df7b482a_o.gif"></img>
## Use cases
### Deleting product configuration
During the QA process and engineer can easily make a mistake in a product configuration, but once it has been imported
and data has been staged against it, it cannot be deleted (This is by design in the FCR Engine).

Instead of forcing the engineer to create new products/schemas/pipelines this app will allow for the quick deletion of a 
product and all its associated entities and tables in phoenix

### Delete a schema during product development
During the product development process, product developers may be working on different schemas within the same product
and they may want to delete the schema without deleting the entire product and therefore disrupting their collegue.

Therefore we allow the deleting of specific schemas within the product. 
The product developer would then have to create a new version of the product i.e. PRODUCT-1.0.0-DEV-1 -> PRODUCT-1.0.0-DEV-2
to only reimport the schema that was deleted.
