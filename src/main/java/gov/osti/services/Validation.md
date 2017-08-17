DOECode Validation Services
=====================

Introduction
------------
Access various validation checks for DOECode API data.  HTTP verb `GET` performs various
single value checks, or use HTTP `POST` to send a batch of validation requests at once.

> The API is available based on `/doecodeapi/services/validation` on the DOECode server.

HTTP Request Methods
--------------------

| Method | Description |
| --- | --- |
| `GET` | Used to retrieve resources |
| `POST` | Send a batch of requests |
| `PUT` | *Not currently used* |
| `DELETE` | *Not currently used* |

Service Endpoints
-----------------

## Individual Validation Checks

Each various type of validation may be performed via single `GET` requests from the
following endpoints.  Each share a common set of response codes, detailed below.

| Response Code | Description |
| --- | --- |
| 200 | OK, value validates successful |
| 400 | Bad Request, value is not valid for the requested type |

### phonenumber

`GET /doecodeapi/services/validation/phonenumber?value=*value-to-check*`

Attempt to validate a phone number for validity.

### email

`GET /doecodeapi/services/validation/email?value=*value-to-check*`

Attempt to valid email address against recognized common patterns.

### awardnumber

`GET /doecodeapi/services/validation/awardnumber?value=*value-to-check*`

Check to see if the value is a valid DOE contract/award number.

### url

`GET /doecodeapi/services/validation/url?value=*value-to-check*`

Check to see if value is a valid URL-pattern; note this *does not* attempt to
connect to the URL.

### repositorylink

`GET /doecodeapi/services/validation/repositorylink?value=*value-to-check*`

Check to see if the indicated value is a valid, accessible git repository.

### doi

`GET /doecodeapi/services/validation/doi?value=*value-to-check*`

Check to see if the value represents a live DOI value.

## Batch Validation Requests

If multiple validation requests should be made at once, you may construct an array
of JSON objects to process.  Each of these will be checked and the results made 
available back with an error message if the individual check failed.

### batch validation

Requests should be constructed in this manner:

```json
[ { "value":"value-to-check", "type":"validation-to-perform" }, ... ]
```

Where *value-to-check* is the desired field value, and *validation-to-perform* is one
of: "doi", "repositorylink", "url", "phonenumber", "email", or "awardnumber".  Responses
will be the same object, with each adding an "error" attribute.  The "error" value
will be blank if the value was acceptable, or a message indicating the failure if not.

> Request:
```html
POST /doecodeapi/services/validation
Content-Type: application/json
```
```json
[ { "value":"sampleurl", "type":"url"},
  { "value":"sampleemail", "type":"email"} ]
```
> Response:
```html
HTTP/1.1 200 OK
Content-Type: application/json
```
```json
[ {"value":"sampleurl", "type":"url", "error":"sampleurl is not a valid URL."},
  {"value":"sampleemail", "type":"email", "error":"sampleemail is not a valid email address."}]
```