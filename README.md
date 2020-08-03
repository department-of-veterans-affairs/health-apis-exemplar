# Exemplar

This simple application is designed to test deployment machinery, such as Kubernetes or ECS.

It provides a simple rest application with following endpoints

- `/actuator/health`  
  Health checks

- `/hello` or `/hello/{greeting}`  
  Say hello and return some useful information

- `/goodbye` or `/goodbye/{number}`  
  Force the application to abruptly exit, useful for testing crash behavior.
  If `number` is provided, it will be application exit code.

- `/poison` and `/heal`
  Force the health checks to report UP or DOWN.

## Usage

Say hello.
```
curl http://exemplar.com/hello
curl http://exemplar.com/hello/howdy
```

Say goodbye.
```
curl -XPOST http://exemplar.com/goodbye
curl -XPOST http://exemplar.com/goodbye/1
```

Force application to be unhealthy
```
curl -XPOST http://exemplar.com/poison
```

Force application to be healthy
```
curl -XPOST http://exemplar.com/heal
```

### Greeting response
```
{
  "hostname": "Bryans-MacBook-Pro.local", // The application hostname
  "instance": 488928549,                  // A unique application instance id
  "time": "2020-08-03T18:56:21.170030Z",  // The request time
  "requestCount": 5,                      // Cumulative request count for the instance
  "greeting": "hello",                    // The supplied greeting or 'hello'
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

