# Delete replicated bucket without source bucket

Replicated Buckets should have corresponding source buckets.

```ccl
Bucket with (listBucketResult) has (replicatedObject == true) in all listBucketResult
```

[comment] Bucket has (replicatedObject == true) in all listBucketResult