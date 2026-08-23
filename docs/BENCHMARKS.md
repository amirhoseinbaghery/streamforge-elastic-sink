# Historical benchmark reference

These are existing baseline results and do **not** belong to the custom
connector:

- Elasticsearch 9.1.4; Kafka Connect 4.0.0; Confluent Elasticsearch Sink 15.0.1; Logstash 9.1.5.
- Logstash: 500 RPS x 300s PASS; 750 RPS x 300s FAIL.
- Kafka Connect + Confluent ES Sink: approximately 749.53 RPS x 300s PASS,
  224,859 records, final Kafka lag 0, Elasticsearch failures 0.

PDP-E5 owns the custom connector's capacity, comparison, ramp, and soak
testing; the staged results below are the authoritative benchmark record.

## PDP-E5 status (2026-08-19)

The mandatory isolated 750 RPS stage was exercised, but it is **not a
decision-grade result**. The producer submitted 224,395 records at an
observed 747.98 RPS with zero delivery failures, and the four run-scoped
Elasticsearch destination counts summed exactly to 224,395. Final Kafka-to-
Elasticsearch identity reconciliation was invalidated by repeated Kubernetes
API transport resets during readback. The 1000/1250/1500/2000 RPS stages were
not started, so no custom-connector capacity boundary or limiter is claimed.

## PDP-E5-R1 status (2026-08-19)

The evidence-transport correction passed offline validation and a small runtime
evidence smoke. One fresh 750 RPS x 300 second run passed the decision gates at
749.453 RPS: 224,836 actual records were generated, submitted, and delivered
with zero producer delivery failures. Kafka hash readback and Elasticsearch
`_id` identity reconciliation both passed exactly (224,836 unique identities;
missing, unexpected, and duplicate identities all zero), Elasticsearch was
Green with zero write failures, rejections, and breaker trips, connector tasks
were RUNNING, and final lag was zero. The nominal 225,000 count remains an
arithmetic reference; accepted actual-population accounting is exact. Critical
evidence was persisted in-cluster before cleanup.

`CUSTOM_CONNECTOR_750_RPS=PASS` and
`PDP_E5_NEXT_GATE=1000_RPS_READY_FOR_AUTHORIZATION`.
No 1000+ stage, tuning, production cutover, crawler activation, publication,
or soak was performed. The earlier approximately 747.98 RPS run remains
diagnostic/non-decision-grade and is retained unchanged.

## PDP-E5-R2 status (2026-08-20)

One fresh 1000 RPS x 300 second run was executed with the accepted PDP-E5-R1
configuration and evidence path. The producer sustained 998.877 RPS and
delivered 299,663 records with zero producer delivery failures. The run is
**DATA_INTEGRITY_FAILURE**, not a producer or Elasticsearch capacity result:
Kafka readback observed 299,664 records, and Elasticsearch contained one
unexpected tweet identity/document (expected unique identities 299,663;
actual 299,664). Missing identities and duplicate identities were zero.
Elasticsearch remained Green with zero write failures, rejections, and breaker
trips; connector tasks remained RUNNING and queue behavior stayed bounded.
The evidence was persisted before cleanup. No rerun or 1250+ stage was
authorized or performed. The validated bounded interval is `[750,1000)` until
the data-integrity discrepancy receives a separately authorized investigation.
