# Exemplar

This simple application is designed to test deployment machinery, such as Kubernetes or ECS.

It provides a simple rest application with following endpoints

- `/actuator/health`  
  Health checks

- `/hello` or `/hello/{status}`  
  Say hello and return some useful information. 
  The HTTP status code can be provided. If omitted, 200 will be used.

- `/goodbye` or `/goodbye/{exitCode}`  
  Force the application to abruptly exit, useful for testing crash behavior.
  If `exitCode` is provided, it will be application exit code, 
  otherwise 0 will be used.

- `/poison` and `/heal`
  Poison the application, forcing the health checks to report UP or DOWN.

## Usage

Say hello.
```
curl http://exemplar.com/hello
```
Say hello with 400 status code.
```
curl http://exemplar.com/hello/400
```

Say goodbye.
```
curl -XPOST http://exemplar.com/goodbye
```
Say goodbye and exit with status 1.
```
curl -XPOST http://exemplar.com/goodbye/1
```

Force application to be unhealthy.
```
curl -XPOST http://exemplar.com/poison
```

Force application to be healthy.
```
curl -XPOST http://exemplar.com/heal
```

### Greeting response
```
{
  "hostname": "Bryans-MacBook-Pro.local", // The application hostname
  "instance": 488928549,                  // A unique application instance id
  "poisoned": false                       // Indicates whether the app is poisoned
  "time": "2020-08-03T18:56:21.170030Z",  // The request time
  "requestCount": 5,                      // Cumulative request count for the instance
  "status": 200,                          // The supplied status code or 200
  "headers": {                            // Any specified request headers
    "host": [
      "localhost:8080"
    ],
    "user-agent": [
      "curl/7.64.1"
    ],
    "accept": [
      "*/*"
    ]
  }
}
```

