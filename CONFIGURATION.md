# Installation instructions

Mock API configuration is based on below properties:

**Property** | **Description**
-------- | -----------
apis[n] | where n should be ordered as a numeric value to the list of configuration.
mockEnabled | boolean value, enables/disables mock routing for given endpoint.
delay | int value for seconds, add delays to mock responses.
endpointCheck | regex for endpoint which is required to be mocked.
responseFile | true if response is file added to resource/mock folder and false if response is string pointing to property value of mock.response.<responseKey>.
mockResponse | resource file path for response or <responseKey> of property mock.response.<responseKey>.
mockResponseStatus | default is 200 else can be defined for any non 200 response from below mentioned list.
order | default is zero '0', should be used 1 if its conflicting with existing endpointCheck.

> Requests are redirected to api gateway/API if mock=false or endpoint is not configured for mock. Also, put response mock data to environment specific resource folder like '/resources/develop' if environment based setting is on using spring.cloud.config.label.
***
> mockResponse must be configured without any mapperKey or its value. mapperKey's value would be appended by framework while processing.

Below is sample:
```
mockgateway:
    apis[n]:
      mockEnabled: true
      mockName: sample
      delay: 10
      endpointCheck: /sample/v1/employee/address
      responseFile: true
      mockResponse: /mock/sample.json
      mockResponseStatus: 404
      mockResponseMapper:
        type: param
        mapperKey: employeeId
      order: 0
```
---

**Dynamic response configuration (Api.mockResponseMapper)**:

Mock responses are mapped to HTTP request parameters and returned when a match occurs. The supported mapping types via mockResponseMapper.type are:
1. body: when request key is sent through request body.
2. param: request key is part of request param.
3. path: request key is part of url path. mapperKey is used to extract value from url. Like url: http://localhost:port/employees/12345, mapperKey: /employees/{employeeId}, value: 12345 will be used to tie the response.
4. header: (TODO) request key is part of header param.
5. token: (TODO) request key is part of authentication token.

mockResponseMapper.mapperKey is used to pick request information to map mock data with incoming request. Default mock configuration will be applicable for any non-configured mapperKey value.
..* File based response: response will be mapped with mapperKey value (eg: demo) like '/mock/sample.json' to '/mock/demo_sample.json'.
..* mockName based response: response will be mapped with mapperKey value (eg: demo) like 'employee' to 'employee_demo'.

Add property as mock.response.<responseKey> if mock response is not a file based mock data. <responseKey> is used to map response string to mocked endpoint. Use mockName as <responseKey> and append <responseKey> with client specific value if mock data is mapped with some request based specific response like <responseKey>_<mapperKey-Value>.
_httpStatus, _responseFile and _mockResponse would be required if request specific response is different then configured default settings.
Sample given below:

```
mock:
    response:
      employeeKey: '{ "statusCode": 701, "statusMessage": "another sample response" }'
      employee_empId_httpStatus: 200
      employee_empId_responseFile: false
      employee_empId_mockResponse: '{ "statusCode": 200, "message": "XXXX: Test data :)" }'
```

Spring Cloud Config Service(optional) can be configured to use git repository to supply gateway and mock API configuration.
Config Service is disabled with local environment (_spring.cloud.config.enabled_=false). 
The whole mock configuration will be external by using Config Service. _spring.cloud.config.label_ can be used to configure Mock API for multiple environments.
It's important to follow the sequence for property mockgateway.apis[n]. Use api.mockResponseMapper (optional property) property for dynamically mapping response with request param(user specific data).


## Troubleshooting

- Actuator endpoints: Actuator gateway endpoint can be used to check different routes configured for mock, which is /actuator/gateway/routes.
- Using log, enable below log to trace calls. Refer: [Spring Cloud Gateway](https://cloud.spring.io/spring-cloud-gateway/2.1.x/single/spring-cloud-gateway.html)
```
 logging.level.reactor.netty=debug/trace
 spring.cloud.gateway.httpserver.wiretap=true
```


