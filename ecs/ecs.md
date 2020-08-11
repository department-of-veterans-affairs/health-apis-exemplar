# ECS

# Issues

### Dockerhup certificates
Using Dockerhub results in the following error
```
CannotPullContainerError: Error response from daemon: Get https://registry-1.docker.io/v2/: x509: certificate signed by unknown authority
```
We suspect VA network infrastructure is guilty.
No know solution to convince ECS to trust the certificate.


### AWS Credentials
Will need to support roles to allow containers to use AWS services, e.g. s3 and parameter store.
This should be an easy solution with different task execution roles


### Slow startup
The test Java application starts up in about 4 seconds locally, it took about 40 seconds in ECS.
This affects health checks, particularly at the target group which do not have an inital delay.


### Autoscaling
- Applies to service which means all images are scaled


# Changes to existing AWS componets
- added listener on 8080 to ALB green-qa-kubernetes
- added inbound rule on port 8080 to security group sg-34138a53 - qa-kubernetes-alb



# Tests
## Setup
- Health APIs Exemplar used as application
  - supports API to force crash or unhealthy status
- Two replicas
- Target group health checks at 100 second interval
- Task definition health checks at 5 seconds


## Crash test
- Run request loop
```
for i in $(seq 10000); do echo -n "$(date) " ; curl -s http://green.qa.lighthouse.va.gov:8080/hello | jq '[.instance,.requestCount,.hostname]|join("\t")' -r ;done
```
- Observe responses from both instances
- Killing one instance
```
curl -s http://green.qa.lighthouse.va.gov:8080/goodbye -XPOST
```
- Observe responses
  - 1/2 requests failed for 7 seconds, then all traffic was migrated to remaining health instance.
  - ~3:11 seconds before traffic began routing to replacement instance


## Unhealthy test
- Run request loop
```
for i in $(seq 10000); do echo -n "$(date) " ; curl -s http://green.qa.lighthouse.va.gov:8080/hello | jq '[.instance,.requestCount,.poisoned,.hostname]|join("\t")' -r ;done
```
- Observe responses from both instances
- Poison one instance
```
date ; curl -s http://green.qa.lighthouse.va.gov:8080/poison -XPOST
```
- Observe responses
  - Both instances will continue to split requests for about ~40 seconds before
    traffic was migrated to health instance
  - Unhealthy node was replaced by ECS
